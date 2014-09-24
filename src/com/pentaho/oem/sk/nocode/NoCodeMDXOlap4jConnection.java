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
package com.pentaho.oem.sk.nocode;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/********* Instructions ********************
 * 
 * @author kevinh
 *
 * This will cause the NoCodeDsp to be automatically applied to catalogs specified in NoCodeDsp.properties
 * 
 * This may be desirable because re-publishing of a Mondrian schema will overwrite the parameters, and remove
 * a DSP that was previously specified
 * 
 * To use this class:
 *   
 *   1. In NoCodeDsp.properties, add an entry of the format:  SteelWheels.autoAdd=true
 *      where the first string is the Mondrian catalog name
 *      
 *   2. In pentaho-solutions/system/pentahoObjects.spring.xml, replace the bean definition
 *         <bean id="connection-MDXOlap4j" class="org.pentaho.platform.plugin.services.connections.mondrian.MDXOlap4jConnection" scope="prototype" />
 *      with
 *         <bean id="connection-MDXOlap4j" class="com.pentaho.oem.sk.nocode.NoCodeMDXOlap4jConnection" scope="prototype">
 *            <property name="dsp" value="com.pentaho.oem.sk.nocode.NoCodeDsp"/>
 *         </bean>
 *
 *   3. restart the BA server

 */
public class NoCodeMDXOlap4jConnection extends org.pentaho.platform.plugin.services.connections.mondrian.MDXOlap4jConnection{
	private static final Log LOG = LogFactory.getLog(NoCodeMDXOlap4jConnection.class);



	
	private static String URL                    = "url";
	private static String DynamicSchemaProcessor = "DynamicSchemaProcessor";
	private static String UseContentChecksum     = "UseContentChecksum=true";
	private static String ContentPattern         = ".*Catalog=mondrian:/([^;]+).*";
	private static String MongoContentPattern    = ".*Catalog=([^;]+).*";
	@SuppressWarnings("unused")
	private static String DatabaseContentPattern = ".*dbname=([^;]+).*";
	private static String HostContentPattern     = ".*Host=([^;]+).*";
	private static String MongoDatasource        = "MongoDataServicesProvider";
	private static String AUTOADD                = ".autoAdd";
	private static String TRUE                   = "true";
	private static String JdbcConnectionUuid     = "JdbcConnectionUuid";
	private static String CONNECTIONUUID         = ".jdbcConnectionUuid";

	public String getDsp()           { return dsp; }
	public void   setDsp(String dsp) { this.dsp = dsp; }

	private String dsp = null;
	
	public boolean connect( Properties props ) {
		
	
//		for (Object p : props.keySet()){
//			String key = p.toString();
//			LOG.debug("Property " + p +  " = " + props.getProperty(key));
//		}
		String url = props.getProperty(URL);
		if (dsp != null && !url.contains(DynamicSchemaProcessor)){
			NoCodeCommon commonRoutines = new NoCodeCommon();
			PropertiesConfiguration config = commonRoutines.getConfiguration();
			if (config != null){
				String catalog;
				String useDsp = dsp;
				String useChecksum = UseContentChecksum;
				if (url.contains(MongoDatasource)){
					catalog = url.replaceAll(MongoContentPattern, "$1");
					useDsp = null; ////////////////////////////////////////////// add back when OSGI is figured out
					useChecksum = null;
				}else{
					catalog = url.replaceAll(ContentPattern, "$1");
				}


				boolean autoAdd = TRUE.equalsIgnoreCase(config.getProperty(catalog + AUTOADD) + "");
				if (autoAdd){
					//  Dynamically change host
					String host = url.replaceAll(HostContentPattern, "$1");
					Object dynamicHost = config.getProperty(catalog + ".host");
					if (dynamicHost != null){
						dynamicHost = commonRoutines.substituteVars(dynamicHost.toString());
						if (dynamicHost != null){
						   url  = url.replaceAll("="+host,"="+dynamicHost.toString());
						}
					}
					//  Dynamically change database
					String database = url.replaceAll(DatabaseContentPattern, "$1");
					Object dynamicDatabase = config.getProperty(catalog + ".database");
					if (dynamicDatabase != null){
						dynamicDatabase = commonRoutines.substituteVars(dynamicDatabase.toString());
						if (dynamicDatabase != null){
						   url  = url.replaceAll("="+database,"="+dynamicDatabase.toString());
						}
					}
					
					Object jdbcConnectionUuid = config.getProperty(catalog + CONNECTIONUUID);
					if (jdbcConnectionUuid != null){
						jdbcConnectionUuid = commonRoutines.substituteVars(jdbcConnectionUuid.toString());
						if (jdbcConnectionUuid != null){
							jdbcConnectionUuid = new String(Base64.encodeBase64(jdbcConnectionUuid.toString().getBytes()));
						}
					}
					
					if (jdbcConnectionUuid != null){
						url = url + ";" + JdbcConnectionUuid + "=" + jdbcConnectionUuid;
					}
					if (useDsp != null){
						url = url + ";" + DynamicSchemaProcessor + "=" + useDsp;
					}
					if (useChecksum != null){
						url = url + ";" + UseContentChecksum;
					}
					props.setProperty(URL, url);
					LOG.debug("Modified Connection URL to " + url);
				}
			}
		}

		return super.connect(props);
	}
	
}
