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



import javax.swing.*;


public class PfixCheckXml {


    JTextComponent textpane;
    String errorMessage;

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
        if (result) {
            wellformed = true;
        }
        else {
        Iterator it=xmlErrors.iterator();
        errorMessage = "";
                    
        while (it.hasNext())
        {
            xErr=(XmlError) it.next();
            errorMessage = errorMessage + "Error at line "+xErr.getLine()+", column "+xErr.getColumn()+", offset:"+ xErr.getOffset() +":" + "   " + xErr.getMessage() + "\n";

        }
            wellformed = false;
        }
        return wellformed;
        
    }

     public boolean checkXML(String zeile) {

        boolean wellformed;
        boolean result=false;
	ArrayList xmlErrors=null;
        XmlError xErr=null;
        XmlTextProcessor xmlProc=new XmlTextProcessor();
        result=xmlProc.parse("<foo>"+ zeile + "</foo>");
        xmlErrors=xmlProc.getXmlErrors();
        if (result) {
            wellformed = true;
        }
        else {
        Iterator it=xmlErrors.iterator();
        errorMessage = "";
                    
        while (it.hasNext())
        {
            xErr=(XmlError) it.next();
            errorMessage = errorMessage + "Error at line "+xErr.getLine()+", column "+xErr.getColumn()+", offset:"+ xErr.getOffset() +":" + "   " + xErr.getMessage() + "\n";

        }
            wellformed = false;
        }
        return wellformed;
        
    }


    


    

    public String getErrorMessage() {
        return errorMessage;
    }
    
}
