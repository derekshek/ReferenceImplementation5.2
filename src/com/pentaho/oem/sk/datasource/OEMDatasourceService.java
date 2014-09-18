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

package com.pentaho.oem.sk.datasource;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.DynamicallyPooledOrJndiDatasourceService;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.PooledOrJndiDatasourceService;

import javax.sql.DataSource;

public class OEMDatasourceService  extends  DynamicallyPooledOrJndiDatasourceService{
	Log logger = LogFactory.getLog(OEMDatasourceService.class);
	private Set<String> datasourcesToModify = new HashSet<String>();
	private String sessionVariableToAppend = null;
	private String separator = "_";


	public void setDatasourcesToModify(String[] datasourcesToModify) {
		for (String ds : datasourcesToModify){
			this.datasourcesToModify.add(ds);
		}
	}
	///////////////////////////////// getters and setters /////////////////////////////////////////////////////////////////////////////
	public String getSessionVariableToAppend() { return sessionVariableToAppend; }
	public void   setSessionVariableToAppend(String sessionVariableToAppend) { this.sessionVariableToAppend = sessionVariableToAppend; }
	public String getSeparator() { return separator; }
	public void   setSeparator(String separator) { this.separator = separator; }
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public DataSource getDataSource(String dsName) throws DBDatasourceServiceException {
		if (datasourcesToModify.contains(dsName)){
			dsName = modifyDsNameForTenancy(dsName);
		}
		return super.getDataSource(dsName);
	}

	@Override
	public void clearDataSource(String dsName) {
		super.clearDataSource(modifyDsNameForTenancy(dsName));
	}

	private String modifyDsNameForTenancy(String dsName){

		logger.debug("Original DSName is "+dsName);
		IPentahoSession session = PentahoSessionHolder.getSession();
		if (session == null){
			logger.warn("Session is null; no modifications made to the JNDI dsName.");
			return dsName;
		}

		String varToAppend = (String)session.getAttribute(sessionVariableToAppend);

		if (StringUtils.isEmpty(varToAppend)){
			logger.warn("ID not found in session; no modifications made to the JNDI dsName.");
			return dsName;
		}
		String dsname = dsName.concat(separator).concat(varToAppend);
		logger.debug("New DSName is "+dsname);
		return dsname;
	}

}
