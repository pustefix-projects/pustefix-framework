package de.schlund.pfixeditor.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Component;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;
import de.schlund.pfixeditor.view.*;
import de.schlund.pfixeditor.misc.Utilities;

/**
 * @author rouven
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Redo extends MenuAction implements EditAction{

  
public Redo()
  {
    super("redo");
  }
  
    /**
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
 
public void actionPerformed(ActionEvent evt)
{
    PfixTextArea textArea = getTextArea(evt);
    
    Utilities.setCursorOnWait((Component)textArea,true);
    UndoManager redo = textArea.getUndo();
    textArea.setUndoing(true);
    if (redo.canRedo())
      redo.redo();
    /*
    if (!undo.canUndo() && textArea.isDirty())
    */  
    textArea.grabFocus();
    textArea.setUndoing(false);
    Utilities.setCursorOnWait(textArea, false);
}
}
/*
  <action name="redo" edit="yes">
from org.jext import Utilities
textArea = __jext__.getTextArea()
Utilities.setCursorOnWait(textArea, 1)
redo = textArea.getUndo()
textArea.setUndoing(1)
if redo.canRedo():
  redo.redo()
textArea.grabFocus()
textArea.setUndoing(0)
Utilities.setCursorOnWait(textArea, 0)
  </action>
*/