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
    JButton hideButton;
    
    // Menu
    JMenu fileMenu;
    JMenu editMenu;

    //ScrollPane
    JScrollPane scrollPane;

    //JMenuItem
    JMenuItem wellformedMenu;
    JMenuItem viewPortMenu;
    JMenuItem colorSubMenu;
    JMenuItem boldSubMenu;
    JMenuItem colorizeMenu;
    JMenuItem tagClose;
    JMenuItem undoedit;
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
        buttonPanel.setLayout(new BorderLayout());
        button = new JButton("Submit");
        hideButton = new JButton("Close");
        button.addActionListener(this);
        hideButton.addActionListener(this);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        buttonPanel.add(button, BorderLayout.EAST);
        buttonPanel.add(hideButton, BorderLayout.WEST);
        
        //Setting Action
        actions = createActionTable(resultArea);
        
        // Creating the EditMenu
        mbar = new JMenuBar();
        this.createFileMenu();       
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
        } else {
            frame.hide();
        }
    }
    
    public void createFileMenu() {
        // Creating File Menu
        fileMenu = new JMenu("File");
        
        wellformedMenu = new JMenuItem("Check wellformed...");
        fileMenu.add(wellformedMenu);
        wellformedMenu.addActionListener(this);
        wellformedMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.ALT_MASK));

        colorizeMenu = new JMenuItem("Redo syntax highlighting");
        fileMenu.add(colorizeMenu);
        colorizeMenu.addActionListener(this);
        colorizeMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.ALT_MASK));

        // addingFile Menu
        this.mbar.add(fileMenu);
    }


    public void createEditMenu() {
        editMenu = new JMenu("Edit");
        Action action;
        
        editMenu.add(undoAction);
        editMenu.add(redoAction);
        
        editMenu.addSeparator();

        action = getActionByName(DefaultEditorKit.cutAction);
        action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        action.putValue(Action.NAME, "Cut");
        editMenu.add(action);

        action = getActionByName(DefaultEditorKit.copyAction);
        action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        action.putValue(Action.NAME, "Copy");
        editMenu.add(action);
        
        action = getActionByName(DefaultEditorKit.pasteAction);
        action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        action.putValue(Action.NAME, "Paste");
        editMenu.add(action);

        editMenu.addSeparator();

        action = getActionByName(DefaultEditorKit.selectAllAction);
        action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        action.putValue(Action.NAME, "Select all");
        editMenu.add(action);
        

        tagClose = new JMenuItem("Close Tag");
        editMenu.add(tagClose);
        tagClose.addActionListener(this);
        tagClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));

        editMenu.addSeparator();

        searchedit  = new JMenuItem("Search...");
        editMenu.add(searchedit);
        searchedit.addActionListener(this);
        searchedit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));

        replaceedit = new JMenuItem("Replace...");
        editMenu.add(replaceedit);
        replaceedit.addActionListener(this);
        replaceedit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
        
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
           } else {
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
         // Keymap km = syntaxPane.getKeymap();
         // 
         // KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_E, Event.CTRL_MASK);
         // Action act = new TextAction("Ctrl-E") {
         //         public void actionPerformed(ActionEvent e) {
         //             syntaxPane.closeFinalTag();
         //             syntaxPane.hilightAll();
         //         }
         //     };
         // km.addActionForKeyStroke(ks, act);
         // 
         // KeyStroke ksFind = KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK);
         // Action actFind = new TextAction("Ctrl-F") {
         //         public void actionPerformed(ActionEvent e) {
         //             find();
         //         }
         //     };
         // 
         // km.addActionForKeyStroke(ksFind, actFind);
         // 
         // KeyStroke ksReplace = KeyStroke.getKeyStroke(KeyEvent.VK_W, Event.CTRL_MASK);
         // Action actReplace = new TextAction("Ctrl-W") {
         //         public void actionPerformed(ActionEvent e) {
         //             replace();
         //         }
         //     };
         // 
         // km.addActionForKeyStroke(ksReplace, actReplace);
         // 
         // KeyStroke ksUndo = KeyStroke.getKeyStroke(KeyEvent.VK_U, Event.CTRL_MASK);
         // km.addActionForKeyStroke(ksUndo, undoAction);
         // 
         // KeyStroke ksRedo = KeyStroke.getKeyStroke(KeyEvent.VK_R, Event.CTRL_MASK);
         // km.addActionForKeyStroke(ksRedo, redoAction);
    }

    public void colorize() {
         syntaxPane.hilightAll(); 
    }
    

    // Action Handler
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == colorizeMenu) {
            syntaxPane.hilightAll();                       
        }

        if (e.getSource() == wellformedMenu) {
            if (xmlChecker.checkXML()) {
              	JOptionPane.showMessageDialog (null,"Code is wellformed" ,"Pustefix Message",JOptionPane.INFORMATION_MESSAGE);
            }
            else {
                JOptionPane.showMessageDialog (null,xmlChecker.getErrorMessage() ,"Pustefix Message",JOptionPane.ERROR_MESSAGE);
            }
        }

        if (e.getSource() == tagClose) {
            syntaxPane.closeFinalTag();
            syntaxPane.hilightAll();
        }
        
        if (e.getSource() == searchedit) {
            find();
            
        }
        
        if (e.getSource() == replaceedit) {
            replace();
        }

        if (e.getSource() == button) {
            doJSAction();
            // FindFrame findframe = new FindFrame(syntaxPane, true);
        }

        if (e.getSource() == hideButton) {
            hideApplet();
        }
    }
    

    // KeyHandler
    public void keyPressed(KeyEvent keyEvent) {
    }


    public void keyReleased(KeyEvent keyEvent) {
        // Remove UndoListener !!
        if (!((keyEvent.getKeyCode() > 36) && (keyEvent.getKeyCode() < 41))) {            
            syntaxPane.getDocument().removeUndoableEditListener(undolistener);
            syntaxPane.realtimeHilight();
            syntaxPane.getDocument().addUndoableEditListener(undolistener);
        }
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
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_U, ActionEvent.CTRL_MASK));
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
                putValue(Action.NAME, undo.getUndoPresentationName());
            } 
            else {
                setEnabled(false);
                putValue(Action.NAME, "Undo");
            }
        }      
    }    
    
    class RedoAction extends AbstractAction {
        public RedoAction() {
            super("Redo");
            setEnabled(false);
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
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
                putValue(Action.NAME, undo.getRedoPresentationName());
            } 
            else {
                setEnabled(false);
                putValue(Action.NAME, "Redo");
            }
        }
    }
    
}
