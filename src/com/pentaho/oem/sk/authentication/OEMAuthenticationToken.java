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
package com.pentaho.oem.sk.authentication;
/**
 * COPYRIGHT (C) 2013 Pentaho. All Rights Reserved.
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
 */


import java.io.Serializable;
import java.security.Principal;

import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.providers.AbstractAuthenticationToken;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.util.Assert;

/**
 * Used to communicate the information used to communicate the information about
 * the user through the authentication process.
 */
public class OEMAuthenticationToken extends AbstractAuthenticationToken
		implements Serializable {

	private static final long serialVersionUID = 1L;
	private String name;
	private String authenticatingToken;
	

	private Object principal;
	private GrantedAuthority[] authorities;

	/**
	 * Constructs an instance.
	 * 
	 * @param name
	 *            the value of the secret token passed on or in the request.
	 *            Value cannot be <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>secret</code> is <code>null</code>.
	 */
	public OEMAuthenticationToken(String name) {
		super(null);
		this.name = name;
		this.authorities = new GrantedAuthority[0];
	}

	public OEMAuthenticationToken(Authentication token) {
		super(null);
		this.name = token.getName();
		this.principal = token.getPrincipal();
		this.authorities = token.getAuthorities();
	}
	

	/**
	 * Reports the secret stored within this instance.
	 * 
	 * @return the secret.
	 */
	public String getName() {
		return name;
	}
	public String setName(String s) {
		this.name = s;
		return name;
	}
	@Override
	public Object getCredentials() {
		return getName();
	}

	@Override
	public Object getPrincipal() {
		Object result = principal;

		if (result == null) {
			result = getDetails();
		}

		if (result == null) {
			result = name;
		}

		return result;
	}

	/**
	 * Sets the principal.
	 * 
	 * @param principal
	 *            the principal associated with this instance. This can be the
	 *            user name, a {@link UserDetails}, or a {@link Principal}.
	 * @throws IllegalArgumentException
	 *             if the principal is <code>null</code>.
	 */
	public void setPrincipal(Object principal) {
		Assert.notNull(principal);
		this.principal = principal;
	}

	/**
	 * Sets the roles or {@link GrantedAuthority granted authorities} associated
	 * with this instance.
	 * 
	 * @param auths
	 *            the list of roles. The instance and any of its indexes cannot
	 *            be <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>auth</code> or any of its member values is
	 *             <code>null</code>.
	 */
	public void setAuthorities(GrantedAuthority auths[]) {
		Assert.notNull(auths);
		Assert.noNullElements(auths);

		this.authorities = auths;
	}

	/**
	 * Sets the roles or {@link GrantedAuthority granted authorities} associated
	 * with this instance.
	 * 
	 * @param roles
	 *            the list of roles. The instance and any of its indexes cannot
	 *            be <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>roles</code> or any of its member values is
	 *             <code>null</code>.
	 */
	public void setAuthorities(String roles[]) {
		GrantedAuthority auths[];

		Assert.notNull(roles);
		Assert.noNullElements(roles);

		auths = new GrantedAuthority[roles.length];
		for (int index = 0; index < roles.length; index++) {
			auths[index] = new GrantedAuthorityImpl(roles[index]);
		}

		authorities = auths;
	}

	@Override
	public GrantedAuthority[] getAuthorities() {
		return authorities;
	}
	
	public String getAuthenticatingToken() {
		return authenticatingToken;
	}

	public void setAuthenticatingToken(String authenticatingToken) {
		this.authenticatingToken = authenticatingToken;
	}
}
