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





public class PfixIncludeDialog extends JFrame implements ItemListener, ActionListener{

    // JFrame dialogFrame;

    JPanel panel;
    JPanel incPanel;
    JPanel imgPanel;
    JPanel incTextPanel;
    JPanel selPanel;
    JPanel butPanel;
    JPanel imgbutPanel;

    JButton imgbutton;
    JButton button;

    ImageIcon neuImg;    
    JButton but;

    JLabel img;
    JLabel incText;

    JTextField incTextField;
    

    JTabbedPane jTabbedPane = new JTabbedPane();
    JComboBox combox;
    JComboBox imbox;

    String [] incElements;
    String [] incImages;

    String documentBase;
    String host;
    PfixTextPane syntaxPane;

    String actInclude = "";
    String actImage = "";
    


    public PfixIncludeDialog(String docBase, PfixTextPane pane) {
        super();

        documentBase = docBase;

        setHost(this.documentBase);
        
        syntaxPane = pane;
        panel = new JPanel();
        setContentPane( panel );
        panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        // panel.setPreferredSize(new Dimension(500,200));
        panel.setLayout(new BorderLayout());
        
        setTitle("PfixDialog Includes/Images");
        
        System.out.println("Hier noch ");
        createTabInc();
        createTabImg();

        jTabbedPane.setLocation(new java.awt.Point(10, 10));
        jTabbedPane.setVisible(true);
        jTabbedPane.setSize(new java.awt.Dimension(550, 600));               

        // jTabbedPane.add(label);

        
        jTabbedPane.addTab("Includes", incPanel);
        jTabbedPane.addTab("Images", imgPanel);                       

        panel.add(jTabbedPane, BorderLayout.CENTER);
        // Dimension dim = new Dimension(600,400);
        // setSize(dim);

        pack();
        show();


        
    }

    private void createTabInc() {
        incPanel = new JPanel();
        selPanel = new JPanel();
        butPanel = new JPanel();
        System.out.println("Here bin ich");
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

        button = new JButton("Choose");
        button.addActionListener(this);

        getDocument();
       
        selPanel.setLayout(new BorderLayout());
        butPanel.setLayout(new BorderLayout());

        selPanel.add(combox, BorderLayout.NORTH);
        butPanel.add(button, BorderLayout.CENTER);
        
        
        incPanel.add(selPanel, BorderLayout.NORTH);
        incPanel.add(incTextPanel, BorderLayout.CENTER);
        incPanel.add(butPanel, BorderLayout.SOUTH);                        
    }


    

    private void createTabImg() {
        imgPanel = new JPanel();
        img = new JLabel("Choose Another Image");
        imgPanel.setLayout(new BorderLayout());
        getImages();
        imgPanel.add(imbox, BorderLayout.NORTH);

        imgbutton = new JButton("Choose");
        imgbutton.addActionListener(this);
        imgbutPanel = new JPanel();

        imgbutPanel.setLayout(new BorderLayout());
        imgbutPanel.add(imgbutton, BorderLayout.CENTER);
        imgPanel.add(imgbutPanel, BorderLayout.SOUTH);        
        
    }


    private void getDocument() {
        PfixAppletInfo info = new PfixAppletInfo(documentBase);
        
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



    private void getImages() {
        PfixAppletInfo info = new PfixAppletInfo(documentBase);
               
        imbox = new JComboBox();
        imbox.addItemListener(this);
        

        for (int i=0; i<incImages.length; i++) {
            imbox.addItem(incImages[i]);             
        }

        imbox.setMaximumRowCount(4);
        imbox.setEnabled(true);                
    }




    public void setHost(String base) {
        // String base = documentBase;
        int pos = 0;
        
        System.out.println("DocumentBase: " + base);
                    
        for (int k = 0; k < 3; k++) {
            // System.out.println("Count + " + k);
            int neupos = base.indexOf("/");
            // System.out.println("NeuPos " + neupos);
            String temp = base.substring(0, base.indexOf("/"));
            // System.out.println("Temp: " + temp);
            pos = pos + base.indexOf("/") + 1;
            base = base.substring(neupos + 1, base.length());
            // System.out.println("base neu " + base);
            
            
            
        }
        
        this.host = documentBase.substring(0, pos);

        System.out.println("Host found: " + this.host);
    }


    public String getHost() {
        return this.host;
    }

    
    
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
                                        
                    String path = incImages[j];                                       
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
                             imgPanel.add(but, BorderLayout.CENTER);
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
        if (e.getSource() == button) {
            syntaxPane.insertTag(actInclude);
             
        }

        if (e.getSource() == imgbutton) {
            syntaxPane.insertTag(actImage);
            
        }
        
    }
    
    
    
    
}
