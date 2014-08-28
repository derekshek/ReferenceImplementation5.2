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
package com.pentaho.oem.sk.userrole;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;

public class OEMMultiUserDetailsService implements InitializingBean,
		UserDetailsService {

	private UserDetailsService[] subUserDetailsServices = null;

	public UserDetailsService[] getUserDetailsServices() {
		return subUserDetailsServices;
	}

	public void setUserDetailsServices(
			UserDetailsService[] subUserDetailsServices) {
		this.subUserDetailsServices = subUserDetailsServices;
	}

	private static final Log LOG = LogFactory
			.getLog(OEMMultiUserDetailsService.class);

	public OEMMultiUserDetailsService() {
		super();
		LOG.debug("Constructor");
	}

	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException, DataAccessException {
		UserDetails details = null;

		for (UserDetailsService service : subUserDetailsServices) {
			LOG.debug("User " + username + " Trying delegate service" + service);
			try {
				details = service.loadUserByUsername(username);
				if (details != null) {
					LOG.debug("Got user: " + details);
					return details;
				}
			} catch (Exception e) {
				LOG.debug("Exception in delegate: " + e);
			}
		}
		return details;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (subUserDetailsServices == null
				|| subUserDetailsServices.length == 0) {
			LOG.error("No delegating User details services");
			Assert.notNull(subUserDetailsServices);
			Assert.noNullElements(subUserDetailsServices);
		}

	}

}
