<%@ page import="java.util.Arrays,java.util.ArrayList,java.util.Collections,java.util.Set,java.util.List,java.util.Map,java.util.HashMap,org.springframework.security.context.SecurityContextHolder,org.springframework.security.GrantedAuthority,org.springframework.security.GrantedAuthorityImpl,org.springframework.security.providers.UsernamePasswordAuthenticationToken,org.springframework.security.*;" %>
<html>
 <head></head>
 <body>

  <h2>.View and Change Session Variables (String only) and Roles</h2>
  <form method="POST">
  <input type="submit"><hr>
  <table border="true">
    <tr><th>Delete</th><th>Variable</th><th>Value</th><th>Previous Value</th></tr>
    <%
     String x = null;
     String val = null;
     Object tmp = null;
     String typ = null;
     Map<String, String> vars = new HashMap<String, String>();
     Map<String, String> changedvars = new HashMap<String, String>();
     Map<String, String> deletedvars = new HashMap<String, String>();
     Map<String, String> addroles = new HashMap<String, String>();
     Map<String, String> delroles = new HashMap<String, String>();

      HttpSession httpSession = request.getSession();

      for(java.util.Enumeration e2 = request.getParameterNames();e2.hasMoreElements() ;){
         String var = (String)e2.nextElement();
         String newVal = request.getParameter(var);
         Object oldVal = httpSession.getAttribute(var);

         if (var.startsWith("__delete_")){
            httpSession.removeAttribute(newVal);
            oldVal = httpSession.getAttribute(newVal);
            if (oldVal != null){
               deletedvars.put(newVal,oldVal.toString());
            }else{
               deletedvars.put(newVal,"");
            }
         }else if (var.startsWith("__new")){
            var = request.getParameter("__newvar");
            newVal = request.getParameter("__newval");
            if (var != null && ! var.equals("")){
               httpSession.setAttribute(var,newVal);
               changedvars.put(var,"Was previously undefined");
            }
         }else if (var.startsWith("__addrole")){
            if (request.getParameter(var) != null && ! request.getParameter(var).equals("")){
               addroles.put(request.getParameter(var),"x");
            }
         }else if (var.startsWith("__deleterole_")){
            delroles.put(request.getParameter(var),"x");
         }else if (deletedvars.get(var) == null){
            httpSession.setAttribute(var,newVal);
            if (oldVal != null){
               if (! newVal.equals(oldVal.toString())){
                  changedvars.put(var,oldVal.toString());
               }
            }else{
               changedvars.put(var,"Was previously undefined");
            }
         }
      }

      for(java.util.Enumeration e2 = httpSession.getAttributeNames();e2.hasMoreElements() ;){
         x = (String)e2.nextElement();
         tmp = httpSession.getAttribute(x);
         if (tmp instanceof String){
            val = (tmp != null) ? tmp.toString() : "null";
            vars.put(x.toString(),val);
         }
      }
     ArrayList <String> varnames = new ArrayList(vars.keySet());
     Collections.sort(varnames);
       for (String var : varnames){
       String msg = changedvars.get(var);
       String color = "";
       if (msg == null){
           msg = "";
       }else{
          color = "bgcolor='wheat'";
       }
         val = vars.get(var);
     %>
                   <tr <%=color%>>
                      <td><input type="checkbox" name="__delete_<%=var%>" value="<%=var%>"></td>
                      <td><%=var%></td><td><input type="text" name="<%=var%>" value="<%=val%>"></td><td><%=msg%></td>
                   </tr>
    <%   
      } %>
    </table>
   Create New Variable: Name: <input type="text" name="__newvar"> <input type="text" name="__newval"><br>

 <h2>Roles</h2>
    <%

// create new array with the current authorities of the user plus the "ROLE_PREMIUM"UsernamePasswordAuthenticationToken
//GrantedAuthority[] grantedAuthorities = (GrantedAuthority[]) ArrayUtils.add(user.getAuthorities(), roleService.findByAuthority("ROLE_PREMIUM"));
    List<GrantedAuthority> foo = Arrays.asList(SecurityContextHolder.getContext().getAuthentication().getAuthorities());
     Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    
    ArrayList<GrantedAuthority> grantedAuthorities = new ArrayList();
    for (GrantedAuthority ga : foo){
         if (delroles.containsKey(ga.toString())){
            %>Remove role: <%=ga%> <br> <%
         }else{
            grantedAuthorities.add(ga);
         }
    }


     for (String s : addroles.keySet()){
        grantedAuthorities.add((GrantedAuthority) new GrantedAuthorityImpl(s)); 
         %>Add role: <%=s%> <br> <%
     }

     GrantedAuthority[] newAuthorities = (GrantedAuthority[]) grantedAuthorities.toArray(new GrantedAuthority[0]);
     UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(principal, principal, newAuthorities);
     SecurityContextHolder.getContext().setAuthentication(authToken);

     GrantedAuthority[] authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
%>
  <table border="true">
     <tr><th>Delete</th><th>Role Name</th></tr>
<%
      for(GrantedAuthority authority : authorities){
         String roleval = (authority != null) ? authority.toString() : "null";
     %>
       <tr>
          <td><input type="checkbox" name="__deleterole_<%=roleval%>" value="<%=roleval%>"></td>
          <td><%=roleval%></td>
       </tr>
    <% } %>
    </table>
   Create Role: Name: <input type="text" name="__addrole"><br>


<hr>
<%
Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
principal = authentication.getPrincipal();
Object details   = authentication.getDetails();
String name      = authentication.getName();

%>
Authentication      = <%=authentication%><br>
Name      = <%=name%><br>
Principal = <%=principal%><br>
Details   = <%=details%><br>
<%
%>


<hr>
   </form>
  </body>
</html>
