package de.schlund.pfixeditor.view;

import java.awt.LayoutManager;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.*;
import javax.swing.JList;
import javax.swing.DefaultListModel;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;
import javax.swing.colorchooser.*;
import javax.swing.filechooser.*;
import javax.accessibility.*;

import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

import java.lang.reflect.*;
import java.awt.*;
import java.util.List;
import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.beans.*;
import java.util.*;
import java.io.*;
import java.applet.*;
import java.net.*;

import org.gjt.sp.jedit.textarea.*;
//import org.jext.PfixTextArea;
import org.jext.event.*;
//import org.jext.GUIUtilities;
import org.gjt.sp.jedit.syntax.SyntaxStyle;
import org.gjt.sp.jedit.syntax.Token;
import org.gjt.sp.jedit.syntax.HTMLTokenMarker;
import org.gjt.sp.jedit.syntax.SyntaxDocument;
import org.gjt.sp.jedit.gui.KeyEventWorkaround;
import de.schlund.pfixeditor.Props;
import de.schlund.pfixeditor.misc.Log;
import de.schlund.pfixeditor.xml.XmlError;
import de.schlund.pfixeditor.misc.Utilities;
import de.schlund.pfixeditor.JavaSupport;
import javax.swing.JList;


/**
 * @author rouven
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Gui extends JPanel {
    
    // The preferred size of the Gui
    private int PREFERRED_WIDTH = 680;
    private int PREFERRED_HEIGHT = 640;
    
    // Status Bar
    private JTextField statusField = null;

    // Tool Bar
    private ToggleButtonToolBar toolbar = null;
    private ButtonGroup toolbarGroup = new ButtonGroup();

    // Menus
    private JMenuBar menuBar = null;
    private JMenu themesMenu = null;
    private ButtonGroup lafMenuGroup = new ButtonGroup();
    private ButtonGroup themesMenuGroup = new ButtonGroup();

    // textarea
    
    private PfixTextArea xmlTextArea=null;  
    private JSplitPane splitPn=null;
    private Container parentContainer=null;
    private Container parent=null;
    
    private /*static*/ InputHandler inputHandler=null;
    private KeyListener keyEventInterceptor=null ;
    
    // messagelist
    
    private ParserMessagesList  msgList=null;
    private DummyScreen          dmScrn=null;
    
  class MouseHandler extends MouseAdapter
  {
    private PfixTextArea textarea=null;

    public MouseHandler(PfixTextArea textarea) {
        setTextArea(textarea);
    }
    
    public void setTextArea(PfixTextArea textarea){
        this.textarea=textarea;
    }
    
    public PfixTextArea getMyTextArea()
    {
      return textarea;   
    }
     /**
     * Invoked when the mouse enters a component.
     */
    public void mouseEntered(MouseEvent e) {
        Log.println("Gui:moueseEntered");
        getMyTextArea().setCaretBlinkEnabled(true);
    }

    /**
     * Invoked when the mouse exits a component.
     */
    public void mouseExited(MouseEvent e) {
        getMyTextArea().setCaretBlinkEnabled(false);
        Log.println("Gui:moueseExited");
    }
  }
 
    /**
     * Constructor for Gui.
     */
    public Gui(Container parent) {
        super();
        this.parentContainer=parent;
        init();
    }

    public Gui(Container parent, Container japp)
    {
        super();
        this.parent=japp;
        this.parentContainer=parent;
        init();
    }
    
    public Gui(Container parent, Container japp,int width, int height){
        super();
        this.setSize(width,height);
        this.parent=japp;
        this.parentContainer=parent;
        init();
    }
    
    public Container getParentObject()
    {
        return this.parent;
    }
    
    public Container getParentContainer() {
        return parentContainer;
    }
    
  /**
   * Returns the input handler.
   */

  public final InputHandler getInputHandler()
  {
    return inputHandler;
  }
  
  /**
   * Returns the listener that will handle all key events in this
   * view, if any.
   */

  public final KeyListener getKeyEventInterceptor()
  {
    return keyEventInterceptor;
  }

  // contains all the actions
  private static HashMap actionHash;
  
  public static MenuAction getAction(String action)
  {
    Object o = actionHash.get(action);
 //   if (o==null)
  
/*
    if (o == null)
      o = pythonActionHash.get(action);
*/
    return (MenuAction) o;
  }
  
  public void setNewText(String title, String text)
  {
    this.xmlTextArea.newText(title, text);
  }
  
  public String getText(String text)
  {
    return xmlTextArea.getText();
  }
 /**
  * Add an action listener to the list.
  * @param action The action listener
  */

  public void addAction(MenuAction action)
  {
    String name = action.getName();
    actionHash.put(name, action);
    String keyStroke = Props.getProperty(name.concat(".shortcut"));

    if (keyStroke != null)
      inputHandler.addKeyBinding(keyStroke, action);
  }
  
  private void initActions()
  {
    actionHash = new HashMap();
    inputHandler = new DefaultInputHandler();
    inputHandler.addDefaultKeyBindings();

    addAction(new Copy());
    addAction(new Paste());
    addAction(new Cut());
    addAction(new Undo());
    addAction(new Redo());
    addAction(new SelectAll());
    addAction(new Find());
    addAction(new Replace());
    addAction(new FindAll());
    addAction(new FindNext());
    addAction(new Parse());

    // key bindings

    addJextKeyBindings();
  }

  /**
   * Adds XmlEditor internal key bindings.
   */

  private void addJextKeyBindings()
  {
    inputHandler.addKeyBinding("CA+UP",           new ScrollUp());
    inputHandler.addKeyBinding("CA+PAGE_UP",      new ScrollPageUp());
    inputHandler.addKeyBinding("CA+DOWN",         new ScrollDown());
    inputHandler.addKeyBinding("CA+PAGE_DOWN",    new ScrollPageDown());
    inputHandler.addKeyBinding("C+UP",            new PrevLineIndent());
    inputHandler.addKeyBinding("C+DOWN",          new NextLineIndent());

    inputHandler.addKeyBinding("ENTER",           new IndentOnEnter());
    inputHandler.addKeyBinding("TAB",             new IndentOnTab());
    inputHandler.addKeyBinding("S+TAB",           new LeftIndent());

    inputHandler.addKeyBinding("C+INSERT",        getAction("copy"));
    inputHandler.addKeyBinding("S+INSERT",        getAction("paste"));
    inputHandler.addKeyBinding("S+DELETE",        getAction("cut"));
 
    inputHandler.addKeyBinding("CA+LEFT",         new CsWord(CsWord.NO_ACTION, TextUtilities.BACKWARD));
    inputHandler.addKeyBinding("CA+RIGHT",        new CsWord(CsWord.NO_ACTION, TextUtilities.FORWARD));
    inputHandler.addKeyBinding("CAS+LEFT",        new CsWord(CsWord.SELECT,    TextUtilities.BACKWARD));
    inputHandler.addKeyBinding("CAS+RIGHT",       new CsWord(CsWord.SELECT,    TextUtilities.FORWARD));
    inputHandler.addKeyBinding("CA+BACK_SPACE",   new CsWord(CsWord.DELETE,    TextUtilities.BACKWARD));
    inputHandler.addKeyBinding("CAS+BACK_SPACE",  new CsWord(CsWord.DELETE,    TextUtilities.FORWARD));
  

  }

public void newText(String title, String text) {
    try {
    getTextArea().newText(title,text);
    } catch (Exception e) {
        Log.println(e.toString());
    }
    
}

public void init() {
    //check keyEvent, only for debug purpose
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
          Log.println("gui: KeyEventListener: evt="+evtText+" char="+evt.getKeyChar()+" code="+evt.getKeyCode()+" text="+evt.getKeyText(evt.getKeyCode()));
          }        
        };
     this.addKeyListener(CheckKeys);   
    // setLayout(new BorderLayout());
    setLayout(new BorderLayout());

    // set the preferred size of the demo
    setPreferredSize(new Dimension(PREFERRED_WIDTH,PREFERRED_HEIGHT));

    // set default keys
    //inputHandler = new DefaultInputHandler();
    initActions();    
    xmlTextArea= this.createTextArea(); //new PfixTextArea(this);
    
    //xmlTextArea.newText("test","Mal sehen, ob dass zu sehen ist \n noch eine ");
    xmlTextArea.getDocument().setTokenMarker(new HTMLTokenMarker());
     
        
    JPanel top = new JPanel();
    top.setLayout(new BorderLayout());
    add(top, BorderLayout.NORTH);

    menuBar = createMenus();
    menuBar.addMouseListener(new MouseHandler(getTextArea()));
    top.add(menuBar, BorderLayout.NORTH);

    JPanel toolbarPanel = new JPanel();
    toolbarPanel.setLayout(new BorderLayout());
    toolbar = new ToggleButtonToolBar();
    toolbar.addMouseListener(new MouseHandler(getTextArea()));
    toolbarPanel.add(toolbar, BorderLayout.CENTER);
    top.add(toolbarPanel, BorderLayout.SOUTH);


    
   
    
 
 String testLabels[]={"erstes","zweites","drittes","viertes","fünftes"};
 // list for the parser messages

    msgList=new ParserMessagesList();
    msgList.setSelectedIndex(1);
    msgList.setVisible(true);
    msgList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    msgList.setVisibleRowCount(2);
    
    msgList.setGui(this);
  
    ListSelectionListener msgListSelListener=new ListSelectionListener(){
        public void valueChanged(ListSelectionEvent evt) {
           ParserMessagesList lst=(ParserMessagesList)evt.getSource();
           
         try {
              if (!lst.isSelectionEmpty())
               {
                  int ind=lst.getSelectedIndex();
                  ListModel lm=msgList.getModel();
                  if (lm!=null && (lm instanceof ParserMessageListModel)) {
                    ParserMessageListModel msgListModel=(ParserMessageListModel) lm;
                    XmlError xmlErr=msgListModel.getXmlError(ind);
                    int pos;
                    if(xmlErr!=null) {     
                        PfixTextArea textArea=lst.getGui().getTextArea();   
                        if (xmlErr.getOffset()!=-2){ 
                            Element curr=textArea.getDocument().getDefaultRootElement().getElement(xmlErr.getLine()-1);
                            int start=curr.getStartOffset();
                            int end  =curr.getEndOffset();
                            pos=start+(xmlErr.getColumn()-1);
                            while ((pos>end) && (pos>start))
                            {
                                pos--;
                            }
                            Log.println("List: parse-Error pos="+pos+" (current:"+textArea.getCaretPosition());
                        }
                        else { //EOF error
                            
                            Element root=textArea.getDocument().getDefaultRootElement();
                            Element curr=root.getElement(root.getElementCount()-1);
                            pos=curr.getEndOffset()-1;
                            Log.println("List: EOF-Error pos="+pos+" (current:"+textArea.getCaretPosition()+ " zeile:"+root.getElementCount()+")");
                        }
                        
                        try {
                            textArea.setCaretPosition(pos);
                        } catch (Exception e) {Log.println("new position failed");}
                        textArea.grabFocus();
                    }
                  }              
               }
               if (lst.getModel().getSize()==1)
                 lst.clearSelection();
        } catch (Exception e) {Log.println("new position failed");}
        
        }
    };
 
    msgList.addListSelectionListener(msgListSelListener); 
    msgList.addMouseListener(new MouseHandler(getTextArea()));
      
    JScrollPane scrollList=new JScrollPane(msgList);
    scrollList.addMouseListener(new MouseHandler(getTextArea()));
    splitPn=new JSplitPane(JSplitPane.VERTICAL_SPLIT,true,xmlTextArea,scrollList); 
    splitPn.addMouseListener(new MouseHandler(getTextArea()));
      
    //splitPn.
    splitPn.setOneTouchExpandable(true);
    splitPn.setVisible(true);
    splitPn.setResizeWeight(0.8);
    add(splitPn, BorderLayout.CENTER);
   
    splitPn.setDividerLocation(10000);
    Log.println("DividerLocation:"+splitPn.getDividerLocation());
    
    // end of splitpane with list and textarea
    
    JPanel statusBar = new JPanel();
    statusBar.setLayout(new BorderLayout());
    statusBar.add(BorderLayout.WEST, status);
    statusBar.add(BorderLayout.EAST, message);
    
    add(statusBar, BorderLayout.SOUTH);
    
    if (Utilities.JDK_VERSION.charAt(2) >= '4')
    {
      try
      {
        //Class cl = Class.forName("de.schlund.pfixeditor.JavaSupport");
        /*
        Class cl = de.schlund.pfixeditor.JavaSupport.class;
        if (cl==null) {
            Log.println("de.schlund.pfixeditor.JavaSupport not found");
        }
        Method m = cl.getMethod("initJavaSupport", new Class[0]);
        if (m !=  null)
          m.invoke(null, new Object[0]);
         */
        JavaSupport.initJavaSupport(); 
      } 

      catch (RuntimeException e) {
           Log.println("no java 1.4 support - reason: "+e.getMessage());
         }
    }
    
    message.setText(" *** Gui established ***");
    xmlTextArea.grabFocus();
    this.addMouseListener(new MouseHandler(this.getTextArea()));
    

    }

    
      /**
   * This method creates a new JMenuItem. See menus/XMenuHandler.class
   * for more informations about its usage.
   * @param label The menu item label
   * @param action The name of the action, specified in Jext
   * @param keyStroke The keystroke used as accelerator
   * @param picture Relative path to an icon
   * @param enabled Disable the item if false
   * @return A new <code>JMenuItem</code>
   */


//----------------------------------------------------------------------
  public JMenuItem loadMenuItem(String label, String action,
                                       String picture, boolean enabled)
//----------------------------------------------------------------------                                     
  {
    return loadMenuItem(label, action, picture, enabled, false);
  }

//----------------------------------------------------------------------
  public JMenuItem loadMenuItem(String label, String action)
//----------------------------------------------------------------------                                     
  {
    return loadMenuItem(label, action, null, true, false);
  }
  
 
    
  /**
   * This method creates a new JMenuItem. See menus/XMenuHandler.class
   * for more informations about its usage.
   * @param label The menu item label
   * @param action The name of the action, specified in Jext
   * @param keyStroke The keystroke used as accelerator
   * @param picture Relative path to an icon
   * @param enabled Disable the item if false
   * @param list If true adds item info to a list
   * @return A new <code>JMenuItem</code>
   */

//------------------------------------------------------------------------------------
  public JMenuItem loadMenuItem(String label, String action,
                                       String picture, boolean enabled, boolean list)
//------------------------------------------------------------------------------------                                       
  {
    String keyStroke = new String();

    if (label == null)
      return null;

    EnhancedMenuItem mi;
    int index = label.indexOf('$');

    if (action != null)
    {
      String _keyStroke = Props.getProperty(action.concat(".shortcut"));
      if (_keyStroke != null)
        keyStroke = _keyStroke;
    }

    if (index != -1 && label.length() - index > 1)
    {
      mi = new EnhancedMenuItem(label.substring(0, index).concat(label.substring(++index)),
                                keyStroke);
      mi.setMnemonic(Character.toLowerCase(label.charAt(index)));
    } else
      mi = new EnhancedMenuItem(label, keyStroke);
/*
    if (picture != null)
    {
      ImageIcon icon = Utilities.getIcon(picture.concat(Jext.getProperty("jext.look.icons")).concat(".gif"),
                       Jext.class);
      if (icon != null)
       mi.setIcon(icon);
    }
*/
    // if (keyStroke != null) mi.setAccelerator(parseKeyStroke(keyStroke));

    if (action != null)
    {
      Log.println("gui:loadmenutitem: action="+action);
      MenuAction a = getAction(action);
      if (a == null)
        mi.setEnabled(false);
      else
      {
        mi.addActionListener(a);
        mi.setEnabled(enabled);

        if (list)
        {
          StringBuffer _buf = new StringBuffer(label.length());
          char c;
          for (int i = 0; i < label.length(); i++)
          {
            if ((c = label.charAt(i)) != '$')
              _buf.append(c);
          }

/*
          if (action.startsWith("one_"))
            _buf.append(" (One Click!)");
*/
/*
          if (menuItemsActions.get(action) == null)
            menuItemsActions.put(action, _buf.toString());
*/
        }
      }
    } else
      mi.setEnabled(enabled);

    return mi;
  }
  
    
    /**
     * Creates a generic menu item
     */
    public JMenuItem createMenuItem(JMenu menu, String label, 
                   String accessibleDescription, String action) {
        //JMenuItem mi = (JMenuItem) menu.add(new JMenuItem(label));
        JMenuItem mi=loadMenuItem(label, action);
        mi.getAccessibleContext().setAccessibleDescription(accessibleDescription);
        menu.add(mi);
     
        if(action == null) {
          mi.setEnabled(false);
        }
        return mi;
    }

   
    /**
     * Create menus
     */
//---------------------------------
    public JMenuBar createMenus() {
//----------------------------------
    JMenuItem mi;
    // ***** create the menubar ****
    JMenuBar menuBar = new JMenuBar();
    //menuBar.getAccessibleContext().setAccessibleName(
    //    getString("MenuBar.accessible_description"));
    menuBar.getAccessibleContext().setAccessibleName("Pustefix Editor Menu");
    
    // ***** create Edit menu
    JMenu editMenu = (JMenu) menuBar.add(new JMenu("Edit"));
    editMenu.setMnemonic('E');
    editMenu.getAccessibleContext().setAccessibleDescription("Edit Menu");

    
    createMenuItem(editMenu,"$Undo",
                   "Placeholder menu item for undo last editing","undo");
                   
    createMenuItem(editMenu,"$Redo",
                   "Placeholder menu item for redo last editing","redo");
                   
    editMenu.addSeparator();
   
    createMenuItem(editMenu,"$Copy",
                   "Placeholder menu item for copy text","copy");
    createMenuItem(editMenu,"Cu$t",
                   "Placeholder menu item for cut text","cut");
    createMenuItem(editMenu,"$Paste",
                   "Placeholder menu item for paste text","paste");
                   
    editMenu.addSeparator();
   
    createMenuItem(editMenu,"Select $All",
                   "Placeholder menu item for select entire text","select_all");
    
    
    JMenu searchMenu = (JMenu) menuBar.add(new JMenu("Search"));
    searchMenu.setMnemonic('S');
    searchMenu.getAccessibleContext().setAccessibleDescription("Search Menu");
    
    createMenuItem(searchMenu,"$Find...",
                   "Placeholder menu item for find","find");
    createMenuItem(searchMenu,"Find $next",
                   "Placeholder menu item for find next","find_next");
    createMenuItem(searchMenu,"Find $All...",
                   "Placeholder menu item for find all","find_all");               
    searchMenu.addSeparator();
    createMenuItem(searchMenu,"$Replace...",
                   "Placeholder menu item for replace","replace");
    
    JMenu xmlMenu = (JMenu) menuBar.add(new JMenu("Xml"));
    xmlMenu.setMnemonic('X');
    xmlMenu.getAccessibleContext().setAccessibleDescription("Xml Menu");
    
    createMenuItem(xmlMenu,"check $wellformed",
                   "Placeholder menu item for undo last editing","parse");
                   
    return menuBar;
    }





    /**
     * A utility function that layers on top of the LookAndFeel's
     * isSupportedLookAndFeel() method. Returns true if the LookAndFeel
     * is supported. Returns false if the LookAndFeel is not supported
     * and/or if there is any kind of error checking if the LookAndFeel
     * is supported.
     *
     * The L&F menu will use this method to detemine whether the various
     * L&F options should be active or inactive.
     *
     */
     protected boolean isAvailableLookAndFeel(String laf) {
         try { 
             Class lnfClass = Class.forName(laf);
             LookAndFeel newLAF = (LookAndFeel)(lnfClass.newInstance());
             return newLAF.isSupportedLookAndFeel();
         } catch(Exception e) { // If ANYTHING weird happens, return false
             return false;
         }
     }

    /**
     * Returns the menubar
     */

    public JMenuBar getMenuBar() {
    return menuBar;
    }

    /**
     * Returns the toolbar
     */
    public ToggleButtonToolBar getToolBar() {
    return toolbar;
    }

    /**
     * Returns the toolbar button group
     */
    public ButtonGroup getToolBarGroup() {
    return toolbarGroup;
    }

    /**
     * Returns the content pane wether we're in an applet
     * or application
     */
    public Container getContentPane() {
 
      return parentContainer;
    }

  
    /**
     * Set the status 
     */
    public void setStatus(String s) {
    // do the following on the gui thread
    SwingUtilities.invokeLater(new XmlEditorRunnable(this, s) {
        public void run() {
        xmlEditor.statusField.setText((String) obj);
        }
    });
    }

    /**
     * Creates an icon from an image contained in the "images" directory.
     */
    public ImageIcon createImageIcon(String filename, String description) {
    String path = "/resources/images/" + filename;
    return new ImageIcon(getClass().getResource(path)); 
    }
 

   // *******************************************************
    // ******************   Runnables  ***********************
    // *******************************************************

    /**
     * Generic XmlEditor runnable. This is intended to run on the
     * AWT gui event thread so as not to muck things up by doing
     * gui work off the gui thread. Accepts a XmlEditor and an Object
     * as arguments, which gives subtypes of this class the two
     * "must haves" needed in most runnables for this demo.
     */
    class XmlEditorRunnable implements Runnable {
    protected Gui xmlEditor;
    protected Object obj;
    
    public XmlEditorRunnable(Gui xmlEditor, Object obj) {
        this.xmlEditor = xmlEditor;
        this.obj = obj;
    }

    public void run() {
    }
    }
    
    
    // *******************************************************
    // **************   ToggleButtonToolbar  *****************
    // *******************************************************
    static Insets zeroInsets = new Insets(1,1,1,1);
    protected class ToggleButtonToolBar extends JToolBar {
    public ToggleButtonToolBar() {
        super();
    }

    JToggleButton addToggleButton(Action a) {
        JToggleButton tb = new JToggleButton(
        (String)a.getValue(Action.NAME),
        (Icon)a.getValue(Action.SMALL_ICON)
        );
        tb.setMargin(zeroInsets);
        tb.setText(null);
        tb.setEnabled(a.isEnabled());
        tb.setToolTipText((String)a.getValue(Action.SHORT_DESCRIPTION));
        tb.setAction(a);
        ToggleButtonToolBar.this.add(tb);
        return tb;
    }
    }

  ////////////////////////////////////
  // PRIVATE FIELDS                 //
  ////////////////////////////////////
  private int _dividerSize;

  private JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
  private JSplitPane textAreaSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

  // splitted edition
  private PfixTextArea splittedTextArea;

  // labels
  private JLabel message = new JLabel();
  private JLabel status = new JLabel("v 1.0"+
                                     " - (C)1999-2001 Romain Guy - www.jext.org");

  // misc datas
  private int waitCount, batchMode;
  private ArrayList jextListeners = new ArrayList();
  private ArrayList transientItems = new ArrayList();
  private boolean transientSwitch;
 
  //////////////////////////////////////////////////////////////////////////////////////////////
  // PUBLIC AND PRIVATE NON-STATIC METHODS
  //////////////////////////////////////////////////////////////////////////////////////////////


  /**
   * Sets the listener that will handle all key events in this
   * view. For example, the complete word command uses this so
   * that all key events are passed to the word list popup while
   * it is visible.
   * @param comp The component
   */

  public void setKeyEventInterceptor(KeyListener listener)
  {
    this.keyEventInterceptor = listener;
  }

  /**
   * Records the componence of the GUI for later restauration.
   * Called after basic initialization of the GUI.
   */

  public void freeze()
  {
    // getJextMenuBar().freeze();
    transientSwitch = true;
    // getJextToolBar().freeze();
  }

  /**
   * Called after adding a new item to the GUI
   */

  public void itemAdded(Component comp)
  {
    if (transientSwitch == true)
      transientItems.add(comp);
  }

  /**
   * Restores the basic GUI.
   */

  public void reset()
  {
    // getJextToolBar().reset();
    for (int i = 0; i < transientItems.size(); i++)
    {
      Component comp = (Component) transientItems.get(i);
      if (comp != null)
      {
        Container parent = comp.getParent();
        if (parent != null)
          parent.remove(comp);
      }
    }

    //if (getJextMenuBar() != null)
    //  getJextMenuBar().reset();
  }

  /**
   * Sets the menu to be used as 'plugins'.
   * @param menu The <code>JMenu</code> used as plugins menu
   */

  public void setPluginsMenu(JMenu menu)
  {
   // pluginsMenu = menu;
  }

  /**
   * Fires a Jext event.
   * @param type Event type
   */

  public void fireJextEvent(int type)
  {
    
    JextEvent evt = new JextEvent(this, type);
    Iterator iterator = jextListeners.iterator();

    while (iterator.hasNext())
      ((JextListener) iterator.next()).jextEventFired(evt);
    
  }

  /**
   * Fires a Jext event.
   * @param textArea Related event text area
   * @param type Event type
   */

  public void fireJextEvent(PfixTextArea textArea, int type)
  {
    
    JextEvent evt = new JextEvent(this, textArea, type);
    Iterator iterator = jextListeners.iterator();

    while (iterator.hasNext())
      ((JextListener) iterator.next()).jextEventFired(evt);
    
  }

  /**
   * Removes all the listeners associated with this instance.
   */

  public void removeAllJextListeners()
  {
    jextListeners.clear();
  }

  /**
   * Adds a propertiesL listener to this class.
   * @param l The <code>JextListener</code> to add
   */

  public void addJextListener(JextListener l)
  {
    jextListeners.add(l);
  }

  /**
   * Remove a specified jext listener from the list.
   * @param l The listener to remove
   */

  public void removeJextListener(JextListener l)
  {
    jextListeners.remove(l);
  }

  /**
   * Load options states from the properties file.
   * Moreover, it sets up the corresponding internal
   * variables and menu items.
   */
  public void loadProperties()
  {
    //loadProperties(true);
  }

  public void loadProperties(boolean triggerPanes)
  {
  }

  public void loadButtonsProperties()
  {
  }


  /**
   * Load text area properties states from the properties file.
   * Should never been called directly.
   */

  public void loadTextAreaProperties()
  {

  }

  /**
   * Load a given text area properties from the user settings.
   * @param textArea The text area which has to be set
   */

  public void loadTextArea(PfixTextArea textArea)
  {
      textArea.setTabSize(8);

      textArea.setElectricScroll(0);

      textArea.setFontSize(12);

      textArea.setFontStyle(0);

      org.gjt.sp.jedit.textarea.TextAreaPainter painter = textArea.getPainter();
    
      painter.setLinesInterval(0);

      painter.setWrapGuideOffset(0);

      painter.setAntiAliasingEnabled(false);
      painter.setLineHighlightEnabled(true);
      painter.setEOLMarkersPainted(false);
      painter.setBlockCaretEnabled(false);
      painter.setLinesIntervalHighlightEnabled(false);
      painter.setWrapGuideEnabled(false);
      painter.setBracketHighlightColor(GUIUtilities.parseColor("#00ff00"));
      painter.setLineHighlightColor(GUIUtilities.parseColor("#e0e0e0"));
      painter.setEOLMarkerColor(GUIUtilities.parseColor("#009999"));
      painter.setCaretColor(GUIUtilities.parseColor("#ff0000"));
      painter.setSelectionColor(GUIUtilities.parseColor("#ccccff"));
      painter.setBackground(GUIUtilities.parseColor("#ffffff"));
      painter.setForeground(GUIUtilities.parseColor("#000000"));
      painter.setLinesIntervalHighlightColor(GUIUtilities.parseColor("#e6e6ff"));
      painter.setWrapGuideColor(GUIUtilities.parseColor("#ff0000"));

      loadGutter(textArea.getGutter());
      loadStyles(painter);

      if (textArea.isNew() && textArea.isEmpty())
        textArea.setColorizing("plain");
    
      textArea.setCaretBlinkEnabled(true);
      textArea.setParentTitle();
      textArea.repaint();
  }

  // loads the text area's gutter properties

  private void loadGutter(Gutter gutter)
  {
    gutter.setCollapsed(false);
    gutter.setGutterWidth(30);
    
    gutter.setLineNumberingEnabled(true);
    gutter.setHighlightInterval(5);
    gutter.setAntiAliasingEnabled(false);
    gutter.setBackground(GUIUtilities.parseColor("#ffffff"));
    gutter.setForeground(GUIUtilities.parseColor("#000000"));
    gutter.setHighlightedForeground(GUIUtilities.parseColor("#ff0000"));
    gutter.setCaretMark(GUIUtilities.parseColor("#00ff00"));
    gutter.setAnchorMark(GUIUtilities.parseColor("#ff0000"));
    gutter.setSelectionMark(GUIUtilities.parseColor("#0000ff"));
    gutter.setLineNumberAlignment(Gutter.RIGHT);
    gutter.setBorder(3, GUIUtilities.parseColor("#8080bd"));
    gutter.setFont(new Font("Monospaced", 0, 10));

  }

  // loads the syntax colorizing styles properties. This method
  // is called by loadTextArea() and exists only to separate the
  // code because loadTextArea() was becoming confusing

  private void loadStyles(TextAreaPainter painter)
  {
    try
    {
      SyntaxStyle[] styles = new SyntaxStyle[Token.ID_COUNT];
 /*
      styles[Token.COMMENT1] = GUIUtilities.parseStyle("color:#009900 style:i");
      styles[Token.COMMENT2] = GUIUtilities.parseStyle("color:#009900 style:i");
      styles[Token.KEYWORD1] = GUIUtilities.parseStyle("color:#0000ff style:b");
      styles[Token.KEYWORD2] = GUIUtilities.parseStyle("color:#ff9900");
      styles[Token.KEYWORD3] = GUIUtilities.parseStyle("color:#ff0000");
      styles[Token.LITERAL1] = GUIUtilities.parseStyle("color:#9999ff");
      styles[Token.LITERAL2] = GUIUtilities.parseStyle("color:#650099 style:b");
      styles[Token.OPERATOR] = GUIUtilities.parseStyle("color:#ffc800 style:b");
      styles[Token.INVALID] = GUIUtilities.parseStyle("color:#ff9900 style:b");
      styles[Token.LABEL] = GUIUtilities.parseStyle("color:#cc00cc style:b");
      styles[Token.METHOD] = GUIUtilities.parseStyle("color:#000000 style:b");
      painter.setStyles(styles);
  */
    } catch(Exception e) { }
  }


  /**
   * Update status label which displays informations about caret's position.
   * @param textArea The text area which caret status has to be updated
   */

  public void updateStatus(PfixTextArea textArea)
  {
    int off = textArea.getCaretPosition();

    Element map = textArea.getDocument().getDefaultRootElement();
    int currLine = map.getElementIndex(off);

    Element lineElement = map.getElement(currLine);
    int start = lineElement.getStartOffset();
    int end = lineElement.getEndOffset();
    int numLines = map.getElementCount();

    status.setText(new StringBuffer().append(' ').append(off - start + 1).append(':')
                   .append(end - start).append(" - ").append(currLine + 1).append('/')
                   .append(numLines).append(" - ").append(((currLine + 1) * 100) / numLines)
                   .append('%').toString());
  }

  /**
   * Display status of a given text area.
   * @param textArea The text area which status has to be displayed
   */

  public void setStatus(PfixTextArea textArea)
  {
  
    StringBuffer text = new StringBuffer();
    if (textArea.isEditable())
    {
      text.append(textArea.isDirty() ? "modified" : "");
    } else
      text.append("read only");

    if (textArea.oneClick != null)
    {
      if (text.length() > 0)
        text.append(" : ");
      text.append("one click!");
    }

    String _text = text.toString();
    if (_text.length() > 0)
      message.setText('(' + _text + ')');
    else
      message.setText("");
  }

  /**
   * Makes the given text area being considered as non modified.
   */

  public void resetStatus(PfixTextArea textArea)
  {
    textArea.clean();
    message.setText("");
    //textAreasPane.setCleanIcon(textArea);
  }

  /**
   * When the user create a new file, we need to reset some stuffs
   * such as the bottom labels and the tab icon.
   * @param textArea The text area which was cleared
   */

  public void setNew(PfixTextArea textArea)
  {
    message.setText(textArea.isEditable() ? "" : "read only");
    //textAreasPane.setCleanIcon(textArea);
    updateStatus(textArea);
  }

  /**
   * When the text change, we warn the user while displaying a text
   * in the lower right corner.
   * @param textArea The text area which was modified
   */

  public void setChanged(PfixTextArea textArea)
  {
    if (!textArea.isDirty())
    {
      textArea.setDirty();
      //textAreasPane.setDirtyIcon(textArea);
      setStatus(textArea);
    }
  }

  /**
   * When the user saves its text, we have to reset modifications done
   * by <code>setChanged()</code>.
   */

  public void setSaved(PfixTextArea textArea)
  {
    if (textArea.isDirty())
    {
      textArea.clean();
      //textAreasPane.setCleanIcon(textArea);
      message.setText("");
    }
  }

  /**
   * Close current window after having checked dirty state
   * of each opened file.
   */

  public void closeToQuit()
  {
    //new SaveDialog(this, SaveDialog.CLOSE_WINDOW);
    // workspaces.closeAllWorkspaces();
  }

  /**
   * Destroys current window and close JVM.
   */

  public void closeWindow()
  {
    closeWindow(true);
  }

  /**
   * Destroy current window and close JVM if necessary.
   * @param jvm If true, we terminate the JVM
   */

  public void closeWindow(boolean jvm)
  {
  }

  // helps GC to clean up memory a bit

  private void cleanMemory()
  {
    splittedTextArea = null;
    inputHandler = null;
    transientItems = jextListeners = null;
    keyEventInterceptor = null;

    System.gc();
  }

  /**
   * Check if content of text area has to be saved or not.
   * @return true if user want to close the area, false otherwise
   */

  public boolean checkContent(PfixTextArea textArea)
  {
    return true;
  }

  /**
   * Shows the wait cursor.
   */

  public void showWaitCursor()
  {
    if (waitCount++ == 0)
    {
      Cursor cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
      setCursor(cursor);
      PfixTextArea[] textAreas = getTextAreas();

      for (int i = 0; i < textAreas.length; i++)
        textAreas[i].getPainter().setCursor(cursor);
    }
  }

  /**
   * Hides the wait cursor.
   */

  public void hideWaitCursor()
  {
    if (waitCount > 0)
      waitCount--;

    if (waitCount == 0)
    {
      Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
      setCursor(cursor);
      cursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
      PfixTextArea[] textAreas = getTextAreas();

      for(int i = 0; i < textAreas.length; i++)
        textAreas[i].getPainter().setCursor(cursor);
    }
  }


  public JList getMsgList() {
    return msgList;  
  }
  
  /**
   * Returns current selected text area.
   */

  public PfixTextArea getTextArea()
  {
    if (splittedTextArea != null && splittedTextArea.hasFocus())
      return splittedTextArea;

    return getNSTextArea();
  }

  /**
   * Returns current selected text area, excluding the splitted area.
   */

  public PfixTextArea getNSTextArea()
  {
    return xmlTextArea;
    //return null;
  }

  /**
   * Returns an array containing all the text areas opened
   * in current window.
   */

  public PfixTextArea[] getTextAreas()
  {
    PfixTextArea[] areas = new PfixTextArea[1];
    areas[0]=xmlTextArea;
    return areas;
  }

  /**
   * Close a specified file and checks if file is dirty first.
   * @param textArea The file to close
   */

  public void close(PfixTextArea textArea)
  {
    close(textArea, true);
  }

  /**
   * Closes a specified file.
   * @param textArea The file to close
   * @param checkContent If true, Jext check if text area is dirty before saving
   */

  public void close(PfixTextArea textArea, boolean checkContent)
  {
    xmlTextArea.getPainter().setDropTarget(null);
  }

  /**
   * Close all the opened files.
   */

  public void closeAll()
  {
    close(xmlTextArea,false);
  }

  /**
   * Opens a file in a new tabbed pane. In case it is already opened, we ask user if
   * he wants to reload it or open it in a new pane.
   */

  public PfixTextArea open(String file)
  {
    return xmlTextArea;
    //return open(file, true);
  }


  // creates a new text area: it constructs it, gives it an
  // input handler, sets default document and finally loads
  // its properties from users settings

  private PfixTextArea createTextArea()
  {
    PfixTextArea textArea = new PfixTextArea(this);
    new DropTarget(textArea.getPainter(), new DnDHandler());
    textArea.setDocument(new SyntaxDocument());
    loadTextArea(textArea);

    return textArea;
  }


  /**
   * Change the name of specified text area.
   * @param textArea The area which name is to be changed
   * @param name The new title which will appear on the tab
   */

  public void setTextAreaName(PfixTextArea textArea, String name)
  {
    message.setText(name);
    //textAreasPane.setTitleAt(textAreasPane.indexOfComponent(textArea), name);
  }

  /**
   * Updates splitted text area to make it edit the same thing as the
   * selected text area.
   * @param textArea The text area which has to be linked with
   */

  public void updateSplittedTextArea(PfixTextArea textArea)
  {
    if (textAreaSplitter.getBottomComponent() == null || textArea == null)
      return;

    splittedTextArea.setDocument(textArea.getDocument());
    String mode = textArea.getColorizingMode();
    if (!mode.equals(splittedTextArea.getColorizingMode()))
      splittedTextArea.setColorizing(mode);
    splittedTextArea.discard();
    splittedTextArea.setEditable(textArea.isEditable());
  }

  /**
   * If user selects a tab containing something different from a text area,
   * we disable 'splitted' one.
   */

  public void disableSplittedTextArea()
  {
    if (textAreaSplitter.getBottomComponent() == null)
      return;

    splittedTextArea.setDocument(new SyntaxDocument());
    splittedTextArea.setEditable(false);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////
  // THE GUI
  //////////////////////////////////////////////////////////////////////////////////////////////


  private Component getFocusOwner()
  {
    if (parentContainer.hasFocus())
    {
        Component[] comps=parentContainer.getComponents();
        
        for (int i=0;i<parentContainer.getComponentCount();i++)
        {
            if (comps[i].hasFocus())
            {
                return comps[i];
            }
        }
    }
    return null;
  }

  /**
   * Sets the input handler.
   * @param inputHandler The new input handler
   */

  public void setInputHandler(InputHandler inputHandler)
  {
    this.inputHandler = inputHandler;
  }

  /**
   * Forwards key events directly to the input handler.
   * This is slightly faster than using a KeyListener
   * because some Swing overhead is avoided.
   */

  public void processKeyEvent(KeyEvent evt)
  {
    Log.println("GUI: processKeyEvent: id="+evt.getID()+" char="+evt.getKeyChar()+" code="+evt.getKeyCode()+" text="+evt.getKeyText(evt.getKeyCode()));
   
    if (getFocusOwner() instanceof JComponent)
    {
      JComponent comp = (JComponent) getFocusOwner();
      InputMap map = comp.getInputMap();
      ActionMap am = comp.getActionMap();

      if (map != null && am != null && comp.isEnabled())
      {
        Object binding = map.get(KeyStroke.getKeyStrokeForEvent(evt));
        if (binding != null && am.get(binding) != null)
          return;
      }
    }

    if (getFocusOwner() instanceof JTextComponent)
    {
      if (evt.getID() == KeyEvent.KEY_PRESSED)
      {
        switch (evt.getKeyCode())
        {
          case KeyEvent.VK_BACK_SPACE:
          case KeyEvent.VK_TAB:
          case KeyEvent.VK_ENTER:
            return;
        }
      }

      Keymap keymap = ((JTextComponent) getFocusOwner()).getKeymap();
      if (keymap.getAction(KeyStroke.getKeyStrokeForEvent(evt)) != null)
        return;
    }

    if (evt.isConsumed())
      return;

    evt = KeyEventWorkaround.processKeyEvent(evt);
    if (evt == null)
      return;

    switch (evt.getID())
    {
      case KeyEvent.KEY_TYPED:
        // Handled in text area
        if (keyEventInterceptor != null)
          keyEventInterceptor.keyTyped(evt);
        else if (inputHandler.isRepeatEnabled())
          inputHandler.keyTyped(evt);
        break;
      case KeyEvent.KEY_PRESSED:
        if (keyEventInterceptor != null)
          keyEventInterceptor.keyPressed(evt);
        else
          inputHandler.keyPressed(evt);
        break;
      case KeyEvent.KEY_RELEASED:
        if (keyEventInterceptor != null)
          keyEventInterceptor.keyReleased(evt);
        else
          inputHandler.keyReleased(evt);
        break;
    }

    if (!evt.isConsumed())
      super.processKeyEvent(evt); 
  }

  //////////////////////////////////////////////////////////////////////////////////////////////
  // INTERNAL CLASSES
  //////////////////////////////////////////////////////////////////////////////////////////////

  /** this is a fake frame, to ensure that the textarea get the focus (for applet view)
    * After receiving the focus, the frame calls the textareas requestFocus method.
    * and terminate itsself.
    * (see mousepressed)
   **/ 

  public class DummyScreen extends JFrame {
        private PfixTextArea txtAr=null;
        public DummyScreen(PfixTextArea ar){
          super();
          this.txtAr=ar;
          Log.println("start splashscreen frame");
          DummyScreen.this.setBackground(Color.lightGray);
          DummyScreen.this.getContentPane().setLayout(new BorderLayout());
          JPanel pane = new JPanel(new BorderLayout());
          pane.setFont(new Font("Monospaced", 0, 18));
          pane.add(BorderLayout.NORTH,
          new JLabel("Please wait ..."));
          DummyScreen.this.getContentPane().add(pane, BorderLayout.NORTH);   
          FocusListener CheckFocus=new FocusListener() {
            public void focusGained(FocusEvent evt) {
              printIt(" GAINED Focus");  
              try{
              DummyScreen.this.txtAr.requestFocus();
              }catch (Exception e) {}
              DummyScreen.this.setVisible(false);
              DummyScreen.this.dispose();
              
            }
            public void focusLost(FocusEvent evt) {
              printIt("LOST");  
            }
            private void printIt(String evtText) {
              Log.println("SplashScreen: FocusListener: evt="+evtText); 
            }
          };
          DummyScreen.this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
          
          WindowListener wl=new WindowListener() {
            public void windowActivated(WindowEvent e){
              Log.println("SplashScreen: activated");
              //SplashScreen.this.setVisible(false);
              //SplashScreen.this.dispose();
            }
            
            public void windowClosing(WindowEvent e){
            }
            
            public void windowDeactivated(WindowEvent e){
                Log.println("SplashScreen: deactivated");
            }
            
            public void windowIconified(WindowEvent e){
            }
            
            public void windowDeiconified(WindowEvent e){
            }
            
            public void windowOpened(WindowEvent e){
            }
            
            public void windowClosed(WindowEvent e){
            }
          }; 
          DummyScreen.this.addFocusListener(CheckFocus);
          DummyScreen.this.addWindowListener(wl);
          DummyScreen.this.pack();
          DummyScreen.this.setVisible(true);    
          DummyScreen.this.requestFocus();
        }
        
  }

  class WindowHandler extends WindowAdapter
  {
    public void windowClosing(WindowEvent evt)
    {
      closeToQuit();
    }
  }

  class DnDHandler implements DropTargetListener
  {
    public void dragEnter(DropTargetDragEvent evt) { }
    public void dragOver(DropTargetDragEvent evt) { }
    public void dragExit(DropTargetEvent evt) { }
    public void dragScroll(DropTargetDragEvent evt) { }
    public void dropActionChanged(DropTargetDragEvent evt) { }

    public void drop(DropTargetDropEvent evt)
    {
      DataFlavor[] flavors = evt.getCurrentDataFlavors();
      if (flavors == null)
        return;

      boolean dropCompleted = false;
      for (int i = flavors.length - 1; i >= 0; i--)
      {
        if (flavors[i].isFlavorJavaFileListType())
        {
          evt.acceptDrop(DnDConstants.ACTION_COPY);
          Transferable transferable = evt.getTransferable();
          try
          {
            Iterator iterator = ((List) transferable.getTransferData(flavors[i])).iterator();
            while (iterator.hasNext())
              open(((File) iterator.next()).getPath());
            dropCompleted = true;
          } catch (Exception e) { }
        }
      }
      evt.dropComplete(dropCompleted);
    }
  }

  /***************************************************************************
  Patch
     -> Memory management improvements : it may help the garbage collector.
     -> Author : Julien Ponge (julien@izforge.com)
     -> Date : 23, May 2001
  ***************************************************************************/
  protected void finalize() throws Throwable
  {
    super.finalize();

    // Note : some de-referenciations may have already been done in cleanMemory() ...

    Log.println("Gui: finalize");
    this.disposeDummyScreen();
    dmScrn=null;
    menuBar = null;

    xmlTextArea=null;  
    
    splitter = null;
    textAreaSplitter = null;

    splittedTextArea = null;

    message = null;
    status = null;

    jextListeners = null;
    transientItems = null;
    keyEventInterceptor = null;
    inputHandler = null;
    
  }
  // End of patch
    /**
     * Returns the splitPn.
     * @return JSplitPane
     */
    public JSplitPane getSplitPn() {
        return splitPn;
    }


    /**
     * Creates the DummyScreen.
     */
    public void createDummyScreen() {
        disposeDummyScreen();        
        dmScrn=new DummyScreen(this.getTextArea());
        dmScrn.requestFocus();
    }
    
    public void disposeDummyScreen() {
        if (dmScrn!=null){
            dmScrn.dispose();
            dmScrn=null;
        }
    }

}
