package com.customerspecific;

import java.util.ArrayList;
import java.util.Map;

import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.springframework.beans.factory.InitializingBean;

import com.pentaho.oem.sk.homefolder.OEMTenantManagerHelper;

public class CustomTenantManagerHelper extends OEMTenantManagerHelper implements InitializingBean {

	protected Map<String,Object> customerSpecificValueMap = null;
	private String[] categoryFolders = null;


	///////////////////////////////////////////// getters and setters ///////////////////////////////////////
	public Map<String, Object> getCustomerSpecificValueMap() { return customerSpecificValueMap; }
	public void setCustomerSpecificValueMap( Map<String, Object> customerSpecificValueMap) { this.customerSpecificValueMap = customerSpecificValueMap; }
	/////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void proceessTenantDir(IUnifiedRepository repository, RepositoryFile tenantFolder){
	     // if you need to do more to a tenant folder, put it here
	}

	@Override
	public void afterPropertiesSet() throws Exception {
	     // if you need to do more on initialization, put it here
	}

}
