/**
 * COPYRIGHT (C) 2013 Pentaho. All Rights Reserved.
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
package com.pentaho.oem.sk.userrole.jndi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.security.AuthenticationServiceException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;

import com.pentaho.oem.sk.OEMUser;
import com.pentaho.oem.sk.OEMUtil;
import com.pentaho.oem.sk.filters.OEMFilterHelper;

public class OEMJndiUserDetailsService implements UserDetailsService {

	
	private static final Log LOG = LogFactory.getLog(OEMJndiUserDetailsService.class);
	private ITenantedPrincipleNameResolver userNameUtils;
	private String sqlRolesForUser;
	private String jndiName = null;
	private String dbconnectUser = null;
	private String dbconnectPassword = null;
	private String dbconnectURL = null;
	private String dbconnectDB = null;
	private OEMFilterHelper filterHelper = null;



	public OEMJndiUserDetailsService(ITenantedPrincipleNameResolver userNameUtils) {
		super();
		this.userNameUtils = userNameUtils;
	}
	
///////////////////////////////////////////////// Getters and Setters //////////////////////////////////
	public String getJndiName()                                  { return jndiName; }
	public void   setJndiName(String jndiName)                   { this.jndiName = jndiName; }
	
	public String getDbconnectUser()                             { return dbconnectUser; }
	public void   setDbconnectUser(String dbconnectUser)         { this.dbconnectUser = dbconnectUser; }
	public String getDbconnectPassword()                         { return dbconnectPassword; }
	public void   setDbconnectPassword(String dbconnectPassword) { this.dbconnectPassword = dbconnectPassword; }
	public String getDbconnectURL()                              { return dbconnectURL; }
	public void   setDbconnectURL(String dbconnectURL)           { this.dbconnectURL = dbconnectURL; }
	public String getDbconnectDB()                               { return dbconnectDB; }
	public void   setDbconnectDB(String dbconnectDB)             { this.dbconnectDB = dbconnectDB; }
	public String getSqlRolesForUser()                           { return sqlRolesForUser; }
	public void   setSqlRolesForUser(String sqlRolesForUser)     { this.sqlRolesForUser = sqlRolesForUser; }

	public OEMFilterHelper getFilterHelper()                     { return filterHelper; }
	public void setFilterHelper(OEMFilterHelper filterHelper)    { this.filterHelper = filterHelper; }
////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String getDatabaseUserId(String userId){
		String dbUserId = userId;
    	if (filterHelper != null){
    		dbUserId = filterHelper.pentahoUserToServiceUser(userId); 
    	}
    	return dbUserId;
	}

	public Connection getConnection(String userId) throws Exception{
		Connection conn = null;
		String userDatabase = dbconnectDB;

		// Use the current user if null userId 
		if (userId == null){
			IPentahoSession session = PentahoSessionHolder.getSession();
			if (session != null){
				userId = session.getName();
			}
		}

		// override the database if needed
    	if (filterHelper != null){
    		String overrideDB = filterHelper.getDatabaseForUser(userId);
    		if (overrideDB != null){
    			userDatabase = overrideDB;
    		}
    	}
    	
		if (jndiName != null){
			conn = OEMUtil.getConnection(getJndiName());
		}else{
			conn = OEMUtil.getConnection(dbconnectURL, userDatabase, dbconnectUser, dbconnectPassword);
		}
		return conn;
	}
	
	
	@Override
	public UserDetails loadUserByUsername(String pentahoUserId) throws UsernameNotFoundException, DataAccessException, AuthenticationServiceException {

		LOG.debug("loadUserByUsername(" + pentahoUserId + ")");
		UserDetails details = null;
		LinkedList<GrantedAuthority> authorities = new LinkedList<GrantedAuthority>();
		
		//Strip internal tenant path from user ID
    	String userId = userNameUtils.getPrincipleName(pentahoUserId);
    	

		PreparedStatement pstmt = null;
		ResultSet  rs = null;
		Connection conn = null;

		try {
			conn = getConnection(userId);
			String dbUserId = getDatabaseUserId(userId);
			String sql = this.getSqlRolesForUser();
				
			
			// Extract the roles
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,dbUserId);
			rs = pstmt.executeQuery();
		
			String role = null;
			while(rs.next()) {
				role = rs.getString(1);
				authorities.add(new GrantedAuthorityImpl(role));
			}
			
			if(role==null) {
				//This means user was not found so return account non-enabled with empty roles
				return null;
//				throw new AuthenticationServiceException("Could not resolve user");
			}
			
			//Add authenticated needed role for Pentaho
			authorities.add(new GrantedAuthorityImpl("Authenticated"));

			//create user details object
			details = new OEMUser(userId, userId, true, true, true, true, authorities.toArray(new GrantedAuthority[0]));
		}
		catch(Exception e) {
			LOG.error("Failed excecuting user details", e);
			throw new UsernameNotFoundException( "UserDetails NOT FOUND for: " + userId + " failed with error message: " + e);
		}
		finally {
			OEMUtil.close(conn, null, pstmt, rs);
		}
		return details;
	}

}

		