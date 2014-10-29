package com.pentaho.oem.sk.userrole.webservice;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.pentaho.oem.sk.OEMUser;

public class OEMWebServiceParser {
	
	
	public OEMWebServiceParser() {
	}
	
	
	// Override all of these...
	public OEMUser parseUserDetailsResponse(InputStream input){
		return null;  
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
