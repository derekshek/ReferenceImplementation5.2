package com.pentaho.oem.sk.homefolder;

import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.springframework.beans.factory.InitializingBean;
import java.util.Map;

public class OEMTenantManagerHelper implements InitializingBean {
	private String[] tenantSubdirs = null;

	public String[] getTenantSubdirs()                   { return tenantSubdirs; }
	public void setTenantSubdirs(String[] tenantSubdirs) { this.tenantSubdirs = tenantSubdirs; }
    protected Map<String,Object> customerSpecificValueMap = null;

    public void setCustomerSpecificValueMap( Map<String, Object> customerSpecificValueMap) { this.customerSpecificValueMap = customerSpecificValueMap; }

	
	public void proceessTenantDir(IUnifiedRepository repository, RepositoryFile tenantFolder){
		// Ovverride this if desired
	}
	
	
	@Override
	public void afterPropertiesSet() throws Exception {
		// Ovverride this if desired
	}

}
