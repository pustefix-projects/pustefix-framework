/*
* This file is part of PFIXCORE.
*
* PFIXCORE is free software; you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* PFIXCORE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with PFIXCORE; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*
*/


package de.schlund.pfixeditor.editor;

import javax.swing.event.*; 
import java.awt.AWTPermission;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.*;
import java.lang.Object.*;
import java.awt.Font;
import java.awt.event.*;
import java.util.*;

import de.schlund.pfixeditor.xml.*;

import javax.swing.text.*;
import java.io.*;
import java.util.*;
import javax.swing.undo.*;
import java.util.Hashtable;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;



import javax.swing.*;
import netscape.javascript.*;

public class PfixAppletNeu extends JApplet implements DocumentListener, ActionListener, KeyListener, UndoableEditListener {
    private static final String TITLE = "PfixEditor";

    // JSObject jsWin, jsDocu, jsForm, jsField;
     JSObject window;

    //JFrame
    JFrame frame;

    //PfixCheckXml
    PfixCheckXml xmlChecker;

    // Document
    Document doc;

    // UndoManager
    
    UndoManager undo;
    UndoAction undoAction;
    RedoAction redoAction;
    private MyUndoableEditListener undolistener;

    // TextComponents
    JTextArea textPane;
    JTextPane resultArea;
    // JTextArea syntaxPane;
    //JEditorPane resultArea;
    // SyntaxPane syntaxPane;
    PfixTextPane syntaxPane;

    //JPanel
    JPanel panel;
    JPanel buttonPanel;

    JButton button;
    
    // Menu
    JMenu fileMenu;
    JMenu editMenu;

    //ScrollPane
    JScrollPane scrollPane;

    //JMenuItem
    JMenuItem fileSubMenu;
    JMenuItem viewPortMenu;
    JMenuItem colorSubMenu;
    JMenuItem boldSubMenu;
    JMenuItem colorizeMenu;
    JMenuItem tagClose;
    JMenuItem undoedit;
    JMenuItem redoedit;
    JMenuItem searchedit;
    JMenuItem replaceedit;
    
    //JMenuBar
    JMenuBar mbar;


    String uploadField;

    //Actions
    Hashtable actions;

    // Boolean Pack
    boolean pack = false;


    public void init() {
        
        frame = new JFrame("Pfix-XML-Editor");
        // frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

        String uploadField = "";

        undo = new UndoManager();

        panel = new JPanel();
        frame.setContentPane( panel );
        panel.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        panel.setLayout(new BorderLayout());

        // building JTextPanes
        // syntaxPane = new JTextArea();
        resultArea = new JTextPane();
        syntaxPane = new PfixTextPane();

        xmlChecker = new PfixCheckXml(syntaxPane);
        
        // TextArea - Properties Setting
        // syntaxPane.setEditorKit(new StyledEditorKit());
        undolistener= new MyUndoableEditListener();
      
        syntaxPane.getDocument().addUndoableEditListener(undolistener);
        // syntaxPane.getDocument().addUndoableEditListener(new MyUndoableEditListener());
        undoAction = new UndoAction();
        redoAction = new RedoAction();
        
        keyListening();
        
        // Document setting
        doc = syntaxPane.getDocument();
        // doc.addDocumentListener(this);
        
        // Creating Scroll-Panel
        scrollPane = new JScrollPane(syntaxPane);
        panel.add(scrollPane);
        scrollPane.setPreferredSize(new Dimension(800, 450));        
        LineNumber lineNumber = new LineNumber(syntaxPane);
        lineNumber.setPreferredSize(99999);
        scrollPane.setRowHeaderView( lineNumber );

        // Creating Button - Panel
        buttonPanel = new JPanel();
        button = new JButton("Submit");
        button.addActionListener(this);
        buttonPanel.add(button);
        
        
        //Setting Action
        actions = createActionTable(resultArea);

        // Creating the EditMenu
        this.createMenu();       
        this.createEditMenu();
        
        // Layouting Applet
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(mbar, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Setting the Frames position
        frame.setLocation(300,100);
        frame.pack();


        // Checking visibility of the applet
        boolean checkVisibile = this.checkJSVisibility();
        if (checkVisibile) {
            checkJSUploadField();
             if (this.uploadField != null) {
                 getJSText();
             }
             frame.show();
        }
        else {
            frame.hide();
            
        }
        
    }




    
    public void createMenu() {
        mbar = new JMenuBar();

        // Creating File Menu
        fileMenu = new JMenu("File");
        fileSubMenu = new JMenuItem("Check wellformed");
        colorSubMenu = new JMenuItem("Color");
        boldSubMenu = new JMenuItem("Bold");
        colorizeMenu = new JMenuItem("Syntax Highlighting");
        tagClose = new JMenuItem("Close Tag (CTRL-E)");
        viewPortMenu = new JMenuItem("Testing ViewPort");

        // Adding File SubMenu
        fileMenu.add(fileSubMenu);
        // fileMenu.add(colorSubMenu);
        // fileMenu.add(boldSubMenu);
        fileMenu.add(colorizeMenu);
        fileMenu.add(tagClose);
        // fileMenu.addSeparator();
        // fileMenu.add(viewPortMenu);

        // Adding ActionListener
        colorizeMenu.addActionListener(this);
        fileSubMenu.addActionListener(this);

        

        // addingFile Menu
        this.mbar.add(fileMenu);
        
        
    }


    public void createEditMenu() {
        editMenu = new JMenu("Edit");
        undoedit = new JMenuItem("Undo");
        redoedit = new JMenuItem("Redo");
        editMenu.add(undoAction);
        editMenu.add(redoAction);
        editMenu.addSeparator();
        editMenu.add(getActionByName(DefaultEditorKit.cutAction));
        editMenu.add(getActionByName(DefaultEditorKit.copyAction));
        editMenu.add(getActionByName(DefaultEditorKit.pasteAction));

        editMenu.addSeparator();

        editMenu.add(getActionByName(DefaultEditorKit.selectAllAction));

        searchedit = new JMenuItem("Search (CTRL-F)");
        replaceedit = new JMenuItem("Replace (CTRL-W)");

        editMenu.addSeparator();
        editMenu.add(searchedit);
        editMenu.add(replaceedit);

        searchedit.addActionListener(this);
        searchedit.setMnemonic(KeyEvent.VK_F);
        replaceedit.addActionListener(this);
        // replaceedit.addMnemonic(KeyEvent.VK_R);
        replaceedit.setMnemonic(KeyEvent.VK_R);
        
        this.mbar.add(editMenu);
    }
    
    




    

    private Hashtable createActionTable(JTextComponent textComponent) {
        Hashtable actions = new Hashtable();
        Action[] actionsArray = textComponent.getActions();
        for (int i = 0; i < actionsArray.length; i++) {
            Action a = actionsArray[i];
            actions.put(a.getValue(Action.NAME), a);
        }
        return actions;
    }

    private Action getActionByName(String name) {
        return (Action)(actions.get(name));
    } 

    
    
    public void doJSAction() {
        try {
           JSObject window = (JSObject) JSObject.getWindow(this);
           JSObject  parent = (JSObject) window.getMember("parent");
           
       
           if (xmlChecker.checkXML()) {
                   window.call("doSub",null);
           }
           else {
               JOptionPane.showMessageDialog (null,xmlChecker.getErrorMessage() ,"Pustefix Message",JOptionPane.ERROR_MESSAGE);
           }
                                                               
        } catch (Exception err ) {
            System.out.println(err.getMessage());
                
        }

    }


    public void destroyApplet() {
        frame.hide();
        stop();
        destroy();
    }
    

    public void showApplet() {
        if (!pack) {
            frame.pack();
            pack = true;
        }

        if (this.checkJSVisibility()) {
            checkJSUploadField();
             if (this.uploadField != null) {
                 getJSText();
                 frame.show();
             }

        }
        
    }

    public void hideApplet() {
        frame.hide();
    }


    public void getJSText() {
        try {
            JSObject win, doc, form, field, frame, parent, parent_parent, frame2, win_doc;
            win = JSObject.getWindow(this);
            doc = (JSObject)win.getMember("document");
            parent = (JSObject)win.getMember("parent");
            frame = (JSObject)parent.getMember("bottom");
            frame2 = (JSObject)frame.getMember("main");
            win_doc = (JSObject)frame2.getMember("document");
            form = (JSObject)win_doc.getMember("my_form");

            String text = "";
         
         if (uploadField.equals("uplinc.Content")) {
             field = (JSObject)form.getMember("uplinc.Content");
             text = (String)field.getMember("value");
         }
         else {
             field = (JSObject)form.getMember("uplcom.Content");        
             text = (String)field.getMember("value");
         }
         
         setText(text);
             
        } catch (Exception e) {
            System.out.println("Error");
            
        }
        
       
         
         
    }

    public void checkJSUploadField() {
        try {
            JSObject win, doc, form, field, frame, parent, parent_parent, frame2, win_doc;
         win = JSObject.getWindow(this);
         doc = (JSObject)win.getMember("document");
         // frame = (JSObject)doc.getMember("frame");
         parent = (JSObject)win.getMember("parent");
         frame = (JSObject)parent.getMember("bottom");
         frame2 = (JSObject)frame.getMember("main");
         win_doc = (JSObject)frame2.getMember("document");                 
         form = (JSObject)win_doc.getMember("my_form");
         field = (JSObject)form.getMember("upload");
         uploadField  = (String)field.getMember("value");
         
            
        } catch (Exception exc) {
            System.out.println("Error");
        }                 
    }

    public boolean checkJSVisibility() {
        boolean bol = true;
        try {
            JSObject win, doc, form, field, frame, parent, parent_parent, frame2, win_doc;
            win = JSObject.getWindow(this);
            doc = (JSObject)win.getMember("document");
            parent = (JSObject)win.getMember("parent");
            frame = (JSObject)parent.getMember("bottom");
            frame2 = (JSObject)frame.getMember("main");
            win_doc = (JSObject)frame2.getMember("document");                 
            form = (JSObject)win_doc.getMember("my_form");
            field = (JSObject)form.getMember("visible");
            String vis = (String)field.getMember("value");
            
            if (vis.equals("false")) {
                bol = false;
              
            }

            if (vis.equals("true")) {
                bol = true;
              
            }
         
         
            
        } catch (Exception exc) {
            System.out.println("---- NotFound");
             bol = false;
            
        }
                         
        return bol;
         
    }
          

    // KeyHandling
    public void keyListening() {

        syntaxPane.addKeyListener(this);
        
        Keymap km = syntaxPane.getKeymap();
        KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_E, Event.CTRL_MASK);
        Action act = new TextAction("Ctrl-E") {
                public void actionPerformed(ActionEvent e) {
                    syntaxPane.closeFinalTag();
                    syntaxPane.hilightAll();
                }
            };
        km.addActionForKeyStroke(ks, act);

        KeyStroke ksFind = KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK);
        Action actFind = new TextAction("Ctrl-F") {
                public void actionPerformed(ActionEvent e) {
                    find();
                }
            };

        km.addActionForKeyStroke(ksFind, actFind);

        KeyStroke ksReplace = KeyStroke.getKeyStroke(KeyEvent.VK_W, Event.CTRL_MASK);
        Action actReplace = new TextAction("Ctrl-W") {
                public void actionPerformed(ActionEvent e) {
                    replace();
                }
            };

        km.addActionForKeyStroke(ksReplace, actReplace);

        KeyStroke ksUndo = KeyStroke.getKeyStroke(KeyEvent.VK_U, Event.CTRL_MASK);
        km.addActionForKeyStroke(ksUndo, undoAction);

        KeyStroke ksRedo = KeyStroke.getKeyStroke(KeyEvent.VK_R, Event.CTRL_MASK);
        km.addActionForKeyStroke(ksRedo, redoAction);

        

        KeyStroke ksTest = KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.CTRL_MASK);
        TextAction actTest = new TextAction("h") {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("I PRESSED Hallo");
                    // undoAction.undo2();
                }
            };
       
        km.addActionForKeyStroke(ksTest, actTest);
            
        
    }



    public void colorize() {
         syntaxPane.hilightAll(); 
    }
    

    // Action Handler
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == colorizeMenu) {
            syntaxPane.hilightAll();                       
        }

        if ( e.getSource() == fileSubMenu) {

            if (xmlChecker.checkXML()) {
              	JOptionPane.showMessageDialog (null,"Code is wellformed" ,"Pustefix Message",JOptionPane.INFORMATION_MESSAGE);
              }
            else {
                JOptionPane.showMessageDialog (null,xmlChecker.getErrorMessage() ,"Pustefix Message",JOptionPane.ERROR_MESSAGE);
            }
		
		
       }

        if (e.getSource() == button) {
             doJSAction();
            // FindFrame findframe = new FindFrame(syntaxPane, true);
        }

        if (e.getSource() == searchedit) {
            // FindFrame findframe = new FindFrame(syntaxPane, true);
            find();
             
        }

        if (e.getSource() == replaceedit) {
            // FindFrame findframe = new FindFrame(syntaxPane, true);
            replace();
             
        }
        
        
    }
    

    // KeyHandler
    public void keyPressed(KeyEvent keyEvent) {
    }


    public void keyReleased(KeyEvent keyEvent) {
        // Remove UndoListener !!
        syntaxPane.getDocument().removeUndoableEditListener(undolistener);
        syntaxPane.realtimeHilight();
        syntaxPane.getDocument().addUndoableEditListener(undolistener);
        
    }

    public void keyTyped(KeyEvent keyEvent) {
    }

    
  
        
    public void setText(String text) {
        if (text != null) {
            syntaxPane.getDocument().removeUndoableEditListener(undolistener);
            syntaxPane.setText(text);
            // syntaxPane.getDocument().addUndoableEditListener(undolistener);
        }
        colorize();
        syntaxPane.getDocument().addUndoableEditListener(undolistener);
        frame.show();        
    }

    public String getText() {
        String text = syntaxPane.getText();
        return text;
    }

    public void find() {
        String tSearchFor = JOptionPane.showInputDialog(frame, "Search");
        if (tSearchFor != null) {
           for (int pos = 0; pos < (syntaxPane.getText().length() - 
                tSearchFor.length()); pos++) {
              try {
                 String tLookAt = syntaxPane.getText(pos, tSearchFor.length());
                 if (tSearchFor.equalsIgnoreCase(tLookAt)) {
                    syntaxPane.setCaretPosition(pos);
                    syntaxPane.setSelectionStart(pos);
                    syntaxPane.setSelectionEnd(pos + tSearchFor.length());
                    if (JOptionPane.showConfirmDialog(frame, "Search more",
                            TITLE, JOptionPane.YES_NO_OPTION) == 
                            JOptionPane.NO_OPTION) {
                       syntaxPane.requestFocus();
                       return;
                    }
                 }
              } catch (BadLocationException e) {}
           }
        }
    }

    public void replace() {
        String tSearchFor = JOptionPane.showInputDialog(frame, "Search");
        String tReplaceWith = JOptionPane.showInputDialog(frame, "Replace with");
      
        if (tSearchFor != null) {
            for (int pos = 0 ; pos < (syntaxPane.getText().length() - 
                    tSearchFor.length()) ; pos++) {
                try {
                    String tLookAt = syntaxPane.getText(pos, tSearchFor.length());
                    if (tSearchFor.equalsIgnoreCase(tLookAt)) {
                       syntaxPane.setCaretPosition(pos);
                       syntaxPane.setCaretPosition(pos);
                       syntaxPane.setSelectionStart(pos);
                       syntaxPane.setSelectionEnd(pos + tSearchFor.length());
                       int option = JOptionPane.showConfirmDialog(frame, 
                                "Replace ?", TITLE, JOptionPane.YES_NO_CANCEL_OPTION);
                       if (option == JOptionPane.YES_OPTION) {
                          syntaxPane.replaceSelection(tReplaceWith);
                          syntaxPane.requestFocus();
                       } else if (option == JOptionPane.CANCEL_OPTION) {
                          return;
                       }
                    }
                } catch (BadLocationException e) {}
            }
        }
        
    }







    
    
    
    

    public void insertUpdate(DocumentEvent e) {
        int curPos = textPane.getCaretPosition();
        StyledDocument sDoc = (StyledDocument) e.getDocument();
    }

    public void removeUpdate(DocumentEvent e) {
    }

    public void changedUpdate(DocumentEvent e) {
    }


      public void undoableEditHappened(UndoableEditEvent e) {
          /*    if (!getOperation())
              {
                  if (compoundEdit == null)
                      {
        currentEdit.addEdit(e.getEdit());
        //undo.addEdit(e.getEdit());
      } else
        compoundEdit.addEdit(e.getEdit());
        }*/
  }


            protected class MyUndoableEditListener implements UndoableEditListener {
                 public void undoableEditHappened(UndoableEditEvent e) {
               //Remember the edit and update the menus.
                        undo.addEdit(e.getEdit());
                        undoAction.updateUndoState();
                        redoAction.updateRedoState();
                }
        }
        



     class UndoAction extends AbstractAction {
                public UndoAction() {
                        super("Undo");
                        setEnabled(false);
                }
                public void actionPerformed(ActionEvent e) {
                        try {
                                
                                undo.undo();
                                System.out.println("Undo " + undo.toString());
                         } 
                         catch (CannotUndoException ex) {
                                System.out.println("Unable to undo: " + ex);
                                ex.printStackTrace();
                        }

                        
                        updateUndoState();
                        redoAction.updateRedoState();
                         
          }
                    

          
                protected void updateUndoState() {
                        if (undo.canUndo()) {
                                setEnabled(true);
                                putValue(Action.NAME, undo.getUndoPresentationName() + "  (CTRL-U)");
                                } 
                       else {
                                setEnabled(false);
                                putValue(Action.NAME, "Undo  (CTRL-U)");
                      }
                }      
        }    

        class RedoAction extends AbstractAction {
                        public RedoAction() {
                        super("Redo");
                        setEnabled(false);
                }

                public void actionPerformed(ActionEvent e) {
                    try {
                        undo.redo();
                        } 
                        catch (CannotRedoException ex) {
                        System.out.println("Unable to redo: " + ex);
                        ex.printStackTrace();
                        }

                         

                        updateRedoState();
                        undoAction.updateUndoState();
                    

                }

                protected void updateRedoState() {
                        if (undo.canRedo()) {
                                setEnabled(true);
                                putValue(Action.NAME, undo.getRedoPresentationName() +"  (CTRL-R)");
                        } 
                        else {
                                setEnabled(false);
                                putValue(Action.NAME, "Redo  CTRL-R");
                        }
                }
        }


    




    
    
}
