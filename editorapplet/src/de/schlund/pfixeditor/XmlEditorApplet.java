/**
 * @author rouven
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */

package de.schlund.pfixeditor;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import java.applet.*;
import javax.swing.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import de.schlund.pfixeditor.view.*;
import de.schlund.pfixeditor.JavaSupport.JextKeyboardFocusManager;
import de.schlund.pfixeditor.misc.Log;
import de.schlund.pfixeditor.misc.Utilities;
import org.jext.search.FindAllDialog;
import sun.awt.DebugHelper;
//import sun.plugin.viewer.frame.MNetscapeEmbeddedFrame;

public class XmlEditorApplet extends JApplet {
    private static final int BROWSER_NETSCAPE=1;
    private static final int BROWSER_EXPLORER=2;
    
    private Component rootComp=null;
    private String inittext="";
    private int currBrowser=0;
    
    
    private XmlEditor xmlEditor=null; //new XmlEditor(this);
    private Gui gui=null;
    private int newtxtcount=0;
    private static int sema=0;    
    //private SplashScreen splashScreen=null;  
    //public class CheckKeys extends KeyListener {
    //}
    class MouseHandler extends MouseAdapter
    {
     /**
     * Invoked when the mouse enters a component.
     */
      public void mouseEntered(MouseEvent e) {
        //System.err.println("applet:moueseEntered");
        Log.println("applet:moueseEntered");
        if (gui!=null) {
          gui.getTextArea().setCaretBlinkEnabled(true);
        }
        else {
            Log.println(" |gui=null");
        }
      }

    /**
     * Invoked when the mouse exits a component.
     */
      public void mouseExited(MouseEvent e) {
        Log.println("applet:moueseExited");
        if (gui!=null) {
        gui.getTextArea().setCaretBlinkEnabled(false);
        }
        else {
            Log.println(" |gui=null");
        }
        
      }
    }
    
  protected void processKeyEvent(KeyEvent evt)
  {
    super.processKeyEvent(evt);
    //JextFrame view = MenuAction.getJextParent(evt);
    Log.println("xmlEditorApplet: processKeyEvent: id="+evt.getID()+" char="+evt.getKeyChar()+" code="+evt.getKeyCode()+" text="+evt.getKeyText(evt.getKeyCode()));
  }
    
    private String decodeText(String txt){
      return URLDecoder.decode(txt); 
    /*
        int len=txt.length();
        char c;
        StringBuffer sb=new StringBuffer(len+(len>>1));
        System.err.println("Start of decode ("+len+" characters):");
        for (int i=0;i<len; i++) {
            c=txt.charAt(i);
            if (c=='|')
                //sb.append('"');
                c='"';
            else if (c=='^') 
                //sb.append('\n');
                c='\n';
            else if (c=='~')
                c='\'';
            else if (c=='!') {
               if (len>i+1) {
                   c=txt.charAt(i+1);
                   if ((c=='!') || (c=='|') || (c=='^') || (c=='~')) {
                       //sb.append(c);
                       i++;
                   }
                   else {
                       System.err.print(" decode error: pos"+i+" ->");
                       c=' ';
                   }
               }
               else {
                   System.err.println(" decode error: pos"+i+" ->");
                   c=' ';
               }
            }             
            sb.append(c);   
            System.err.print(c);
        }        
        System.err.println ("result of decodeParamText: ");
        System.err.println (sb);
        return sb.toString();  
       */
    }
    
    private String encodeText(String txt)
    {
       String ec=URLEncoder.encode(txt);
       Log.println(" encode text:\n"+ec);
       return ec;
    }
    
    private void reset()
    {
        inittext="";
        if (xmlEditor!=null) {
            inittext=gui.getTextArea().getText();
        }
        else {
          inittext="<xmlEditor:jetzt time=\"0:00:00\"> \r <!-- ist der Editor -->\r\n als Applet zu sehen.";
          String newText=getParameter("text");
          if (newText!=null)
            inittext=this.decodeText(newText);
        }   
        getContentPane().removeAll();
        getContentPane().setLayout(new BorderLayout());
        xmlEditor=new XmlEditor(this);
        gui=xmlEditor.getGui();
        getContentPane().add(gui, BorderLayout.CENTER);
        xmlEditor.newText("teschd",inittext);
        //System.err.println("------------ Init ----------");
        Log.println("text >"+inittext);
        
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
          Log.println("xmlEditorApplet: KeyListener: evt="+evtText+" char="+evt.getKeyChar()+" code="+evt.getKeyCode()+" text="+evt.getKeyText(evt.getKeyCode()));
          }        
        };
        this.addKeyListener(CheckKeys);
        
        FocusListener CheckFocus=new FocusListener() {
            public void focusGained(FocusEvent evt) {
              printIt("GAINED");  
            }
            public void focusLost(FocusEvent evt) {
              printIt("LOST");  
            }
            private void printIt(String evtText) {
              Log.println("pfixtextarea: FocusListener: evt="+evtText); 
            }
          };
          gui.getTextArea().addFocusListener(CheckFocus);
          
       FocusListener CheckFocus2=new FocusListener() {
            public void focusGained(FocusEvent evt) {
              printIt("GAINED");  
            }
            public void focusLost(FocusEvent evt) {
              printIt("LOST");  
            }
            private void printIt(String evtText) {
              Log.println("xmlEditorApplet: FocusListener: evt="+evtText); 
            }
          };
          this.addFocusListener(CheckFocus2);
    }
    
    public void init() {
      Log.setUseFrame(false);
      String paramLog=getParameter("log");
      if (paramLog!=null){
        Log.println("parameter log="+paramLog);
      }
      if (paramLog!=null && ((paramLog.equals("true")) || paramLog.equals("on"))){
        Log.enable();
      }else{
        Log.disable();
      }
      this.addMouseListener(new MouseHandler());
      Log.println("------------ Init ----------");
      //splashScreen=null;
      /*
      splashScreen = new SplashScreen();
      splashScreen.setVisible(true);
      splashScreen.requestFocus();
      */
      reset();  
      //splashScreen = new SplashScreen();
      gui.createDummyScreen();
      Enumeration en =this.getAppletContext().getApplets(); 
      int i=1;
      while (en.hasMoreElements()) {
        Log.println("applet "+i+": "+((Applet)en.nextElement()).getName());
        i++;
      }
        Component c=this;
        Component p=this.getParent();
        Log.println("--- parents -------");
        while (p!=null){
            Log.println(p.toString()+" is parent of "+c.toString());
            c=p;
            p=p.getParent();
        }
        rootComp=c;
        /*
        if (c instanceof MNetscapeEmbeddedFrame){
            MNetscapeEmbeddedFrame nsFrame=(MNetscapeEmbeddedFrame)c;
            
            FocusListener CheckFocus3=new FocusListener() {
            public void focusGained(FocusEvent evt) {
              printIt("GAINED");  
            }
            public void focusLost(FocusEvent evt) {
              printIt("LOST");  
            }
            private void printIt(String evtText) {
              Log.println("NetscapeEmbeddedFrame: FocusListener: evt="+evtText); 
            }
            };
            
            nsFrame.addFocusListener(CheckFocus3);
            if (nsFrame.getFocusOwner()!=null){
            Log.println("nsframe focusowner:"+nsFrame.getFocusOwner().toString());
            }
            
            //nsFrame.isActive();
            Log.println("embedded frame: "+nsFrame.toString());
            nsFrame.requestFocusInWindow();
            nsFrame.requestFocus();
            nsFrame.toFront();
           if (Utilities.JDK_VERSION.charAt(2) >= '4')
           {
             try
             {
               JavaSupport.JextKeyboardFocusManager kfm=(JextKeyboardFocusManager) KeyboardFocusManager.getCurrentKeyboardFocusManager();
               kfm.activateFrame(nsFrame);
             } 

           catch (Exception e) {
             Log.println("no java 1.4 support - reason: "+e.getMessage());
           }
        }
        }
      */  
      this.requestFocus();
      this.requestFocusInWindow();
      this.gui.getTextArea().requestFocus();
      this.gui.getTextArea().requestFocusInWindow();
      
      try{
        String objStr=rootComp.toString().toLowerCase();
        if (objStr.indexOf("netscape")>=0){
          Log.println("Typ of Browser: Netscape");
          currBrowser=BROWSER_NETSCAPE;
          this.setSize(rootComp.getWidth(),rootComp.getHeight());
        }
        else if (objStr.indexOf("explorer")>=0){
            currBrowser=BROWSER_EXPLORER;
            Log.println("Typ of Browser: Explorer");
        }
        
      }catch (Exception e) {}
    }
    
    
    public void start()
    {   
        /*
        if(splashScreen!=null){
          splashScreen.setVisible(false);
          splashScreen.dispose();
          splashScreen=null;
        }
        */
        Log.println("---------- start ----------");
        //reset();
        if (gui!=null)
        {          
            gui.getTextArea().setFocusable(true);
            gui.getTextArea().setVisible(true);
            gui.getTextArea().setEditable(true);
            gui.getTextArea().startCaretTimer();    
            gui.getTextArea().resetKeyboardActions();
            gui.getTextArea().grabFocus();
            gui.getTextArea().requestFocusInWindow();
            gui.getTextArea().enable();
            
            
        }
       
    }
    
    public void stop()
    {
        Log.println("----------- stop -----------");
        gui.getTextArea().stopCaretTimer();
        this.requestFocus();
        this.requestFocusInWindow();        
    }
    
    public void destroy()
    {
        this.requestFocus();
        this.requestFocusInWindow();
        
        Log.println("----------- destroy -----------");
       //gui.getTextArea().getFocusListeners().;
        gui.getTextArea().removeAll();
       // gui.removeAllJextListeners();
        gui.removeAll();
        
        //gui.getTextArea().enable();
        gui.getTextArea().setFocusable(false);
        gui.getTextArea().setVisible(false);
        gui.getTextArea().setEditable(false);
        gui.setFocusable(false);
        this.setFocusable(false);
        gui=null;
        this.xmlEditor=null;
        this.removeAll();
        System.runFinalization();
        System.gc();
        
    }

    public URL getURL(String filename) {
        URL codeBase = this.getCodeBase();
        URL url = null;
    
        try {
            url = new URL(codeBase, filename);
        System.out.println(url);
        } catch (java.net.MalformedURLException e) {
            System.out.println("Error: badly specified URL");
            return null;
        }

        return url;
    }

    
    public void setNewText(String title,String text) {
        if (newtxtcount==0) {
          //gui.setNewText(title,text);
          xmlEditor.newText(title,text);
          newtxtcount++;
        }
      
    }
    
    public void setNewText(String text)
    {
        this.setNewText("",text);
    }
    
    public boolean isValid() {
        
      return gui.getTextArea().parse();      
    }
  
  public String getText()
  {
    String txt=gui.getTextArea().getText();
    if (txt!=null)
      return this.encodeText(txt);
    else
      return "";
  }
  
  public void setIESize(int width, int height){
    rootComp.setSize(width,height);
    Component c=this.getParent();
    while (c!=null){
     try{
        c.setSize(width,height);
        c=c.getParent();
     }catch (Exception e){}; 
    }
    if (gui!=null){
        gui.getTextArea().setSize(gui.getTextArea().getWidth()-21,gui.getTextArea().getWidth());    
    }
    super.setSize(width,height);
  }
  
  public synchronized void setSize(int width, int height){
    //correction of the width for browser rendering 
    width=width-21;
    if (width<=0)
      width=1;
    try{
    rootComp.setSize(width,height);
    }catch (Exception e){}
    
    Component c=this.getParent();
  /*
    while (c!=null){
  */
    try{
      c.setSize(width,height);
      c=c.getParent();
    }catch (Exception e){};
  /*
    }
  */
    super.setSize(width,height);
    
  /*  
    Log.println("Applet.setSize: width="+width+" height="+height);
    Log.println("Applet.getSize: width="+this.getWidth()+" height="+this.getHeight());
    Log.println("gui.getSize   : width="+gui.getWidth()+" height="+gui.getHeight());
    */
  }
  
  public void paint(Graphics g){
    super.paint(g);
    
    Log.println("Applet: paint");
    Log.println("--------------------------");
    Log.println("TextArea          : width:"+gui.getTextArea().getWidth()+" height:"+gui.getTextArea().getHeight());
    Log.println("Gui               : width:"+gui.getWidth()+" height:"+gui.getHeight());
    Log.println("Applet.contentPane: width:"+this.getContentPane().getWidth()+" hieght:"+this.getContentPane().getHeight());
    Log.println("Applet            : width:"+this.getWidth()+" height:"+this.getHeight());
    Log.println("Appletviewer      : width:"+this.getParent().getWidth()+"height:"+this.getParent().getHeight());
    Log.println("rootComp          : width:"+rootComp.getWidth()+" height:"+rootComp.getHeight());
   
  }
}
