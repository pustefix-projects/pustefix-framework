package de.schlund.pfixeditor;

import javax.swing.JPanel;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;
import javax.swing.colorchooser.*;
import javax.swing.filechooser.*;
import javax.accessibility.*;

import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

import java.lang.reflect.*;
import java.lang.String;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.io.*;
import java.applet.*;
import java.net.*;
import de.schlund.pfixeditor.*;
import de.schlund.pfixeditor.Props;
import de.schlund.pfixeditor.view.*;
import de.schlund.pfixeditor.misc.Log;
/**
 * @author rouven
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */

public class XmlEditor {
    
    private Gui gui=null;
    

    // Used only if XmlEditor is an application 
    private static JFrame frame = null;
    private JWindow splashScreen = null;

    // Used only if XmlEditor is an applet 
    private XmlEditorApplet applet = null;

    // To debug or not to debug, that is the question
    private boolean DEBUG = true;
    private int debugCounter = 0;
/*
    // The tab pane that holds the demo
    private JTabbedPane tabbedPane = null;

    private JEditorPane demoSrcPane = null;

    private JLabel splashLabel = null;
*/
    // contentPane cache, saved from the applet or application frame
    Container contentPane = null;

   /**
     * Returns the content pane wether we're in an applet
     * or application
     */
    public Container getContentPane() {
    if(contentPane == null) {
        if(getFrame() != null) {
        contentPane = getFrame().getContentPane();
        } else if (getApplet() != null) {
        contentPane = getApplet().getContentPane();
        }
    }
    
    KeyListener CheckKeys=new KeyListener() {
          public void keyPressed(KeyEvent keyEvent) {
            printIt("Pressed",keyEvent);
          }
        
          public void keyReleased(KeyEvent keyEvent) {
            printIt("Released",keyEvent);
          }
        
          public void keyTyped(KeyEvent keyEvent) {
            printIt("Typed",keyEvent);
          }
        
          private void printIt(String evtText, KeyEvent evt){
          Log.println("contentPane: processKeyEvent: evt="+evtText+" char="+evt.getKeyChar()+" code="+evt.getKeyCode()+" text="+evt.getKeyText(evt.getKeyCode()));
          }        
        };
    contentPane.addKeyListener(CheckKeys);
       
    return contentPane;
    }
    
    public String getString(String key) {
    String value = key;
/*
    try {
        value = getResourceBundle().getString(key);
    } catch (MissingResourceException e) {
        System.out.println("java.util.MissingResourceException: Couldn't find value for: " + key);
    }
    if(value == null) {
        value = "Could not find resource: " + key + "  ";
    }
*/
    return value;
    }

    /**
     * Create a frame for XmlEditor to reside in if brought up
     * as an application.
     */
    public static JFrame createFrame() {
        
        JFrame frame = new JFrame();
        WindowListener l = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
            //Log.finish();
            System.exit(0);
            }
        };
        frame.addWindowListener(l);
        
        KeyListener CheckKeys=new KeyListener() {
          public void keyPressed(KeyEvent keyEvent) {
            printIt("Pressed",keyEvent);
          }
        
          public void keyReleased(KeyEvent keyEvent) {
            printIt("Released",keyEvent);
          }
        
          public void keyTyped(KeyEvent keyEvent) {
            printIt("Typed",keyEvent);
          }
        
          private void printIt(String evtText, KeyEvent evt){
          Log.println("xmlEditor: processKeyEvent: evt="+evtText+" char="+evt.getKeyChar()+" code="+evt.getKeyCode()+" text="+evt.getKeyText(evt.getKeyCode()));
          }        
        };
        frame.addKeyListener(CheckKeys);
        return frame;
    }
    
     /**
     * Determines if this is an applet or application
     */
    public boolean isApplet() {
    return (applet != null);
    }

    /**
     * Returns the applet instance
     */
    public XmlEditorApplet getApplet() {
    return applet;
    }


    /**
     * Returns the frame instance
     */
    public JFrame getFrame() {
    return frame;
    }

    public Gui getNewGui(){
        gui=null;
        return getGui();
    }
    
    public Gui getNewGui(int width, int height) {
      gui=null;
      if (isApplet()){
         gui=new Gui(this.getContentPane(),getApplet(),width,height);
      }else {            
         gui=new Gui(this.getContentPane());
      }
      return gui;  
    }
    public Gui getGui() {
        if (gui==null) {
            if (isApplet()){
                gui=new Gui(this.getContentPane(),getApplet());
            }else {            
            gui=new Gui(this.getContentPane());
            }
        }
        else
        {
            Log.println("getGui:gui isn't null");
        }
        return gui;
    }
        
    public void newText(String title, String text){
        
        if (gui!=null)
            gui.newText(title,text);
    }
 /**
     * Bring up the XmlEditor by showing the frame (only
     * applicable if coming up as an application, not an applet);
     */
    public void showXmlEditor() {
    if(!isApplet() && getFrame() != null) {
        // put swingset in a frame and show it
        JFrame f = getFrame();
        f.setTitle(getString("Pustefix Bubble Device"));
        f.getContentPane().add(getGui(), BorderLayout.CENTER);
        newText("test", "<Mal sehen,> \r <ob das=\"\" geht/> \r\n  noch eine \n <zeile \r\n bla \r bla >\n<!-- kommentar -->");
        f.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        getFrame().setLocation(
        screenSize.width/2 - f.getSize().width/2,
        screenSize.height/2 - f.getSize().height/2);
        getFrame().show();
    } 
    }
    
    public XmlEditor(XmlEditorApplet applet) {
    // Note that the applet may null if this is started as an application
    this.applet = applet;
      
    Log.init();
    //Log.disable();
    Props.init();
    if (applet!=null)
      Props.setProperty("applet","true");
    
    
    // setLayout(new BorderLayout());
//    setLayout(new BorderLayout());

    // set the preferred size of the demo
//    setPreferredSize(new Dimension(PREFERRED_WIDTH,PREFERRED_HEIGHT));

    // Create and throw the splash screen up. Since this will
    // physically throw bits on the screen, we need to do this
    // on the GUI thread using invokeLater.
//    createSplashScreen();

    // do the following on the gui thread
/*    SwingUtilities.invokeLater(new Runnable() {
        public void run() {
        showSplashScreen();
        }
    });
        
    initializeDemo();
    preloadFirstDemo();

    // Show the demo and take down the splash screen. Note that
    // we again must do this on the GUI thread using invokeLater.
    SwingUtilities.invokeLater(new Runnable() {
        public void run() {
        showSwingSet2();
        hideSplash();
        }
    });
*/

    showXmlEditor();
    
/*
    // Start loading the rest of the demo in the background
    DemoLoadThread demoLoader = new DemoLoadThread(this);
    demoLoader.start();
*/    

    }


    /**
     * XmlEditor Main. Called only if we're an application, not an applet.
     */
    public static void main(String[] args) {
    frame = createFrame();
    XmlEditor xmlEditor = new XmlEditor(null);
    }
    
}