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

// import de.schlund.pfixeditor.xml.*;

import javax.swing.text.*;
import java.io.*;
import java.util.*;
import javax.swing.undo.*;

import javax.swing.*;
import java.net.*;
import javax.swing.tree.*;
import javax.swing.border .*;




/**
 * PfixIncludeDialog.java
 *
 *
 * Created: Fri Jul 04 16:40:07 2003
 *
 * @author <a href="mailto:zaich@schlund.de">Volker Zaich</a>
 * @version
 *
 * This is a Part of the Pustefix-Applet. A Frame, where the User gets can
 * choose all Includes and Images per Project.
 *
 *
 *
 *
 *
 *
 */





public class PfixIncludeDialog extends JFrame implements ItemListener, ActionListener, TreeSelectionListener,WindowListener{

    // JFrame dialogFrame;
    GridBagLayout gridbag;

    URL ucont;

    JTabbedPane jTabbedPane = new JTabbedPane();

    // Panels
    JPanel panel;
    JPanel incPanel;
    JPanel imgPanel;
    JPanel incTextPanel;
    JPanel selPanel;
    JPanel butPanel;
    JPanel imgbutPanel;
    JPanel imgFilter;

    // Buttons
    JButton imgbutton;
    JButton button;
    JButton but;
    JButton close;
    JButton imgclose;

    // ImageIcons
    ImageIcon neuImg;    
    
    // Labels
    JLabel img;
    JLabel incText;

    // Trees
    JTree tree;
    JTree jimagetree;
        

    // Text Field -- Not Needed at the Moment, maybe used later
    JTextField incTextField;

    // Not needed
    JComboBox combox;
    JComboBox imbox;

    String [] incElements;
    String [] incImages;
    String documentBase;
    String host;
    String actInclude;
    String actImage;
    
    PfixTextPane syntaxPane;    
    PfixAppletInfo info;

    PfixAppletNeu applet;


    // buttonPanel.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
    
    // buttonPanel.add(button, BorderLayout.EAST);
    // buttonPanel.add(hideButton, BorderLayout.WEST);
        

    
    public PfixIncludeDialog(String docBase, PfixTextPane pane, PfixAppletNeu japplet) {
        super();
        
        this.documentBase = docBase;
        this.applet = japplet;
        actInclude = "";
        actImage = "";

        // Getting Informations from the Webserver
        info = new PfixAppletInfo(documentBase);

        setHost(this.documentBase);
        
        syntaxPane = pane;


        // Building Layout
        panel = new JPanel();

        // panel.setMinimumSize(new Dimension(300,300));
        setContentPane( panel );
        
        panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        panel.setPreferredSize(new Dimension(700,446));
        panel.setLayout(new BorderLayout());
        
        setTitle("PfixDialog Includes/Images");

        // Building the Tabs
        createTabInc();
        createTabImg();
        
        jTabbedPane.setLocation(new java.awt.Point(10, 10));
        jTabbedPane.setVisible(true);
        // jTabbedPane.setSize(new java.awt.Dimension(550, 400));
        // jTabbedPane.setMinimumSize(new Dimension(300,300));

        // jTabbedPane.add(label);        
        jTabbedPane.addTab("Includes", incPanel);
        jTabbedPane.addTab("Images", imgPanel);                       

        panel.add(jTabbedPane, BorderLayout.CENTER);
        // Dimension dim = new Dimension(600,400);
        // setSize(dim);

        tree = buildTree(info.getRealElements());
        tree.addTreeSelectionListener(this);
        tree.setRootVisible(false);
        // tree.setMinimumSize(new Dimension(200,200));

        selPanel.add(new JScrollPane(tree), BorderLayout.CENTER);

        // Opens all Nodes in the Tree
        expandAll(this.tree);

        jimagetree = buildTree(info.getRealImages());
        jimagetree.addTreeSelectionListener(this);
        // imgFilter.add(new JScrollPane(jimagetree), GridBagConstraints.WEST);        
        expandAll(this.jimagetree);

        but = new JButton();

        createGridBagLayout();
                
        // setSize(1200, 500);
        setLocation(350,250);
        setVisible(true);
        //setResizable(false);
        
        tree.getSelectionModel().setSelectionMode
            (TreeSelectionModel.SINGLE_TREE_SELECTION);

        jimagetree.getSelectionModel().setSelectionMode
            (TreeSelectionModel.SINGLE_TREE_SELECTION);

        jimagetree.setRootVisible(false);
        
        pack();
        show();


        
    }





    private void createGridBagLayout() {
        imgFilter = new JPanel();
        
        JSplitPane jsp = new JSplitPane();        
        jsp.setLeftComponent(new JScrollPane(jimagetree));

        String url = getHost() + "/core/editor/img/alpha.gif" ;

        URL		urli;
        URLConnection	urlConn;
        
        try {
            urli = new URL(url);
            urlConn = urli.openConnection();
            ucont = urli;            
            neuImg = new ImageIcon(urli);
        }
        catch (Exception e ) {
            System.out.println("Background Image not Found");                     
        }
        
        JPanel imgButPanel;

        if (ucont != null) {
            imgButPanel = new JPanel(){
                    public void paintComponent(Graphics g)	{
                        ImageIcon bgImage = new ImageIcon(ucont);
                        
                        g.drawImage(bgImage.getImage(), 0, 0, this);
                        super.paintComponent(g);
                        
                        if(bgImage != null) { 
                            int x = 0, y = 0; 
                            while(y < size().height) { 
                                x = 0; 
                                while(x< size().width) { 
                                    g.drawImage(bgImage.getImage(), x, y, this); 
                                    x=x+bgImage.getImage().getWidth(null); 
                                } 
                                y=y+bgImage.getImage().getHeight(null); 
                            } 
                        } 
                        else {
                            g.clearRect(0, 0, size().width, size().height); 
                        } 
                    }                    
                };
            imgButPanel.setOpaque(false);
            
        }
        else {
            imgButPanel = new JPanel();
        }
        

        // JPanel neu = imgButPanel;

        imgButPanel.setLayout(new BorderLayout());

        // setting Transparent-Background
        but.setOpaque( false );
        but.setBorder(new EmptyBorder(0,0,0,0));
        imgButPanel.add(but, BorderLayout.CENTER);
        
        jsp.setRightComponent(imgButPanel);
        GridBagLayout grid = new GridBagLayout();        
        GridBagConstraints c = new GridBagConstraints();

        // Setting GridBagLayout to panel
        imgFilter.setLayout(grid);        
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.5;
        grid.setConstraints(jsp, c);
        
        imgFilter.add(jsp);
        imgPanel.add(imgFilter, BorderLayout.NORTH);
        
    }



    // Creating the Include-Tab
    private void createTabInc() {
        incPanel = new JPanel();
        selPanel = new JPanel();
        butPanel = new JPanel();
        incPanel.setLayout(new BorderLayout());
        // selPanel.setPreferredSize(new Dimension(400,200));
        // incPanel.setPreferredSize(new Dimension(100,70));
        // butPanel.setPreferredSize(new Dimension(75,20));


        incTextPanel = new JPanel();
        incTextPanel.setLayout(new BorderLayout());

        
        incText = new JLabel("Choosen Include");
        incTextField = new JTextField();
        // incTextField.setPreferredSize(new Dimension(180,10));

        incTextPanel.add(incText, BorderLayout.WEST);
        incTextPanel.add(incTextField, BorderLayout.CENTER);

        // Creating Button Panel
        button = new JButton("Choose");
        close = new JButton("Close");
        button.addActionListener(this);
        close.addActionListener(this);       
        
        butPanel.setLayout(new BorderLayout());
        butPanel.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));

        // selPanel.add(combox, BorderLayout.NORTH);
        butPanel.add(button, BorderLayout.EAST);
        butPanel.add(close, BorderLayout.WEST);

        selPanel.setLayout(new BorderLayout());
        
        
        incPanel.add(selPanel, BorderLayout.NORTH);
        // incPanel.add(incTextPanel, BorderLayout.CENTER);
        incPanel.add(butPanel, BorderLayout.SOUTH);                        
    }


    
    // Creating the Image-Tab
    private void createTabImg() {
        imgPanel = new JPanel();
        img = new JLabel("Choose Another Image");
        imgPanel.setLayout(new BorderLayout());
        // getImages();
        // imgPanel.add(imbox, BorderLayout.NORTH);
        
        imgbutton = new JButton("Choose");
        imgclose = new JButton("Close");
        imgbutton.addActionListener(this);
        imgclose.addActionListener(this);
        imgbutPanel = new JPanel();

        imgbutPanel.setLayout(new BorderLayout());
        imgbutPanel.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        imgbutPanel.add(imgbutton, BorderLayout.EAST);
        imgbutPanel.add(imgclose, BorderLayout.WEST);
        imgPanel.add(imgbutPanel, BorderLayout.SOUTH);
        
    }



    // * Method not in use at the Moment, Combo-Box is replaced by
    // * the JTree --> Will be removed l8ter
    private void getDocument() {
        
        incElements = info.getIncludeElements();
        incImages = info.getImages();
        
        combox = new JComboBox();
        combox.addItemListener(this);
        

        for (int i=0; i<incElements.length; i++) {
            System.out.println("Inc " + incElements[i]);
            combox.addItem(incElements[i]);             
        }
        
        
        combox.setMaximumRowCount(4);
        combox.setEnabled(true);
    }



    // * Method not in use at the Moment, Combo-Box is replaced by
    // * the JTree --> Will be removed l8ter
    private void getImages() {
               
        imbox = new JComboBox();
        imbox.addItemListener(this);
        

        for (int i=0; i<incImages.length; i++) {
            imbox.addItem(incImages[i]);             
        }

        imbox.setMaximumRowCount(4);
        imbox.setEnabled(true);                
    }



    // Setting the Host Name
    public void setHost(String base) {
        int pos = 0;
                    
        for (int k = 0; k < 3; k++) {
            int neupos = base.indexOf("/");
            String temp = base.substring(0, base.indexOf("/"));
            pos = pos + base.indexOf("/") + 1;
            base = base.substring(neupos + 1, base.length());            
        }
        
        this.host = documentBase.substring(0, pos);
    }

    
    // Returns the Host Name
    public String getHost() {
        return this.host;
    }

    


    // this method isnt needed at the Moment, and will be removed l8ter    
    public void itemStateChanged(ItemEvent e) {        
        
        for (int i=0; i<incElements.length; i++) {
            if (e.getItem().equals(incElements[i])) {
                incTextField.setText(incElements[i]);
                actInclude = incElements[i];
                break;
            }
                
        }

        if (incImages.length > 0) {
            for (int j=0; j<incImages.length; j++) {
                if (e.getItem().equals(incImages[j])) {

                    // System.out.println(" IMAGE PATH = " + path);
                                        
                    String path = incImages[j];
                    System.out.println(" IMAGE PATH = " + path);
                    String addPath = path.substring(path.indexOf("\"")+1, path.lastIndexOf("\""));                   


                    String url = getHost() + addPath;


                   

                    try {
                        URL		urli;
                        URLConnection	urlConn;
                        
                        urli = new URL(url);
                        urlConn = urli.openConnection();
                        
                        System.out.println(url);

                        actImage = path;
                        
                        neuImg = new ImageIcon(urli);

                        if ((but == null)) {
                             but = new JButton(neuImg);
                             imgPanel.add(but, BorderLayout.SOUTH);
                             // System.out.println("Here bin ische drinne !!");
                        }
                        else {
                            but.setIcon(neuImg);
                        }
                        
                        
                        break;
                        
                    } catch (Exception es) {
                        System.out.println("Couldnt get image");
                        System.out.println("Exception " +  es.getMessage());
                    }
                    
                }
                
            }
            
        }        
        
    }
    

    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == close) {
            // stop();
            applet.setFrameShow(false);
            dispose();
        }

        if (e.getSource() == imgclose) {
            applet.setFrameShow(false);
            dispose();
        }
        
        
        if (e.getSource() == button) {
            applet.setIncText(actInclude);             
        }

        if (e.getSource() == imgbutton) {
            System.out.println("ActImage : " + actImage);
            applet.setIncText(actImage);
            
        }
        
    }



    // Method builds the JTree
    public JTree buildTree(String [] url) {
        
        JTree temptree;

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Hallo");
        DefaultMutableTreeNode root_neu = new DefaultMutableTreeNode();
    
        DefaultTreeModel treeModel = new DefaultTreeModel( root );
    

        for (int i=0; i<url.length; i++) {
            
            root_neu = root;

            String path = url[i];
                    
            while (path.indexOf("/") > 0) {
                String knoten = path.substring(0, path.indexOf("/"));                
                
                Enumeration en = root_neu.children();
                
                int count = 0;                
                boolean found = false;
                DefaultMutableTreeNode temp_node = new DefaultMutableTreeNode();
                
                while (en.hasMoreElements()) {                    
                    count ++;
                    temp_node = (DefaultMutableTreeNode) en.nextElement();
                    String tempStr = temp_node.toString();
                    // System.out.println("Found:" +  tempStr);
                    // System.out.println("Knoten " + temp_node);                    
                    
                    if (tempStr.equals(knoten)) {
                        found = true;
                        break;
                        // root_neu = node;
                        
                    }
                    else {
                        found = false;                                
                        
                    }                    
                    
                }
                
            
                if (count == 0) {                    
                    path = path.substring(knoten.length() + 1, path.length());
                
                    DefaultMutableTreeNode node = new DefaultMutableTreeNode(knoten);
                    root_neu.add(node);
                    root_neu = node;
                    
                    // System.out.println("Root-Knoten: " + root_neu.toString());                
                }

                
                else {
                    if (found) {
                        // System.out.println("Knoten Found");
                        path = path.substring(knoten.length() + 1, path.length());
                        System.out.println("ROOOT-NEU ---> " + root_neu.toString());
                        DefaultTreeModel trModel = new DefaultTreeModel(root_neu.getRoot());
                        
                        root_neu = temp_node;
                        // System.out.println(trModel.getChild(knoten,0).toString());
                        // root_neu = root_neu.getNextNode();
                        // System.out.println("Sein Nexter knoten ist " + root_neu.toString());
                        
                    }
                    else 
                        
                        {
                            // System.out.println("Knoten not Found");
                            // System.out.println("Pfad " + path);
                            // System.out.println("Knoten " + knoten);
                            path = path.substring(knoten.length() + 1, path.length());
                            // System.out.println("Neu Pfad: " + path);
                            
                            DefaultMutableTreeNode node = new DefaultMutableTreeNode(knoten);
                            root_neu.add(node);
                            root_neu = node;
                            // System.out.println("Root-Knoten: " + root_neu.toString());
                            
                            
                        }
                    
                }                
                
            } 
            
            DefaultMutableTreeNode element = new DefaultMutableTreeNode(path);
            root_neu.add(element);
        }        
        temptree = new JTree(treeModel);
        temptree.addTreeSelectionListener(this);
        
        return temptree;        
    }


    public void valueChanged(TreeSelectionEvent e) {

        if (e.getSource() == tree) {

        
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            // System.out.println("in VALUE CHANGER");
                    
                    
            if (node == null) {
                System.out.println("Node = Null");
                return;
            }
        
            Object nodeInfo = node.getUserObject();
            if (node.isLeaf()) {
                String property = (String)nodeInfo;   
                // System.out.println("Property + " + property);
                
                TreeNode[] trnode = node.getPath();                
                String pfad = gettingRootPath(trnode);
                
                System.out.println("Path to Root " + pfad);                
                
                // remove last slash
                String pfadNeu = pfad.substring(0, pfad.length() - 1);
                
                // getting the Part
                String partName = pfadNeu.substring(pfadNeu.lastIndexOf("/") + 1, pfadNeu.length());
                // System.out.println("part name " + partName);
                
                String realPath = pfadNeu.substring(0, pfadNeu.lastIndexOf("/"));
                // System.out.println("real Path " + realPath);
                
                String includeElement = "<pfx:include path=\"" + realPath + "\" part=\"" + partName + "\" />";
                
                // System.out.println(includeElement);
                                                
                
                // incTextField.setText(includeElement);
                
                this.actInclude = includeElement;
                
                
                
                

            } else {
                System.out.println("Error while retrieving nodes");
            }

        }

        if (e.getSource() == jimagetree) {
             
            DefaultMutableTreeNode nodeImg = (DefaultMutableTreeNode) jimagetree.getLastSelectedPathComponent();
            System.out.println("in VALUE CHANGER");
                    
                    
            if (nodeImg == null) {
                System.out.println("Node = Null");
                return;
            }
        
            Object nodeInfoImg = nodeImg.getUserObject();
            if (nodeImg.isLeaf()) {
                String property = (String)nodeInfoImg;   
                

                

                TreeNode[] trnode = nodeImg.getPath();
                
                String pfad = gettingRootPath(trnode);
                
                System.out.println("Pfad " + pfad);
                
                String neuImgPfad = pfad.substring(0, pfad.length()-1);
                
                String imgEl = "<pfx:image path=\"" + neuImgPfad + "\" />";
                
                String url = getHost() + neuImgPfad;
                
                
                try {
                    URL		urli;
                    URLConnection	urlConn;
                    
                    urli = new URL(url);
                    urlConn = urli.openConnection();                    
                    
                    actImage = imgEl;                    
                    neuImg = new ImageIcon(urli);
                    
                    if ((but == null)) {
                        but = new JButton(neuImg);
                        but.setVisible(true);                        
                        imgFilter.add(but);
                    }
                    else {
                        but.setIcon(neuImg);
                    }
                    
                } catch (Exception es) {
                    System.out.println("Couldnt get image");
                    System.out.println("Exception " +  es.getMessage());
                }
                                                                                
            } else {

                System.out.println("Error while getting nodes");
            }

        }        
    }

    public void expandAll(JTree temptree) {
        Enumeration e = ((DefaultMutableTreeNode) temptree.getModel().getRoot()).depthFirstEnumeration();
        for (; e.hasMoreElements();) {
            TreePath t = new TreePath(((DefaultMutableTreeNode)e.nextElement()).getPath());
            temptree.expandPath(t);
        }
    }


    private String gettingRootPath(TreeNode [] trnode) {
        String pfad = new String();
        for (int i = 1; i < trnode.length; i++) {
            // System.out.println(trnode[i].toString());
            pfad = pfad + trnode[i].toString() + "/";            
        }
        return pfad;
    }



    
    public void windowActivated(WindowEvent e)    {
    }
    
    public void windowClosed(WindowEvent e) {
    }
    public void windowClosing(WindowEvent e) {
        // System.exit(0); // Exit program
        applet.setFrameShow(false);
    }
    public void windowDeactivated(WindowEvent e)    {
    }
    public void windowDeiconified(WindowEvent e)    {
    }
    public void windowIconified(WindowEvent e)    {
    }
    public void windowOpened(WindowEvent e)    {
    }
    




    
    
}
