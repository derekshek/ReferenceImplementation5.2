/**
 * COPYRIGHT (C) 2014 Pentaho. All Rights Reserved.
 * THE SOFTWARE PROVIDED IN THIS SAMPLE IS PROVIDED "AS IS" AND PENTAHO AND ITS 
 * LICENSOR MAKE NO WARRANTIES, WHETHER EXPRESS, IMPLIED, OR STATUTORY REGARDING 
 * OR RELATING TO THE SOFTWARE, ITS DOCUMENTATION OR ANY MATERIALS PROVIDED BY 
 * PENTAHO TO LICENSEE.  PENTAHO AND ITS LICENSORS DO NOT WARRANT THAT THE 
 * SOFTWARE WILL OPERATE UNINTERRUPTED OR THAT THEY WILL BE FREE FROM DEFECTS OR 
 * THAT THE SOFTWARE IS DESIGNED TO MEET LICENSEE'S BUSINESS REQUIREMENTS.  PENTAHO 
 * AND ITS LICENSORS HEREBY DISCLAIM ALL OTHER WARRANTIES, INCLUDING, WITHOUT 
 * LIMITATION, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE, TITLE AND NONINFRINGMENT.  IN ADDITION, THERE IS NO MAINTENANCE OR SUPPORT 
 * INCLUDED WITH THIS SAMPLE OF ANY NATURE WHATSOEVER, INCLUDING, BUT NOT LIMITED TO, 
 * HELP-DESK SERVICES. 
 * @author khanrahan
 * @version 1.01 
 */
package com.pentaho.oem.sk;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;

public class OEMUser extends User {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Log LOG = LogFactory.getLog(OEMUser.class);

	Map<String, String> sessionVariables = new HashMap<String, String>();

	public OEMUser(String username) {
		super(username, username, true, true, true, true,
				new GrantedAuthority[0]);
	}

	public OEMUser(String username, String password, boolean enabled,
			boolean accountNonExpired, boolean credentialsNonExpired,
			boolean accountNonLocked, GrantedAuthority[] authorities)
			throws IllegalArgumentException {
		super(username, password, enabled, accountNonExpired,
				credentialsNonExpired, accountNonLocked, authorities);
	}

	@SuppressWarnings("deprecation")
	public OEMUser(UserDetails details) {
		super(details.getUsername(), details.getPassword(),
				details.isEnabled(), details.isAccountNonExpired(), details
						.isAccountNonLocked(), details.getAuthorities());
	}

	public void addSessionVariable(String id, String value) {
		sessionVariables.put(id, value);
	}

	public String getSessionVariable(String id) {
		if (sessionVariables.containsKey(id)) {
			return sessionVariables.get(id);
		}
		return null;
	}

	public String[] getSessionVariables() {
		Set<String> set = sessionVariables.keySet();
		return (String[]) set.toArray(new String[0]);
	}

	public void addRole(String role) {
		boolean alreadyHas = false;
		LinkedList<GrantedAuthority> roleList = new LinkedList<GrantedAuthority>();
		for (GrantedAuthority curRole : this.getAuthorities()) {
			roleList.add(curRole);
			if (curRole.toString().equals(role)) {
				alreadyHas = true;
			}
		}
		if (!alreadyHas) {
			roleList.add(new GrantedAuthorityImpl(role));
		}
		this.setAuthorities(roleList.toArray(new GrantedAuthority[0]));
	}

	public void deleteRole(String role) {
		LinkedList<GrantedAuthority> roleList = new LinkedList<GrantedAuthority>();
		for (GrantedAuthority curRole : this.getAuthorities()) {
			if (!curRole.toString().equals(role)) {
				roleList.add(curRole);
			}
		}
		this.setAuthorities(roleList.toArray(new GrantedAuthority[0]));
	}

	public boolean hasRole(String role) {
		for (GrantedAuthority myRole : this.getAuthorities()) {
			if (myRole.toString().equals(role)) {
				// LOG.debug("Has role "+role);
				return true;
			}
		}
		// LOG.debug("Does not have role "+role);
		return false;
	}

	public boolean isAdmin() {
		for (GrantedAuthority myRole : this.getAuthorities()) {
			if (myRole.toString().equals(OEMUtil.PENTAHOADMIN)) {
				// LOG.debug("Is admin - pentaho");
				return true;
			}
		}
		LOG.debug("Is not admin");
		return false;
	}

	public boolean isAdmin(String tenantName) {
		return hasRole(tenantName + "-" + OEMUtil.PENTAHOADMIN);
	}

}
