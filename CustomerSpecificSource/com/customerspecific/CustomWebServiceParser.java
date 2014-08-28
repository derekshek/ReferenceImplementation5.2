package com.customerspecific;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import com.pentaho.oem.sk.OEMUtil;
import com.pentaho.oem.sk.userrole.webservice.OEMWebServiceParser;

public class CustomWebServiceParser extends OEMWebServiceParser{

	private static final Log LOG = LogFactory.getLog(CustomWebServiceParser.class);
	private String[] variableTags = { "tenantid" };
	private String[] roleTags =  { "roles/role" };
	private String   usernameTag = "name";
	
	public String getUsernameTag()                     { return usernameTag; }
	public void setUsernameTag(String usernameTag)     { this.usernameTag = usernameTag; }
	public String[] getVariableTags()                  { return variableTags; }
	public void setVariableTags(String[] variableTags) { this.variableTags = variableTags; }
	public String[] getRoleTags()                      { return roleTags; }
	public void setRoleTags(String[] roleTags)         { this.roleTags = roleTags; }
	
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean parseUserDetailsResponse(InputStream input){
		ArrayList<String> roleList = new ArrayList<String>();
		SAXReader reader;
		Node root;

		reader = new SAXReader();
		try {
			root = reader.read(input).getRootElement();
		} catch (DocumentException e) {
			LOG.error("Error parsing XML: "+e);
			return false;
		}

		List<Element> list;

		if (root == null || root.selectSingleNode(usernameTag) == null){
			return false;
		}
		this.userName = root.selectSingleNode(usernameTag).getStringValue();

		for (String variable : variableTags){
			Node node;
			if ((node = root.selectSingleNode(variable)) != null){
				sessionVariables.put(variable, node.getStringValue());
			}
		}
		for (String role : roleTags){
            list = root.selectNodes(role);
            for (Element current : list) {
                    LOG.debug("Got role: " + current.getStringValue());
                    roleList.add(current.getStringValue());
            }
		}
		roleList.add(OEMUtil.PENTAHOAUTH);

	
		this.roles = roleList.toArray(new String[roleList.size()]);

		return this.userName != null;
	}

}
