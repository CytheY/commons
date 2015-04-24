package org.jvalue.commons.auth.rest;


import com.google.common.base.Optional;

import org.jvalue.commons.auth.AbstractUserDescription;
import org.jvalue.commons.auth.BasicAuthUserDescription;
import org.jvalue.commons.auth.BasicAuthUtils;
import org.jvalue.commons.auth.OAuthUserDescription;
import org.jvalue.commons.auth.OAuthUtils;
import org.jvalue.commons.auth.RestrictedTo;
import org.jvalue.commons.auth.Role;
import org.jvalue.commons.auth.UnauthorizedException;
import org.jvalue.commons.auth.User;
import org.jvalue.commons.auth.UserManager;
import org.jvalue.commons.rest.RestUtils;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserApi {

	private final UserManager userManager;
	private final BasicAuthUtils basicAuthUtils;
	private final OAuthUtils oAuthUtils;

	@Inject
	protected UserApi(UserManager userManager, BasicAuthUtils basicAuthUtils, OAuthUtils oAuthUtils) {
		this.userManager = userManager;
		this.basicAuthUtils = basicAuthUtils;
		this.oAuthUtils = oAuthUtils;
	}


	@GET
	public List<User> getAllUsers(@RestrictedTo(Role.ADMIN) User user) {
		return userManager.getAll();
	}


	@POST
	public User addUser(@RestrictedTo(value = Role.ADMIN, isOptional = true) User user, AbstractUserDescription userDescription) {
		// check for valid role (only admins can add admins)
		if (userDescription.getRole().equals(Role.ADMIN) && user == null) throw new UnauthorizedException("missing admin privileges");

		if (userDescription instanceof BasicAuthUserDescription) return addUser((BasicAuthUserDescription) userDescription);
		else return addUser((OAuthUserDescription) userDescription);
	}


	@GET
	@Path("/{userId}")
	public User getUser(@RestrictedTo(Role.PUBLIC) User user, @PathParam("userId") String userId) {
		if (!userId.equals(user.getId()) && !user.getRole().equals(Role.ADMIN)) throw new UnauthorizedException();
		return userManager.findById(userId);
	}


	@GET
	@Path("/me")
	public User getUser(@RestrictedTo(Role.PUBLIC) User user) {
		return user;
	}


	@DELETE
	@Path("/{userId}")
	public void removeUser(@RestrictedTo(Role.PUBLIC) User user, @PathParam("userId") String userId) {
		if (!userId.equals(user.getId()) && !user.getRole().equals(Role.ADMIN)) throw new UnauthorizedException();
		userManager.remove(userManager.findById(userId));
	}


	private User addUser(BasicAuthUserDescription userDescription) {
		assertUserNotRegistered(userDescription.getEmail());

		// check for partially secure password
		if (!basicAuthUtils.isPartiallySecurePassword(userDescription.getPassword()))
			throw RestUtils.createJsonFormattedException("password must be at least 8 characters and contain numbers", 409);

		return userManager.add(userDescription);
	}


	private User addUser(OAuthUserDescription userDescription) {
		// check for valid auth token
		Optional<OAuthUtils.OAuthDetails> tokenDetails = oAuthUtils.checkAuthHeader(userDescription.getAuthToken());
		if (!tokenDetails.isPresent()) throw new UnauthorizedException();

		assertUserNotRegistered(tokenDetails.get().getEmail());

		return userManager.add(userDescription, tokenDetails.get());
	}


	private void assertUserNotRegistered(String email) {
		if (userManager.contains(email)) throw RestUtils.createJsonFormattedException("user already registered", 409);
	}

}
