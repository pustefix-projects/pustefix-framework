package de.schlund.pfixeditor.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.text.*;

//import org.jext.*;
//import org.gjt.sp.jedit.textarea.*;

import de.schlund.pfixeditor.view.*;


/**
 * @author rouven
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Copy extends MenuAction {

public Copy()
  {
    super("copy");
  }
  
    /**
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
 
public void actionPerformed(ActionEvent evt)
{
    PfixTextArea textArea = getTextArea(evt);

    textArea.copy();
    textArea.grabFocus();
}
}
/* <action name="copy">
textArea = __jext__.getTextArea()
textArea.copy()
textArea.grabFocus()
  </action>

  <action name="cut" edit="yes">
textArea = __jext__.getTextArea()
textArea.cut()
textArea.grabFocus()
  </action>*/