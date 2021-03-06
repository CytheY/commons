package org.jvalue.commons.auth;

import java.util.EnumSet;

/**
 * User roles.
 */
public enum Role {

	PUBLIC,ADMIN;

	private EnumSet<Role> includedRoles;

	static {
		PUBLIC.includedRoles = EnumSet.noneOf(Role.class);
		ADMIN.includedRoles = EnumSet.of(Role.PUBLIC);
	}


	/**
	 * @return true if the roles are equal or this role has more "rights".
	 */
	public boolean isMatchingRole(Role role) {
		return this.equals(role) || includedRoles.contains(role);
	}

}
