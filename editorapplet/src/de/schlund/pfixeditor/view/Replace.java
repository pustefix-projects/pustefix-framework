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
import javax.swing.text.*;

//import org.jext.*;
//import org.gjt.sp.jedit.textarea.*;
import de.schlund.pfixeditor.view.*;
import de.schlund.pfixeditor.misc.Log;
import org.jext.search.*;


/**
 * @author rouven
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Replace extends MenuAction {

public Replace()
  {
    super("replace");
  }
  
    /**
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
 
public void actionPerformed(ActionEvent evt)
{  
    Gui gui=getJextParent(evt);
    Component cmp_frame = (Component)gui.getParent();
    int i=1;
    
    while (!(cmp_frame instanceof Frame) && i<20) {
      cmp_frame = ((Component)cmp_frame).getParent();
      Log.println("Replace:actionperformed: looking for frame loop "+i);
      i++;
    }
    if (cmp_frame instanceof Frame) {
      FindReplace fr=new FindReplace(gui, (Frame) cmp_frame, FindReplace.REPLACE, false);
    }
    else
      Log.println("Replace Dialog not instantiated");
}
}

