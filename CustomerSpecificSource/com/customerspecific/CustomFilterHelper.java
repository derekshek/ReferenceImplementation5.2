package com.customerspecific;

import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.Authentication;
import com.pentaho.oem.sk.filters.OEMFilterHelper;


public class CustomFilterHelper extends OEMFilterHelper{

	private static final Log LOG = LogFactory.getLog(CustomFilterHelper.class);
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
			if ("false".equals("true")){
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
