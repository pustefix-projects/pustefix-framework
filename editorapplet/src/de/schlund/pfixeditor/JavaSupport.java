/*
 * 03/23/2002 - 00:31:45
 *
 * JavaSupport.java - Support for JDK 1.4
 * Copyright (C) 2002 Romain Guy
 * Portions copyright (C) 2002 Slava Pestov
 * romain.guy@jext.org
 * www.jext.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
 
package de.schlund.pfixeditor;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import de.schlund.pfixeditor.view.*;
import de.schlund.pfixeditor.misc.Log;

import javax.swing.*;

public class JavaSupport
{
    
  public static void initJavaSupport()
  {
    //JFrame.setDefaultLookAndFeelDecorated(Jext.getBooleanProperty("decoratedFrames"));
    //JDialog.setDefaultLookAndFeelDecorated(Jext.getBooleanProperty("decoratedFrames"));
    KeyboardFocusManager.setCurrentKeyboardFocusManager(new JextKeyboardFocusManager());
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener ("focusOwner", 
    new PropertyChangeListener() { public void propertyChange (PropertyChangeEvent pce) 
        {
            Object nval=pce.getNewValue();
            Object oval=pce.getOldValue();
            String snval="value not available";
            String soval="value not available";
            if (nval!=null){
                snval=nval.toString();
            }
            if (oval!=null){
                soval=oval.toString();
            }
            Log.println("KeyBoardFocusMng focusowner changed: "+ snval+ " (old:"+soval+")"); 
            
        }
    });
    
    Log.println("init KeyboardFocusManager");
  }

  public static void setMouseWheel(final PfixTextArea area)
  {
    area.addMouseWheelListener(new MouseWheelListener()
    {
      public void mouseWheelMoved(MouseWheelEvent e)
      {
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL)
        {
          area.setFirstLine(area.getFirstLine() + e.getUnitsToScroll());
        }
      }
    });
  }

  static class JextKeyboardFocusManager extends DefaultKeyboardFocusManager
  {
    private static KeyEvent last_evt=null;
    
    JextKeyboardFocusManager()
    {
      setDefaultFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
      //this.clearGlobalFocusOwner(); 
      Log.println("Keyboard Focus Manager instantiated");
      //this.printInfo();     
    }
    
    private String getObjectString(Object x)
    {
        if (x!=null)
          return x.toString();
        return "null";
    }
    
    private void printInfo() {
      Log.println("--- KFM: FOCUS INFO --------------------------");
     
      Log.println("Focusowner                 : "+getObjectString(this.getFocusOwner()));
      
      Log.println("Permanent focusowner       : "+getObjectString(this.getPermanentFocusOwner()));
      
      Log.println("Window of focusowner       : "+getObjectString(this.getFocusedWindow()));
      
      Log.println("Active window              : "+getObjectString(this.getActiveWindow()));
     
      Log.println("Global permanent focusowner: "+getObjectString(this.getGlobalPermanentFocusOwner()));
      
      Log.println("Global active window       : "+getObjectString(this.getGlobalActiveWindow()));
      
      Log.println("Global focused window      : "+getObjectString(this.getGlobalFocusedWindow()));
      
      Log.println("---------------------------------------------");
      
    }
    
    public void activateFrame(Window win)
    {
      this.setGlobalActiveWindow(win);
      this.setGlobalFocusedWindow(win);
    }
    
    private void printWinEvtInfo(WindowEvent we)
    {
      Log.println("---- Window Event Info ------------------------");
      Log.println("Triggered through Component : "+getObjectString(we.getComponent()));  
      Log.println("Event occured at            : "+getObjectString(we.getSource()));
      Log.println("Originator window           : "+getObjectString(we.getWindow()));
      Log.println("OppositeWindow              : "+getObjectString(we.getOppositeWindow()));
      Log.println("-----------------------------------------------");
    }
    
    public boolean postProcessKeyEvent(KeyEvent evt)
    {
      String evtType="";
        switch(evt.getID()){
          case KeyEvent.KEY_TYPED:{
            evtType="TYPED";
            break;
          }          
          case KeyEvent.KEY_PRESSED:{
            evtType="PRESSED";
            break;
          }
          case KeyEvent.KEY_RELEASED:{    
            evtType="RELEASED";
            break;
          }
          default:{
            evtType="UNKNOWN";
          }
        }
      Log.println("--- POSTPROCESS "+evtType +" ----------------------------------");
      Log.println("char:"+evt.getKeyChar()+" isConsumed:"+evt.isConsumed());
      Log.println("Source of keyevent:"+evt.getSource().toString());
      printInfo();
      if (!evt.isConsumed())
      {
        Component comp = (Component) evt.getSource();
        //Log.println("Source of keyevent:"+evt.getSource().toString());
        if (!comp.isShowing())
          return true;
       
        for ( ; ; )
        {
          if (comp instanceof Gui)
          {
            ((Gui) comp).processKeyEvent(evt);
            return true;
          } else if (comp == null || comp instanceof Window || comp instanceof PfixTextArea)
            break;
          else
            comp = comp.getParent();
        }
      }

      return super.postProcessKeyEvent(evt);
     
    }
    
    public boolean dispatchEvent(AWTEvent e) {

        switch (e.getID()) {
            case WindowEvent.WINDOW_GAINED_FOCUS: {
            Log.println("KeyBoardFocusMng: WINDOW_GAINED_FOCUS");
            printWinEvtInfo( (WindowEvent) e);
            break;
           
        }

        // If there exists a current focused window, then notify it
        // that it has lost focus.
       

        // Because the native libraries do not post WINDOW_ACTIVATED
        // events, we need to synthesize one if the active Window
        // changed.
       

        // Identify which Component should initially gain focus in
        // the Window.
        //
        // * If we're in SendMessage, then this is a synthetic
        //   WINDOW_GAINED_FOCUS message which was generated by a
        //   the FOCUS_GAINED handler. Allow the Component to which the
        //   FOCUS_GAINED message was targeted to receive the focus.
        // * Otherwise, look up the correct Component here. If we
        //   perform the lookup after the call to setGlobalFocusOwner,
        //   'null' will be returned.
  

        // Restore focus to the Component which last held it. We do
        // this here so that client code can override our choice in
        // a WINDOW_GAINED_FOCUS handler.
        //
        // Make sure that the focus change request doesn't change the
        // focused Window in case we are no longer the focused Window
        // when the request is handled.



        case WindowEvent.WINDOW_ACTIVATED: {
           Log.println("KeyBoardFocusMng: WINDOW_ACTIVATED");
           printWinEvtInfo( (WindowEvent) e);
           break;
        }

        // If there exists a current active window, then notify it that
        // it has lost activation.
       

        case FocusEvent.FOCUS_GAINED: {
            Log.println("KeyBoardFocusMng: FOCUS_GAINED");
            break;
        }

        // If there exists a current focus owner, then notify it that
        // it has lost focus.
        
            
        // Because the native windowing system has a different notion
        // of the current focus and activation states, it is possible
        // that a Component outside of the focused Window receives a
        // FOCUS_GAINED event. We synthesize a WINDOW_GAINED_FOCUS
        // event in that case.
      

        case FocusEvent.FOCUS_LOST: {
            Log.println("KeyBoardFocusMng: FOCUS_LOST");
            break;
        }
        // Ignore cases where a Component loses focus to itself.
        // If we make a mistake because of retargeting, then the
        // FOCUS_GAINED handler will correct it.
       

        case WindowEvent.WINDOW_DEACTIVATED: {
            Log.println("KeyBoardFocusMng: WINDOW_DEACTIVATED");
            printWinEvtInfo( (WindowEvent) e);
            break;
        }

        case WindowEvent.WINDOW_LOST_FOCUS: {
            Log.println("KeyBoardFocusMng: WINDOW_LOST_FOCUS");
            printWinEvtInfo( (WindowEvent) e);         
            break;
        }

            // Special case -- if the native windowing system posts an
        // event claiming that the active Window has lost focus to the
        // focused Window, then discard the event. This is an artifact
        // of the native windowing system not knowing which Window is
        // really focused.
        

            case KeyEvent.KEY_TYPED:{
            Log.println("=== KeyBoardFocusMng: KEY_TYPED =========================");
            Log.println("Source of keyevent:"+e.getSource().toString());
            //printInfo();
            break;
        }
            case KeyEvent.KEY_PRESSED:{
            Log.println("=== KeyBoardFocusMng: KEY_PRESSED =======================");
            Log.println("Source of keyevent:"+e.getSource().toString());
            //printInfo();
            break;
        }
            case KeyEvent.KEY_RELEASED:{
            Log.println("=== KeyBoardFocusMng: KEY_RELEASED ======================");
            Log.println("Source of keyevent:"+e.getSource().toString());
            //printInfo();
            break;
        }
        }
        return super.dispatchEvent(e);
    }

    /**
     * Called by <code>dispatchEvent</code> if no other
     * KeyEventDispatcher in the dispatcher chain dispatched the KeyEvent, or
     * if no other KeyEventDispatchers are registered. If the event has not
     * been consumed, its target is enabled, and the focus owner is not null,
     * this method dispatches the event to its target. This method will also
     * subsequently dispatch the event to all registered
     * KeyEventPostProcessors.
     * <p>
     * In all cases, this method returns <code>true</code>, since
     * DefaultKeyboardFocusManager is designed so that neither
     * <code>dispatchEvent</code>, nor the AWT event dispatcher, should take
     * further action on the event in any situation.
     *
     * @param e the KeyEvent to be dispatched
     * @return <code>true</code>
     * @see Component#dispatchEvent
     */
    public boolean dispatchKeyEvent(KeyEvent e) {
        switch (e.getID()) {
           case KeyEvent.KEY_TYPED:{
            Log.println("--- KeyBoardFocusMng: dispatchKeyEvent: KEY_TYPED  char:'"+e.getKeyChar()+"' code:"+e.getKeyCode());
            printInfo();
            break;
        }
            case KeyEvent.KEY_PRESSED:{
            Log.println("--- KeyBoardFocusMng: dispatchKeyEvent: KEY_PRESSED  char:'"+e.getKeyChar()+"' code:"+e.getKeyCode());
            printInfo();
            break;
        }
            case KeyEvent.KEY_RELEASED:{
            Log.println("--- KeyBoardFocusMng: dispatchKeyEvent: KEY_RELEASED  char:'"+e.getKeyChar()+"' code:"+e.getKeyCode());
            printInfo();
            break;
        }
        }

        return super.dispatchKeyEvent(e);
    }
    
  }
}

// End of JavaSupport.java
