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


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.util.Assert;

import com.pentaho.oem.sk.OEMUtil;

/**
 *
 * @author pedro
 */
public class OEMMultiUserRoleListService implements IUserRoleListService, InitializingBean {

	private static final Log LOG = LogFactory.getLog(OEMMultiUserRoleListService.class);

    private List<IUserRoleListService> userRoleListServices;
    @SuppressWarnings("unused")
	private Comparator<String> usernameComparator;
    @SuppressWarnings("unused")
	private Comparator<GrantedAuthority> grantedAuthorityComparator;

	private ITenantedPrincipleNameResolver userNameUtils;

    public OEMMultiUserRoleListService(ITenantedPrincipleNameResolver userNameUtils) {

        super();
        userRoleListServices = new ArrayList<IUserRoleListService>();
        LOG.debug("Constructor");
		this.userNameUtils = userNameUtils;
    }

    public void afterPropertiesSet() throws Exception {

//        if (this.userRoleListServices == null || this.userRoleListServices.isEmpty()) {
//            throw new Exception("UserDetailsRoleListService.ERROR_0001_USERROLELISTSERVICE_NOT_SET");
//        }
    }

    public void setUserRoleListServices(final IUserRoleListService[] values) {
    	
    	LOG.debug("MultiUser");

        for (int i = 0; i < values.length; i++) {
        	LOG.debug("    adding UserRoleListService "+values[i]);
            IUserRoleListService iUserRoleListService = values[i];
            this.userRoleListServices.add(iUserRoleListService);
        }

    }

    public void setGrantedAuthorityComparator(final Comparator<GrantedAuthority> grantedAuthorityComparator) {
        Assert.notNull(grantedAuthorityComparator);
        this.grantedAuthorityComparator = grantedAuthorityComparator;
    }

    public void setUsernameComparator(final Comparator<String> usernameComparator) {
        Assert.notNull(usernameComparator);
        this.usernameComparator = usernameComparator;
    }

    public List<String> getAllRoles() {
    	LOG.debug("MultiUser");
    	return getAllRoles(null);
    }
    
    
    public List<String> getAllRoles(ITenant tenant) {

    	LOG.debug("MultiUser");
        Set<String> results = new HashSet<String>();

        for (Iterator<IUserRoleListService> it = userRoleListServices.iterator(); it.hasNext();) {
            IUserRoleListService iUserRoleListService = it.next();
            if (tenant == null){
            	results.addAll(iUserRoleListService.getAllRoles());
            }else{
            	results.addAll(iUserRoleListService.getAllRoles(tenant));
            }

        }
        return new ArrayList<String>(results);
    }

    public List<String> getAllUsers() {
    	LOG.debug("MultiUser");
    	return getAllUsers(null);
    }

    public List<String> getAllUsers(ITenant tenant) {
    	LOG.debug("MultiUser");

        Set<String> results = new HashSet<String>();

        for (Iterator<IUserRoleListService> it = userRoleListServices.iterator(); it.hasNext();) {
            IUserRoleListService iUserRoleListService = it.next();
            if (tenant == null){
            	results.addAll(iUserRoleListService.getAllUsers());
            }else{
            	results.addAll(iUserRoleListService.getAllUsers(tenant));
            }
        }
        return new ArrayList<String>(results);

    }

    /**
     * Search for the usernames
     *
     * @param authority
     * @return
     */
    public List<String> getUsersInRole(ITenant tenant, String role) {
    	LOG.debug("MultiUser");

        Set<String> results = new HashSet<String>();

        for (Iterator<IUserRoleListService> it = userRoleListServices.iterator(); it.hasNext();) {
            IUserRoleListService iUserRoleListService = it.next();
            results.addAll(iUserRoleListService.getUsersInRole(tenant,role));
        }

        return new ArrayList<String>(results);
    }

    

    public List<String> getRolesForUser(ITenant tenant, String username) {

    	username = userNameUtils.getPrincipleName(username);
    	GrantedAuthority[] authorities = null;
        Set<String> results = new HashSet<String>();
 

        for (Iterator<IUserRoleListService> it = userRoleListServices.iterator(); it.hasNext();) {
            IUserRoleListService iUserRoleListService = it.next();

            try{
                results.addAll(iUserRoleListService.getRolesForUser(tenant,username));
            }
            catch(Exception UsernameNotFoundException){
                // In multiple providers, this errors are not uncommon
            }
            
        }

        return new ArrayList<String>(results);
    }



	@Override
	public List<String> getSystemRoles() {
    	LOG.debug("MultiUser");
		List<String> roles = new ArrayList<String>();
		roles.add(OEMUtil.PENTAHOADMIN);
		roles.add(OEMUtil.PENTAHOAUTH);
        return roles;
	}

}
