package com.customerspecific;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.security.GrantedAuthority;

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
		String name;
		LinkedList<GrantedAuthority> authorities;

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
			Node node;
            list = root.selectNodes(role);
            for (Element current : list) {
                    LOG.debug("Got role: " + current.getStringValue());
                    roleList.add(current.getStringValue());
            }
		}
		roleList.add(OEMUtil.PENTAHOAUTH);

	
		authorities = new LinkedList<GrantedAuthority>();
		this.roles = roleList.toArray(new String[roleList.size()]);

		return this.userName != null;
	}

}
