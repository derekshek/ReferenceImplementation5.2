<%@ page import="java.io.*" %>

<html>
 <head></head>
 <body>
   <center><h1>Last Mondrian SQL Command Sent</h1></center>
<%
		String line = "foo";
     	String lastSQL = null;
     	String justSQL = null;
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("/opt/pentaho510/logs/mondrian_sql.log"));
			while ((line = br.readLine()) != null) {
				if (line.contains("executing sql")){
					lastSQL = line;
				}
			}
//			System.out.println(lastSQL);
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Some error " + e);
		}

        if (lastSQL != null){
            String pat = ".*\\[(.*)\\].*";
            justSQL = lastSQL.replaceAll(pat,"$1");
            pat = "((where)|(from)|(group by))";
            justSQL = justSQL.replaceAll(pat,"<br>$1");
        }
%>
<hr>
<%=justSQL%>
<hr>
 </body>
</html>