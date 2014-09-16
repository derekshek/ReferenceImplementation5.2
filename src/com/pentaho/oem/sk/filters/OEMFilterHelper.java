package com.pentaho.oem.sk.filters;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.Authentication;

public class OEMFilterHelper  implements InitializingBean{


	String parameterName   = null;
	String whereIsTheToken = null;
	public static final String LOOKINPARAMETER = "parameter";
	public static final String LOOKINHEADER    = "header";
	public static final String LOOKINCOOKIE    = "cookie";
	
///////////////////////////////////////////////  Getters and setters ///////////////////////////////////////
	public String getParameterName() { return parameterName; }
	public void setParameterName(String parameterName) { this.parameterName = parameterName; }

	public String getWhereIsTheToken()                       { return whereIsTheToken; }
	public void   setWhereIsTheToken(String whereIsTheToken) { this.whereIsTheToken = whereIsTheToken; }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String getToken(HttpServletRequest request){ 
		String token = null;
		if (LOOKINPARAMETER.equals(whereIsTheToken)){
			token = request.getParameter(parameterName);
		}else if (LOOKINHEADER.equals(whereIsTheToken)){
			token = request.getHeader(parameterName);
		}
		// TODO - add other places to get token
		return token; 
	}
	
	public String resolveUsername (String token){ 
		return token; 
	}
	
	public void setSessionVariables(HttpServletRequestWrapper wrapper, Authentication authResult) {
		return;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
	}
	
}
