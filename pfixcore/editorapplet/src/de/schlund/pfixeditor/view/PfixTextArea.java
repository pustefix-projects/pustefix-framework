package de.schlund.pfixeditor.view;

import gnu.regexp.*;

import java.lang.reflect.Method;
import java.lang.StringBuffer;
import java.io.*;

import java.util.StringTokenizer;
import java.util.zip.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;

import javax.swing.undo.*;
import javax.swing.event.*;
import javax.swing.JPopupMenu;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import javax.swing.text.Element;
import javax.swing.text.Segment;
import javax.swing.text.Position;
import javax.swing.text.PlainDocument;
import javax.swing.text.BadLocationException;

import org.jext.event.JextEvent;
//import org.jext.misc.Workspaces;
//import org.jext.misc.ZipExplorer;
import org.jext.search.*;
//import org.jext.xml.XPopupReader;

import org.jext.*;

import org.gjt.sp.jedit.syntax.*;
import org.gjt.sp.jedit.textarea.*;


import org.gjt.sp.jedit.textarea.JEditTextArea;
import de.schlund.pfixeditor.Props;
import de.schlund.pfixeditor.misc.Utilities;
import de.schlund.pfixeditor.misc.Log;

//for function parse()
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.text.Element;
import java.util.Iterator;
import de.schlund.pfixeditor.xml.XmlTextProcessor;
import de.schlund.pfixeditor.xml.XmlError;
import java.util.ArrayList;
import de.schlund.pfixeditor.view.Gui;
import de.schlund.pfixeditor.view.ParserMessageListModel;
import de.schlund.pfixeditor.view.ParserMessagesList;

/**
 * Extending JEditTextArea allow us to support syntax colorization. We also implement
 * some listeners: for the caret, for the undos and redos, for the keys (indent)
 * and for the modifications which can occures in the text. This component provides
 * its own methods to read and save files (even to zip them).
 * @autor Romain Guy
 * @see Jext
 * 
 * this is the modified JextTextArea. Some not used methods are removed to downsize the code
 * 
 */

public class PfixTextArea extends JEditTextArea implements UndoableEditListener, DocumentListener
{
  // static fields
  private static JPopupMenu popupMenu;
  
  // private fields
  private Gui parent;

  // misc properties
  private String mode="html";
  private long modTime;
  private Position anchor;
  private int fontSize, fontStyle;
  private String fontName, currentFile;

  // undo
  private boolean undoing;
  private UndoManager undo = new UndoManager();
  private boolean dirty, newf, operation, protectedCompoundEdit;
  private CompoundEdit compoundEdit, currentEdit = new CompoundEdit();

  // highlighters
  private SearchHighlight searchHighlight;
  private ParseHighlight  parseHighlight;

  /** This constant defines the size of the buffer used to read files */
  public static final int BUFFER_SIZE = 32768;
  
  /**
   * The constructor add the necessary listeners, set some stuffs
   * (caret color, borers, fonts...).
   * @param parent <code>PfixTextArea</code> needs a <code>Jext</code> parent
   * because it provides a lot of 'vital' methods
   */

  public PfixTextArea(Gui parent)
  {
    super(parent);

    addCaretListener(new CaretHandler());
    addFocusListener(new FocusHandler());
    setMouseWheel();

    undo.setLimit(1000);

    setBorder(null);
    getPainter().setInvalidLinesPainted(false);

    this.parent = parent;
    //Font defaultFont = new Font("Monospaced", Font.PLAIN, 12);
    Font defaultFont = new Font("Monospaced", Font.PLAIN, 12);
    fontName = defaultFont.getName();
    fontSize = defaultFont.getSize();
    fontStyle = defaultFont.getStyle();
    setFont(defaultFont);

    modTime = -1;
    
    newf = true;
    setTabSize(8);

    FontMetrics fm = getFontMetrics(getFont());
    setMinimumSize(new Dimension(40 * fm.charWidth('m'), 5 * fm.getHeight()));
    setPreferredSize(new Dimension(80 * fm.charWidth('m'), 15 * fm.getHeight()));

    mode = ""; //Jext.getProperty("editor.colorize.mode");
  }

  private void setMouseWheel()
  {
    if (Utilities.JDK_VERSION.charAt(2) >= '4')
    {
      try
      {
        Class cl = Class.forName("de.schlund.pfixeditor.JavaSupport");
        Method m = cl.getMethod("setMouseWheel", new Class[] { getClass() });
        if (m !=  null)
        m.invoke(null, new Object[] { this });
      } catch (Exception e) { }
    }
  }

  /**
   * Adds a search highlighter if none exists.
   */

  public void initSearchHighlight()
  {
    if (searchHighlight == null)
    {
      searchHighlight = new SearchHighlight();
      getPainter().addCustomHighlight(searchHighlight);
    }
  }

  /**
   * Returns the associated search highlighter.
   */

  public SearchHighlight getSearchHighlight()
  {
    return searchHighlight;
  }

/**
   * Adds a search highlighter if none exists.
   */

  public void initParseHighlight()
  {
    if (parseHighlight == null)
    {
      parseHighlight = new ParseHighlight();
      getPainter().addCustomHighlight(parseHighlight);
    }
  }

  /**
   * Returns the associated search highlighter.
   */

  public ParseHighlight getParseHighlight()
  {
    return parseHighlight;
  }

  /**
   * Returns text area popup menu.
   */

  public static JPopupMenu getPopupMenu()
  {
    return popupMenu;
  }

  /**
   * Get property inherent to current syntax colorizing mode.
   */

  public String getProperty(String key)
  {
     Log.println("Try to get property  mode.html." + key);
     return Props.getProperty("mode.html"+mode+'.'+key);
  }

  /**
   * Set a new document
   */

  public void setDocument(org.gjt.sp.jedit.syntax.SyntaxDocument document)
  {
    document.removeUndoableEditListener(this);
    document.removeDocumentListener(this);
    super.setDocument(document);
    document.addDocumentListener(this);
    document.addUndoableEditListener(this);
  }

  /**
   * Return current font's name
   */

  public String getFontName()
  {
    return fontName;
  }

  /**
   * Return current font's size
   */

  public int getFontSize()
  {
    return fontSize;
  }

  /**
   * Return current font's style (bold, italic...)
   */

  public int getFontStyle()
  {
    return fontStyle;
  }

  /**
   * Set the font which has to be used.
   * @param name The name of the font
   */

  public void setFontName(String name)
  {
    fontName = name;
    changeFont();
  }

  /**
   * Set the size of the font.
   * @param size The new font's size
   */

  public void setFontSize(int size)
  {
    fontSize = size;
    changeFont();
    FontMetrics fm = getFontMetrics(getFont());
    setMinimumSize(new Dimension(80 * fm.charWidth('m'), 5 * fm.getHeight()));
    repaint();
  }

  /**
   * Set the style of the font.
   * @param style The new style to apply
   */

  public void setFontStyle(int style)
  {
    fontStyle = style;
    changeFont();
    repaint();
  }

  /**
   * Set the new font.
   */

  private void changeFont()
  {
    getPainter().setFont(new Font(fontName, fontStyle, fontSize));
  }

  /**
   * Show/hide waiting cursor
   */

  public void waitingCursor(boolean on)
  {
    if (on)
    {
      parent.showWaitCursor();
    } else {
      parent.hideWaitCursor();
    }
  }

  /**
   * This is necessary to determine if we have to indent on tab key press or not.
   */

  public static boolean getTabIndent()
  {
    return Props.getBooleanProperty("editor.tabIndent");
  }

  /**
   * This is necessary to determine if we have to indent on enter key press or not.
   */

  public static boolean getEnterIndent()
  {
    return Props.getBooleanProperty("editor.enterIndent");
  }

  /**
   * Return the state of the softtab check menu item.
   * This is necessary to know if tabs have to be replaced
   * by whitespaces.
   */

  public static boolean getSoftTab()
  {
    return Props.getBooleanProperty("editor.softTab");
  }

  /**
   * When an operation has began, setChanged() cannot be called.
   * This is very important when we need to insert or remove some
   * parts of the texte without turning on the 'to_be_saved' flag.
   */

  public void beginOperation()
  {
    operation = true;
    waitingCursor(true);
  }

  /**
   * Calling this will allow the DocumentListener to use setChanged().
   */

  public void endOperation()
  {
    operation = false;
    waitingCursor(false);
  }

  /**
   * Return the parent of this component. Note that a LOT of
   * external functions need to call methods contained in the parent.
   */

  public Gui getPfixParent()
  {
    return parent;
  }

  /**
   * Return true if we can use the setChanged() method,
   * false otherwise.
   */

  public boolean getOperation()
  {
    return operation;
  }

  /**
   * Return current opened file as a <code>File</code> object.
   */

  public File getFile()
  {
    return (currentFile == null ? null : new File(currentFile));
  }

  /**
   * Return the full path of the opened file.
   */

  public String getCurrentFile()
  {
    return currentFile;
  }

  /**
   * Set path of current opened file.
   */

  public void setCurrentFile(String path)
  {
    currentFile = path;
  }

  /**
    * Places the selected text into the clipboard, if we are an application
    * or a signed applet.
    * Otherwise, the text will be placed in an local String
    */
  private static String locClipboard=null;
  
  public void copy()
  {
    
    //public void init() { // Obtain a reference to the system clipboard clipboard = getToolkit().getSystemClipboard(); copyFrom = new TextField(20); copyTo = new TextArea(3, 20); copy = new Button( Copy To System Clipboard ); paste = new Button( Paste From System Clipboard ); add(copyFrom); add(copy); add(paste); add(copyTo); copy.addActionListener (new CopyListener()); paste.addActionListener(new PasteListener()); } class CopyListener implements ActionListener { public void actionPerformed(ActionEvent event) { // Wrap the data in a transferable object StringSelection contents = new StringSelection(copyFrom.getText()); // Place the transferable onto the clipboard clipboard.setContents(contents, ClipboardTest.this); } } class PasteListener implements ActionListener { public void actionPerformed(ActionEvent event) { Transferable contents = clipboard.getContents(this); // Determine if data is available in string flavor if(contents != null && contents.isDataFlavorSupported( DataFlavor.stringFlavor)) { try { String string;
    try {
      super.copy();
    }catch (Exception e) {
    if (Props.getBooleanProperty("applet"))
    {
        if (selectionStart == selectionEnd)
        {
          int line = getCaretLine();
          int start = getLineStartOffset(line);
          int end = getLineEndOffset(line);

          locClipboard = getText(start, end - start);
          setSelectionStart(start);
          setSelectionEnd(start);
        } else
          locClipboard = getSelectedText();
    }
    }
  }

 /**
    * Deletes the selected text from the text area and places it
    * into the clipboard. If we are an unsigned applet, 
    * we place it in a local clipboard
    */
  public void cut()
  {
    try{
        super.cut();
    } catch (Exception e) {
    if (Props.getBooleanProperty("applet") && (editable))
    {
        copy();
        if (selectionStart == selectionEnd) {
            int line = getCaretLine();
            int start = getLineStartOffset(line);
            int end = getLineEndOffset(line);

            if (end == document.getLength() + 1)
                end--;

            try {
                document.remove(start, end - start);
            } catch (BadLocationException ble) {}
        } else
            setSelectedText("");
    }
    }
  }

private final boolean isWhitespace (char c)
  {
    switch ((int)c) {
    case 0x20:
    case 0x09:
    case 0x0d:
    case 0x0a:
      return true;
    default:
      return false;
    }
  }
  
public boolean parse()
{
    boolean result=false;
    
    ArrayList xmlErrors=null;
    Utilities.setCursorOnWait((Component)this,true);
    Gui gui=(Gui)this.getPfixParent();
    XmlError xErr=null;
    ParserMessageListModel errList=null;
    String[] messages=null;
    this.setEditable(false);
    
    ParserMessagesList msgList=(ParserMessagesList)gui.getMsgList();
    if (msgList!=null)
    {
        msgList.disable();
        ListModel lm=msgList.getModel();
        if (lm!=null && (lm instanceof ParserMessageListModel)) {
            errList=(ParserMessageListModel) lm;
            errList.clearMessages();
        }
        else
        {
            errList=new ParserMessageListModel(xmlErrors);
            msgList.setModel(errList);
        }   
    }
    else {
        errList=new ParserMessageListModel(xmlErrors);
        msgList=new ParserMessagesList (errList);
        msgList.setGui(gui);
    }    
    XmlTextProcessor xmlProc=new XmlTextProcessor();
    result=xmlProc.parse("<foo>"+this.getText()+"</foo>");
    xmlErrors=xmlProc.getXmlErrors(); 
    if (xmlErrors==null) {
       errList.clearMessages();
       errList.addElement("      Code is wellformed XML");
    
       ParseHighlight h = this.getParseHighlight();
       if (h != null)
       {
         h.trigger(false);
         h.setErrors(null);
       }
       gui.getSplitPn().setDividerLocation(0.95); 
    }
    else {   
        Iterator it=xmlErrors.iterator();
        while (it.hasNext())
        {
            xErr=(XmlError) it.next();
            Log.println("Error at line "+xErr.getLine()+", column "+xErr.getColumn()
                        +", offset:"+xErr.getOffset()+":");
            Log.println("      "+xErr.getMessage());
            //textArea.set
            //evaluate position in document
            //Element line=this.getDocument().getDefaultRootElement().getElement(xErr.getLine()-1);
            //line.getDocument().getText()
            if (xErr.getOffset()!=-2){ 
                int offs=xErr.getColumn();
                int lin=xErr.getLine();
                //int start,line;
                Element line=this.getDocument().getDefaultRootElement().getElement(lin-1);
                int start=line.getStartOffset();
                int end  =line.getEndOffset();
                while (offs<=0) {
                    if (lin>1) {
                        lin--;
                        line=this.getDocument().getDefaultRootElement().getElement(lin-1);
                        start=line.getStartOffset();
                        end=line.getEndOffset();
                        offs=end-start;
                    }else {
                       offs=0;
                       break;
                    }
                }
                xErr.setColumn(offs);
                xErr.setLine(lin);
                int pos  =start+offs;
                
                //Log.println("Error Line ("+xErr.getLine()+"):"+text+"|");
            
                boolean haveTokenStart=false;
                boolean haveTokenEnd=false;
                int fwd=offs;
                int bkw=offs--;
                Log.println("startOffset="+start+" endOffset="+end+" pos="+pos+" offs="+offs);
                //while (!(haveTokenStart && haveTokenEnd)){
                String text="";
                try{
                  text=line.getDocument().getText(start,end-start);
                }catch (Exception e) {}
                int len=text.length();
                xErr.setTokenStart(-1);
                xErr.setTokenEnd(-1);
                try {
                  Log.print("Looking for end of token:");
                  while (fwd<=len && !haveTokenEnd) {
                    Log.print(""+text.charAt(fwd));
                    if (!isWhitespace(text.charAt(fwd)))
                      fwd++;
                    else {   
                      haveTokenEnd=true;
                    }
                  }
                  if (fwd>0)
                    fwd--;
                  xErr.setTokenEnd(fwd+start);  
                   
                  Log.println("");
                  Log.print("Looking for start of token:");     
                  while (bkw>=0 && !haveTokenStart) {
                    Log.print(""+text.charAt(bkw));
                    if (! isWhitespace(text.charAt(bkw)))
                      bkw--;
                    else {   
                      haveTokenStart=true;
                    }
                  }
                  if (bkw<len)
                    bkw++;
                  xErr.setTokenStart(bkw+start);
                }catch (Exception e){Log.println("no token:"+e.getMessage());}
                
                /*try{
                  text=line.getDocument().getText(start,end-start);
                }catch (Exception e) {}*/
                Log.println("Error Line ("+xErr.getLine()+") - Token "+(bkw)+","+(fwd)+":"+text+"|");
             }
             else { //EOF error                                   
                Log.println("Error: EOF");
             }
           
        } 
        Element map = this.getDocument().getDefaultRootElement(); 
        Log.println("Elements of document:");
        printElement(map,0,1);
        errList.addMessages(xmlErrors);   
        
        gui.getSplitPn().setDividerLocation(0.8);
    /*    
        this.initParseHighlight();
        ParseHighlight h = this.getParseHighlight();
        h.trigger(true);
        h.setErrors(xmlErrors);
    */
 
    }
    
    this.setEditable(true);
    msgList.setVisible(true);
    msgList.enable();
    msgList.validate();
    msgList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    Utilities.setCursorOnWait((Component)this,false);
    grabFocus();
    this.repaint();
    return result;  
} 
         


private void printElement(Element elem, int deep, int count)
{
  int i;
  Log.println("Element deep:"+deep+" count:"+count+"= "+elem.getName());
  for (i=1;i<=elem.getElementCount();i++)
    printElement(elem.getElement(i-1),deep+1,i);
}

public void paste()
{
    try{
        super.paste();
    }catch (Exception e){
        
        if(Props.getBooleanProperty("applet") && (editable))
        {
          if ((locClipboard!=null) && (locClipboard.length()>0))
          {
            
            try {setSelectedText (locClipboard);} catch (Exception ex) {}
          }
          else
            getToolkit().beep();
        }
    }
}

  /**
   * Performs a 'filtered' paste. A filtered paste is a paste action performed after having
   * made some search and replace operations over the clipboard text.
   */

  public void filteredPaste()
  {
    if (editable)
    {
      Clipboard clipboard = getToolkit().getSystemClipboard();
      try
      {
        // The MacOS MRJ doesn't convert \r to \n,
        // so do it here
        String selection =
               ((String) clipboard.getContents(this).getTransferData(DataFlavor.stringFlavor)).replace('\r', '\n');
        String replaced = null;

        if (Search.getFindPattern().length() > 0)
        {
          if (Props.getBooleanProperty("useregexp"))
          {
            RE regexp = new RE(Search.getFindPattern(),
                               (Props.getBooleanProperty("ignorecase") == true ?
                               RE.REG_ICASE : 0) | RE.REG_MULTILINE,
                               RESyntax.RE_SYNTAX_PERL5);
            if (regexp == null)
              return;
            replaced = regexp.substituteAll(selection, Search.getReplacePattern());
          } else {
            LiteralSearchMatcher matcher = new LiteralSearchMatcher(Search.getFindPattern(),
                                                                    Search.getReplacePattern(),
                                                                    Props.getBooleanProperty("ignorecase"));
            replaced = matcher.substitute(selection);
          }
        }

        if (replaced == null)
          replaced = selection;

        setSelectedText(replaced);

      } catch (Exception e) {
        getToolkit().beep();
      }
    }
  }



  /**
   * This overrides standard insert method. Indeed, we need
   * to update the label containing caret's position.
   * @param insert The string to insert
   * @param pos The offset of the text where to insert the string
   */

  public void insert(String insert, int pos)
  {
    setCaretPosition(pos);
    setSelectedText(insert);
  }

  public void userInput(char c)
  {
        Log.println ("PfixTextArea: userinput: "+c);
        String indentOpenBrackets = getProperty("indentOpenBrackets");
        String indentCloseBrackets = getProperty("indentCloseBrackets");

        if ((indentCloseBrackets != null && indentCloseBrackets.indexOf(c) != -1) ||
        (indentOpenBrackets != null && indentOpenBrackets.indexOf(c) != -1))
        {
            de.schlund.pfixeditor.misc.Indent.indent(this, getCaretLine(), false, true);
        }
  }

  /**
   * Because JEditorPane doesn't have any getTabSize() method,
   * we implement our own one.
   * @return Current tab size (in amount of spaces)
   */

  public int getTabSize()
  {
    String size = Props.getProperty("editor.tabSize");
    if (size == null)
      return 8;

    Integer i = new Integer(size);
    if (i != null)
      return i.intValue();
    else
      return 8;
  }

  /**
   * See getTabSize().
   * @param size The new tab size (in amount of spaces)
   */

  public void setTabSize(int size)
  {
    Props.setProperty("editor.tabSize", String.valueOf(size));

    document.putProperty(PlainDocument.tabSizeAttribute,  new Integer(size));
  }

  /**
   * Set parent title according to the fullfilename flag
   * in the user properties.
   */

  public void setParentTitle()
  {
  }

  // get the name of a file from its absolute path name

  private String getFileName(String file)
  {
    if (file == null)
      return Props.getProperty("textarea.untitled");
    else
      return file.substring(file.lastIndexOf(File.separator) + 1);
  }

  /**
   * Get name of this text area. This name is made of the current opened
   * file name.
   */

  public String getName()
  {
    return getFileName(currentFile);
  }

  private static TokenMarker PfixTokenMarker=null; 

  private static TokenMarker getPfixTokenMarker()
  {
    if (PfixTokenMarker==null)
    {
        PfixTokenMarker=new HTMLTokenMarker();
    }
    return PfixTokenMarker;
  }
  
   /**
   * Turn syntax colorization on or off.
   * @param mode Colorization mode
   */
  
  public void setColorizing(String mode)
  {
    Log.println("  calling setColorizing(String mode)");
    //enableColorizing(mode, Jext.getMode(mode).getTokenMarker());
    enableColorizing(Props.getProperty("mode.pfix.name"),getPfixTokenMarker());
  }


  private void enableColorizing(String mode, TokenMarker token)
  {
    Log.println("Try to enable colorizing for mode " + mode );
    if (mode == null || token == null || mode.equals(this.mode))
      return;

    setTokenMarker(token);

    this.mode = mode;
    getPainter().setBracketHighlightEnabled("on".equals(getProperty("bracketHighlight")));

    Props.setProperty("editor.colorize.mode", mode);
    parent.fireJextEvent(this, JextEvent.SYNTAX_MODE_CHANGED);

    repaint();
  }

  /**
   * Sets current colorizing mode.
   * @param mode The colorizing mode name
   */

  public void setColorizingMode(String mode)
  {
    this.mode = mode;
  }

  /**
   * Returns current syntax colorizing mode.
   */

  public String getColorizingMode()
  {
    return mode;
  }

  /**
   * Called to load a new file in the text area.
   * Determines which line separator (\n, \r\n...) are used in the
   * file to open. Convert'em into Swing line separator (\n).
   * @param path The path of the file to be loaded
   * @param _in You can specify an InputStreamReader (see ZipExplorer)
   * @param bufferSize Size of the StringBuffer, useful if _in != null
   * @param web True if open an url
   * @param addToRecentList If false, the file name is not added to recent list
   */


public void newText(String srcTitle, String txt)
  {
 
    beginOperation();

    // we do the same thing as in newFile() for the listeners
    document.removeUndoableEditListener(this);
    document.removeDocumentListener(this);
    clean();
    discard();
    anchor = null;
    modTime = -1;

    try
    {
  
      StringBuffer buffer;
      setEditable(true);
      int len=txt.length();
      char[] buf = new char[len+1];
      buf=txt.toCharArray();
      int lineCount = 0;
      boolean CRLF = false;
      boolean CROnly = false;
      boolean lastWasCR = false;
      buffer=new StringBuffer(len+1);

      int lastLine = 0;
      for (int i = 0; i < len; i++)
      {
        switch(buf[i])
        {
          // and we convert system's carriage return char into \n
           case '\r':
            if (lastWasCR)
            {
              CROnly = true;
              CRLF = false;
            } else
              lastWasCR = true;
            buffer.append(buf, lastLine, i - lastLine);
            buffer.append('\n');
            lastLine = i + 1;
            break;
          case '\n':
            if (lastWasCR)
            {
              CROnly = false;
              CRLF = true;
              lastWasCR = false;
              lastLine = i + 1;
            } else {
              CROnly = false;
              CRLF = false;
              buffer.append(buf, lastLine, i - lastLine);
              buffer.append('\n');
              lastLine = i + 1;
            }
            break;
          default:
            if (lastWasCR)
            {
              CROnly = true;
              CRLF = false;
              lastWasCR = false;
            }
            break;
        }
      }
      buffer.append(buf, lastLine, len - lastLine);

      if (buffer.length() != 0 && buffer.charAt(buffer.length() - 1) == '\n')
        buffer.setLength(buffer.length() - 1);

      // we clear the area
      document.remove(0, getLength());
      // we put the text in it
      document.insertString(0, buffer.toString(), null);
      buffer = null;

      setCaretPosition(0);
      parent.setNew(this);

      setParentTitle(); 
      setColorizing("html");

      document.addUndoableEditListener(this);
      document.addDocumentListener(this);

      parent.fireJextEvent(this, JextEvent.FILE_OPENED);
    
    } catch(BadLocationException bl) {
      bl.printStackTrace();
    } 
    
    catch(Exception io) {
      Log.println(io.toString());
    } 
    
    finally {
      endOperation();
    }
    
  }

  /**
   * Set the new flag.
   */

  public void setNewFlag(boolean newFlag)
  {
    newf = newFlag;
  }

  /**
   * Return true if current text is new, false otherwise.
   */

  public boolean isNew()
  {
    return newf;
  }

  /**
   * Return true if area is empty, false otherwise.
   */

  public boolean isEmpty()
  {
    if (getLength() == 0)
      return true;
    else
      return false;
  }

  /**
   * Return true if area content has changed, false otherwise.
   */

  public boolean isDirty()
  {
    return dirty;
  }

  /**
   * Called when the content of the area has changed.
   */

  public void setDirty()
  {
    dirty = true;
  }

  /**
   * Called after having saved or created a new document to ensure
   * the content isn't 'dirty'.
   */

  public void clean()
  {
    dirty = false;
  }

  /**
   * Discard all edits contained in the UndoManager.
   * Update the corresponding menu items.
   */

  public void discard()
  {
    undo.discardAllEdits();
  }

  /**
   * Set the anchor postion.
   */

  public void setAnchor()
  {
    try
    {
      anchor = document.createPosition(getCaretPosition());
    } catch (BadLocationException ble) { }
  }

  /**
   * Go to anchor position
   */

  public void gotoAnchor()
  {
    if (anchor == null)
      getToolkit().beep();
    else
      setCaretPosition(anchor.getOffset());
  }

  public int getAnchorOffset()
  {
    if (anchor == null)
      return -1;
    else
      return anchor.getOffset();
  }

  /**
   * Used by Jext to update its menu items.
   */

  public UndoManager getUndo()
  {
    return undo;
  }

  /**
   * Used for ReplaceAll.
   * This merges all text changes made between the beginCompoundEdit()
   * and the endCompoundEdit() calls into only one undo event.
   */

  public void beginCompoundEdit()
  {
    beginCompoundEdit(true);
  }

  public void beginCompoundEdit(boolean cursorHandle)
  {
    if (compoundEdit == null && !protectedCompoundEdit)
    {
      endCurrentEdit();
      compoundEdit = new CompoundEdit();

      if (cursorHandle)
        waitingCursor(true);
    }
  }

  /**
   * A protected compound edit is a compound edit which cannot be ended by
   * a normal call to endCompoundEdit().
   */

  public void beginProtectedCompoundEdit()
  {
    if (!protectedCompoundEdit)
    {
      beginCompoundEdit(true);
      protectedCompoundEdit = true;
    }
  }

  /**
   * See beginCompoundEdit().
   */

  public void endCompoundEdit()
  {
    endCompoundEdit(true);
  }

  public void endCompoundEdit(boolean cursorHandle)
  {
    if (compoundEdit != null && !protectedCompoundEdit)
    {
      compoundEdit.end();

      if (compoundEdit.canUndo())
        undo.addEdit(compoundEdit);

      compoundEdit = null;

      if (cursorHandle)
        waitingCursor(false);
    }
  }

  /**
   * This terminates a protected compound edit.
   */

  public void endProtectedCompoundEdit()
  {
    if (protectedCompoundEdit)
    {
      protectedCompoundEdit = false;
      endCompoundEdit(true);
    }
  }

  /**
   * Return the lentgh of the text in the area.
   */

  public int getLength()
  {
    return document.getLength();
  }

  /**
   * When an undoable event is fired, we add it to the undo/redo list.
   */

  public void undoableEditHappened(UndoableEditEvent e)
  {
    if (!getOperation())
    {
      if (compoundEdit == null)
      {
        currentEdit.addEdit(e.getEdit());
        //undo.addEdit(e.getEdit());
      } else
        compoundEdit.addEdit(e.getEdit());
    }
  }

  public void endCurrentEdit()
  {
    if (currentEdit.isSignificant())
    {
      currentEdit.end();
      if (currentEdit.canUndo())
        undo.addEdit(currentEdit);
      currentEdit = new CompoundEdit();
    }
  }

  public void setUndoing(boolean action)
  {
    undoing = action;
  }

  /**
   * When a modification is made in the text, we turn
   * the 'to_be_saved' flag to true.
   */

  public void changedUpdate(DocumentEvent e)
  {
    if (!getOperation())
      parent.setChanged(this);
    //setCaretPosition(e.getOffset() + e.getLength());
    parent.fireJextEvent(this, JextEvent.CHANGED_UPDATE);
  }

  /**
   * When a modification is made in the text, we turn
   * the 'to_be_saved' flag to true.
   */

  public void insertUpdate(DocumentEvent e)
  {
    if (!getOperation())
      parent.setChanged(this);
    if (undoing)
    {
      
      //if ((e.getLength() == 1) && (e.getOffset() == 0))
      //  setCaretPosition(e.getOffset() + 1);
      //else
     
        setCaretPosition(e.getOffset());
    }
    parent.fireJextEvent(this, JextEvent.INSERT_UPDATE);
  }

  /**
   * When a modification is made in the text, we turn
   * the 'to_be_saved' flag to true.
   */

  public void removeUpdate(DocumentEvent e)
  {
    parent.updateStatus(this);
    if (!getOperation())
      parent.setChanged(this);
    if (undoing)
      setCaretPosition(e.getOffset());
    parent.fireJextEvent(this, JextEvent.REMOVE_UPDATE);
  }

  /**
   * Return a String representation of this object.
   */

  public String toString()
  {
    StringBuffer buf = new StringBuffer();
    buf.append("PfixTextArea: ");
    buf.append("[filename: " + getCurrentFile() + ";");
    buf.append(" filesize: " + getLength() + "] -");
    buf.append(" [is dirty: " + isDirty() + ";");
    buf.append(" is new: " + isNew() + ";");
    if (anchor != null)
      buf.append(" anchor: " + anchor.getOffset() + "] -");
    else
      buf.append(" anchor: not defined] -");
    buf.append(" [font-name: " + getFontName() + ";");
    buf.append(" font-style: " + getFontStyle() + ";");
    buf.append(" font-size: " + getFontSize() + "] -");
    buf.append(" [syntax mode: " + mode + "]");
    return buf.toString();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////
  // INTERNAL CLASSES
  //////////////////////////////////////////////////////////////////////////////////////////////

  class FocusHandler extends FocusAdapter
  {
    public void focusGained(FocusEvent evt)
    {
      /*
      if (!parent.getBatchMode())
      {
        SwingUtilities.invokeLater(new Runnable()
        {
          public void run()
          {
            
          }
        });
      }
      */
    }
  }

  class CaretHandler implements CaretListener
  {
    public void caretUpdate(CaretEvent evt)
    {
      parent.updateStatus(PfixTextArea.this);
    }
  }

  class PfixTextAreaPopupMenu extends Thread
  {
    private PfixTextArea area;

    PfixTextAreaPopupMenu(PfixTextArea area)
    {
      super("---Thread:PfixTextArea Popup---");
      this.area = area;
      start();
    }

    public void run()
    {
      /*
      popupMenu = XPopupReader.read(Jext.class.getResourceAsStream("jext.textarea.popup.xml"),
                                    "jext.textarea.popup.xml");
      if (Jext.getFlatMenus())
        popupMenu.setBorder(javax.swing.border.LineBorder.createBlackLineBorder());
      area.setRightClickPopup(popupMenu);
      */
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
    
    parent = null;
    mode = null;
    anchor = null;
    fontName = null;
    currentFile = null;
    undo = null;
    compoundEdit = null;
    currentEdit = null;
  }
  // End of patch
}


