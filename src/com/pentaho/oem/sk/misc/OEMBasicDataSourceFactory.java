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
package com.pentaho.oem.sk.misc;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.naming.Context;
import javax.naming.Name;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.util.encoders.Base64;

/*************************************************************************************************
 * 
 * @author kevinh
 *
 * Used for preventing plaintext passwords for database connections.
 * 
 * 1. Use JNDI for hibernate, jackrabbit
 * 
 *    a. Hibernate - in system/hibernate/<dialect>.hibernate.cfg.xml
 *        i. comment out or remove connection properties
 *        ii. add JNDI reference  <property name="hibernate.connection.datasource">java:comp/env/jdbc/Hibernate</property>
 *        iii. add dialect property <property name="hibernate.dialect">org.hibernate.dialect.PostgreSQLDialect</property>
 *        
 *    b. jackrabbit - in jackrabbit/repository.xml
 *        i. comment our Filesystem/Datasource/PersistanceManager
 *        ii. replace with JNDI reference (keep properties other than url and  driver  (6 changes)
 *        
 * 2. Modify tomcat/webapps/pentaho/META-INF/context.xml
 *    a. replace password with encrypted password
 *    b. replace factory with this class
 *        
 * 3. Modify tomcat/webapps/pentaho/WEB-INF/web.xml
 *    a. add new resource for DataStore (search for "add additional resource-refs"):
 *           <resource-ref>
 *              <description>DataStore</description>
 *              <res-ref-name>jdbc/DataStore</res-ref-name>
 *              <res-type>javax.sql.DataSource</res-type>
 *              <res-auth>Container</res-auth>
 *           </resource-ref>
 *           
 * 4. Define JNDI datastore in tomcat/conf/context.xml 
 *    a. it does not work if you add this to tomcat/webapps/pentaho/META-INF/context.xml
 *       <Resource name="jdbc/DataStore" auth="Container" type="javax.sql.DataSource"
 *          factory="com.pentaho.oem.sk.misc.OEMBasicDataSourceFactory" maxActive="20" maxIdle="5"
 *          maxWait="10000" username="jcr_user" password="jiqInIAasT4kJ1RBNxencg=="
 *          driverClassName="org.postgresql.Driver" url="jdbc:postgresql://localhost:5432/jackrabbit"
 *          validationQuery="select 1" />
 * 
 * 
 * 5. To find the encrypted version of a password, just run this directly
 * 
 * 6. Comment out debugging in this file to prevent plaintext passwords from showing in the logs!
 * 
 * References: http://wiki.apache.org/jackrabbit/UsingJNDIDataSource
 */
public class OEMBasicDataSourceFactory extends org.apache.commons.dbcp.BasicDataSourceFactory {

	String key = "MultipleOf16char";
	
	Log LOG = LogFactory.getLog(OEMBasicDataSourceFactory.class);

	
	@Override
	public Object getObjectInstance(Object obj, Name name, Context nameCtx, @SuppressWarnings("rawtypes") Hashtable environment) throws Exception {
		Object o = super.getObjectInstance(obj, name, nameCtx, environment);
		if (o != null) {
			BasicDataSource ds = (BasicDataSource) o;
			if (ds.getPassword() != null && ds.getPassword().length() > 0) {
				String pwd = unscramblePassword(ds.getPassword());
				LOG.debug("Unscrambling passwrd "+ds.getPassword() + " for user "+ ds.getUsername() + " in ds "+ ds.getUrl() + " = "+ pwd);
				ds.setPassword(pwd);
			}
			return ds;
		} else {
			return null;
		}
	}
	
	private String unscramblePassword(String enc){
		String plain = null;
		try {
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
			SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			plain = new String(cipher.doFinal(Base64.decode(enc)));
			LOG.debug("------------ " + enc + "----------- decrypted is "+ plain);
		} catch (Exception e) {
			LOG.error("Bad decryption call:" +e);
			e.printStackTrace();
		}
		return plain;
	}
	
	private String scramblePassword(String plain){
		String enc = null;
		try {
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");  
			SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			enc = new String(Base64.encode(cipher.doFinal(plain.getBytes())));
			LOG.debug("-------------" + plain + "--------- encrypted is "+enc);
		} catch (Exception e) {
			LOG.error("Bad encryption call:" +e);
			e.printStackTrace();
		}
		return enc;
	}
	
	public static void main(String args[]){
		OEMBasicDataSourceFactory me = new OEMBasicDataSourceFactory();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter plaintext password");
		String plain = "Something went wrong!";
		try {
			plain = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(me.scramblePassword(plain));
	}
}
