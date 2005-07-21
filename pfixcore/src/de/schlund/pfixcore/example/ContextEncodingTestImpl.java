/*
 * Created on 19.02.2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixcore.example;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.w3c.dom.Element;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.ResultDocument;

/**
 * @author ml
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ContextEncodingTestImpl implements ContextEncodingTest {

    private final static String DEFAULT_TEXT="abcdäöüß";
    private Context ctx;
    private String text=DEFAULT_TEXT;
    private String encoding="";
    
    public String getText() {
    	return text;
    }
    
    public void setText(String text) {
    	this.text=text;
    }
    
    public String getEncoding() {
    	return encoding;
    }
    
    public void setEncoding(String encoding) {
    	this.encoding=encoding;
    }
    
    public void init(Context ctx) {
        this.ctx=ctx;
    }
    
    public void reset() {
        ctx=null;
        text=DEFAULT_TEXT;
        encoding="";
    }
    
    public void insertStatus(ResultDocument resDoc,Element elem) {
    	ResultDocument.addTextChild(elem,"encoding",encoding);
    	ResultDocument.addTextChild(elem,"original",text);
    	try {
    		String utfEnc=URLEncoder.encode(text,"UTF-8");
      	ResultDocument.addTextChild(elem,"urlenc-utf",utfEnc);
      	String isoEnc=URLEncoder.encode(text,"ISO-8859-1");
      	ResultDocument.addTextChild(elem,"urlenc-iso",isoEnc);
      } catch(UnsupportedEncodingException x) {}
    }
    
}
