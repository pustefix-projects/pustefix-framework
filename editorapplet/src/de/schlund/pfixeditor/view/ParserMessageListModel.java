package de.schlund.pfixeditor.view;

import javax.swing.DefaultListModel;
import java.util.ArrayList;
import java.util.Iterator;
import de.schlund.pfixeditor.xml.XmlError;

/**
 * @author rouven
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ParserMessageListModel extends DefaultListModel {

  private ArrayList parsErrs=null;

  public ParserMessageListModel(ArrayList messages) {
    super();
    addMessages(messages);
  }
  
  public void addMessages(ArrayList messages)
  {
    if (messages!=null)
    {
      parsErrs=messages;
      XmlError parsErr=null;
      Iterator it=parsErrs.iterator();
      while (it.hasNext()) {
        parsErr=(XmlError)it.next();
        String elem="";
        if (parsErr.getOffset()!=-2) {
            elem="line "+parsErr.getLine()+", column "+parsErr.getColumn()+": ";
        }
        this.addElement(elem+parsErr.getMessage());    
      }
    }  
  }
  
  public void clearMessages()
  {
    this.clear();
    this.parsErrs=null;
  }
  
  public XmlError getXmlError(int index)
  {
    if (parsErrs!=null)
    try{
      return (XmlError) parsErrs.get(index);
    }catch (Exception e) {};
    return null;
  }

}
