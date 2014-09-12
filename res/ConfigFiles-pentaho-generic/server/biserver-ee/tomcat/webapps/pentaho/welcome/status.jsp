<%@ page import="java.util.Arrays,java.util.ArrayList,java.util.Collections,java.util.Set,java.util.List,java.util.Map,java.util.HashMap,org.springframework.security.context.SecurityContextHolder,org.springframework.security.GrantedAuthority,org.springframework.security.GrantedAuthorityImpl,org.springframework.security.providers.UsernamePasswordAuthenticationToken,org.springframework.security.*;" %>
<html>
 <head></head>
 <body bgcolor="@@@OEM_PAGE_BACKGROUND@@@">

<%
Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
Object principal = authentication.getPrincipal();
Object details   = authentication.getDetails();
String name      = authentication.getName();

%>
<h1>Logged into Pentaho as <%=name%></h1>
  <table border="true" bgcolor="@@@OEM_HEADER_COLOR_1@@@">
  <tr bgcolor="@@@OEM_HEADER_COLOR@@@" ><th colspan=2>Your Session Variables are</th></tr>
    <%
    HttpSession httpSession = request.getSession();

      for(java.util.Enumeration e2 = httpSession.getAttributeNames();e2.hasMoreElements() ;){
         String x = (String)e2.nextElement();
         Object tmp = httpSession.getAttribute(x);
         if (tmp instanceof String && ! ((String)x).startsWith("SPRING_")){
            String val = (tmp != null) ? tmp.toString() : "null";
        %>
          <tr>
             <td><%=x%></td><td><%=val%></td>
          </tr>
       <% 
         } 
       }

     GrantedAuthority[] authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
%>
     <tr bgcolor="@@@OEM_HEADER_COLOR@@@" ><th colspan=2>Your Roles Are:</th></tr>
<%
      for(GrantedAuthority authority : authorities){
         String roleval = (authority != null) ? authority.toString() : "null";
     %>
       <tr>
          <td colspan=2><%=roleval%></td>
       </tr>
    <% } %>
    </table>

   </form>
  </body>
</html>
