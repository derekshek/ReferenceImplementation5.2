package com.pentaho.oem.sk.filters;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.Authentication;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;

import com.pentaho.oem.sk.authentication.OEMAuthenticationToken;

import java.util.Map;

public class OEMFilterHelper  implements InitializingBean{


	String parameterName   = null;
	String whereIsTheToken = null;
	public static final String LOOKINPARAMETER = "parameter";
	public static final String LOOKINHEADER    = "header";
	public static final String LOOKINCOOKIE    = "cookie";
    protected Map<String,Object> customerSpecificValueMap = null;

///////////////////////////////////////////////  Getters and setters ///////////////////////////////////////
	public String getParameterName() { return parameterName; }
	public void setParameterName(String parameterName) { this.parameterName = parameterName; }

	public String getWhereIsTheToken()                       { return whereIsTheToken; }
	public void   setWhereIsTheToken(String whereIsTheToken) { this.whereIsTheToken = whereIsTheToken; }
    public void setCustomerSpecificValueMap( Map<String, Object> customerSpecificValueMap) { this.customerSpecificValueMap = customerSpecificValueMap; }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String getToken(HttpServletRequest request){ 
		String token = null;
		if (LOOKINPARAMETER.equals(whereIsTheToken)){
			token = request.getParameter(parameterName);
		}else if (LOOKINHEADER.equals(whereIsTheToken)){
			token = request.getHeader(parameterName);
		}else if (LOOKINCOOKIE.equals(whereIsTheToken)){
			Cookie[] cookies = request.getCookies();
			if (cookies != null){
				for (Cookie cookie : request.getCookies()){
					if (parameterName.equals(cookie.getName())){
						token = cookie.getValue();
						return token;
					}
				}
			}
		}
		int qmark;
		if (token != null && (qmark = token.indexOf('?')) > -1){
			token = token.substring(0,qmark);
		}
		return token; 
	}
	
	
	public boolean requiresAuthentication(OEMAuthenticationToken existing, String secretValue) {
		 return false;
	}
	
	public OEMAuthenticationToken resolveUsername (String secret){ 
		return new OEMAuthenticationToken(secret,secret); 
	}
	
	public void setSessionVariables(HttpServletRequestWrapper wrapper, Authentication authResult) {
		return;
	}
	
	
	public String pentahoUserToServiceUser(String pentahoUser){
		return pentahoUser;
	}
	
	public String getDatabaseForUser(String pentahoUser){
		return null;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
	}
	
}
