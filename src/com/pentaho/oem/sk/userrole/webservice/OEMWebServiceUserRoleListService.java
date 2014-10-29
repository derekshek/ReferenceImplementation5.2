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


import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.plugin.services.security.userrole.PentahoCachingUserDetailsService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.AuthenticationServiceException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.util.Assert;

import com.pentaho.oem.sk.OEMUtil;

public class OEMWebServiceUserRoleListService  implements IUserRoleListService, InitializingBean {

    private static final Log LOG = LogFactory.getLog(OEMWebServiceUserRoleListService.class);
	protected ITenantedPrincipleNameResolver userNameUtils;
	protected UserDetailsService userDetailsService;
    private String urlAllRoles;
    private String urlAllUsers;
    private String urlUsersInRole;
	private   OEMWebServiceParser            webServiceParser;

    ////////////////////////////////// Getters & Setters /////////////////////////////////////////////////////////////////
    public OEMWebServiceParser getWebServiceParser()                      { return webServiceParser; }
	public void setWebServiceParser(OEMWebServiceParser webServiceParser) { this.webServiceParser = webServiceParser; }
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



	public OEMWebServiceUserRoleListService(ITenantedPrincipleNameResolver userNameUtils) {
		super();
		this.userNameUtils = userNameUtils;
	}



	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(userDetailsService);
		Assert.notNull(webServiceParser);
	}

    
	
	public void setParentUserDetailsService(PentahoCachingUserDetailsService userDetailsService){
		this.userDetailsService = userDetailsService;
	}
  
  
    public String getUrlAllRoles() {
        return urlAllRoles;
    }

    public void setUrlAllRoles(String urlAllRoles) {
        this.urlAllRoles = urlAllRoles;
    }

    public String getUrlAllUsers() {
        return urlAllUsers;
    }

    public void setUrlAllUsers(String urlAllUsers) {
        this.urlAllUsers = urlAllUsers;
    }

    public String getUrlUsersInRole() {
        return urlUsersInRole;
    }

    public void setUrlUsersInRole(String urlUsersInRole) {
        this.urlUsersInRole = urlUsersInRole;
    }


    /**
     * Returns all authorities known to the provider. Cannot return
     * <code>null</code>
     * 
     * @return the authorities (never <code>null</code>)
     */
    public List<String> getAllRoles() {
        LOG.debug("getAllRoles()");

        InputStream input = null;
        LinkedList<String> roleList = new LinkedList<String>();
        try {
            input = executeService(getUrlAllRoles());

            SAXReader reader;
            Element root;
            reader = new SAXReader();
            root = reader.read(input).getRootElement();

            // Get List
            roleList = new LinkedList<String>();
            @SuppressWarnings("unchecked")
			List<Element> list = root.selectNodes("role");
            for (Element current : list) {
                roleList.add(current.getTextTrim());
            }

        } catch (Exception ex) {
            throw new AuthenticationServiceException(
                    "Unable to communicate with service " + getUrlAllRoles(),
                    ex);
        } finally {
            if (input != null)
                try {
                    input.close();
                } catch (Exception e) {
                }
        }
        return roleList;
    }

    /**
     * Returns all System authorities known to the provider. Cannot return
     * <code>null</code>
     * 
     * @return the authorities (never <code>null</code>)
     */
  
    public List<String> getSystemRoles() {
        List<String> roleList = new ArrayList<String>();
        roleList.add(OEMUtil.PENTAHOADMIN);
        return roleList;
    }

    /**
     * Returns all authorities known to the provider for a given tenant. Cannot
     * return <code>null</code>
     * 
     * @param tenant
     * @return the authorities (never <code>null</code>)
     */
    public List<String> getAllRoles(ITenant tenant) {
        return getAllRoles();
    }

    /**
     * Returns all user names known to the provider. Cannot return
     * <code>null</code>
     * 
     * @return the users (never <code>null</code>)
     */
    public List<String> getAllUsers() {
        LOG.debug("getAllUsers()");

        LinkedList<String> userList;
        InputStream input = null;
        try {

            input = executeService(getUrlAllUsers());

            SAXReader reader;
            Element root;
            reader = new SAXReader();
            root = reader.read(input).getRootElement();

            // Get List
            userList = new LinkedList<String>();
            @SuppressWarnings("unchecked")
			List<Element> list = root.selectNodes("user");
            for (Element current : list) {
                userList.add(current.getTextTrim());
            }

        } catch (Exception ex) {
            throw new AuthenticationServiceException(
                    "Unable to communicate with service " + getUrlAllUsers(),
                    ex);
        } finally {
            if (input != null)
                try {
                    input.close();
                } catch (Exception e) {
                }
        }
        return userList;
    }

    /**
     * Returns all user names known to the provider for a given tenant. Cannot
     * return <code>null</code>
     * 
     * @param tenant
     * @return the users (never <code>null</code>)
     */
    public List<String> getAllUsers(ITenant tenant) {
        return getAllUsers();
    }

    /**
     * Returns all known users in the specified role. Cannot return
     * <code>null</code>
     * 
     * @param tenant
     *            . tenant information
     * @param authority
     *            The authority to look users up by. Cannot be <code>null</code>
     * @return the users. (never <code>null</code>)
     */
    public List<String> getUsersInRole(ITenant tenant, String role) {
    	
        LinkedList<String> userList;
        InputStream input = null;
        try {
            input = executeService(getUrlUsersInRole());
            SAXReader reader;
            Element root;
            reader = new SAXReader();
            root = reader.read(input).getRootElement();

            // Get List roles
            userList = new LinkedList<String>();
            @SuppressWarnings("unchecked")
			List<Element> list = root.selectNodes(role + "/user");
            for (Element current : list) {
                userList.add(current.getTextTrim());
            }

        } catch (Exception ex) {
            throw new AuthenticationServiceException(
                    "Unable to communicate with service " + getUrlUsersInRole(),
                    ex);
        } finally {
            if (input != null)
                try {
                    input.close();
                } catch (Exception e) {
                }
        }
        return userList;
    }

    /**
     * Returns all authorities granted for a specified user.
     * 
     * @param tenant
     *            information
     * @param username
     *            The name of the user to look up authorities for
     * @return the authorities. (Never <code>null</code>)
     */  
    public List<String> getRolesForUser(ITenant tenant, String username) {

     	username = userNameUtils.getPrincipleName(username);
     	GrantedAuthority[] authorities = null;
         LinkedList<String> roleList = new LinkedList<String>();
         
//         // Most of these calls are for the current user - avoid calling userDetails if possible
//         try {
//         	if (username.equals(PentahoSessionHolder.getSession().getName())){
//         		authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
//         		LOG.trace("getRolesForUser(tenant,"+username+") -same-user-");
//         	}
//         } catch (NullPointerException e){
//         	LOG.debug("No session or security context for "+username);
//         }

         // Call UserDetailsService if that didnt work
         if (authorities == null){
         	UserDetails user = userDetailsService.loadUserByUsername(username);
         	authorities = user.getAuthorities();
         }
         
         // Convert to List for return
         for (GrantedAuthority ga : authorities){
         	roleList.add(ga.toString());
         }

         LOG.debug("getRolesForUser (tenant,"+username+"):" + StringUtils.join(roleList,", "));
         return roleList;
         
         
     }

  
    protected InputStream executeService(String sUrl) throws Exception {
        LOG.debug("executeService");

        URLConnection conx;
        URL url = new URL(sUrl);
        conx = url.openConnection();
        conx.connect();
        return conx.getInputStream();
    }


    
}
