package com.customerspecific;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.pentaho.platform.api.engine.security.IAuthenticationRoleMapper;

import com.pentaho.oem.sk.OEMUtil;
import com.pentaho.oem.sk.userrole.webservice.OEMWebServiceParser;

public class CustomWebServiceParser extends OEMWebServiceParser{

	private static final Log LOG = LogFactory.getLog(CustomWebServiceParser.class);
	private String[] variableTags = { "tenantid" };
	private String[] roleTags =  { "roles/role" };
	private String   usernameTag = "name";
	private IAuthenticationRoleMapper roleMapper;
	
	public String getUsernameTag()                     { return usernameTag; }
	public void setUsernameTag(String usernameTag)     { this.usernameTag = usernameTag; }
	public String[] getVariableTags()                  { return variableTags; }
	public void setVariableTags(String[] variableTags) { this.variableTags = variableTags; }
	public String[] getRoleTags()                      { return roleTags; }
	public void setRoleTags(String[] roleTags)         { this.roleTags = roleTags; }
	public void setRoleMapper(IAuthenticationRoleMapper roleMapper) { this.roleMapper = roleMapper; }

	
	@SuppressWarnings("unchecked")
	@Override
	public boolean parseUserDetailsResponse(InputStream input){
		ArrayList<String> roleList = new ArrayList<String>();
	
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json;
		try {
			json = mapper.readTree(input);
	        JsonNode emailNode = json.findValue("email");
	        if (emailNode != null){
	                this.userName = emailNode.asText();
	        }
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		
		for (String variable : variableTags){
			JsonNode node;
			if ((node = json.findValue(variable)) != null){
				if (node instanceof ArrayNode){
					for (JsonNode subnode : (ArrayNode)node){
						Iterator<String> i = subnode.getFieldNames();
						while (i.hasNext()){
							String name = i.next();
							sessionVariables.put(name,subnode.get(name).asText());
						}
					}
				}else{
					sessionVariables.put(variable, node.asText());
				}
			}
		}
		for (String role : roleTags){
			JsonNode node;
			if ((node = json.findValue(role)) != null){
				String roleName = node.asText();
				if (roleMapper != null){
					roleName = roleMapper.toPentahoRole(roleName);
				}
				roleList.add(node.asText());
			}
		}
		roleList.add(OEMUtil.PENTAHOAUTH);
		roleList.add("SSOUser");

	
		this.roles = roleList.toArray(new String[roleList.size()]);

		return this.userName != null;
	}


}
