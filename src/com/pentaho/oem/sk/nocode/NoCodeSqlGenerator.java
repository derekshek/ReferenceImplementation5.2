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

import java.util.Iterator;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.pentaho.metadata.query.impl.sql.*;
import org.pentaho.pms.mql.dialect.SQLQueryModel;
import org.pentaho.metadata.query.model.Selection;
import org.pentaho.metadata.model.IPhysicalTable;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.di.core.database.DatabaseMeta; // kettle-db jar file
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;

public class NoCodeSqlGenerator extends SqlGenerator {
	private static final Logger LOG = Logger.getLogger(NoCodeSqlGenerator.class);
	private String businessView = null;
    private NoCodeCommon commonRoutines = null;

	
	public NoCodeSqlGenerator() {
		super();
	}

	
	
	@Override
	  protected void generateSelect(
	          SQLQueryModel query, LogicalModel model, DatabaseMeta databaseMeta, List<Selection> selections, 
	          boolean disableDistinct, int limit, boolean group, String locale, Map<LogicalTable, String> tableAliases, 
	          Map<String, String> columnsMap, Map<String, Object> parameters, boolean genAsPreparedStatement) {

	    super.generateSelect(query, model, databaseMeta, selections, disableDistinct, limit, group, locale, tableAliases,columnsMap, parameters, genAsPreparedStatement);
	    LOG.debug("Business view is "+ model.getId());
	    businessView = model.getId();
	}
	
	
	@Override
	protected void preprocessQueryModel(
			SQLQueryModel query,
			List<Selection> selections, 
			Map<LogicalTable, String> tableAliases,
			DatabaseMeta databaseMeta) {
		
		Set<LogicalTable> selectedLogicalTables = new HashSet<LogicalTable>();
		@SuppressWarnings("unused")
		Set<IPhysicalTable> selectedPhysicalTables = new HashSet<IPhysicalTable>();
		
		// Get the user's custId from the session
		IPentahoSession session = PentahoSessionHolder.getSession();
		if (session == null){
			LOG.debug("In preprocessQueryModel -- no session!!!!!");
			return;
		}
		LOG.debug("In preprocessQueryModel for model "+ businessView);
		commonRoutines = new NoCodeCommon();
		
		PropertiesConfiguration configuration;
		if ((configuration = commonRoutines.getConfiguration()) == null){
			LOG.debug("No properties file - returning original");
			return;
		}
		
		@SuppressWarnings("unchecked")
		Iterator<String> keys = configuration.getKeys(businessView);
		if (! keys.hasNext()){
			LOG.debug("No properties for this view");
			 return;
		}
		LOG.debug("Got a key " + keys.next());
		
		// Figure out and gather up the selected logical tables. 
		if (selections != null && !selections.isEmpty()) {
			for (Selection selection : selections) {
				LogicalColumn column = selection.getLogicalColumn();
				LogicalTable table = column.getLogicalTable();
				@SuppressWarnings("unused")
				IPhysicalTable ptable = column.getPhysicalColumn().getPhysicalTable();
				selectedLogicalTables.add(table);
			}
		}

		// We now find the column to constrain, and add our where
		// clause, using a dialect specific where clause...
		for (LogicalTable table : selectedLogicalTables) {
			String ptable = table.getPhysicalTable().getProperty("target_table").toString();
			LOG.debug("Checking " + table.getId() + " = " + ptable);
			String propBase = null;
			String sql;
			if ((propBase = configuration.getString(businessView + ".physical." + ptable)) != null){
				LOG.debug("Mapping physical to Mondrian base " + propBase);
				sql = commonRoutines.sqlString(propBase);
			}else if ((propBase = configuration.getString(businessView + ".logical." + table.getId())) != null){
				LOG.debug("Mapping logical to Mondrian base " + propBase);
				sql = commonRoutines.sqlString(propBase);
			}else {
				propBase = businessView + ".physical." + ptable;
				LOG.debug("No mondrian conection - physical to " + propBase);
				sql = commonRoutines.sqlString(propBase);
				if (sql == null){
					propBase = businessView + ".logical." + table.getId();
					LOG.debug("No physical properties - trying logical to " + propBase);
					sql = commonRoutines.sqlString(propBase);
				}
			}
			LOG.debug("Get back SQL " + sql);

			if (sql != null){
				LOG.debug("adding sql to query:"+sql);
				query.addWhereFormula(sql, "AND");
			}else{
				LOG.debug("Nothing to add - query unchanged");
			}
		}
	}

	@Override
	protected String processGeneratedSql(String sql) {
		String markupSql = sql.replaceAll("\n", "<br>");
		LOG.debug("processGeneratedSql SQL is:["+ markupSql+ "]");
		return super.processGeneratedSql(sql);
	}
	
}
