package com.pentaho.oem.sk.security.voter;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;

public class OEMVoterHelper {
	
	public Boolean hasAccess(RepositoryFile file, 
			RepositoryFilePermission operation, 
			RepositoryFileAcl acl, 
			IPentahoSession session,
			String[] topLevelDirs){

		return null;
	}

}
