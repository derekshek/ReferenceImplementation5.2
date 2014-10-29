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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.BadCredentialsException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.providers.dao.DaoAuthenticationProvider;
import org.springframework.security.userdetails.UserDetails;

import com.pentaho.oem.sk.OEMUser;
import com.pentaho.oem.sk.OEMUtil;

/**
 * Attempts to authenticate a {@link OEMAuthenticationToken}. The class
 * interacts with a {@link #setServiceURL(String) parameterized URL} to retrieve
 * an input stream that provides the user's name and roles.
 */

public class OEMAuthenticationProvider extends DaoAuthenticationProvider {

	private static final Log LOG = LogFactory.getLog(OEMAuthenticationProvider.class);


	@Override
	public Authentication authenticate(Authentication token) throws AuthenticationException {
		OEMAuthenticationToken results = null;

		LOG.debug("authenticate");
		if (supports(token.getClass())) {
			results = executeService((OEMAuthenticationToken) token);
		}
		return results;
	}


	protected OEMAuthenticationToken executeService(OEMAuthenticationToken intoken) throws AuthenticationException {
		UserDetails details;

		Object inDetails = intoken.getDetails();
		
		// If the token details has role Authenticated, we just return - user is authenticated
		if (inDetails != null && inDetails instanceof OEMUser && ((OEMUser)inDetails).hasRole(OEMUtil.PENTAHOAUTH)){
			LOG.debug("User is pre-authenticated by the OEM Filter");
			return new OEMAuthenticationToken(intoken.getName(), intoken.getCredentials().toString(),intoken.getAuthorities());
		}

		// Defer to UserDetails to get/validate the user - this happens if ResolveUsername does not have access to all the info
		LOG.debug("calling loadUserByUsername " + intoken.getName() + " using " + getUserDetailsService());
		details = getUserDetailsService().loadUserByUsername(intoken.getName());
		if (!(details instanceof OEMUser)){
			details = new OEMUser(details);
		}
		OEMAuthenticationToken newTok = new OEMAuthenticationToken(details.getUsername(), intoken.getName().toString(),details.getAuthorities());
		newTok.setDetails(details);
		return newTok;
	}


	@SuppressWarnings("rawtypes")
	@Override
	public boolean supports(Class clazz) {
		return (OEMAuthenticationToken.class.isAssignableFrom(clazz));
	}

}
