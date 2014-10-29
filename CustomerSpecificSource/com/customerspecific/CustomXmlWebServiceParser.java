package com.customerspecific;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.pentaho.platform.api.engine.security.IAuthenticationRoleMapper;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;

import com.pentaho.oem.sk.OEMUser;
import com.pentaho.oem.sk.OEMUtil;
import com.pentaho.oem.sk.userrole.webservice.OEMWebServiceParser;

public class CustomXmlWebServiceParser extends OEMWebServiceParser{

	private static final Log LOG = LogFactory.getLog(CustomXmlWebServiceParser.class);
	private String[] variableTags = { "tenantid" };
	private String[] roleTags =  { "roles/role" };
	private String   usernameTag = "name";
	private IAuthenticationRoleMapper roleMapper = null;

	public String getUsernameTag()                     { return usernameTag; }
	public void setUsernameTag(String usernameTag)     { this.usernameTag = usernameTag; }
	public String[] getVariableTags()                  { return variableTags; }
	public void setVariableTags(String[] variableTags) { this.variableTags = variableTags; }
	public String[] getRoleTags()                      { return roleTags; }
	public void setRoleTags(String[] roleTags)         { this.roleTags = roleTags; }
	public void setRoleMapper(IAuthenticationRoleMapper roleMapper) { this.roleMapper = roleMapper; }

	
	@SuppressWarnings("unchecked")
	@Override
	public OEMUser parseUserDetailsResponse(InputStream input){
		SAXReader reader;
		Node root;

		reader = new SAXReader();
		try {
			root = reader.read(input).getRootElement();
		} catch (DocumentException e) {
			LOG.error("Error parsing XML: "+e);
			return null;
		}

		List<Element> list;

		if (root == null || root.selectSingleNode(usernameTag) == null){
			return null;
		}
		String userName = root.selectSingleNode(usernameTag).getStringValue();

		LinkedList<GrantedAuthority> roleList = new LinkedList<GrantedAuthority>();

		for (String role : roleTags){
            list = root.selectNodes(role);
            for (Element current : list) {
            	String newRole = current.getStringValue();
            	LOG.debug("Got role: " + current.getStringValue());
    			roleList.add(new GrantedAuthorityImpl(roleMapper == null ? newRole : roleMapper.toPentahoRole(newRole)));
            }
		}
		roleList.add(new GrantedAuthorityImpl(OEMUtil.PENTAHOAUTH));
		roleList.add(new GrantedAuthorityImpl("SSOUser"));

		OEMUser parsedUser = new OEMUser(userName,"",roleList.toArray(new GrantedAuthority[0]));

		for (String variable : variableTags){
			Node node;
			if ((node = root.selectSingleNode(variable)) != null){
				parsedUser.addSessionVariable(variable, node.getStringValue());
			}
		}

		return parsedUser;
	}

}
