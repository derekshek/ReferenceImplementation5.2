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
package com.pentaho.example;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;

public class ExampleMDXOlap4jConnection extends org.pentaho.platform.plugin.services.connections.mondrian.MDXOlap4jConnection{

	private static String MONGOHOSTSESSIONVAR    = "mongoHost";
	private static String MONGODBSESSIONVAR      = "mongoDatabase";
	private static String URL                    = "url";
	private static String DatabaseContentPattern = ".*dbname=([^;]+).*";
	private static String HostContentPattern     = ".*Host=([^;]+).*";
	private static String MongoDatasource        = "MongoDataServicesProvider";
	private static String JDBCCONNECTIONUUID     = "JdbcConnectionUuid";
	private static final Log LOG = LogFactory.getLog(ExampleMDXOlap4jConnection.class);

	public boolean connect( Properties props ) {
		String url = props.getProperty(URL);
		if (url.contains(MongoDatasource)){
			LOG.debug("Original Connection URL is " + url);
			IPentahoSession ps = PentahoSessionHolder.getSession();

			// Override host if session variable is set
			String host = url.replaceAll(HostContentPattern, "$1");
			String overrideHost = (String) ps.getAttribute(MONGOHOSTSESSIONVAR);
			if (overrideHost != null){
				url  = url.replaceAll(host,overrideHost.toString());
			}
					
			// Override database if session variable is set
			String database = url.replaceAll(DatabaseContentPattern, "$1");
			String overrideDatabase = (String) ps.getAttribute(MONGODBSESSIONVAR);
			if (overrideDatabase != null){
				url  = url.replaceAll(database,overrideDatabase.toString());
			}
			
			String jdbcConnectionUuid = new String(Base64.encodeBase64(("Host:"+overrideHost+" DB:" + overrideDatabase).getBytes()));
			url = url + ";" + JDBCCONNECTIONUUID + "=" + jdbcConnectionUuid;
			props.setProperty(URL, url);
			LOG.debug("Modified Connection URL to " + url);
		}

		return super.connect(props);
	}
	
}
