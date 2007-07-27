package de.schlund.pfixcore.example;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.ResultDocument;

/**
 * @author mleidig@schlund.de
 */
public class ContextEncodingTestImpl implements ContextEncodingTest {

    private final static String DEFAULT_TEXT="abcdäöüß";
    
    private String text=DEFAULT_TEXT;
    private String encoding="";
    private File file;
    
    public String getText() {
    	return text;
    }
    
    public void setText(String text) {
    	this.text=text;
    }
    
    public File getFile() {
        return file;
    }
    
    public void setFile(File file) {
        this.file=file;
    }
    
    public String getEncoding() {
    	return encoding;
    }
    
    public void setEncoding(String encoding) {
    	this.encoding=encoding;
    }
    
    public void init(Context ctx) {
    }
    
    public void insertStatus(ResultDocument resDoc,Element elem) {
    	ResultDocument.addTextChild(elem,"encoding",encoding);
    	ResultDocument.addTextChild(elem,"original",text);
        if(file!=null) ResultDocument.addTextChild(elem,"file",file.getAbsolutePath());
    	try {
    	    String utfEnc=URLEncoder.encode(text,"UTF-8");
    	    ResultDocument.addTextChild(elem,"urlenc-utf",utfEnc);
    	    String isoEnc=URLEncoder.encode(text,"ISO-8859-1");
    	    ResultDocument.addTextChild(elem,"urlenc-iso",isoEnc);
    	} catch(UnsupportedEncodingException x) {}
    	ResultDocument.addObject(elem,"alphabet",new RussianAlphabet());
    }
    
    public class RussianAlphabet {
        
        private String description;
        private List<String> characters;
        
        public RussianAlphabet() {
            description="Basic Russian Alphabet";
            characters=new ArrayList<String>();
            for(char ch='\u0410';ch<'\u0430';ch++) {
                characters.add(ch+" "+Character.toLowerCase(ch));
            }
        }
        
        public String getDescription() {
            return description;
        }
        
        public List<String> getCharacters() {
            return characters;
        }
        
    }
    
}
