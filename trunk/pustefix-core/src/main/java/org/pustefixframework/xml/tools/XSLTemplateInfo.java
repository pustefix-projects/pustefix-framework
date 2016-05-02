package org.pustefixframework.xml.tools;

public class XSLTemplateInfo {
    
    private String match;
    private String name;
    
    public XSLTemplateInfo(String match, String name) {
        this.match = match;
        this.name = name;
    }
    
    public String getMatch() {
        return match;
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<xsl:template");
        if(name != null) {
            sb.append(" name=\"").append(name).append("\"");
        }
        if(match != null) {
            sb.append(" match=\"").append(match).append("\"");
        }
        sb.append("/>");
        return sb.toString();
    }

}
