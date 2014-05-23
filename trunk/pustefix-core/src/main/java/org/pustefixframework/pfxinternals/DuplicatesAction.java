package org.pustefixframework.pfxinternals;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DuplicatesAction implements Action {

    @Override
    public void execute(HttpServletRequest req, HttpServletResponse res, PageContext pageContext) throws IOException {
       
        boolean html = true;
        String param = req.getParameter("format");
        if(param != null && param.equals("text")) {
            html = false;
        }
        PrintWriter writer = res.getWriter();
        try {
            String includePattern = "";
            param = req.getParameter("includes");
            if(param != null) {
                String[] patterns = param.split(",");
                for(String pattern: patterns) {
                    pattern = pattern.trim();
                    if(pattern.length() > 0) {
                        pattern = pattern.replace(".", "\\.").replace("+", "\\+").replace("*", ".*").replace("?", ".");
                        includePattern = includePattern + (pattern.length() == 0 ? "" : "|") + "(" + pattern + ")";
                    }
                }
            }
            String excludePattern = "";
            param = req.getParameter("excludes");
            if(param != null) {
                String[] patterns = param.split(",");
                for(String pattern: patterns) {
                    pattern = pattern.trim();
                    if(pattern.length() > 0) {
                        pattern = pattern.replace(".", "\\.").replace("+", "\\+").replace("*", ".*").replace("?", ".");
                        excludePattern = excludePattern + (pattern.length() == 0 ? "" : "|") + "(" + pattern + ")";
                    }
                }
            } 
            
            Map<String, String[]> result = DuplicateClassFinder.find(Pattern.compile(includePattern), Pattern.compile(excludePattern));
            if(html) {
                res.setContentType("text/html");
                printHTMLReport(writer, result);
            } else {
                res.setContentType("text/plain");
                printTextReport(writer, result);
            }
        } catch(Exception e) {
            writer.println("Error while searching for duplicate classes:");
            e.printStackTrace(writer);
        }
        writer.close();
    }
    
    private void printTextReport(PrintWriter writer, Map<String, String[]> classToLocation) {
        
        for(Map.Entry<String, String[]> entry: classToLocation.entrySet()) {
            if(entry.getValue().length > 1) {
                writer.print(entry.getKey());
                for(String value: entry.getValue()) {
                    writer.print("|");
                    writer.print(value);
                }
                writer.print("\n");
            }
        }
    }
     
    private void printHTMLReport(PrintWriter writer, Map<String, String[]> classToLocation) {
        
        writer.println("<html>\n");
        writer.println("<head>\n");
        writer.println("</head>\n");
        writer.println("<body>\n");
        
        int total = 0;
        int duplicates = 0;
        for(Map.Entry<String, String[]> entry: classToLocation.entrySet()) {
            total++;
            if(entry.getValue().length > 1) {
                duplicates++;
            }
        }
        
        writer.println("<table>");
        writer.println("<tr><th align=\"left\">Total classes:</th><td align=\"right\">" + total + "</td></tr>");
        writer.println("<tr><th align=\"left\">Duplicate classes:</th><td align=\"right\">" + duplicates + "</td></tr>");
        writer.println("</table>");
        writer.println("<br/>");
        
        for(Map.Entry<String, String[]> entry: classToLocation.entrySet()) {
            if(entry.getValue().length > 1) {
                writer.println("<div>");
                writer.print(entry.getKey());
                writer.println("<ul>");
                for(String value: entry.getValue()) {
                    writer.println("<li>");
                    writer.print(value);
                    writer.println("</li>");
                }
                writer.println("</ul>");
                writer.println("</div>");
            }
        }
       
        writer.println("</body>\n");
        writer.println("</html>\n");
        
    }

}
