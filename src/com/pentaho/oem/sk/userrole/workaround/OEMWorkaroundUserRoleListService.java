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
package com.pentaho.oem.sk.userrole.workaround;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.springframework.beans.factory.InitializingBean;
import com.pentaho.oem.sk.OEMUtil;

public class OEMWorkaroundUserRoleListService  implements IUserRoleListService, InitializingBean {

	private ITenantedPrincipleNameResolver userNameUtils;

    private static final Log LOG = LogFactory.getLog(OEMWorkaroundUserRoleListService.class);

	public OEMWorkaroundUserRoleListService(ITenantedPrincipleNameResolver userNameUtils) {
		super();
		this.userNameUtils = userNameUtils;
	}



	@Override
	public void afterPropertiesSet() throws Exception {
	}

    
	

    /**
     * Returns all authorities known to the provider. Cannot return
     * <code>null</code>
     * 
     * @return the authorities (never <code>null</code>)
     */
    public List<String> getAllRoles() {
        LOG.debug("getAllRoles()");

        LinkedList<String> roleList = new LinkedList<String>();
        return roleList;
    }

    /**
     * Returns all System authorities known to the provider. Cannot return
     * <code>null</code>
     * 
     * @return the authorities (never <code>null</code>)
     */
  
    public List<String> getSystemRoles() {
        LOG.debug("getSystemRoles() tenant");
        List<String> roleList = new ArrayList<String>();
        roleList.add(OEMUtil.PENTAHOADMIN);
        roleList.add(OEMUtil.PENTAHOAUTH);
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
        LOG.debug("getAllRoles() tenant");
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

        List<String> userList = new ArrayList<String>();
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
        LOG.debug("getAllUsers() tenant");
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
        LOG.debug("getUsersInRole() tenant");
        List<String> userList = new ArrayList<String>();
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

        LOG.debug("getRolesForUser() tenant");
     	username = userNameUtils.getPrincipleName(username);
     	LinkedList<String> roleList = new LinkedList<String>();
     	roleList.add(OEMUtil.PENTAHOAUTH);
     	return roleList;
     }
    
}
