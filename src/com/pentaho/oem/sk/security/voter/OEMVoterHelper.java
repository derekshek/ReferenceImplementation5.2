package com.pentaho.oem.sk.security.voter;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.springframework.beans.factory.InitializingBean;
import java.util.Map;

public class OEMVoterHelper implements InitializingBean {
    protected Map<String,Object> customerSpecificValueMap = null;

    public void setCustomerSpecificValueMap( Map<String, Object> customerSpecificValueMap) { this.customerSpecificValueMap = customerSpecificValueMap; }

	public Boolean hasAccess(RepositoryFile file, 
			RepositoryFilePermission operation, 
			RepositoryFileAcl acl, 
			IPentahoSession session,
			String[] topLevelDirs){

		return null;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// Override me if you need to
	}

}
