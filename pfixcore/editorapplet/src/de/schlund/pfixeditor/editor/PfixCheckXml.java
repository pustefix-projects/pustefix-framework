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



import javax.swing.*;


public class PfixCheckXml {


    JTextComponent textpane;

    public PfixCheckXml(JTextComponent pane) {
        textpane = pane;
        
    }

    
    public boolean checkXML() {

        boolean wellformed;
        boolean result=false;
	ArrayList xmlErrors=null;
        XmlError xErr=null;
        XmlTextProcessor xmlProc=new XmlTextProcessor();
        result=xmlProc.parse("<foo>"+this.textpane.getText()+"</foo>");
        xmlErrors=xmlProc.getXmlErrors();
        // System.out.println("---");
        // System.out.println(xErr.getLine());
        if (result) {
            wellformed = true;
        }
        else {
            wellformed = false;
        }
        return wellformed;
	
    }
    
}
