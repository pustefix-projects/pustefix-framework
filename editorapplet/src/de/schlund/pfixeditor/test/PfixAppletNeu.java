package de.schlund.pfixeditor.test;

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

public class PfixAppletNeu extends JApplet implements DocumentListener, ActionListener, KeyListener {

    //JFrame
    JFrame frame;

    //PfixCheckXml
    PfixCheckXml xmlChecker;

    // Document
    Document doc;

    // UndoManager
    
    UndoManager undo = new UndoManager();
    UndoAction undoAction = new UndoAction();
    RedoAction redoAction = new RedoAction();
    

    // TextComponents
    JTextArea textPane;
    JTextPane resultArea;
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
    
    //JMenuBar
    JMenuBar mbar;


    //Actions
    Hashtable actions;
    


    public void init() {
    //     public static void main(String[] args) { 
        
        frame = new JFrame("Pfix-XML-Editor");
        // frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

       

        panel = new JPanel();
        frame.setContentPane( panel );
        panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        panel.setLayout(new BorderLayout());

        // building JTextPanes
        textPane = new JTextArea();
        resultArea = new JTextPane();
        // syntaxPane = new SyntaxPane();
        syntaxPane = new PfixTextPane();


        xmlChecker = new PfixCheckXml(syntaxPane);
        

        // TextArea - Properties Setting
        syntaxPane.setEditorKit(new StyledEditorKit());
        syntaxPane.getDocument().addUndoableEditListener(new MyUndoableEditListener());
        // syntaxPane.addKeyListener(this);
        keyListening();
        
        

        // Document setting
        // doc = resultArea.getDocument();
        doc = syntaxPane.getDocument();
        doc.addDocumentListener(this);
        
        // ScrollPane aufbauen
        // scrollPane = new JScrollPane(resultArea);
        scrollPane = new JScrollPane(syntaxPane);
        panel.add(scrollPane);
        scrollPane.setPreferredSize(new Dimension(300, 250));        
        // LineNumber lineNumber = new LineNumber( resultArea );
        LineNumber lineNumber = new LineNumber(syntaxPane);
        lineNumber.setPreferredSize(99999);
        scrollPane.setRowHeaderView( lineNumber );

        buttonPanel = new JPanel();
        button = new JButton("Submit");
        button.addActionListener(this);
        buttonPanel.add(button);


        //Setting Action
        actions = createActionTable(resultArea);

        // Creating the EditMenu
        this.createMenu();       
        this.createEditMenu();
        
        panel.add(scrollPane);
        panel.add(mbar, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        frame.pack();
        frame.setVisible(true);
        frame.show();
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
        fileMenu.add(colorSubMenu);
        fileMenu.add(boldSubMenu);
        fileMenu.add(colorizeMenu);
        fileMenu.add(tagClose);
        fileMenu.addSeparator();
        fileMenu.add(viewPortMenu);

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
            // String[] args = {"Wir sind die Wauzies", "Wir haben keine Kinder"};
           JSObject window = (JSObject) JSObject.getWindow(this);
           JSObject  parent = (JSObject) window.getMember("parent");
       
           if (xmlChecker.checkXML()) {
                  window.eval("test()");
           }
           else {
               JOptionPane.showMessageDialog (null,"Code is full of Errors" ,"Pustefix Message",JOptionPane.ERROR_MESSAGE);
           }
                                                               
        } catch (Exception err ) {
            System.out.println(err.getMessage());
                
        }

    }


    





    // KeyHandling
    public void keyListening() {

        syntaxPane.addKeyListener(this);
        
        Keymap km = syntaxPane.getKeymap();
        KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_E, Event.CTRL_MASK);
        Action act = new TextAction("Ctrl-Z") {
                public void actionPerformed(ActionEvent e) {
                    // pane.replaceSelection("ZZZ");                    
                    // this.closeFinalTag();
                    syntaxPane.closeFinalTag();
                    syntaxPane.hilightAll();
                }
            };
        km.addActionForKeyStroke(ks, act);
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
                JOptionPane.showMessageDialog (null,"Code is full of Errors" ,"Pustefix Message",JOptionPane.ERROR_MESSAGE);
            }
		
		
       }

        if (e.getSource() == button) {
            doJSAction();
        }
        
        
    }
    

    // KeyHandler
    public void keyPressed(KeyEvent keyEvent) {
    }


    public void keyReleased(KeyEvent keyEvent) {
        syntaxPane.realtimeHilight();
    }

    public void keyTyped(KeyEvent keyEvent) {
    }

    
  
        
    public void setText(String text) {
        syntaxPane.setText(text);
    }

    public String getText() {
        String text = syntaxPane.getText();
        return text;
    }







    
    
    
    

    public void insertUpdate(DocumentEvent e) {
        int curPos = textPane.getCaretPosition();
        StyledDocument sDoc = (StyledDocument) e.getDocument();
        // syntaxPane.setMarks(sDoc, 0, sDoc.getLength());
        // syntaxPane.setMarks(sDoc, 0, sDoc.getLength());
        // syntaxPane.realtimeHilight();
    }

    public void removeUpdate(DocumentEvent e) {
    }

    public void changedUpdate(DocumentEvent e) {
    }


        public class MyUndoableEditListener implements UndoableEditListener{
        
        public void undoableEditHappened(UndoableEditEvent e) {
        //Remember the edit and update the menus
            undo.addEdit(e.getEdit());
            undoAction.update();
            redoAction.update();
            // checkPress();
            // updateLinePane2();
        }
        
    }  

    //UNDO AND REDOACTION CLASSES
    //THIS PASRT OF CODE WAS TAKEN FROM THE NOTEPAD DEMO FOUND IN THE JDK1.4.1 DEMO DIRECTORY
    class UndoAction extends AbstractAction {
	public UndoAction() {
	    super("Undo");
	    setEnabled(false);
	}

	public void actionPerformed(ActionEvent e) {
	    try {
		undo.undo();
	    } catch (CannotUndoException ex) {
		System.out.println("Unable to undo: " + ex);
		ex.printStackTrace();
	    }
	    update();
	    redoAction.update();
	}

	protected void update() {
	    if(undo.canUndo()) {
		setEnabled(true);
		putValue("Undo", undo.getUndoPresentationName());
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
	}

	public void actionPerformed(ActionEvent e) {
	    try {
		undo.redo();
	    } catch (CannotRedoException ex) {
		System.out.println("Unable to redo: " + ex);
		ex.printStackTrace();
	    }
	    update();
	    undoAction.update();
	}

	protected void update() {
	    if(undo.canRedo()) {
		setEnabled(true);
		putValue("Redo", undo.getRedoPresentationName());
	    }
	    else {
		setEnabled(false);
		putValue(Action.NAME, "Redo");
	    }
	}
    }


    
    
}
