package de.schlund.pfixeditor.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Component;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;
import de.schlund.pfixeditor.view.*;
import de.schlund.pfixeditor.misc.Utilities;
import de.schlund.pfixeditor.misc.Log;

/**
 * @author rouven
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Undo extends MenuAction implements EditAction{

  
public Undo()
  {
    super("undo");
  }
  
    /**
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
 
public void actionPerformed(ActionEvent evt)
{
    PfixTextArea textArea = getTextArea(evt);
    
    Utilities.setCursorOnWait((Component)textArea,true);
    UndoManager undo = textArea.getUndo();
    textArea.setUndoing(true);
    //Log.println("undo presentation name: "+undo.getPresentationName());
    if (undo.canUndo())
    {
      //textArea.select(textArea.)
      undo.undo();
    }
    /*
    if (!undo.canUndo() && textArea.isDirty())
    */  
    textArea.grabFocus();
    textArea.setUndoing(false);
    Utilities.setCursorOnWait(textArea, false);
}
}

/*
 <action name="undo" edit="yes">
from org.jext import Utilities
textArea = __jext__.getTextArea()
Utilities.setCursorOnWait(textArea, 1)
undo = textArea.getUndo()
textArea.setUndoing(1)
if undo.canUndo():
  undo.undo()
  if not undo.canUndo() and textArea.isDirty():
    __jext__.resetStatus(textArea)
textArea.grabFocus()
textArea.setUndoing(0)
Utilities.setCursorOnWait(textArea, 0)
  </action>
*/