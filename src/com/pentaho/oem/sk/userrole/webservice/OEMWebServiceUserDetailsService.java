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
package com.pentaho.oem.sk.userrole.webservice;
/**
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



import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.pentaho.platform.api.engine.security.IAuthenticationRoleMapper;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.security.AuthenticationException;
import org.springframework.security.AuthenticationServiceException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;

import com.pentaho.oem.sk.OEMUser;
import com.pentaho.oem.sk.OEMUtil;


public class OEMWebServiceUserDetailsService implements UserDetailsService, InitializingBean {


	private static final Log LOG = LogFactory.getLog(OEMWebServiceUserDetailsService.class);
	protected IAuthenticationRoleMapper      roleMapper;
	protected ITenantedPrincipleNameResolver userNameUtils;
	private   OEMWebServiceParser            webServiceParser;
	private   String                         authHeader;
	private   Set<String> pentahoAdmins = new HashSet<String>();
	protected String[] serviceURLs;
	

	public String getAuthHeader()                                         { return authHeader; }
	public void setAuthHeader(String authHeader)                          { this.authHeader = authHeader; }
	public OEMWebServiceParser getWebServiceParser()                      { return webServiceParser; }
	public void setWebServiceParser(OEMWebServiceParser webServiceParser) { this.webServiceParser = webServiceParser; }
	public void setServiceURLs(String[] url) { serviceURLs = url; }
	protected String[] getServiceURL() { return serviceURLs; }
	public void setRoleMapper(IAuthenticationRoleMapper roleMapper) { this.roleMapper = roleMapper; }
	public String[] getPentahoAdminsArray(){
		String[] rv = pentahoAdmins.toArray(new String[pentahoAdmins.size()]);
		for (String u : rv){
			pentahoAdmins.add(u);
		}
		return pentahoAdmins.toArray(new String[pentahoAdmins.size()]);
	}

	


	public OEMWebServiceUserDetailsService(ITenantedPrincipleNameResolver userNameUtils) {
		super();
		this.userNameUtils = userNameUtils;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(serviceURLs);
		Assert.notNull(webServiceParser);
		pentahoAdmins.add("admin");
		pentahoAdmins.add("pentahoRepoAdmin");
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
		OEMUser details = null;
		InputStream input = null;
    	username = userNameUtils.getPrincipleName(username);
		ArrayList<GrantedAuthorityImpl>authorities = new ArrayList<GrantedAuthorityImpl>();


		for (String serviceURL : serviceURLs){
			String actualURL = serviceURL.replaceAll("\\u007Bsecret\\u007D", username);
			try {
				URL url = new URL(actualURL);

				URLConnection conx = url.openConnection();
				if (authHeader != null){
					conx.setRequestProperty("Authorization", "Basic "+ authHeader);
				}
				conx.connect();
				input = conx.getInputStream();
				boolean success = webServiceParser.parseUserDetailsResponse(input);
				if (success){
					// Create the user from the parsed values
					String name = webServiceParser.getUserName();
					String password = webServiceParser.getPassword();
					for (String current : webServiceParser.getRoles()){
						authorities.add(new GrantedAuthorityImpl(roleMapper.toPentahoRole(current)));
					}
					if (pentahoAdmins.contains(username)){
						authorities.add(new GrantedAuthorityImpl(OEMUtil.PENTAHOADMIN));
					}
					details = new OEMUser(name, password, true, true, true, true, authorities.toArray(new GrantedAuthorityImpl[0]));
					Map<String, String> sessionVars = webServiceParser.getSessionVariables();
					for (String variable : sessionVars.keySet()){
						details.addSessionVariable(variable, sessionVars.get(variable));
					}
					return details;
				}
				LOG.debug("User "+ username + " not validated in webservice response from serviceURL");
			} 
			catch (AuthenticationException ex) {
				LOG.debug("Unable to communicate with service ", ex);
			} catch (MalformedURLException ex) {
				LOG.debug("Bad service URL ", ex);
			} catch (IOException ex) {
				LOG.debug("IO error "+ ex);
			}
		}
		return details;
	}


}
	
	