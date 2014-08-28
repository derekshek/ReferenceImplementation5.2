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
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.plugin.services.security.userrole.PentahoCachingUserDetailsService;
import org.springframework.dao.DataAccessException;
import org.springframework.security.AuthenticationServiceException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;

import com.pentaho.oem.sk.OEMUser;
import com.pentaho.oem.sk.OEMUtil;

public class OEMJndiUserDetailsService implements UserDetailsService {

	
	private static final Log LOG = LogFactory.getLog(OEMJndiUserDetailsService.class);
	private ITenantedPrincipleNameResolver userNameUtils;
	private String sqlRolesForUser;
	private String jndiName;


	public OEMJndiUserDetailsService(ITenantedPrincipleNameResolver userNameUtils) {
		super();
		this.userNameUtils = userNameUtils;
	}
	
///////////////////////////////////////////////// Getters and Setters //////////////////////////////////
	public String getJndiName()                              { return jndiName; }
	public void   setJndiName(String jndiName)               { this.jndiName = jndiName; }
	
	public String getSqlRolesForUser()                       { return sqlRolesForUser; }
	public void   setSqlRolesForUser(String sqlRolesForUser) { this.sqlRolesForUser = sqlRolesForUser; }

////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public UserDetails loadUserByUsername(String pentahoUserId) throws UsernameNotFoundException, DataAccessException, AuthenticationServiceException {

		LOG.debug("loadUserByUsername(" + pentahoUserId + ")");
		UserDetails details = null;
		LinkedList<GrantedAuthority> authorities = new LinkedList<GrantedAuthority>();
		
		//Strip internal tenant path from user ID
    	String userId = userNameUtils.getPrincipleName(pentahoUserId);

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			
			conn = OEMUtil.getConnection(getJndiName());
			String sql = this.getSqlRolesForUser();
			
			// Extract the roles
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,userId);
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

		