package de.schlund.pfixeditor.view;

/**
 * @author rouven
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Component;
import java.awt.Frame;
import java.lang.String;
import javax.swing.text.*;
import javax.swing.JOptionPane;

//import org.jext.*;
//import org.gjt.sp.jedit.textarea.*;
import de.schlund.pfixeditor.view.*;
import de.schlund.pfixeditor.misc.Log;
import de.schlund.pfixeditor.Props;
import org.jext.search.*;


/**
 * @author rouven
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class FindNext extends MenuAction {

public FindNext()
  {
    super("find_next");
  }
  
    /**
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
 
public void actionPerformed(ActionEvent evt)
{  
    Gui gui=getJextParent(evt);
    PfixTextArea textArea=gui.getTextArea();
    try {
        if (!Search.find(textArea,textArea.getCaretPosition())){
            String[] args = { "text" };
            Log.println("Dialog: matchnotfound");
      
            int response = JOptionPane.showConfirmDialog(null,
                       Props.getProperty("find.matchnotfound", args),
                       Props.getProperty("find.title"),
                       JOptionPane.YES_NO_OPTION,
                       JOptionPane.QUESTION_MESSAGE);

            switch (response)
            {
              case JOptionPane.YES_OPTION:
               textArea.setCaretPosition(0);
               gui.getAction("find_next").actionPerformed(evt);
              break;
              case JOptionPane.NO_OPTION:
                return;
              case JOptionPane.CANCEL_OPTION:
                return;
            }
        }
    }catch (Exception e) {Log.println("findnext failed: "+e.getMessage());}
}
}



/*
  <action name="find_next">
from jarray import array
from java.lang import String
from javax.swing import JOptionPane
from org.jext import Jext
from org.jext.search import Search
textArea = __jext__.getTextArea()
try:
  if not Search.find(textArea, textArea.getCaretPosition()):
    args = array([textArea.getName()], String)
    response = JOptionPane.showConfirmDialog(None, Jext.getProperty("find.matchnotfound", args), Jext.getProperty("find.title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
    if response == 0:
      textArea.setCaretPosition(0)
      Jext.getAction("find_next").actionPerformed(__evt__)
except:
  pass
  </action>
  */