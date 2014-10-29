package com.customerspecific;

import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.Authentication;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;

import com.pentaho.oem.sk.authentication.OEMAuthenticationToken;
import com.pentaho.oem.sk.filters.OEMFilterHelper;
import com.pentaho.oem.sk.userrole.webservice.OEMWebServiceParser;


public class CustomFilterHelper extends OEMFilterHelper{

	private static final Log LOG = LogFactory.getLog(CustomFilterHelper.class);
	private OEMWebServiceParser            webServiceParser = null;
	private UserDetailsService             userDetailsService = null;

/////////////////////////////////////   Getters and Setters //////////////////////////////////////////////////
	public OEMWebServiceParser getWebServiceParser() { return webServiceParser; }
	public void setWebServiceParser(OEMWebServiceParser webServiceParser) { this.webServiceParser = webServiceParser; }
	public void setUserDetailsService(UserDetailsService userDetailsService){ this.userDetailsService = userDetailsService; }
/////////////////////////////////////   Getters and Setters //////////////////////////////////////////////////

	@Override
	public OEMAuthenticationToken resolveUsername (String secret) { 
		OEMAuthenticationToken token = null;
		LOG.debug("secret:" + secret);
		String username = null;
		try {
			if (userDetailsService != null){
				UserDetails uds = userDetailsService.loadUserByUsername(secret);
				// if no userdetails can be extracted, user is not authenticated
				if (uds == null){
					return null;
				}
				// we got userdetails from the secret, so authenticate the user with the authorities returned
				token = new OEMAuthenticationToken(uds.getUsername(),uds.getPassword(),uds.getAuthorities());
				token.setDetails(uds);
			}else{
				// In this case we translate the secret to a username, and return a token with no authorities
				username = secret;
				token = new OEMAuthenticationToken(username, secret);
			}
		}
		catch(Exception e) {
			LOG.error("Failed to decode or decrypt token" + e);
			return null;
		}
		return token; 
	}
	

	@Override
	public void setSessionVariables(HttpServletRequestWrapper wrapper, Authentication authResult) {
		
//		IPentahoSession session = PentahoSessionHolder.getSession();
		
		
//		session.setAttribute(param,wrapper.getParameter(param));
		return;
	}
	

	
}
