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


import java.io.Serializable;
import java.security.Principal;

import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.providers.AbstractAuthenticationToken;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.util.Assert;

/**
 * Used to communicate the information used to communicate the information about
 * the user through the authentication process.
 */
public class OEMAuthenticationToken extends UsernamePasswordAuthenticationToken
		implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an instance.
	 * 
	 * @param name
	 *            the value of the secret token passed on or in the request.
	 *            Value cannot be <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if <code>secret</code> is <code>null</code>.
	 */
	
	
	public OEMAuthenticationToken(Object name, Object authenticatingToken, GrantedAuthority[] authorities) {
		super(name,authenticatingToken,authorities);
	}

	public OEMAuthenticationToken(Object name, Object authenticatingToken) {
		this(name,authenticatingToken,new GrantedAuthority[0]);
	}

	public OEMAuthenticationToken(Object name) {
		this(name,"");
	}

	public OEMAuthenticationToken(Authentication token) {
		super(token.getName(),token.getCredentials(),token.getAuthorities());
	}

}
