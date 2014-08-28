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
package com.pentaho.oem.sk.nocode;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;

public class NoCodeCommon {
	private static Log LOG = LogFactory.getLog(NoCodeCommon.class);
	private static final String SQLCLAUSE         = ".sql";
	private static final String JOINWITH          = ".joinWith";
	private static final String SEEALLROLE        = ".seeAllRole";
	private static final String NOVARSEEALL       = ".noVariableSeesAll";
	private static final String SEENOTHING        = "1=0";
	private static final String REMOVEIFROLE      = ".removeIfRole";
	private static final String REMOVEUNLESSROLE  = ".removeUnlessRole";
	private static final String propFileName = "NoCodeDsp.properties";
	private static PropertiesConfiguration configuration = null;

	
	static {
		try {
			configuration = new PropertiesConfiguration( propFileName);
			configuration.setReloadingStrategy(new FileChangedReloadingStrategy());
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}
	

	
	// Get one or more properties values, ensuring a String array (or null)
	public String[] getProperties(String propBase, String directive){
		String[] roleList = null;
		Object roleString = configuration.getProperty(propBase + directive);
		if (roleString == null){
			return null;
		}
		if (roleString instanceof ArrayList){
			roleList = new String[((ArrayList) roleString).size()];
			for (int i=0; i < ((ArrayList) roleString).size(); i++){
				roleList[i] = ((ArrayList) roleString).get(i).toString();
			}
		}else{
			roleList = new String[1];
			roleList[0] = (String) roleString;
		}
		return roleList;
	}
	

	public boolean removeByRole(String propBase){
		//  Explicitly remove logic
		String[] roleList = getProperties(propBase, REMOVEIFROLE);
		if (roleList != null){
			for (String role : roleList){
				if (hasRole(role)){
//					LOG.debug("Element explicitly denined by "+ propBase + " with role " + role);
					return true;
				}
			}
		}
		// If Removeunless is defined, deny unless the user has the role
		roleList = getProperties(propBase, REMOVEUNLESSROLE);
		if (roleList != null){
			for (String role : roleList){
				if (hasRole(role)){
//					LOG.debug("Element explicitly permitted by "+ propBase + " with role " + role);
					return false;
				}
			}
//			LOG.debug("Element denied by lack of role "+ propBase);
			return true;
		}
		return false;
	}

	public String sqlString(String propBase){
		String[] clauses = getProperties(propBase, SQLCLAUSE);
		if (clauses == null){
			return null;
		}
		// If user has "seeAll" role, just return null - no SQL added
		if (canSeeAll(propBase)){
			LOG.debug(propBase + " Has see all role");
			return null;
		}


		// Substitute session variables into SQL (return original if no variable in it)
		String newSql = null;
		ArrayList<String> newClauses = new ArrayList<String>();
		for (String sql : clauses){
			String newClause = null;
			if (hasVars(sql)){
				newClause = substituteVars(sql);

			    // If no session vars are substituted, we see all (return null) or see nothing
				if (newClause == null){
					if (configuration.containsKey(propBase+NOVARSEEALL) && configuration.getProperty(propBase+NOVARSEEALL).equals("true")){
//						LOG.debug("No session var and NOVARSEEALL is true - user will see everything");
					}else{
						newClause = SEENOTHING;
//						LOG.debug("No session var and NOVARSEEALL is not true - user will see nothing");
					}
				}
			}
			if (newClause != null){
				newClauses.add(" ( " + newClause + " ) "  );
			}
		}
		
		if (newClauses.size() > 0){
			String joinWith = "AND";
			if (configuration.containsKey(propBase+JOINWITH)){
				joinWith = (String) configuration.getProperty(propBase+JOINWITH);
			}
			newSql = StringUtils.join(newClauses, joinWith);
		}
//		LOG.debug("sqlString returns " + newSql);
		return newSql;
	}

	
	
	
	public boolean canSeeAll(String propBase){
		if (configuration.containsKey(propBase+SEEALLROLE)){
			String[] roleList = getProperties(propBase, SEEALLROLE);
			for (String role : roleList){
				if (hasRole(role)){
					return true;
				}
			}
		}
		return false;
	}
	
	
	
	private boolean hasVars(String sql) {
		Matcher matcher = Pattern.compile("\\$\\{([^\\}]*)\\}").matcher(sql);
		if (matcher.find()) {
			return true;
		}
		return false;
	}



	public String substituteVars(String sql) {
		IPentahoSession session = PentahoSessionHolder.getSession();
		if (session == null){
			return null;
		}
		Matcher matcher = Pattern.compile("\\$\\{([^\\}]*)\\}").matcher(sql);
		StringBuffer newSql = new StringBuffer();
		while (matcher.find()) {
			String varname = matcher.group(1);
			String value = (String) session.getAttribute(varname);
			if (value == null){
//				LOG.debug("User does not have session variable "+ varname);
				return null;
			}
//			LOG.debug("Replacing "+ varname + " with "+ value);
			matcher.appendReplacement(newSql, value);
		}
		return matcher.appendTail(newSql).toString();
	}



	private boolean hasRole(String checkRole){
		GrantedAuthority[] roles = null;
//		LOG.debug("Looking for role to see all:"+checkRole);

		if (    SecurityContextHolder.getContext()                                      != null &&
				SecurityContextHolder.getContext().getAuthentication()                  != null &&
				(roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities()) != null
				){
//			LOG.debug("Looking for role to see all:"+checkRole);
			for (GrantedAuthority role : roles){
//				LOG.debug("Has role? :"+role);
				if (role.toString().equals(checkRole)){
					return true;
				}
//				LOG.debug("no joy");
			}
		}
		return false;
	}



	public PropertiesConfiguration getConfiguration() {
		return configuration;
	}
}
