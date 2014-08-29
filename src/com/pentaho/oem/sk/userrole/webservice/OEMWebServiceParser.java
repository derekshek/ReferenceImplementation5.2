package com.pentaho.oem.sk.userrole.webservice;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class OEMWebServiceParser {
	
	protected String userName = null;
	protected String password = null;
	protected String[] roles = new String[0];
	protected Map<String,String> sessionVariables = new HashMap<String,String>();
			
	public String   getUserName()         { return userName; }
	public String[] getRoles()            { return roles; }
	public Map<String,String>             getSessionVariables() { return sessionVariables; }
	
	public String getPassword() {
		if (password == null){
			return "";
		}
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public boolean parseUserDetailsResponse(InputStream input){
		return false;
	}

	public boolean parseGetAllUsersResponse(InputStream input){
		return false;
	}

	public boolean parseGetAllRolesResponse(InputStream input){
		return false;
	}

	public boolean parseGetUsersInRoleResponse(InputStream input){
		return parseGetAllRolesResponse(input);
	}


}
