package de.schlund.pfixeditor.xml;
import java.lang.String;
import javax.swing.tree.DefaultMutableTreeNode;
/**
 * @author rouven
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class XmlNode extends DefaultMutableTreeNode{

private String tagName;

private int startLine=-1;
private int startColumn=-1;
private int startOffset=-1;

private int endLine=-1;
private int endColumn=-1;
private int endOffset=-1;
private boolean error=false;
private XmlError xmlError=null;
  /**
    * Construct a new XMLNode.
    * @param message The error message from the parser.
    * @param line The line number where the error appeared.
    * @param column The column number where the error appeared.
    */
  
  public XmlNode (String name)
  {
    super(name);
    init(name,0,0,0);
  }
  
  public XmlNode (String tagName, int startLine, int startColumn, int startOffset)
  {
    super();
    String name="<" + tagName + " ...>";
    this.setUserObject(name);
    init( tagName, startLine, startColumn, startOffset);
  }
  
  private void init(String tagName, int startLine, int startColumn, int startOffset)
  {
    this.tagName = tagName;
//    this.systemId = systemId;
    this.startLine = startLine;
    this.startColumn = startColumn;
    this.startOffset=startOffset;
  }

/**
 * Returns the endColumn.
 * @return int
 */
public int getEndColumn() {
    return endColumn;
}


/**
 * Returns the endLine.
 * @return int
 */
public int getEndLine() {
    return endLine;
}


/**
 * Returns the endOffset.
 * @return int
 */
public int getEndOffset() {
    return endOffset;
}


/**
 * Returns the error.
 * @return boolean
 */
public boolean isError() {
    return error;
}


/**
 * Returns the startLine.
 * @return int
 */
public int getStartLine() {
    return startLine;
}


/**
 * Returns the startOffset.
 * @return int
 */
public int getStartOffset() {
    return startOffset;
}


/**
 * Returns the tagName.
 * @return String
 */
public String getTagName() {
    return tagName;
}


/**
 * Sets the endColumn.
 * @param endColumn The endColumn to set
 */
public void setEndColumn(int endColumn) {
    this.endColumn = endColumn;
}


/**
 * Sets the endLine.
 * @param endLine The endLine to set
 */
public void setEndLine(int endLine) {
    this.endLine = endLine;
}


/**
 * Sets the endOffset.
 * @param endOffset The endOffset to set
 */
public void setEndOffset(int endOffset) {
    this.endOffset = endOffset;
}


/**
 * Sets the error.
 * @param error The error to set
 */
public void setError(boolean error) {
    this.error = error;
}


/**
 * Sets the startLine.
 * @param startLine The startLine to set
 */
public void setStartLine(int startLine) {
    this.startLine = startLine;
}


/**
 * Sets the startOffset.
 * @param startOffset The startOffset to set
 */
public void setStartOffset(int startOffset) {
    this.startOffset = startOffset;
}


/**
 * Sets the tagName.
 * @param tagName The tagName to set
 */
public void setTagName(String tagName) {
    this.tagName = tagName;
}


/**
 * Returns the startColumn.
 * @return int
 */
public int getStartColumn() {
    return startColumn;
}


/**
 * Returns the xmlError.
 * @return XmlError
 */
public XmlError getXmlError() {
    return xmlError;
}


/**
 * Sets the startColumn.
 * @param startColumn The startColumn to set
 */
public void setStartColumn(int startColumn) {
    this.startColumn = startColumn;
}


/**
 * Sets the xmlError.
 * @param xmlError The xmlError to set
 */
public void setXmlError(XmlError xmlError) {
    this.xmlError = xmlError;
}


}