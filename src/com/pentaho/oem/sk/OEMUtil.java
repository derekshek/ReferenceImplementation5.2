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
package com.pentaho.oem.sk;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;


public class OEMUtil {
	public static final String PENTAHOADMIN = "Administrator";
	public static final String PENTAHOAUTH = "Authenticated";
	public static final String TOKEN_PREFIX = "TOKEN:";

	private static final Log LOG = LogFactory.getLog(OEMUtil.class);

	public static User getCurrentUser() {
		Object principal;

		if (SecurityContextHolder.getContext() == null
				|| SecurityContextHolder.getContext().getAuthentication() == null
				|| (principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal()) == null) {
			// LOG.debug("No principal");
			return null;
		}
//		return principal;
//		if (principal instanceof OEMUser) {
//			return (OEMUser) principal;
//		}
		if (principal instanceof User) {
			return new OEMUser((User)principal);
		}
//		// We should never get here if SSO is used
//		LOG.warn("Some other principal "
//				+ SecurityContextHolder.getContext().getAuthentication());
		return null;
	}

	/**
	 * Get Connection
	 * @param dataSourceName - the JNDI name of the connection
	 * @return
	 * @throws Exception
	 */
	public static Connection getConnection(String dataSourceName) throws Exception {
		try  {
			
			InitialContext initCtx = new InitialContext();
			DataSource ds = (DataSource) initCtx.lookup("java:/comp/env/jdbc/" + dataSourceName);
			return ds.getConnection();
		}
		catch (Exception e)
		{
			LOG.error("Failed to create a connection to:" + dataSourceName, e);
			throw e;
		}
    }

	public static void addRoleToCurrentSession(String role){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		boolean alreadyHas = false;
//		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(auth.getAuthorities());
		
		ArrayList<GrantedAuthority> roleList = new ArrayList<GrantedAuthority>();
		for (GrantedAuthority curRole : auth.getAuthorities()) {
			LOG.debug("Adding "+curRole.getAuthority() + " to newAuth");
			roleList.add(curRole);
			if (curRole.toString().equals(role)) {
				alreadyHas = true;
			}
		}
		if (!alreadyHas) {
			LOG.debug("Adding new "+ role + " to newAuth");
			roleList.add(new GrantedAuthorityImpl(role));
		}
		
		
//		authorities.add(new GrantedAuthorityImpl(role));

		Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(),auth.getCredentials(),roleList.toArray(new GrantedAuthority[roleList.size()]));
		SecurityContextHolder.getContext().setAuthentication(newAuth);
	}
	
	/**
	 * Close any type of connection
	 * @param conn
	 * @param stmt
	 * @param rs
	 */
	public static void close(Connection conn, Statement stmt, PreparedStatement pstmt, ResultSet rs) {
		try { if( rs != null ) 		rs.close(); 	} catch(SQLException e) {}
		try { if( stmt != null ) 	stmt.close(); 	} catch(SQLException e) {}
		try { if( pstmt != null ) 	pstmt.close(); 	} catch(SQLException e) {}
		try { if( conn != null ) 	conn.close(); 	} catch(SQLException e) {}
	}
	
	
	
}
