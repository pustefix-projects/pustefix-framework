/**
 * @author rouven
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */

package de.schlund.pfixeditor.misc;
import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JFrame;

import java.util.*;
import java.io.*;
import java.net.URL;
import java.io.IOException;

public class Log extends Object{


  final static String NL ="\n";

  private static boolean isLog=true;
  private static boolean useFrame=true;
  private static Frame logFrame=null;
  private static TextArea logOut=null;
  private JFrame logJFrame=null;

  public Log() {
    init();   
    System.err.println("Log constructed");
  }

  public static void init()
  {
    /*
    if (logFrame==null) {
      logFrame=new Frame();
      logFrame.setTitle("Log");
      Panel p;
      logFrame.add("North", p = new Panel() );
      p.add(logOut = new TextArea("This is the DebugFrame\n",40,40));
      //p.setSize(new Dimension(200, 75));
    
      logOut.setEditable(false);    
      
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints c = new GridBagConstraints();
      
      p.setLayout(gridbag);
      c.gridx = 0;
      c.gridy = 0;
      c.weightx =1.0;
      c.weighty =0.0;
      c.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(logOut, c);
      
    }
    */
  }

  public static void enable ()
  {
    init();
    isLog=true;
    System.err.println("Logging activated");
    if(useFrame){
    if(logFrame!=null){
      logFrame.dispose();
      logFrame=null;
    }
    logFrame=new Frame();
    
    logFrame.setTitle("Log");
    Panel p;
    logFrame.add("North", p = new Panel() );
    p.add(logOut = new TextArea("This is the DebugFrame\n",40,40));
    p.setSize(new Dimension(150, 75));
    logOut.setEditable(false);    
     
    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
      
    p.setLayout(gridbag);
    c.gridx = 0;
    c.gridy = 0;
    c.weightx =1.0;
    c.weighty =0.0;
    c.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(logOut, c);
    logFrame.pack();      
    logFrame.show();
    }
  }
  
  public static void show()
  {
    init();
    if (isLog)
    {
//      logFrame.pack();
//      logFrame.setVisible(true);
//    logFrame.show();
    }
  }
 
  public static void setUseFrame(boolean val){
    useFrame=val;
  } 
  
  public static void disable ()
  {
    init();
    System.err.println("Logging deactivated");
    isLog=false;
    if(logFrame!=null){
      logFrame.dispose();
      logFrame=null;
    }
//    logFrame.setVisible(false);
  }
  
  public static void print(String text)
  {
    if(isLog==true)
    {
      try{
          System.err.print(text);
          if (useFrame){
            logOut.append(text);
            logOut.setCaretPosition(logOut.getText().length());
          }
      }catch (Exception e){};
    }
  }
  
  public static void println(String line)
  {
    if ((isLog==true)) {
      try{
        System.err.println(line);
        if (useFrame){
          if (logOut.getRows()>80)
            logOut.setText("");
          logOut.append(line+NL);
          logOut.setCaretPosition(logOut.getText().length());
        }
      } catch (Exception e){};
    }
  }
  
}