package de.schlund.pfixeditor.xml;

import com.microstar.xml.XmlParser;
import com.microstar.xml.HandlerBase;
import com.microstar.xml.XmlException;
import java.util.ArrayList;
import javax.swing.tree.*;
import javax.swing.JTree;

/**
 * @author rouven
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class XmlTextHandler extends HandlerBase {
  private ArrayList xmlErrors=null;
  private JTree tree=null;
  private XmlNode currentNode=null;
  private XmlNode rootNode=null;
  private String docName=null;
  private XmlParser parser=null;
  private int startCount=0;
  private String lastStartTag=null;
  
  public XmlTextHandler(String docName, XmlParser parser)
  {
    if ((docName==null) || (docName.length()==0))
      this.docName="Document";
    else
      this.docName=docName;
    this.parser=parser;
    startCount=0;
  }    
  
  public JTree getXmlTree()
  {
    return tree;
  }
  
  public ArrayList getXmlErrors() {
    return xmlErrors;
  }
  
  /**
    * Handle the start of the document.
    * <p>The default implementation does nothing.
    * @see com.microstar.xml.XmlHandler#startDocument
    * @exception java.lang.Exception Derived methods may throw exceptions.
    */
  public void startDocument () 
    throws java.lang.Exception
  {    
    rootNode=currentNode=new XmlNode(docName);
    xmlErrors=null;
    //tree=new(JTree());
  }

  /**
    * Handle the end of the document.
    * <p>The default implementation does nothing.
    * @see com.microstar.xml.XmlHandler#endDocument
    * @exception java.lang.Exception Derived methods may throw exceptions.
    */
  public void endDocument ()
    throws java.lang.Exception
  {
    tree=new JTree(rootNode);    
  }

  /**
    * Resolve an external entity.
    * <p>The default implementation simply returns the supplied
    * system identifier.
    * @see com.microstar.xml.XmlHandler#resolveEntity
    * @exception java.lang.Exception Derived methods may throw exceptions.
    */
  public Object resolveEntity (String publicId, String systemId) 
    throws java.lang.Exception
  {
    return null;
  }


  /**
    * Handle the start of an external entity.
    * <p>The default implementation does nothing.
    * @see com.microstar.xml.XmlHandler#startExternalEntity
    * @exception java.lang.Exception Derived methods may throw exceptions.
    */
  public void startExternalEntity (String systemId)
    throws java.lang.Exception
  {
  }

  /**
    * Handle the end of an external entity.
    * <p>The default implementation does nothing.
    * @see com.microstar.xml.XmlHandler#endExternalEntity
    * @exception java.lang.Exception Derived methods may throw exceptions.
    */
  public void endExternalEntity (String systemId)
    throws java.lang.Exception
  {
  }

  /**
    * Handle a document type declaration.
    * <p>The default implementation does nothing.
    * @see com.microstar.xml.XmlHandler#doctypeDecl
    * @exception java.lang.Exception Derived methods may throw exceptions.
    */
  public void doctypeDecl (String name, String publicId, String systemId)
    throws java.lang.Exception
  {
  }

  /**
    * Handle an attribute assignment.
    * <p>The default implementation does nothing.
    * @see com.microstar.xml.XmlHandler#attribute
    * @exception java.lang.Exception Derived methods may throw exceptions.
    */
  public void attribute (String aname, String value, boolean isSpecified)
    throws java.lang.Exception
  {
  }

  /**
    * Handle the start of an element.
    * <p>The default implementation does nothing.
    * @see com.microstar.xml.XmlHandler#startElement
    * @exception java.lang.Exception Derived methods may throw exceptions.
    */
  public void startElement (String elname)
    throws java.lang.Exception
  {
    XmlNode tmpNode=new XmlNode(elname,parser.getLineNumber(), parser.getColumnNumber(),parser.getCurrentByteCount());
    currentNode.add(tmpNode);
    currentNode=tmpNode;
    startCount++;
    lastStartTag=elname;
  }

  /**
    * Handle the end of an element.
    * <p>The default implementation does nothing.
    * @see com.microstar.xml.XmlHandler#endElement
    * @exception java.lang.Exception Derived methods may throw exceptions.
    */
  public void endElement (String elname)
    throws java.lang.Exception
  {
    currentNode.setEndColumn(parser.getColumnNumber());
    currentNode.setEndLine(parser.getLineNumber());
    currentNode.setEndOffset(parser.getCurrentByteCount());
    currentNode=(XmlNode)currentNode.getParent();
    startCount--;
  }

  /**
    * Handle character data.
    * <p>The default implementation does nothing.
    * @see com.microstar.xml.XmlHandler#charData
    * @exception java.lang.Exception Derived methods may throw exceptions.
    */
  public void charData (char ch[], int start, int length)
    throws java.lang.Exception
  {
  }

  /**
    * Handle ignorable whitespace.
    * <p>The default implementation does nothing.
    * @see com.microstar.xml.XmlHandler#ignorableWhitespace
    * @exception java.lang.Exception Derived methods may throw exceptions.
    */
  public void ignorableWhitespace (char ch[], int start, int length)
    throws java.lang.Exception
  {
  }

  /**
    * Handle a processing instruction.
    * <p>The default implementation does nothing.
    * @see com.microstar.xml.XmlHandler#processingInstruction
    * @exception java.lang.Exception Derived methods may throw exceptions.
    */
  public void processingInstruction (String target, String data)
    throws java.lang.Exception
  {
  }

  /**
    * Throw an exception for a fatal error.
    * <p>The default implementation throws <code>XmlException</code>.
    * @see com.microstar.xml.XmlHandler#error
    * @exception com.microstar.xml.XmlException A specific parsing error.
    * @exception java.lang.Exception Derived methods may throw exceptions.
    */
/*  public void error (String message, String systemId, int line, int column)
    throws XmlException, java.lang.Exception
  {
    throw new XmlException(message, systemId, line, column);
  }
*/
    /**
     * @see com.microstar.xml.XmlHandler#error(String, String, int, int)
     */
    public void error(String message, String systemId, int line, int column)
        throws Exception {
      try {
        super.error(message, systemId, line, column);
      }catch (XmlException xmlE) {
         if (xmlErrors==null) {
             xmlErrors=new ArrayList();
         }
         int col=xmlE.getColumn();
         int lin=xmlE.getLine();
         /*if ((col<=0) && (lin>1)) {
            lin--;
            col=-1;
         }*/
         XmlError err=new XmlError(xmlE.getMessage(), lin, 
                           col, parser.getCurrentByteCount());
         xmlErrors.add(err);
         currentNode.setError(true);
         currentNode.setXmlError(err); 
      }
    }
/**
 * Returns the lastStartTag.
 * @return String
 */
public String getLastStartTag() {
    return lastStartTag;
}


/**
 * Returns the startCount.
 * @return int
 */
public int getStartCount() {
    return startCount;
}


}

