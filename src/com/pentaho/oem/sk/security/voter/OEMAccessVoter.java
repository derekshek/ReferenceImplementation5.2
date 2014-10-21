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
package com.pentaho.oem.sk.security.voter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IRepositoryAccessVoter;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.springframework.security.userdetails.User;

import com.pentaho.oem.sk.OEMUtil;

public class OEMAccessVoter implements IRepositoryAccessVoter{

	Log LOG = LogFactory.getLog(OEMAccessVoter.class);
	private OEMVoterHelper helper = null;
	private String tenantTop;
	private String tenantVar;
	private String sharedFolder;
	private Set<String> hideTheseForTenants = new HashSet<String>();
	

	///////////////////////////////////////////// getters and setters ///////////////////////////////////////
	public OEMVoterHelper getHelper() { return helper; }
	public void setHelper(OEMVoterHelper helper) { this.helper = helper; }
	public void setHideTheseForTenants(String[] hideThese) {
		for (String dir : hideThese){
			this.hideTheseForTenants.add(dir);
		}
	}
	public String getTenantTop()                 { return tenantTop; }
	public void setTenantTop(String tenantTop)   { this.tenantTop = tenantTop; }
	public String getSharedFolder()                 { return sharedFolder; }
	public void setSharedFolder(String sharedFolder)   { this.sharedFolder = sharedFolder; }
	public String getTenantVar()                { return tenantVar; }
	public void setTenantVar(String tenantVar) { this.tenantVar = tenantVar; }


	@Override
	public boolean hasAccess(RepositoryFile file, 
			RepositoryFilePermission operation, 
			RepositoryFileAcl acl, 
			IPentahoSession session) {


        String msg = "--------------------" + file.getPath() + "  " + operation;

		// Get the content request path - turn into array
		String[] topLevelDirs = getTopLevelDirNames(file);
		if (topLevelDirs == null || topLevelDirs.length < 2){
			LOG.debug("Toplevel too short");
			return true;
		}
		String level1 = topLevelDirs[1];
		String level2 = topLevelDirs.length > 2 ? topLevelDirs[2] : null;

		//  system level stuff - these are needed for datasources, among others
		if ("etc".equals(level1)){
			return true;
		}
			
		// tenant folders if here
		String tenantid = "" + session.getAttribute(tenantVar);
		if (tenantTop != null && tenantTop.equals(level1)){
			if (level2 == null){ 
     			LOG.debug("Allow level 2 null " + msg);
				return true;
			}
			
			if (tenantid != null && level2.equals(sharedFolder) && operation.equals(RepositoryFilePermission.READ)){
     			LOG.debug("Allow level 2 shared " + msg);
				return true;
			}
			
			///////////////////////////////// Isolate any custom logic in the helper ////////////////////
			if (helper != null){
				Boolean customCheck = helper.hasAccess(file, operation, acl, session, topLevelDirs);
				if (customCheck != null){
					return customCheck;
				}
			}

			
			if (tenantid != null && tenantid.equals(level2)){
				LOG.debug("Allow  by tenant id " + msg);
				return true;
			}
			LOG.debug("Deny by default " + msg);
			return false;
		}
		
		// Hide to tenants
		if (tenantid != null){
			if (hideTheseForTenants.contains(level1)){
				LOG.debug("Deny by hide toplevel " + msg);
				return false;
			}
		}

		LOG.debug("Allow by default " + msg);
		return true; 
	}
	

	protected String[] getTopLevelDirNames(RepositoryFile f){

		String fullpath = f.getPath();
		LOG.debug("File is "+fullpath);
		if (fullpath != null){
			String [] dirs = fullpath.split("/");
			return dirs;
		} else {
			return null;
		}
	}		

}
