package de.schlund.pfixeditor.xml;

import java.lang.String;

/**
 * @author rouven
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class XmlError {

  private String message;
  //private String systemId;
  private int line;
  private int column;
  private int offset;
  private int tokenstart;
  private int tokenend;

  /**
    * Construct a new XMLError.
    * @param message The error message from the parser.
    * @param line The line number where the error appeared.
    * @param column The column number where the error appeared.
    */
  public XmlError (String message, int line, int column, int offset)
  {
    this.message = message;
//    this.systemId = systemId;
    this.line = line;
    this.column = column;
    this.offset = offset;
  }


  /**
    * Get the error message from the parser.
    * @return A string describing the error.
    */
  public String getMessage ()
  {
    return message;
  }


  /**
    * Get the line number containing the error.
    * @return The line number as an integer.
    */
  public int getLine ()
  {
    return line;
  }

  /**
    * Get the column number containing the error.
    * @return The column number as an integer.
    */
  public int getColumn ()
  {
    return column;
  }

  public int getOffset()
  {
    return offset;
  }
/**
 * Returns the tokenend.
 * @return int
 */
public int getTokenEnd() {
    return tokenend;
}


/**
 * Returns the tokenstart.
 * @return int
 */
public int getTokenStart() {
    return tokenstart;
}


/**
 * Sets the tokenend.
 * @param tokenend The tokenend to set
 */
public void setTokenEnd(int tokenend) {
    this.tokenend = tokenend;
}


/**
 * Sets the tokenstart.
 * @param tokenstart The tokenstart to set
 */
public void setTokenStart(int tokenstart) {
    this.tokenstart = tokenstart;
}


/**
 * Sets the column.
 * @param column The column to set
 */
public void setColumn(int column) {
    this.column = column;
}


/**
 * Sets the line.
 * @param line The line to set
 */
public void setLine(int line) {
    this.line = line;
}


}