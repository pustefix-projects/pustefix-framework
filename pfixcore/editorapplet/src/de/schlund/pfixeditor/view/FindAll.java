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
public class FindAll extends MenuAction {

public FindAll()
  {
    super("find_all");
  }
  
    /**
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
 
public void actionPerformed(ActionEvent evt)
{  
    Gui gui=getJextParent(evt);
   
    FindAllDialog fr=new FindAllDialog(gui);
}
}



/*
<action name="find_all">
from org.jext.search import FindAllDialog
FindAllDialog(__jext__)
  </action>
*/