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
package com.pentaho.oem.sk.userrole.ktr;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.userroledao.service.UserRoleDaoUserRoleListService;
import org.pentaho.platform.plugin.services.security.userrole.PentahoCachingUserDetailsService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.AuthenticationServiceException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.util.Assert;

import com.pentaho.oem.sk.OEMUtil;
import com.pentaho.oem.sk.userrole.webservice.OEMWebServiceUserDetailsService;

public class OEMKTRUserRoleListService  implements IUserRoleListService, InitializingBean {

	private ITenantedPrincipleNameResolver userNameUtils;
	private UserDetailsService userDetailsService;
    private String ktrAllRoles;
    private String ktrAllUsers;
    private String ktrUsersInRole;

    private static final Log LOG = LogFactory.getLog(OEMKTRUserRoleListService.class);

	public OEMKTRUserRoleListService(ITenantedPrincipleNameResolver userNameUtils) {
		super();
		this.userNameUtils = userNameUtils;
	}



	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(userDetailsService);
	}

    
	
	public void setParentUserDetailsService(PentahoCachingUserDetailsService userDetailsService){
		this.userDetailsService = userDetailsService;
	}
  
  
    public String getKtrAllRoles() {
        return ktrAllRoles;
    }

    public void setKtrAllRoles(String urlAllRoles) {
        this.ktrAllRoles = urlAllRoles;
    }

    public String getKtrAllUsers() {
        return ktrAllUsers;
    }

    public void setKtrAllUsers(String urlAllUsers) {
        this.ktrAllUsers = urlAllUsers;
    }

    public String getKtrUsersInRole() {
        return ktrUsersInRole;
    }

    public void setKtrUsersInRole(String urlUsersInRole) {
        this.ktrUsersInRole = urlUsersInRole;
    }


    /**
     * Returns all authorities known to the provider. Cannot return
     * <code>null</code>
     * 
     * @return the authorities (never <code>null</code>)
     */
    public List<String> getAllRoles() {
        LOG.debug("getAllRoles()");

        List<String> roleList = new LinkedList<String>();
        try {
            roleList = executeKtr(getKtrAllRoles(),null);
        } catch (Exception ex) {
            throw new AuthenticationServiceException( "Unable to communicate with service " + getKtrAllRoles(), ex);
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

        List<String> userList = new LinkedList<String>();
        try {
            userList = executeKtr(getKtrAllUsers(),null);
        } catch (Exception ex) {
            throw new AuthenticationServiceException( "Unable to communicate with service " + getKtrAllUsers(), ex);
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
        List<String> userList = new LinkedList<String>();
        
        try {
			userList = executeKtr(getKtrUsersInRole(),role);
		} catch (Exception e) {
			LOG.error("Got exception "+e);
			e.printStackTrace();
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
         
         // Most of these calls are for the current user - avoid calling userDetails if possible
         try {
         	if (username.equals(PentahoSessionHolder.getSession().getName())){
         		authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
//         		LOG.trace("getRolesForUser(tenant,"+username+") -same-user-");
         	}
         } catch (NullPointerException e){
         	LOG.debug("No session or security context for "+username);
         }

         // Call UserDetailsService if that didnt work
         if (authorities == null){
         	UserDetails user = userDetailsService.loadUserByUsername(username);
         	authorities = user.getAuthorities();
         }
         
         // Convert to List for return
         for (GrantedAuthority ga : authorities){
         	roleList.add(ga.toString());
         }
         	
         LOG.trace("getRolesForUser(tenant,"+username+"):" + StringUtils.join(roleList,", "));
         return roleList;
     }

  
	public static List<String> executeKtr(String arg, String ktrFile) throws Exception{
		String  args[] = { arg };

		KettleEnvironment.init();
		TransMeta metaData = new TransMeta(ktrFile);
		Trans trans = new Trans( metaData );
		trans.setLogLevel(LogLevel.BASIC);
		trans.setVariable("arg", arg);
		trans.prepareExecution(args);

		StepInterface step = trans.findRunThread("Output");

		if (step == null){
			throw new KettleException("Cant find step with name 'Output' step  in " + ktrFile);
		}
		
		final List<String> output = new LinkedList<String>();
		
		step.addRowListener(new RowAdapter() {
			public void rowWrittenEvent(RowMetaInterface rowMeta, Object[] row) throws KettleStepException {
				// Here you get the rows as they are written by the step
				if (row.length >= 1){
					output.add(row[0].toString());
				}
			}
		}
				);
		trans.startThreads();
		trans.waitUntilFinished();
		if ( trans.getErrors() > 0 ) {
			throw new KettleException( "Error Executing transformation for " + ktrFile + " with arg " + arg);
		}

		return output;
	}
	
	

    
}
