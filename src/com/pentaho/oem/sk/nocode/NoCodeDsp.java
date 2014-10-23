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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mondrian.i18n.LocalizingDynamicSchemaProcessor;
import mondrian.olap.Util;
import mondrian.spi.impl.FilterDynamicSchemaProcessor;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.context.SecurityContextImpl;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultElement;



public class NoCodeDsp extends mondrian.i18n.LocalizingDynamicSchemaProcessor implements mondrian.spi.DynamicSchemaProcessor {

	private static final String SUBSTITUTESTRING = ".replaceString";
	private static String ALLSCHEMAS             = "ALL_SCHEMAS";
	private static final String COMMENT = ".comment";
	private static Log LOG = LogFactory.getLog(NoCodeDsp.class);
    private NoCodeCommon commonRoutines = null;
	@Override 
	public String filter(String schemaUrl, Util.PropertyList connectInfo, InputStream stream){

		String before = null;
		// call super filter to get the string
		try {
			before = super.filter(schemaUrl, connectInfo, stream);
			
		} catch (Exception e2) {
			LOG.error("Failed calling super.filter: "+e2);
			return null;
		}
		return filter(before);
	}
	
	@SuppressWarnings("unchecked")
	public String filter(String before){
		commonRoutines = new NoCodeCommon();
		PropertiesConfiguration config;
		
		if ((config = commonRoutines.getConfiguration()) == null){
			LOG.debug("No properties file - returning original");
			return before;
		}
		
		// OK - do our stuff
		try {
			SAXReader reader;
			Element schema;

			reader = new SAXReader();
			schema = reader.read(new ByteArrayInputStream(before.getBytes("UTF-8"))).getRootElement();


			String schemaName = schema.attributeValue("name");
			
			///////////////////////  yuck ////////////////////
			String key = schemaName + SUBSTITUTESTRING;
			if (config.containsKey(key)){
				String marker = config.getProperty(key).toString();
				return uglyBackwardsCompatibleStringSubstitute(before, marker);
			}
			//////////////////////////////////////////////////
		
			if (commonRoutines.canSeeAll(schemaName)){
				LOG.debug("No change to schema due to see all role");
				return before;
			}
			List <Element> cubes = schema.selectNodes("//Cube");
			cubes.addAll(schema.selectNodes("//VirtualCube"));
			for (Element cube : cubes){
				String cubeName = cube.attributeValue("name");
				String propBase = schemaName + "." + cubeName;
				if (commonRoutines.canSeeAll(propBase)){
					LOG.debug(propBase + " not modified due to seeAllRole");
				}else if (isRemovedCube(cubeName, schemaName)){
					LOG.debug(propBase + " removed from schema due to role");
					schema.remove(cube);
				}else{
					LOG.debug(propBase + " checking for modification");

					///     remove dimensions and dimension usages
					List <Element> dimensions = cube.selectNodes(".//Dimension");
					dimensions.addAll(cube.selectNodes(".//DimensionUsage"));
					dimensions.addAll(cube.selectNodes(".//Measure"));
					dimensions.addAll(cube.selectNodes(".//CalculatedMember"));
					for (Element dimension : dimensions){
						String dimensionName = dimension.attributeValue("name");
						if (isRemovedDimension(dimensionName, cubeName,schemaName)){
							LOG.debug(propBase + "." + dimensionName + " removed");
							cube.remove(dimension);
						}
					}

					///     Add Sql to tables defined in the cube
					List <Element> tables = cube.selectNodes(".//Table");
					for (Element table : tables){
						addSqlToTable(table,cubeName,schemaName);
					}

					///     Add Sql to tables defined in the schema (conformed dimensions with dimension usages)
					List <Element> dimensionUsages = cube.selectNodes(".//DimensionUsage");
					for (Element dimensionUsage : dimensionUsages){
						String sharedDimensionSource = dimensionUsage.attributeValue("source");
						Element sharedDimension = (Element) schema.selectSingleNode("Dimension[@name='" + sharedDimensionSource + "']");
						if (sharedDimension != null){
							Element table = (Element) sharedDimension.selectSingleNode(".//Table");
							if (table != null){
								addSqlToTable(table,sharedDimension.attributeValue("name"),schemaName);
							}
						}
					}
				}
			}
			// Convert Tree back into CML for returning
			ByteArrayOutputStream bres = new ByteArrayOutputStream();
			XMLWriter writer = new XMLWriter(bres);
			writer.write(schema);
//			LOG.debug("New schema:\n"+bres.toString());
			return addComment(bres.toString(), schemaName, config, commonRoutines);
		} catch (Exception e1) {
			LOG.debug("Exception caught:"+e1);
			e1.printStackTrace();
		}
		return before;	
	}


	///////////////////////////////   Add a comment for uniqueness when IDBDatasourceService is used
	///////////////////////////////      look of JdbcConnectionUuid fix to make this obsolete eventually
	public String addComment(String before,String catalog, PropertiesConfiguration config, NoCodeCommon commonRoutines){
		Object comment = config.getProperty(catalog + COMMENT);
		if (comment == null){
			comment = config.getProperty(ALLSCHEMAS + COMMENT);
		}
		if (comment != null){
			String s = commonRoutines.substituteVars(comment.toString(), false);
			return before.replace("</Schema>", "\n<!--\n" + s + "\n-->\n" + "</Schema>");
		}
	    return before;	
	}
	
	public String uglyBackwardsCompatibleStringSubstitute(String before, String marker){
		//  Backwards compatibility with the old string substitute stuff
		LOG.debug("Doing string replace on " + marker);
		Matcher matcher = Pattern.compile(marker + "#([^#]*)#").matcher(before);
		StringBuffer newSchema = new StringBuffer();
		while (matcher.find()) {
			String clause = matcher.group(1);
			String newclause = commonRoutines.substituteVars(clause);
			if (newclause != null){
				matcher.appendReplacement(newSchema, newclause);
			}else{
				matcher.appendReplacement(newSchema, "");
			}
		}
		return matcher.appendTail(newSchema).toString();
	}




	public boolean isRemovedDimension(String dimName, String cubeName, String schemaName){
		String propBase = schemaName + "." + cubeName + "." + dimName;
		return commonRoutines.removeByRole(propBase);
	}

	public boolean isRemovedCube(String cubeName, String schemaName){
		String propBase = schemaName + "." + cubeName;
		return commonRoutines.removeByRole(propBase) || commonRoutines.removeByRole(schemaName);
	}
	
	
	public void addSqlToTable(Element table, String cubeName, String schemaName){
		String tableName = table.attributeValue("name");
		String propBase = schemaName + "." + cubeName + "." + tableName;
		DefaultElement sqlNode = sqlNode(propBase);
		if (sqlNode != null){
			LOG.debug(propBase + " added SQL: "+ sqlNode.getText());
			table.add(sqlNode);
		}
	}

	public DefaultElement sqlNode(String propBase){
		String newSql = commonRoutines.sqlString(propBase);
		// Return the new node
		if (newSql == null){
			return null;
		}
		DefaultElement sqlNode = new DefaultElement("SQL");
		sqlNode.addAttribute("dialect", "generic");
		sqlNode.addCDATA(newSql);
//		LOG.debug("Added SQL tag: "+newSql);
		return sqlNode;
	}

	public static void main (String[] args){
		String path = "/home/kevinh/pentaho/Projects/Evalue/EvalueDataMartSchema.xml";
		IPentahoSession session = new org.pentaho.platform.engine.core.system.StandaloneSession();
		session.setAttribute("SubUnit_ID", "12345");
//		session.setAttribute("PayerList", "p1,p2,p3,p4");
//		session.setAttribute("OrganizationList", "o1,o2,o3,o4");
//		session.setAttribute("LocationList", "l1,l2,l3,l4");
		PentahoSessionHolder.setSession(session);
		
		
		SecurityContext context = new SecurityContextImpl();
		LinkedList<GrantedAuthority> roleList = new LinkedList<GrantedAuthority>();
		roleList.add(new GrantedAuthorityImpl("BSC"));
		roleList.add(new GrantedAuthorityImpl("Pentaho_PayerData"));
		Authentication authentication = new UsernamePasswordAuthenticationToken("test", "test", roleList.toArray(new GrantedAuthority[0]));
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);

		try {
//			encoded = Files.readAllBytes(Paths.get(path));
//			String before = encoding.decode(ByteBuffer.wrap(encoded)).toString();
			String before = FileUtils.readFileToString(new File(path));
//			System.out.println("---------------------------------------------before:\n"+ before);
			NoCodeDsp my = new NoCodeDsp();
			String after = my.filter(before);
//			System.out.println("---------------------------------------------after:\n"+ after);
			PrintWriter writer = new PrintWriter(path+".modified", "UTF-8");
			writer.println(after);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}