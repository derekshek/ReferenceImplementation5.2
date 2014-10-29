package com.customerspecific;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.springframework.beans.factory.InitializingBean;

import com.pentaho.oem.sk.security.voter.OEMAccessVoter;
import com.pentaho.oem.sk.security.voter.OEMVoterHelper;

public class CustomVoterHelper extends OEMVoterHelper implements InitializingBean {


	Log LOG = LogFactory.getLog(CustomVoterHelper.class);

	/////////////////////////////////////////////////////////////////////////
}
