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
public class Paste extends MenuAction implements EditAction{

public Paste()
  {
    super("paste");
  }
  
    /**
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
 
public void actionPerformed(ActionEvent evt)
{
    PfixTextArea textArea = getTextArea(evt);

    textArea.paste();
    textArea.grabFocus();
}
}
