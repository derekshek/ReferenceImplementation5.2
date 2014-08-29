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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.providers.AuthenticationProvider;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.util.Assert;

import com.pentaho.oem.sk.OEMUser;
import com.pentaho.oem.sk.OEMUtil;
import com.pentaho.oem.sk.authentication.OEMAuthenticationToken;

/**
 * Attempts to authenticate a {@link OEMAuthenticationToken}. The class
 * interacts with a {@link #setServiceURL(String) parameterized URL} to retrieve
 * an input stream that provides the user's name and roles.
 */

public class OEMAuthenticationProvider implements InitializingBean, AuthenticationProvider {

	private static final Log LOG = LogFactory.getLog(OEMAuthenticationProvider.class);
	
	private UserDetailsService userDetailsService;

	public void setUserDetailsService(UserDetailsService s){
		LOG.debug("settng userdetails service to "+s);
		this.userDetailsService = s;
	}

	public UserDetailsService getUserDetailsService(){
		return userDetailsService;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		LOG.debug("Properties are set");
		Assert.notNull(userDetailsService);
	}

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


		if ("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx".equals("0")) {
			OEMUser user = new OEMUser("sean", "foo", true, true, true, true, new GrantedAuthority[0]);
//			user.addRole(OEMUtil.PENTAHOADMIN);
			user.addRole(OEMUtil.PENTAHOAUTH);
			details = user;
		}else{
			LOG.debug("calling loadUserByUsername " + intoken.getName() + " using " + userDetailsService);
			details = userDetailsService.loadUserByUsername(intoken.getName());
		}
		
		if (!(details instanceof OEMUser)){
			details = new OEMUser(details);
		}

		OEMAuthenticationToken result = new OEMAuthenticationToken(intoken);

		result.setAuthenticated(true);
		result.setPrincipal(details);
		result.setAuthorities(details.getAuthorities());
		result.setName(details.getUsername());
		result.setDetails(details);
		IPentahoSession session = PentahoSessionHolder.getSession();
		LOG.debug("setting in the session " + session);
		for (String var : ((OEMUser)details).getSessionVariables()){
			LOG.debug("Setting var " + var);
			session.setAttribute(var, ((OEMUser)details).getSessionVariable(var));
		}

		return result;
	}
	

	@SuppressWarnings("rawtypes")
	@Override
	public boolean supports(Class clazz) {
		return (OEMAuthenticationToken.class.isAssignableFrom(clazz));
	}

}
