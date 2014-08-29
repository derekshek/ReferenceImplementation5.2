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

package com.pentaho.oem.sk.homefolder;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.mt.RepositoryTenantManager;
import org.pentaho.platform.repository2.unified.IRepositoryFileAclDao;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.security.userdetails.UserDetailsService;

import com.pentaho.oem.sk.OEMUser;

public class OEMRepositoryTenantManager extends RepositoryTenantManager {


	private String noCreateHomeRole;


	public String getNoCreateHomeRole()                      { return noCreateHomeRole; }
	public void setNoCreateHomeRole(String noCreateHomeRole) { this.noCreateHomeRole = noCreateHomeRole; }


	public OEMRepositoryTenantManager(IRepositoryFileDao contentDao,
			IUserRoleDao userRoleDao,
			IRepositoryFileAclDao repositoryFileAclDao,
			IRoleAuthorizationPolicyRoleBindingDao roleBindingDao,
			JcrTemplate jcrTemplate, String repositoryAdminUsername,
			String tenantAuthenticatedAuthorityNamePattern,
			ITenantedPrincipleNameResolver tenantedUserNameResolver,
			ITenantedPrincipleNameResolver tenantedRoleNameResolver,
			String tenantAdminRoleName,
			List<String> singleTenantAuthenticatedAuthorityRoleBindingList) {
		super(contentDao, userRoleDao, repositoryFileAclDao, roleBindingDao,
				jcrTemplate, repositoryAdminUsername,
				tenantAuthenticatedAuthorityNamePattern, tenantedUserNameResolver,
				tenantedRoleNameResolver, tenantAdminRoleName,
				singleTenantAuthenticatedAuthorityRoleBindingList);
	}

	private static final Log LOG = LogFactory.getLog(OEMRepositoryTenantManager.class);
	private String tenantTop;
	private String tenantVar;

	public String getTenantTop()                 { return tenantTop; }
	public void setTenantTop(String tenantTop)   { this.tenantTop = tenantTop; }
	public String getTenantVar()                { return tenantVar; }
	public void setTenantVar(String tenantVar) { this.tenantVar = tenantVar; }
	
	private UserDetailsService userDetailsService = PentahoSystem.get(UserDetailsService.class);
	 @Override
	  public RepositoryFile createUserHomeFolder(ITenant theTenant, String username) {
	    RepositoryFile userHomeFolder = null;
	    RepositoryFile tenantParentFolder = null;
	    RepositoryFile tenantSubFolder = null;

		    	
	    IUnifiedRepository repository = PentahoSystem.get(IUnifiedRepository.class, PentahoSessionHolder.getSession());
	    if (userDetailsService == null){
	    	LOG.error("No user details service");
			super.createUserHomeFolder(theTenant, username);
	    	return null;
	    }
	    
		Object userDetails = userDetailsService.loadUserByUsername(username);
		if (! (userDetails instanceof OEMUser)){
			LOG.debug("User is not a OEM user");
			super.createUserHomeFolder(theTenant, username);
		}
		
		
		OEMUser thisUser = (OEMUser) userDetails;
		LOG.debug(username + " is a new user");
		///////////////////////////////////////////  Create Home folder for appropriate users
	    if (! thisUser.hasRole(noCreateHomeRole)){
	    	try{
	    		super.createUserHomeFolder(theTenant, username);
	    	}catch (Exception e){
	    		LOG.error("Home dir creation - I am here again? " + e);
	    	}
	    }

		///////////////////////////////////////////  Create Tenant folder for Tenant users
	    String tenantName = thisUser.getSessionVariable(tenantVar);
	    if (tenantName == null || tenantName.length() == 0){
	    	return userHomeFolder;
	    }
	    //////////// first make sure Tenant folder exists
	    try{
	    	tenantParentFolder   = repository.getFile("/" + tenantTop);
	    }catch (Exception e){
	    }
	    if (tenantParentFolder == null){
	    	LOG.warn("No top level tenant folder (creating) "+ tenantTop);
	    	RepositoryFile topFolder = repository.getFile("/");
	    	RepositoryFile newFolder = new RepositoryFile.Builder(tenantTop).folder(true).build();
	    	tenantParentFolder = repository.createFolder(topFolder.getId(), newFolder, "Autocreated app dir");
	    }
	    ////////////  Create tenant subfolder if not exists
	    try{
	    	tenantSubFolder = repository.getFile("/" + tenantTop + "/" + tenantName);
	    }catch (Exception e){
	    }
	    if (tenantSubFolder == null){
	    	LOG.warn("No  tenant folder (creating) "+ tenantName);
	    	RepositoryFile newTenantFolder = new RepositoryFile.Builder(tenantName).folder(true).build();
	    	RepositoryFile appFolder = repository.createFolder(tenantParentFolder.getId(), newTenantFolder, "Autocreated tenant dir");
	    	return appFolder;
	    }
	    
	    /////////////  all done
	    return userHomeFolder;
	  }

	public UserDetailsService getUserDetailsService() {
		return userDetailsService;
	}
	public void setUserDetailsService(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}


}