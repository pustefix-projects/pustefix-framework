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


public class PfixIncludeDialog extends JFrame implements ItemListener, ActionListener{

    // JFrame dialogFrame;

    JPanel panel;
    JPanel incPanel;
    JPanel imgPanel;
    JPanel incTextPanel;
    JPanel selPanel;
    JPanel butPanel;
    JButton button;


    
    JLabel label;
    JLabel img;
    JLabel incText;

    JTextField incTextField;
    

    JTabbedPane jTabbedPane = new JTabbedPane();
    JComboBox combox;
    JComboBox imbox;


    String [] incElements;
    String [] incImages;

    String documentBase;
    PfixTextPane syntaxPane;
    


    public PfixIncludeDialog(String docBase, PfixTextPane pane) {
        super();

        documentBase = docBase;
        syntaxPane = pane;
        panel = new JPanel();
        setContentPane( panel );
        panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        panel.setPreferredSize(new Dimension(700,800));
        panel.setLayout(new BorderLayout());
        


        setTitle("PfixDialog Includes/Images");
        
        System.out.println("Hier noch ");
        createTabInc();
        createTabImg();



        jTabbedPane.setLocation(new java.awt.Point(10, 10));
        jTabbedPane.setVisible(true);
        jTabbedPane.setSize(new java.awt.Dimension(550, 600));




        
        
        label = new JLabel("Hallo");

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
        selPanel.setPreferredSize(new Dimension(550,300));


        incTextPanel = new JPanel();
        incTextPanel.setLayout(new BorderLayout());

        
        incText = new JLabel("Choosen Include");
        incTextField = new JTextField();

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
        combox.setPreferredSize(new Dimension(200,250));
        // combox.setEditable(true);
        
        
        // panel.add(comboPanel, BorderL        ayout.WEST);
    }



    private void getImages() {
        PfixAppletInfo info = new PfixAppletInfo(documentBase);
        
        // incImages = info.getImages();
        
        imbox = new JComboBox();
        imbox.addItemListener(this);
        

        for (int i=0; i<incImages.length; i++) {
            // System.out.println("Inc " + incElements[i]);
            imbox.addItem(incImages[i]);             
        }

        imbox.setMaximumRowCount(4);
        imbox.setEnabled(true);

        

        
    }

    
    public void itemStateChanged(ItemEvent e) {
        if (e.getItem().equals("Hamburger SV")) {
            syntaxPane.setText("Hamburch iss goil");
            
        }
        
        
        for (int i=0; i<incElements.length; i++) {
            if (e.getItem().equals(incElements[i])) {
                incTextField.setText("Included " + incElements[i]);
                break;
            }
            
        }

        System.out.println("Inc Images " + incImages.length);

        if (incImages.length > 0) {
            for (int j=0; j<incImages.length; j++) {
                if (e.getItem().equals(incImages[j])) {
                    // incTextField.setText("Included " + incElements[i]);
                    
                    String path = incImages[j];
                    
                    System.out.println("PATH " + path);
                    
                    String addPath = path.substring(path.indexOf("/"), path.lastIndexOf("\""));
                    
                    String url = "http://sample1.zaich.ue.schlund.de" + addPath;

                    try {
                        URL			urli;
                        URLConnection	urlConn;
                        
                        urli = new URL(url);
                        urlConn = urli.openConnection();
                        
                        System.out.println(url);
                        
                        ImageIcon neuImg = new ImageIcon(urli);
                        JButton but = new JButton(neuImg);
                        imgPanel.add(but, BorderLayout.CENTER);
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
        if (e.getSource() == "button") {
            syntaxPane.setText(incTextField.getText());
             
        }
        
    }
    
    
    
    
}
