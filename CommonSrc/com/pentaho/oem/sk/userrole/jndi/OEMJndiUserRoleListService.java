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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.plugin.services.security.userrole.PentahoCachingUserDetailsService;
import org.springframework.security.AuthenticationServiceException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;

import com.pentaho.oem.sk.OEMUtil;



public class OEMJndiUserRoleListService implements IUserRoleListService {

	private static final Log LOG = LogFactory.getLog(OEMJndiUserRoleListService.class);

	private String sqlAllRoles;
	private String sqlAllUsers;
	private String sqlUsersInRole;
	private String sqlRolesForUser;
	private String jndiName;
	private UserDetailsService userDetailsService;
	private ITenantedPrincipleNameResolver userNameUtils;

	public OEMJndiUserRoleListService(ITenantedPrincipleNameResolver userNameUtils) {
		super();
		this.userNameUtils = userNameUtils;
	}
	

/////////////////////////////////////   Getters and Setters  ///////////////////////////////////////////////
	public String getJndiName()                              { return jndiName; }
	public void   setJndiName(String jndiName)               { this.jndiName = jndiName; }

	public String getSqlAllRoles()                           { return sqlAllRoles; }
	public void   setSqlAllRoles(String sqlAllRoles)         { this.sqlAllRoles = sqlAllRoles; }

	public String getSqlAllUsers()                           { return sqlAllUsers; }
	public void   setSqlAllUsers(String sqlAllUsers)         { this.sqlAllUsers = sqlAllUsers; }

	public String getSqlUsersInRole()                        { return sqlUsersInRole; }
	public void   setSqlUsersInRole(String sqlUsersInRole)   { this.sqlUsersInRole = sqlUsersInRole; }

	public String getSqlRolesForUser()                       { return sqlRolesForUser; }
	public void   setSqlRolesForUser(String sqlRolesForUser) { this.sqlRolesForUser = sqlRolesForUser; }

	public UserDetailsService getParentUserDetailsService()  { return userDetailsService; }
	public void setParentUserDetailsService(UserDetailsService userDetailsService) { this.userDetailsService = userDetailsService; }

/////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Returns all authorities known to the provider. Cannot return
	 * <code>null</code>
	 * 
	 * @return the authorities (never <code>null</code>)
	 */
	public List<String> getAllRoles() {
		LOG.debug("getAllRoles()");


		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ArrayList<String> rolesList = new ArrayList<String>();

		try {

			String sql = this.getSqlAllRoles();
			
			conn = OEMUtil.getConnection(jndiName);
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			while(rs.next())
				rolesList.add(rs.getString(1));

			return rolesList;
		} 
		catch (Exception e) {
			throw new AuthenticationServiceException("Error in getAllRoles():", e);
		}
		finally {
			OEMUtil.close(conn, null, pstmt, rs);
		}
	}


	/**
	 * Returns all System authorities known to the provider. Cannot return
	 * <code>null</code>
	 * 
	 * @return the authorities (never <code>null</code>)
	 */
	public List<String> getSystemRoles() {
		List<String> roles = new ArrayList<String>();
		roles.add("Administrator");
		return roles;
	}

	/**
	 * Returns all authorities known to the provider for a given tenant. Cannot
	 * return <code>null</code>
	 * 
	 * @param tenant
	 * @return the authorities (never <code>null</code>)
	 */
	public List<String> getAllRoles(ITenant tenant) {
		//Internal tenant is not used
		return getAllRoles();
	}


	/**
	 * Returns all user names known to the provider for a given tenant. Cannot
	 * return <code>null</code>
	 * 
	 * @param tenant
	 * @return the users (never <code>null</code>)
	 */
	public List<String> getAllUsers(ITenant tenent) {
		//Internal tenant is not used
		return getAllUsers();
	}
	
	
	
	/**
	 * Returns all user names known to the provider. Cannot
	 * return <code>null</code>
	 * 
	 * @return the users (never <code>null</code>)
	 */
	public List<String> getAllUsers() {
		LOG.debug("getAllUsers()");

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			String sql = this.getSqlAllUsers();
			
			conn = OEMUtil.getConnection(jndiName);
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
						
			ArrayList<String> userList = new ArrayList<String>();
			while(rs.next())
				userList.add(rs.getString(1));
			
			return userList;
		} 
		catch (Exception e) {
			throw new AuthenticationServiceException("Error in getAllUsers():", e);
		}
		finally {
			OEMUtil.close(conn, null, pstmt, rs);
		}
	}



	/**
	 * Returns all known users in the specified role. Cannot return
	 * <code>null</code>
	 * 
	 * @param tenant
	 *            . tenant information
	 * @param authority
	 *            The authority to look users up by. Cannot be <code>null</code>
	 * @return the users. (never <code>null</code>)
	 */
	public List<String> getUsersInRole(ITenant tenant, String role) {
		LOG.debug("Enter: getUsersInRole(" + role + ")");

			
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			String sql = this.getSqlUsersInRole();

			conn = OEMUtil.getConnection(jndiName);
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, role);
			rs = pstmt.executeQuery();

			ArrayList<String> userList = new ArrayList<String>();
			while(rs.next())
				userList.add(rs.getString(1));

			return userList;
		} 
		catch (Exception e) {
			throw new AuthenticationServiceException("Error in getUsersInRole(" + role + ")", e);
		}
		finally {
			OEMUtil.close(conn, null, pstmt, rs);
		}
	}



	/**
	 * Returns all authorities granted for a specified user.
	 * 
	 * @param tenant
	 *            information
	 * @param username
	 *            The name of the user to look up authorities for
	 * @return the authorities. (Never <code>null</code>)
	 */
	public List<String> getRolesForUser(ITenant tenant, String username) {
		//LOG.debug("getRolesForUser(" + username + ")");

		ArrayList<String> roleList = new ArrayList<String>();

		//Strip Pentaho default tenant, it is not needed
    	String userId = userNameUtils.getPrincipleName(username);

		// if security context already has this info, use it
		try {
			if (userId.equals(PentahoSessionHolder.getSession().getName())){
				GrantedAuthority[] authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
				for (GrantedAuthority ga : authorities){
					roleList.add(ga.toString());
				}
				return roleList;
			}
		} 
		catch (NullPointerException e){
//			LOG.debug("No session or security context. Ignore and move on."); //Swallow exception
		}

		String sql = getSqlRolesForUser();
		
		
		//Call UserDetails Service to get the roles
		UserDetails user = userDetailsService.loadUserByUsername(userId);

		roleList.add(OEMUtil.PENTAHOAUTH);
		for ( GrantedAuthority role : user.getAuthorities() ) {
			String principalName = role.getAuthority();
			roleList.add( principalName );
		}
		return roleList;
	}


}
