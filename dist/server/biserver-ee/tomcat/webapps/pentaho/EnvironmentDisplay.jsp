<%@ page import="org.springframework.security.context.SecurityContextHolder,org.springframework.security.*" %>

<html>
 <head></head>
 <body>
   <center><h1>Pentaho Troubleshooting Environment Display</h1></center>
  <h2>Roles</h2>
  <table border="true">
       <tr><th>Role Name</th></tr>
    <%
      HttpSession httpSession = request.getSession();
      GrantedAuthority[] authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
    
      for(GrantedAuthority authority : authorities){
         String val = (authority != null) ? authority.toString() : "null";
     %>
       <tr>
          <td><%=val%></td>
       </tr>
    <% } %>
    </table>

   
  <h2>Session Variables</h2>
  <table border="true">
       <tr><th>Variable</th><th>Type</th><th>Value</th></tr>
    <%
      for(java.util.Enumeration e2 = httpSession.getAttributeNames();e2.hasMoreElements() ;){
         String x = (String)e2.nextElement();
         Object tmp = httpSession.getAttribute(x);
         String val = (tmp != null) ? tmp.toString() : "null";
         String typ = (tmp != null) ? tmp.getClass().toString() : "null";
     %>
       <tr>
          <td><%=x%></td><td><%=typ%></td><td><%=val%></td>
       </tr>
    <% } %>
    </table>

<%
Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
Object principal = authentication.getPrincipal();
Object details   = authentication.getDetails();
String name      = authentication.getName();

%>
Authentication      = <%=authentication%><br>
Name      = <%=name%><br>
Principal = <%=principal%><br>
Details   = <%=details%><br>



   <h2>Java System Properties</h2>
   <table border="true">
     <tr><th>Setting</th><th>Value</th></tr>
     <%
     java.util.Properties props = System.getProperties();
     String x = null;
     String val = null;
     Object tmp = null;
     String typ = null;
     for(java.util.Enumeration e = props.propertyNames(); e.hasMoreElements() ;){
        x = (String)e.nextElement();
        val = props.getProperty(x);
     %>
     <tr>
       <td><%=x%></td><td><%=val%></td>
     </tr>
  <% } %>
  </table>
    <h2>Servlet Context Init Parameters</h2>
    <table border="true">
      <tr><th>Setting</th><th>Value</th></tr>
      <%
      for(java.util.Enumeration e3 = httpSession.getServletContext().getInitParameterNames(); e3.hasMoreElements() ;){
         x = (String)e3.nextElement();
         val = httpSession.getServletContext().getInitParameter(x);
      %>
      <tr>
       <td><%=x%></td><td><%=val%></td>
      </tr>
     <% } %>
     </table>
    <h2>Runtime Information</h2>
    <br /># Processors: 
    <%=Runtime.getRuntime().availableProcessors()%>
    <br />maxMemory:
    <%=Runtime.getRuntime().maxMemory()%>
    <br />totalMemory:
    <%=Runtime.getRuntime().totalMemory()%>
    <br />totalMemory:
    <%=Runtime.getRuntime().totalMemory()%>
  </body>
</html>
