package de.schlund.pfixeditor.xml;

/**
 * @author rouven
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */



import java.io.*;
import java.io.StringReader;
import java.io.Reader;
import com.microstar.xml.*;
import de.schlund.pfixeditor.misc.Log;
import de.schlund.pfixeditor.xml.*;
import java.lang.Exception;
import java.util.ArrayList;
import javax.swing.JTree;
//import org.jext.Jext;

//public class XPropertiesReader
//{
public class XmlTextProcessor {

  public XmlTextProcessor(){}
  
  private /*static*/ ArrayList errors=null;
  private /*static*/ JTree tree=null;
  
  public /*static*/ ArrayList getXmlErrors()
  {
   return errors;
  }
  
  public /*static*/ JTree getXmlTree()
  {
    return tree;
  }
  
  public /*static*/ boolean parse(String text)
  {
    //XmlError[] xmlErrors=null;  
    errors=null;
    tree=null;
    
    boolean result=true;  
    XmlParser parser = new XmlParser();
    XmlTextHandler xmlTextHandler=new XmlTextHandler(null,parser);
    parser.setHandler(xmlTextHandler);
    try
    {
      StringReader rdr=new StringReader(text);
      parser.parse(null,null,rdr);
      //parser.parse(Jext.class.getResource("xproperties.dtd").toString(), null, reader);
    } catch (XmlException e) {
      Log.println(" Error parsing grammar " );
      Log.println(" Error occured at line " + e.getLine() +
                         ", column " + e.getColumn());
      Log.println(" " + e.getMessage());
      //return false;
    } catch (EOFException eof) {
        Log.println(" Xml: End of text");
        Log.println(" xmlTextHandler.getStartCount()="+xmlTextHandler.getStartCount());
        
        if (xmlTextHandler.getStartCount()!=0){
            String msg=xmlTextHandler.getLastStartTag();
            Log.println(" last start tag: "+msg);
            if (msg!=null) {
                msg="Unexpected end of file (expect end tag "+msg+")";
                errors=xmlTextHandler.getXmlErrors();
                if (errors==null)
                    errors=new ArrayList();
                XmlError err=new XmlError(msg,-2,-2,-2);
                errors.add(err);
                result=false;
            }
            
        }
        
    } catch (Exception e) {
        Log.println(" Xml: "+e.getMessage());
        return false;
    }
 
   
     
    if(errors==null)
    {
      errors=xmlTextHandler.getXmlErrors();
      if (errors!=null)
        result=false;
    }
      
    tree=xmlTextHandler.getXmlTree(); 
    return result;
  }
}
  
/*
  public static boolean read(InputStream fileName, String file)
  {
    InputStream in = Jext.getLanguageStream(fileName, file);
    if (in == null)
      return false;

    InputStreamReader reader = new InputStreamReader(in);
    if (reader == null)
      return false;

    XmlParser parser = new XmlParser();
    parser.setHandler(new XPropertiesHandler());

    try
    {
      parser.parse(Jext.class.getResource("xproperties.dtd").toString(), null, reader);
    } catch (XmlException e) {
      System.err.println("XProperties: Error parsing grammar " + file);
      System.err.println("XProperties: Error occured at line " + e.getLine() +
                         ", column " + e.getColumn());
      System.err.println("XProperties: " + e.getMessage());
      return false;
    } catch (Exception e) {
      return false;
    }

    try
    {
      fileName.close();
      reader.close();
    } catch (IOException ioe) { }

    return true;
  }
}
*/