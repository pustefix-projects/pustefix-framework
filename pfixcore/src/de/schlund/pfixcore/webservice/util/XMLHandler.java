package de.schlund.pfixcore.webservice.util;

import java.io.*;
import org.apache.log4j.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * This class acts as general SAX handler (ContentHandler, ErrorHandler, EntityResolver), which does low level stuff
 * for derived, application specific, classes. It supports basic error logging, attribute value trimming and entity resolving
 * using the classloader.
 * 
 * @author mleidig
 * 
 */
public class XMLHandler extends DefaultHandler {    

    protected static Category LOG=Category.getInstance(XMLHandler.class.getName());

    private String contextPath="";
    private CharArrayWriter contents=new CharArrayWriter();
    private boolean withFatalErrors;
    private boolean withErrors;
    private boolean withWarnings;
    private String systemId;
    	
    public XMLHandler() {
    }
	
    /**
     * Returns if parsing threw errors.
     */
    public boolean withErrors() {return withErrors;}
    
    /**
     * Returns if parsing threw fatal errors.
     */  
    public boolean withFatalErrors() {return withFatalErrors;}
    
    /**
     * Returns if parsing threw warnings.
     */   
    public boolean withWarnings() {return withWarnings;}

    /**
     * Returns text content
     */
    protected String getContent() {
        return contents.toString().trim();
    }

    /**
     * Returns element context path
     */
    protected String getContextPath() {
        if(contextPath.length()==0) return "/";
        return contextPath;
    }
    
    /**
     * Callback method implemented by derived classes to receive preprocessed startElement events
     * 
     * @param uri
     * @param localName
     * @param qName
     * @param attributes
     */
    public void handleStartElement(String uri,String localName,String qName,XMLAttributes attributes) throws XMLProcessingException {}

    public final void startElement(String uri,String localName,String qName,Attributes attributes) throws SAXException {
        handleStartElement(uri,localName,qName,new XMLAttributes(attributes));
        contextPath+=("/"+qName);
        contents.reset();
    }

    /**
     * Callback method implemented by derived classes to receive preprocessed endElement events
     * 
     * @param uri
     * @param localName
     * @param qName
     */
    public void handleEndElement(String uri,String localName,String qName) throws XMLProcessingException {}
       
    public final void endElement(String uri,String localName,String qName) throws SAXException {
        int ind=contextPath.lastIndexOf("/");
        if(ind>-1) contextPath=contextPath.substring(0,ind);
        handleEndElement(uri,localName,qName);
    }

    public void characters(char[] ch,int start,int length) {
        contents.write(ch,start,length);
    }
	
    public void error(SAXParseException e) {
        LOG.error("Error occurred while parsing XML ('"+systemId+"': line "+e.getLineNumber()+" column "+e.getColumnNumber()+").",e);
        withErrors=true;
    }
	 
    public void fatalError(SAXParseException e) {
        LOG.fatal("Fatal error occurred while parsing XML ('"+systemId+"': line "+e.getLineNumber()+" column "+e.getColumnNumber()+").",e);
        withFatalErrors=true;
    }
    
    public void warning(SAXParseException e) {
        LOG.warn("Warning occurred while parsing XML ('"+systemId+"': line "+e.getLineNumber()+" column "+e.getColumnNumber()+").",e);
        withWarnings=true;
    }

    /**
     * EntityResolver interface implementation, which tries to get input sources using the systemid for loading via the file system 
     * or the publicid for loading via the classloader.
     */
    public InputSource resolveEntity(String publicId,String systemId) {
        if(systemId!=null && !systemId.equals("")) {
            String file=createSystemId(getSystemId(),systemId);
            if(file!=null) {
                try {
                    InputStream in=new FileInputStream(file);
                    return new InputSource(in);
                } catch(FileNotFoundException x) {
                    LOG.warn("Can't resolve systemid '"+systemId+"' as loadable resource.");
                }
            }
        }
        if(publicId!=null && !publicId.equals("")) {
            InputStream in=getClass().getClassLoader().getResourceAsStream(publicId);
            if(in==null) {
                LOG.warn("Can't resolve publicId '"+publicId+"' as loadable resource.");
            } else {
                return new InputSource(in);
            }
        }
        return null;
    }

    protected void setSystemId(String systemId) {
        this.systemId=systemId;
    }

    protected String getSystemId() {
        return systemId;
    }
    
    private static String createSystemId(String base,String part) {
        if(base==null || part==null) return part;
        char sep=File.separatorChar;
        if(part.startsWith(""+sep)) return part;
        if(base.indexOf(sep)>-1) {
            return base.substring(0,base.lastIndexOf(sep)+1)+part;
        } else return part;
    }

}
