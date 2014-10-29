package com.customerspecific;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import org.pentaho.platform.engine.security.DefaultJdbcRoleMapper;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;

import com.pentaho.oem.sk.OEMUser;
import com.pentaho.oem.sk.OEMUtil;
import com.pentaho.oem.sk.userrole.webservice.OEMWebServiceParser;

public class CustomJsonWebServiceParser extends OEMWebServiceParser{

	private static final Log LOG = LogFactory.getLog(CustomJsonWebServiceParser.class);
	private String[] variableTags = { "tenantid" };
	private String[] roleTags =  { "roles/role" };
	private String   usernameTag = "email";
	private IAuthenticationRoleMapper roleMapper = new DefaultJdbcRoleMapper();

	public String getUsernameTag()                     { return usernameTag; }
	public void setUsernameTag(String usernameTag)     { this.usernameTag = usernameTag; }
	public String[] getVariableTags()                  { return variableTags; }
	public void setVariableTags(String[] variableTags) { this.variableTags = variableTags; }
	public String[] getRoleTags()                      { return roleTags; }
	public void setRoleTags(String[] roleTags)         { this.roleTags = roleTags; }
	public void setRoleMapper(IAuthenticationRoleMapper roleMapper) { this.roleMapper = roleMapper; }

	public CustomJsonWebServiceParser(Map<String, Object> customerSpecificValueMap) {
		if (customerSpecificValueMap != null){
			if (customerSpecificValueMap.containsKey("usernameTag")){
				this.usernameTag = customerSpecificValueMap.get("usernameTag") + "";
			}
		}
	}

	public CustomJsonWebServiceParser() {

	}


	@Override
	public OEMUser parseUserDetailsResponse(InputStream input){

		ObjectMapper mapper = new ObjectMapper();
		JsonNode json;
		String userName = null;
		try {
			json = mapper.readTree(input);
			JsonNode emailNode = json.findValue(usernameTag);
			if (emailNode != null){
				userName = emailNode.asText();
			}
		} catch (JsonProcessingException e) {
			LOG.error("JSON parse failed - "+e);
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		LinkedList<GrantedAuthority> roleList = new LinkedList<GrantedAuthority>();
		for (String role : roleTags){
			JsonNode node;
			if ((node = json.findValue(role)) != null){
				if (node instanceof ArrayNode){
					for (JsonNode subnode : node){
						roleList.add(new GrantedAuthorityImpl(roleMapper.toPentahoRole(subnode.asText())));
					}
				}else{
					roleList.add(new GrantedAuthorityImpl(roleMapper.toPentahoRole(node.asText())));
				}
			}
		}
		roleList.add(new GrantedAuthorityImpl(OEMUtil.PENTAHOAUTH));
		roleList.add(new GrantedAuthorityImpl("SSOUser"));

		OEMUser parsedUser = new OEMUser(userName,"",roleList.toArray(new GrantedAuthority[0]));

		for (String variable : variableTags){
			JsonNode node;
			if ((node = json.findValue(variable)) != null){
				if (node instanceof ArrayNode){
					for (JsonNode subnode : (ArrayNode)node){
						Iterator<String> i = subnode.getFieldNames();
						while (i.hasNext()){
							String name = i.next();
							parsedUser.addSessionVariable(variable, subnode.get(name).asText());
						}
					}
				}else{
					parsedUser.addSessionVariable(variable, node.asText());
				}
			}
		}

		return parsedUser;

	}


}
