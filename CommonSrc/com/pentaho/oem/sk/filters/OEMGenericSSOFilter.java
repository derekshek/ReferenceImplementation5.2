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
package com.pentaho.oem.sk.filters;
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


import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.AuthenticationManager;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.event.authentication.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.providers.AuthenticationProvider;
import org.springframework.security.providers.anonymous.AnonymousAuthenticationToken;
import org.springframework.security.ui.AuthenticationDetailsSource;
import org.springframework.security.ui.AuthenticationEntryPoint;
import org.springframework.security.ui.FilterChainOrder;
import org.springframework.security.ui.SpringSecurityFilter;
import org.springframework.security.ui.WebAuthenticationDetailsSource;
import org.springframework.security.ui.webapp.AuthenticationProcessingFilter;
import org.springframework.util.Assert;

import com.pentaho.oem.sk.authentication.OEMAuthenticationToken;
//import org.pentaho.platform.web.http.*;
/**
 * The instance works by creating a {@link OEMAuthenticationToken} using the
 * secret value passed in a parameter on the URL.
 * 
 * <b>Secret Code Settings</b> By the default, the parameter name to contain the
 * secret is named <i>secret</i>; however, the name of the parameter can be
 * {@link #setParameterName(String) configured}. If the parameter is not found,
 * the {@link #doFilter(ServletRequest, ServletResponse, FilterChain) filter}
 * does not attempt to authenticate.
 * 
 * <b>Authentication Leniency</b> The instance can function in an lenient or
 * non-lenient mode. If lenient and the authentication attempt fails, the
 * instance will pass the request to other filters for potential processing.
 * Otherwise, failure to authenticate using the provided secret value will be
 * treated as a login failure. By default, the class is <i>lenient</i>.
 * 
 * @see AuthenticationEntryPoint#commence(ServletRequest, ServletResponse,
 *      AuthenticationException)
 */
public class OEMGenericSSOFilter extends AuthenticationProcessingFilter implements InitializingBean, ApplicationEventPublisherAware {

	private static final Log LOG = LogFactory.getLog(OEMGenericSSOFilter.class);
	private AuthenticationManager    authenticationManager;
	private AuthenticationProvider   overrideAuthenticationProvider = null;
	private AuthenticationEntryPoint authenticationEntryPoint = new com.pentaho.oem.sk.authentication.OEMProcessingFilterEntryPoint();
	private OEMFilterHelper          filterHelper = new OEMFilterHelper();

	private ApplicationEventPublisher eventPublisher;

	private boolean                  lenient = true;
	
	
///////////////////////////////////////////////   Getters and Setters ///////////////////////////////////////////////
	public    AuthenticationProvider   getOverrideAuthenticationProvider() { return overrideAuthenticationProvider; }

	public    void                     setOverrideAuthenticationProvider(AuthenticationProvider overrideAuthenticationProvider) { this.overrideAuthenticationProvider = overrideAuthenticationProvider; }

	protected AuthenticationManager    getAuthenticationManager()          { return authenticationManager; }

	public    void                     setAuthenticationManager(AuthenticationManager authenticationManager) { this.authenticationManager = authenticationManager; }

	protected boolean                  isLenient()                         { return lenient; }

	public    void                     setLenient(boolean lenient)         { this.lenient = lenient; }
	
	protected AuthenticationEntryPoint getAuthenticationEntryPoint()       { return authenticationEntryPoint; }

	public    void                     setAuthenticationEntryPoint(AuthenticationEntryPoint authenticationEntryPoint) { this.authenticationEntryPoint = authenticationEntryPoint; }

	public    OEMFilterHelper          getFilterHelper()                   { return filterHelper; }

	public    void                     setFilterHelper(OEMFilterHelper filterHelper) { this.filterHelper = filterHelper; }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	@Override
	public void doFilterHttp(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		AuthenticationException failure = null;
		//////////////////////// as this filter consumes input, we need to buffer it for later -- this class does the trick
		HttpServletRequestWrapper wrapper = new BufferingHttpServletRequest(request, LOG);
		
		String secretValue = filterHelper.getToken(wrapper);
		if (secretValue != null) {
			LOG.debug("Found secret "+secretValue);
			String username = filterHelper.resolveUsername(secretValue);
			LOG.debug("Resolved to "+username);

			if (username != null && requiresAuthenication(username)) {
				OEMAuthenticationToken token;
				Authentication authResult;
				LOG.debug("User Requires Authentication!");

				token = new OEMAuthenticationToken(username);
				token.setDetails(authenticationDetailsSource.buildDetails(wrapper));
				LOG.debug("done setting token!");
				
				try {
					if (overrideAuthenticationProvider != null){
						LOG.debug("calling authenticate with override authenticationProvider "+overrideAuthenticationProvider);
						authResult = overrideAuthenticationProvider.authenticate(token);
					}else{
						LOG.debug("calling authenticate with default authenticationManager "+authenticationManager);
						authResult = authenticationManager.authenticate(token);
					}
					filterHelper.setSessionVariables(wrapper,authResult);
					LOG.debug("Got authResult " + authResult);
					SecurityContextHolder.getContext().setAuthentication(authResult);
					LOG.debug("Authenticated User "+authResult.getName()); //$NON-NLS-1$
					// New for 5.0 -- this ensures the startup action gets called....
					InteractiveAuthenticationSuccessEvent event = new InteractiveAuthenticationSuccessEvent(authResult, this.getClass());
					eventPublisher.publishEvent(event);
				} catch (AuthenticationException ex) {
					failure = ex;
					LOG.error("Authenticating failed for "+username +":"+ ex); //$NON-NLS-1$
				}
			}
		}
		if (isLenient() || (failure == null)) {
			chain.doFilter(wrapper, response);
		} else {
			LOG.debug("Non-lenient; Using the entry point to communicate failure"); //$NON-NLS-1$

			SecurityContextHolder.getContext().setAuthentication(null);
			authenticationEntryPoint.commence(request, response, failure);
		}
	}

	
	
	private boolean requiresAuthenication(String username) {
		boolean required = false;
		Authentication existing = SecurityContextHolder.getContext().getAuthentication();

		if ((existing == null) || !existing.isAuthenticated() || (username == null)) {
			required = true;
		}

		if (!required && (existing instanceof OEMAuthenticationToken)) {
			required =  !((OEMAuthenticationToken) existing).getCredentials().equals(username);
		}

		if (existing instanceof AnonymousAuthenticationToken) {
			required = true;
		}
		return required;
	}
	
	
	@Override
	public void afterPropertiesSet() throws Exception {
	}

	@Override
	public int getOrder() {
		return 0;
	}
}


class BufferingHttpServletRequest extends HttpServletRequestWrapper {
	  private ByteArrayOutputStream cachedBytes;

	  public BufferingHttpServletRequest(HttpServletRequest request, Log LOG) {
	    super(request);
		try {
			getInputStream();
		} catch (IOException e) {
			LOG.error("HTTP Wrapper failed: "+e);
			e.printStackTrace();
		}
	  }

	  @Override
	  public ServletInputStream getInputStream() throws IOException {
	    if (cachedBytes == null)
	      cacheInputStream();

	      return new CachedServletInputStream();
	  }

	  private void cacheInputStream() throws IOException {
	    /* Cache the inputstream in order to read it multiple times. For
	     * convenience, I use apache.commons IOUtils
	     */
	    cachedBytes = new ByteArrayOutputStream();
	    IOUtils.copy(super.getInputStream(), cachedBytes);
	  }

	  /* An inputstream which reads the cached request body */
	  public class CachedServletInputStream extends ServletInputStream {
	    private ByteArrayInputStream input;

	    public CachedServletInputStream() {
	      /* create a new input stream from the cached request body */
	      input = new ByteArrayInputStream(cachedBytes.toByteArray());
	    }

	    @Override
	    public int read() throws IOException {
	      return input.read();
	    }
	  }
	}
