package com.customerspecific;

import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationServiceException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;

import com.pentaho.oem.sk.filters.OEMFilterHelper;
import com.pentaho.oem.sk.filters.OEMGenericSSOFilter;
import com.pentaho.oem.sk.OEMUtil;
import com.pentaho.oem.sk.OEMUser;


public class CustomFilterHelper extends OEMFilterHelper{

	private static final Log LOG = LogFactory.getLog(CustomFilterHelper.class);
	private int tokenTimeout = 0;
	private String key;
	private String iv;
	private String[] sessionVariableParameters = new String[0];

/////////////////////////////////////   Getters and Setters //////////////////////////////////////////////////
	public String[] getSessionVariableParameters() { return sessionVariableParameters; }
	public void setSessionVariableParameters(String[] sessionVariableParameters) { this.sessionVariableParameters = sessionVariableParameters; }
/////////////////////////////////////   Getters and Setters //////////////////////////////////////////////////

	@Override
	public String resolveUsername (String token) { 
	
		LOG.debug("token:" + token);
		String decoded = null;
		try {
			//URL-Decode the token
			//decoded = URLDecoder.decode(token,"UTF-8");
			//LOG.debug("decoded=" + decoded);
			//Decrypt tokeno
			if (1 == 0){
				byte[] bytes = Base64.decodeBase64(token.getBytes());
				decoded = new String(bytes);
			}else{
				decoded = token;
			}
			LOG.debug("decrypted=" + decoded);
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
		
//		IPentahoSession session = PentahoSessionHolder.getSession();
		
		
//		session.setAttribute(param,wrapper.getParameter(param));
		return;
	}
	

	
}
