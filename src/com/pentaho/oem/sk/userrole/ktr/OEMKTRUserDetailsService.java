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
package com.pentaho.oem.sk.userrole.ktr;
/**
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
 */



import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.security.AuthenticationServiceException;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;
import com.pentaho.oem.sk.OEMUser;
import com.pentaho.oem.sk.OEMUtil;


public class OEMKTRUserDetailsService implements UserDetailsService, InitializingBean {


	private static final Log LOG = LogFactory.getLog(OEMKTRUserDetailsService.class);
	private ITenantedPrincipleNameResolver userNameUtils;
	private String serviceKTR;
	private String tokenResolverKTR;

	public OEMKTRUserDetailsService(ITenantedPrincipleNameResolver userNameUtils) {
		super();
		this.userNameUtils = userNameUtils;
	}

	public void setServiceKTR(String url) {
		LOG.debug("ServiceKTR is:"+url);
		serviceKTR = url;
	}
	
	
	protected String getServiceKTR() {
		return serviceKTR;
	}

	public String getTokenResolverKTR() {
		return tokenResolverKTR;
	}

	public void setTokenResolverKTR(String tokenResolverKTR) {
		this.tokenResolverKTR = tokenResolverKTR;
	}


	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(serviceKTR);
		Assert.notNull(tokenResolverKTR);
	}

	
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
		UserDetails details = null;
    	username = userNameUtils.getPrincipleName(username);

		LOG.debug("Loading info for "+username);
		
		if (username.startsWith(OEMUtil.TOKEN_PREFIX)){
			username = username.replace(OEMUtil.TOKEN_PREFIX, "");
			LOG.debug("Token found - calling KTR to resolve "+username);
			try {
				List<String> results = OEMKTRUserRoleListService.executeKtr(username, tokenResolverKTR);
				if (results.size() > 0){
					username = results.get(0);
					LOG.debug("Token resolved to "+username);
				}else{ 
					LOG.debug("No rows returned for "+username);
					throw new AuthenticationServiceException("Could not resolve token");
				}
			} catch (Exception e) {
				throw new AuthenticationServiceException("token resolver KTR failed");
			}
		}


		try {
			details = loadUserByUsername(userNameUtils.getPrincipleName(username),serviceKTR, new HashMap<String,String>());
		} 
		catch (Exception ex) {
			throw new AuthenticationServiceException("Unable to communicate with service ", ex);
		}


		LOG.debug("Returning details: "+details);
		return details;

	}


	public UserDetails loadUserByUsername(String username, String ktrFile,Map<String, String> envArgs) throws Exception{
        final OEMUser user = new OEMUser(username);
		String  args[] = { username };

		KettleEnvironment.init();
		TransMeta metaData = new TransMeta(ktrFile);
		Trans trans = new Trans( metaData );
		trans.setLogLevel(LogLevel.BASIC);
		trans.setVariable("principalName", username);
		for (String var : envArgs.keySet()){
			trans.setVariable(var, envArgs.get(var));
		}
		trans.prepareExecution(args);

		StepInterface step = trans.findRunThread("Output");

		if (step == null){
			throw new KettleException("Cant find step with name 'Output' step  in " + ktrFile);
		}
		
		step.addRowListener(new RowAdapter() {
			public void rowWrittenEvent(RowMetaInterface rowMeta, Object[] row) throws KettleStepException {
				// Here you get the rows as they are written by the step
				if (row.length >= 3 && row[0] != null && row[1] != null){
					String roleOrVariable =  row[0].toString();
					String name =  row[1].toString();
					if (roleOrVariable.equalsIgnoreCase("variable") && row[2] != null){
						String value = row[2].toString();
						user.addSessionVariable(name, value);
					}else if (roleOrVariable.equalsIgnoreCase("role") && user != null){
						user.addRole(name);
					}
				}
			}
		}
				);
		trans.startThreads();
		trans.waitUntilFinished();
		if ( trans.getErrors() > 0 ) {
			throw new KettleException( "Error Executing transformation for user " + username);
		}
		
		// return null here if the user is not authenticated
		if (! user.hasRole(OEMUtil.PENTAHOAUTH)){
			LOG.debug("ktr did not have authenticated role");
			throw new UsernameNotFoundException("not found with ktr");
		}

		return user;
	}
	
	

}
	
	