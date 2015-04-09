package org.jvalue.commons.auth;


import com.google.common.base.Optional;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.internal.inject.AbstractContainerRequestValueFactory;
import org.glassfish.jersey.server.internal.inject.AbstractValueFactoryProvider;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.model.Parameter;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;


/**
 * Responsible for extracting credentials from an http request and forwarding
 * those to an {@link Authenticator}.
 */
public final class RestrictedToProvider extends AbstractValueFactoryProvider {

	private final BasicAuthenticator basicAuthenticator;

	@Inject
	protected RestrictedToProvider(
			MultivaluedParameterExtractorProvider mpep,
			ServiceLocator locator,
			BasicAuthenticator basicAuthenticator) {

		super(mpep, locator, Parameter.Source.UNKNOWN);
		this.basicAuthenticator = basicAuthenticator;
	}


	@Override
	protected Factory<User> createValueFactory(final Parameter parameter) {
		Class<?> classType = parameter.getRawType();
		if (classType == null || (!classType.equals(User.class))) return null;

		// factory for getting user instances based on an http request
		return new AbstractContainerRequestValueFactory<User>() {
			@Override
			public User provide() {
				// find auth header
				List<String> headers = getContainerRequest().getRequestHeader(HttpHeaders.AUTHORIZATION);
				if (headers == null || headers.isEmpty()) onUnauthorized();
				String authHeader = headers.get(0);

				// check authentication
				Optional<User> user = Optional.absent();
				if (authHeader.startsWith("Basic ")) user = basicAuthenticator.authenticate(authHeader);
				if (!user.isPresent()) onUnauthorized();

				// check authorization
				Role requiredRole = parameter.getAnnotation(RestrictedTo.class).value();
				if (!user.get().getRole().equals(requiredRole)) onUnauthorized();

				return user.get();
			}

			private void onUnauthorized() {
				throw new UnauthorizedException();
			}
		};
	}

}
