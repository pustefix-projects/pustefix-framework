package de.schlund.pfixeditor.view;

import java.awt.event.ActionEvent;
import java.awt.Component;
import java.util.Iterator;
import de.schlund.pfixeditor.xml.XmlTextProcessor;
import de.schlund.pfixeditor.xml.XmlError;
import java.util.ArrayList;
import de.schlund.pfixeditor.view.Gui;
import de.schlund.pfixeditor.view.ParserMessageListModel;
import de.schlund.pfixeditor.misc.Log;
import de.schlund.pfixeditor.misc.Utilities;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.text.Element;

/**
 * @author rouven
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Parse extends MenuAction {
    
    public Parse() {
        super("parse");
    }

    private ArrayList xmlErrors=null;
    private Gui gui=null;
    /**
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent evt) {
            
    PfixTextArea textArea = getTextArea(evt);
    textArea.parse();
    /*
    Utilities.setCursorOnWait((Component)textArea,true);
    Gui gui=(Gui)textArea.getPfixParent();
    XmlError xErr=null;
    
    String[] messages=null;
    textArea.setEditable(false);
    
    XmlTextProcessor.parse(textArea.getText());
    xmlErrors=XmlTextProcessor.getXmlErrors();
    JList msgList=gui.getMsgList(); 
    
    if (xmlErrors!=null){
        
    }
    ParserMessageListModel errList=null;
            
    if (msgList!=null) {
        ListModel lm=msgList.getModel();
        if (lm!=null && (lm instanceof ParserMessageListModel)) {
            errList=(ParserMessageListModel) lm;
            errList.clearMessages();
            //if (xmlErrors!=null)
            errList.addMessages(xmlErrors);
        }
        else 
        {
          errList=new ParserMessageListModel(xmlErrors);
          msgList.setModel(errList);
        }
    } 
    else {
        errList=new ParserMessageListModel(xmlErrors);
        msgList=new JList(errList);
    }
    
    //msgList.setSelectedIndex(1);
    msgList.setVisible(true);
    msgList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
    if (xmlErrors!=null) {
        Iterator it=xmlErrors.iterator();
        while (it.hasNext())
        {
            xErr=(XmlError) it.next();
            Log.println("Error at line "+xErr.getLine()+", column "+xErr.getColumn()
                        +", offset:"+xErr.getOffset()+":");
            Log.println("      "+xErr.getMessage());
            //textArea.set
        } 
        Element map = textArea.getDocument().getDefaultRootElement(); 
        Log.println("Elements of document:");
        printElement(map,0,1);
    }
/*
    else
    {
        if (msgList)
        
    }
*/
    //XmlTextProcessor.getXmlTree();
/*    textArea.setEditable(true);
    Utilities.setCursorOnWait((Component)textArea,false);
    textArea.grabFocus();
*/    
    }    


private void printElement(Element elem, int deep, int count)
{
  int i;
  Log.println("Element deep:"+deep+" count:"+count+"= "+elem.getName());
  for (i=1;i<=elem.getElementCount();i++)
    printElement(elem.getElement(i-1),deep+1,i);
}
}