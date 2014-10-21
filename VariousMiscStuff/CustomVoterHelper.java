package com.customerspecific;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;

import com.pentaho.oem.sk.security.voter.OEMAccessVoter;
import com.pentaho.oem.sk.security.voter.OEMVoterHelper;

public class CustomVoterHelper extends OEMVoterHelper {
	
	
	Log LOG = LogFactory.getLog(CustomVoterHelper.class);
	private Set<String> tenantSubdirs = new HashSet<String>();

	public String[] getTenantSubdirs() { return (String[]) tenantSubdirs.toArray(); }
	public void setTenantSubdirs(String[] tenantSubdirs) { 
		for (String dir : tenantSubdirs){
			this.tenantSubdirs.add(dir);
		}
	}


	/////////////////////////////////// TD Specific  ////////////////////////
	@Override
	public Boolean hasAccess(RepositoryFile file, 
			RepositoryFilePermission operation, 
			RepositoryFileAcl acl, 
			IPentahoSession session,
			String[] topLevelDirs){


        //////////////////////  Here we enforce that only approved subfolders (categories) are accessible
		if (topLevelDirs.length > 3){
			String msg = "--------------------" + file.getPath() + "  " + operation;
			String me = session.getName();
//			String owner = acl.getOwner().getName();
//			if (me.equals(owner)){
//				LOG.debug("Allow same owner " + msg);
//				return true;
//			}
			if (me.equals(topLevelDirs[3])){
				LOG.debug("Allow same name " + msg);
				return true;
			}
			if (tenantSubdirs.contains(topLevelDirs[3])){
				LOG.debug("Allow subdir " + msg);
				return true;
			}
			LOG.debug("Denied " + msg);
			return false;
		}
		
		return null;
	}
	/////////////////////////////////////////////////////////////////////////
}
