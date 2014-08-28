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

package com.pentaho.oem.sk.xactions;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.core.row.*;
import org.springframework.security.context.SecurityContextHolder;

import com.pentaho.oem.sk.OEMUser;
import com.pentaho.oem.sk.OEMUtil;
import com.pentaho.oem.sk.authentication.OEMAuthenticationToken;


@SuppressWarnings("deprecation")
public class OEMKTRSetSessionVarsComponent extends ComponentBase {

	private static final long serialVersionUID = 9050456842938084174L;

	// Required to extend Component Base
	public    Log     getLogger()              {return LogFactory.getLog(OEMKTRSetSessionVarsComponent.class);	}
	public    boolean init()                   {return true; }
	public    void    done()                   { }
	protected boolean validateSystemSettings() {return true; }
	protected boolean validateAction()         {return true; }
	// other stuff
	private   IPentahoSession session  = null;
	private   PrintStream printStream = null;
	private   Log         logger      = getLogger(); 
	private   String      username    = null;
	Map <String,String> envArgs = new HashMap<String,String>();
	
	
	private class NullOutputStream extends OutputStream {
		@Override
		public void write(int b) throws IOException {
		}
	}

	
	protected boolean executeAction() {
		String ktrFile          = null;
		
		initOutputStream();
		if (initPentahoSession() == false){
			printStream.println("No session!");
			return true;
		}

		if (initUserName() == false){
			printStream.println("No username!");
			return true;
		}
		

		
		// Make sure we have a ktr
		if( isDefinedResource("transformationfile") ) { 
			ktrFile = getResource("transformationfile").getAddress(); 
		}else{
			printStream.println("Transformation file is not defined (resource transformation-file)");
			return true;
		}
		
		
		// dont run ktr if required info is not there
		if (username == null || ktrFile == null){
			return true;
		}
		// run it
		try {
			runTransform(ktrFile , envArgs, printStream);
		} catch (Exception e) {
			getLogger().error("Transform failed: "+ e); 
			printStream.println("Transform "+ktrFile+ " failed for user " + username + ":" + e.getMessage()+"\n<br>"+e);
			//return false;
		}
		return true;
	}


	
	private void runTransform(String ktrFile,Map<String, String> envArgs,  final PrintStream printStream) throws Exception{

        final IPentahoSession ps = PentahoSessionHolder.getSession();
		String  args[] = { username };

		KettleEnvironment.init();
		TransMeta metaData = new TransMeta(ktrFile);
		Trans trans = new Trans( metaData );
		trans.setLogLevel(LogLevel.DEBUG);
		trans.setVariable("principalName", username);
		for (String var : envArgs.keySet()){
			trans.setVariable(var, envArgs.get(var));
		}
		trans.prepareExecution(args);

		StepInterface step = trans.findRunThread("Output");

		if (step == null){
			throw new KettleException("Cant find step with name 'Output' step  in " + ktrFile);
		}
		
		step.addRowListener(new RowAdapter() {
			public void rowWrittenEvent(RowMetaInterface rowMeta, Object[] row) throws KettleStepException {
				// Here you get the rows as they are written by the step
				if (row.length >= 3 && row[0] != null && row[1] != null){
					String roleOrVariable =  row[0].toString();
					String name =  row[1].toString();
					if (roleOrVariable.equalsIgnoreCase("variable") && row[2] != null){
						String value = row[2].toString();
						ps.setAttribute(name, value);
						printStream.println(name + " set to " + value + "<br>");
					}else if (roleOrVariable.equalsIgnoreCase("role")){
						OEMUtil.addRoleToCurrentSession(name);
						printStream.println("Added role " + name + "<br>");
					}
				}
			}
		}
				);
		trans.startThreads();
		trans.waitUntilFinished();
		if ( trans.getErrors() > 0 ) {
			throw new KettleException( "Error Executing transformation for user " + username);
		}
	}

	
	
	/***********************   initialization stuff **************************************/
	private void initOutputStream(){
		// If run at session startup, there is no feedback output stream....
		if (this.getRuntimeContext().feedbackAllowed()){
			printStream =  new PrintStream(this.getFeedbackOutputStream()); 
		}else{
			printStream =  new PrintStream(new NullOutputStream());
		}
	}
	
	private boolean initUserName(){
		// pass a user, or use the current session - xaction is configured to pass
		if (isDefinedInput("user")) {
			username = getInputStringValue("user");
		}else{
			String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
			username = currentUser;
			printStream.println("no username passed -- using " + username);
		}
		if (username == null || username.equals("")){
			return false;
		}
		return true;
	}
	
	private boolean initPentahoSession(){
		// check and see if we have a connection
		session = PentahoSessionHolder.getSession();
		if (session == null){
			logger.error("Pentaho Session is null");
			printStream.println("Pentaho Session is null");
			return false;
		}
		return true;
	}
	
	
	
	
}