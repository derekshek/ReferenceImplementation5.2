package com.customerspecific;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.Authentication;

import com.pentaho.oem.sk.filters.OEMFilterHelper;


public class CustomFilterHelper extends OEMFilterHelper{

	private static final Log LOG = LogFactory.getLog(CustomFilterHelper.class);
	private String[] sessionVariableParameters = new String[0];
	private String serviceURL = null;

/////////////////////////////////////   Getters and Setters //////////////////////////////////////////////////
	public String[] getSessionVariableParameters() { return sessionVariableParameters; }
	public void setSessionVariableParameters(String[] sessionVariableParameters) { this.sessionVariableParameters = sessionVariableParameters; }
	public void setServiceURL(String serviceURL) { this.serviceURL = serviceURL; }
/////////////////////////////////////   Getters and Setters //////////////////////////////////////////////////

	@Override
	public String resolveUsername (String token) { 
	
		LOG.debug("token:" + token);
		String decoded = null;
		try {
			String actualURL = serviceURL.replaceAll("\\u007Bsecret\\u007D", token);
			URL url = new URL(actualURL);
			URLConnection conx = url.openConnection();
			conx.connect();
			InputStream input = conx.getInputStream();
	        CustomWebServiceParser parser = new CustomWebServiceParser();
	        if (parser.parseUserDetailsResponse(input)){
	        	decoded = parser.getUserName();
	        }

			LOG.debug("resolved to=" + decoded);
		}
		catch(Exception e) {
			// Decode or Decryption failed
			LOG.error("Failed to decode or decrypt token" + e);
			return null;
		}
			
		return decoded; 
	}
	

	@Override
	public void setSessionVariables(HttpServletRequestWrapper wrapper, Authentication authResult) {
		return;
	}
	
	
	
	
    public static void main(String argv[]){
        CustomFilterHelper me= new CustomFilterHelper();
		String rv = me.resolveUsername("1");
		System.out.println("Username is "+rv);
    }

	
}
