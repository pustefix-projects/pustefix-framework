/* HAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAALOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO */

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
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;

public class PfixHighlight extends DefaultStyledDocument {

    PfixDebug log = new PfixDebug(true);

    PfixTextPane textpane;

    private Color colUnset = new Color(0,0,0);
    private Color colParam = new Color(34,170,0);
    private Color colElement = new Color(255,170,68);
    // private Color colElement = new Color(ffaa44);
    
    private Color colAttr = new Color(0,0,250);
    private Color colComment =  new Color(153,153,153);

    
    private boolean inComment = false;
    private boolean inElement = false;
    private boolean inParam = false;
    private boolean inAttribute = false;
    private boolean preFixSet = false;
    private boolean inBlank = false;
    private int     preFixMode = 0;
    private boolean inUp = false;
    

    private boolean closedComment = false;
    private boolean paramUnsetter = false;
    private boolean elemUnsetter = false;
    private boolean elemStartUnsetter = false;
    private boolean commentUnsetter = false;
    private boolean prefixUnsetter = false;

    private String status = "none";

    private boolean styleUnsetter = false;

    private String[] prefixes = new String[3]; {
     prefixes[0] = "pfx";
     prefixes[1] = "xsl";
     prefixes[2] = "ixsl";
    };    
        

    public PfixHighlight(PfixTextPane pane) {
        textpane = pane;
    }


    public void setInComment(boolean bol) {
        this.inComment = bol;
    }

    public boolean getInComment() {
        return this.inComment;
    }

    public void setInElement(boolean bol) {
        this.inElement = bol;
    }

    public boolean getInElement() {
        return this.inElement;
    }

    public void setInParam(boolean bol) {
        this.inParam = bol;
    }

    public boolean getInParam() {
        return this.inParam;
    }

    public void setInAttribute(boolean bol) {
        this.inAttribute = bol;
    }

    public boolean getInAttribute() {
        return this.inAttribute;
    }


    public void setInBlank(boolean bol) {
        this.inBlank = bol;
    }

    public boolean getInBlank() {
        return this.inBlank;
    }
    
    private void setInUp(boolean bol) {
        this.inUp = bol;
    }

    public boolean getInUp() {
        return this.inUp;
    }

    
    public void setPrefixCol(int index) {
        MutableAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setBold (attr, true);
        Color color = getPrefixCol(index);                   
        StyleConstants.setForeground(attr, color);        
        textpane.setCharacterAttributes(attr, false);
    }


    public void setPrefixCol(int start, int end, int index) {
        MutableAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setBold (attr, true);
        Color color = getPrefixCol(index);                   
        StyleConstants.setForeground(attr, color);
        log.debug("START " + start);
        log.debug("END " + end);
        textpane.getStyledDocument().setCharacterAttributes(start, end, attr, false);
    }


    public void colorizeAll(String stati) {
        Color color = getStatiCol(stati);
        MutableAttributeSet attr = new SimpleAttributeSet();
        if (stati.equals("element")) {
          StyleConstants.setBold (attr, true);    
        }
        else {
          StyleConstants.setBold (attr, false);    
        }
        
        StyleConstants.setForeground(attr, color);
        textpane.setCharacterAttributes(attr, false);
    }

    public void colorizeAll(String stati, int start, int end) {
        Color color = getStatiCol(stati);
        MutableAttributeSet attr = new SimpleAttributeSet();
        if (stati.equals("element")) {
            StyleConstants.setBold (attr, true);
        }
        else {
             StyleConstants.setBold (attr, false); 
        }
        
        StyleConstants.setForeground(attr, color);
        try {
             textpane.getStyledDocument().setCharacterAttributes(start, end, attr, false);
        } catch (Exception e) {
            log.debug("Message " + e.getMessage());
        }
        
       
    }

    public Color getPrefixCol(int index) {
        Color color;

        switch (index) {
        case 0:
            color = new Color(0,0,170); 
            break;

        
        case 1:
            color = new Color(27,92,95);   
            break;

            
        case 2:
            color = new Color(204,68,170);   
            break;



        default:
            color = new Color(0,0,0);
            break;
        }

        
        return color;
    }




    public Color getStatiCol(String stati) {
        Color color = new Color(0,0,0);
        if (stati.equals("element")) {       
            color = colElement;
        }

        if (stati.equals("attribute")) {
            color = colAttr;
        }

        if (stati.equals("param")) {
            color = colParam;
        }

        return color;
    }

    public void unsetStyle() {
          MutableAttributeSet attr = new SimpleAttributeSet();
          StyleConstants.setItalic (attr, false);
          StyleConstants.setBold (attr, false);
          textpane.setCharacterAttributes(attr, false);
          StyleConstants.setForeground(attr, colUnset);
          textpane.setCharacterAttributes(attr, false);
    }

    public void unsetStyle(int start, int end) {
          MutableAttributeSet attr = new SimpleAttributeSet();
          StyleConstants.setItalic (attr, false);
          StyleConstants.setBold (attr, false);
          textpane.setCharacterAttributes(attr, false);
          StyleConstants.setForeground(attr, colUnset);
          textpane.getStyledDocument().setCharacterAttributes(start, end, attr, false);       
    }

    public void setCommentStyle() {
        MutableAttributeSet attr = new SimpleAttributeSet();
        // StyleConstants.setItalic (attr, true);
        StyleConstants.setForeground(attr, colComment);
        textpane.setCharacterAttributes(attr, false);       
    }

    public void setCommentStyle(int start, int end) {
        MutableAttributeSet attr = new SimpleAttributeSet();
        // StyleConstants.setItalic (attr, true);
        StyleConstants.setForeground(attr, colComment);
        textpane.getStyledDocument().setCharacterAttributes(start, end, attr, false);
    }





    public void realtimeHilight(int code) {
        // log.debug("REEEEEEEEEEEEEEEEEEEEEEEEAAAAL TIMEEEEEEEEEEEEEEEEEEEEEEEEEEEEEe --- HIGHLIGHT ");
        int currentPos = textpane.getCaretPosition();
        boolean makegrey = false;
        


        String newStatus = "none";
        DefaultStyledDocument docNeed = (DefaultStyledDocument) textpane.getStyledDocument();

        // PfixDocument docNeed = (DefaultStyledDocument) textpane.getStyledDocument();
        
        String text = "";
        
        try {
            text = docNeed.getText(0, docNeed.getLength()); 
        }
        catch (Exception ex) {
            
        }


        String preText = text.substring(0, currentPos);
        String afterText = text.substring(currentPos, text.length());
        int comPos = preText.lastIndexOf("<!--");

        int closeIt = preText.lastIndexOf(">");

        int closeTeil = preText.lastIndexOf("-->");

            // setCommentStyle(comPos, afterText.length());
            

        
        
            // very ugly, i know
            if (closeIt == currentPos - 2) {
                log.debug("CLOSE IT !!");
                Character car = new Character(preText.charAt(currentPos - 1));
                Character carStart = new Character('a');

                if (afterText.length() > 0) {
                    carStart = new Character(afterText.charAt(0));   
                }            
                
                if (!(car.toString().equals("<"))) {
                    
                    if (!(carStart.toString().equals("<"))) {
                        unsetStyle(closeIt+1, 1);
                        unsetStyle();
                        newStatus="none";  
                    }
                    
                }
                
            }
        
        
        
        if (comPos > -1) {            
            int endPos = preText.lastIndexOf("-->");
            int afterPos = afterText.indexOf("-->");
            
         
            if (afterPos > -1) {
                // 7 must be added because the Commentstart and the CommentEnd have at 4 resp. 3 Characters
                 setCommentStyle(comPos, afterPos + 7);
                 log.debug("Found Comment End Tag");
            }

            else {
                // 4 must be added because the Comment-Start has 4 Characters
                setCommentStyle(comPos, afterText.length() + 4);                
                
            }
            
            



            
            if (endPos < comPos) {
                newStatus = "comment";
                if (!commentUnsetter) {                
                    this.setInComment(true);
                    setCommentStyle(comPos, 4);
                    setCommentStyle();
                    commentUnsetter = true;
                    
                }
            
            }
            else {
                commentUnsetter = false;
                this.setInComment(false);
                // hilightRegion(endPos + 3, docNeed.getLength());
                
                // unsetStyle();
                newStatus="none";
                // closedComment = true;
                 
            }
            
            
            
        }

        log.debug("---------------- NOT CHECKING COMMENT !!!!!-----------------------------");
                        
        if (!this.getInComment()) {
            int elPos = preText.lastIndexOf("<");
            int endPos = preText.lastIndexOf(">");
                        
            if (elPos > -1){

                if ((code == 50) || (code == 520)) {
                    log.debug("Param CHECK");

                    Character car = new Character(text.charAt(currentPos - 1));

                    if (car.toString().equals("\"")){
                        log.debug("PARAM SIGN FOUND");

                        String paramText = preText.substring(0, preText.length() - 1);
                        int paramOpenPos = paramText.lastIndexOf("=");
                        int blankPos = paramText.lastIndexOf("\"");
                        

                        log.debug("PARAM TEXT " + paramText);
                        log.debug("paramOpenPos " + paramOpenPos);
                                                                                                              
                        if (blankPos > paramOpenPos) {
                            log.debug("re-Colering of Param");
                            String colString = preText.substring(blankPos, preText.length());
                            log.debug("COLORIZED PARAM: " + colString);

                            int attrStart = preText.lastIndexOf(colString);
                            int sepPos = colString.lastIndexOf("=");
                            int paramStart = attrStart + colString.indexOf("\"");

                            log.debug("attrStart " + attrStart);
                            log.debug("sepPos" + sepPos);
                            log.debug("PARAM START " + paramStart);

                            // colorizeAll("attribute", attrStart, sepPos);
                            colorizeAll("param", paramStart, colString.length());
                            
                        }
                                                                                                 
                    }
                                                            
                }

                if (code == 520) {

                     Character car = new Character(text.charAt(currentPos - 1));

                    if (car.toString().equals("'")){
                        log.debug("PARAM SIGN FOUND");

                        String paramText = preText.substring(0, preText.length() - 1);
                        int paramOpenPos = paramText.lastIndexOf("'");
                        int blankPos = paramText.lastIndexOf(" ");
                        

                        log.debug("PARAM TEXT " + paramText);
                        log.debug("paramOpenPos " + paramOpenPos);
                        
                                           
                                           

                        if (paramOpenPos > blankPos) {
                            log.debug("re-Colering of Param");
                            String colString = preText.substring(blankPos, preText.length());
                            log.debug("COLORIZED PARAM: " + colString);
                        }
                                                                                                
                    }
                     
                }
                                
                

                if (code == 153) {
                    log.debug("REAL CHECK ------------------------------------------ ");

                    int afterTag  =  afterText.indexOf(">");
                    int nextTag   =  afterText.indexOf("<");                    
                    int len       =  docNeed.getLength();

                    log.debug("len " + len);
                    log.debug("elPos " + elPos);

                    int diff = len - elPos;

                    if (diff > 1) {

                        log.debug("-- Zeichen " + preText.lastIndexOf("-->"));
                        log.debug("CurrentPos" + currentPos);

                        int difference = endPos - preText.lastIndexOf("-->");

                        if (difference != 2) {                            
                            Character car = new Character(text.charAt(currentPos - 1));
                            log.debug("PRESSDED CAR " + car.toString());



                            if (car.toString().equals("<")) {
                                log.debug("OPEN TAG FOUND");
                                log.debug(".... LOOKING for  NEXT REGIONS");

                                if (afterTag > -1) {
                                    log.debug("CLOSE TAG FOUND");
                                    log.debug("REGION CLOSING");
                                     hilightRegion(currentPos-1, currentPos + afterTag);  
                                }
                                else {
                                    log.debug("NO NEXT TAG FOUND");
                                    log.debug("Highlighting the whole Document");
                                    hilightRegion(currentPos-1, len-1);                                                                                                                                           }                                                                                                                                 
                            }

                            if (car.toString().equals(">")) {
                                log.debug("CLOSE TAG PLACED");
                                                             
                                if (nextTag > -1) {
                                    log.debug("FOUND NEXT TAG");
                                    log.debug("UNSET STYLE TO NEXT TAG");
                                     unsetStyle(currentPos, nextTag);
                                }
                                else {
                                    log.debug("NO NEXT TAG FOUND");
                                    log.debug("UNSET THE WHOLE DOCUMENT");
                                    unsetStyle(currentPos-1, len-1);                                                                                                            
                                }                                
                                
                                String     preTextNeu      =   preText.substring(0, preText.length() - 1);
                                int        endLastTag      =   preTextNeu.lastIndexOf(">");
                                int        startLastTag    =   preTextNeu.lastIndexOf("<");

                                
                                // Neccesary for re-Highlighting the whole Tag
                                
                                if (startLastTag > -1) {
                                    
                                    if (startLastTag > endLastTag) {
                                        log.debug("TAG CLOSES TAG -- re-HIGHLIGHT TAG");
                                        hilightRegion(startLastTag, currentPos-1);
                                    }
                                    
                                }                                                                                                                                
                                
                            }
                                                                                     
                            /*    
                        

                        // if (!closedComment) {
                           
                            
                        
                        // if (nextTag < afterTag) {
                        //    if (nextTag > -1) {
                        //       hilightRegion(elPos, elPos + nextTag);   
                        //    }
                            
                             
                        // }

                        // else {
                             

                            
                            
                            Character car = new Character(text.charAt(currentPos - 1));
                            log.debug("CAR " + car.toString());
                            log.debug("LENGE GESAMT " + len);


                            
                            if (afterTag > -1) {
                                log.debug("Hallo bin drin");
                                log.debug("-------------");
                                log.debug("el POS " + elPos);
                                int coco = elPos + afterTag;
                                log.debug("el Pos + after" + coco);

                                
                                
                                if (car.toString().equals("<")) {
                                    hilightRegion(elPos, elPos + afterTag);
                                }
                                else {
                                     int startLastElement = preText.lastIndexOf("<");
                                     int endLastElement = preText.lastIndexOf(">");

                                     log.debug("CLOSE TAG - CLOSE NOW ELEMENT");
                                     log.debug("start " + startLastElement);
                                     log.debug("end " + endLastElement);

                                     if (startLastElement > endLastElement) {
                                         log.debug("----> <-----");
                                         hilightRegion(startLastElement, currentPos);
                                }
                                
                                  
                                    
                                }
                                
                     
                            }

                            if (nextTag > 1) {
                                log.debug("Next Tag > 1");
                                if (car.toString().equals(">")) {
                                    log.debug("NEXT TAG CLOSING !!!");
                                    // hilightRegion(currentPos, nextTag);
                                    unsetStyle(currentPos, nextTag);
                                }
                                
                                 
                            }
                            
                    
                            /*

                                int startLastElement = preText.lastIndexOf("<");
                                int endLastElement = preText.lastIndexOf(">");

                                log.debug("start " + startLastElement);
                                log.debug("end " + endLastElement);

                                /*
                                if (startLastElement > endLastElement) {
                                    log.debug("----> <-----");
                                    hilightRegion(startLastElement, currentPos);
                                }

                            */

                            /*
                                if (startNextElement > -1) {

                                    hilightRegion(elPos, currentPos);
                                    

                                    // log.debug("Start Next Element: " + startNextElement);
                                    // hilightRegion(elPos, elPos + startNextElement);
                                    // hilightRegion(elPos, len);
                                    
                                    } */
                    
                    
                                // }
                         
                             }


                        
                        else {
                             hilightRegion(endPos-2, docNeed.getLength());
                        }
                        
                        
                    }
                     
                    
                }
                
                
                if (preText.lastIndexOf(">") < elPos) {
                    newStatus = "element";
                    this.setInElement(true);
                    if (!this.elemStartUnsetter) {
                        colorizeAll("element", elPos, 1);
                        this.elemStartUnsetter = true;
                        // unsetStyle();

                    }
                    
                    int prePos = preText.lastIndexOf(":");
                    
                    if (prePos > elPos) {
                        String fix = preText.substring(elPos + 1, prePos);

                        if (!prefixUnsetter) {                            
                            if (currentPos == prePos + 1) {                                
                                for (int j = 0; j < prefixes.length; j++) {
                                    if (prefixes[j].equals(fix)) {                                    
                                        int lenge = prePos - elPos;
                                        
                                        setPrefixCol(elPos+1, lenge, j);
                                        setPrefixCol(j);
                                        prefixUnsetter = true;
                                        // this.elemStartUnsetter = false;
                                        
                                        
                                    }
                                    String endfix = "/" + prefixes[j];
                                
                                    if (fix.equals(endfix)) {                                    
                                        setPrefixCol(elPos+1, prePos, j);
                                        setPrefixCol(j);
                                        prefixUnsetter = true;
                                        // this.elemStartUnsetter = false; 
                                    }
                                }  
                            }                                                                                        
                        }                                                   
                    }  
                }

                else {
                    
                    int endCom = preText.lastIndexOf(">");
                    int posi = endCom -1 ;
                    Character car = new Character(preText.charAt(posi));


                    if (car.toString().equals("-")) {
                        setCommentStyle(preText.lastIndexOf(">"), 1); 
                    }
                    else {
                      colorizeAll("element", preText.lastIndexOf(">"), 1);   
                    }

                    // This is very ugly ! It fixes the Style-Bug after closing a Tag       
                    int neu = endPos + 2;

                    if (currentPos == endPos + 2) {
                        if (afterText.length() > 0) {
                            Character carNeu = new Character(afterText.charAt(0));

                            // if (!(carNeu.toString().equals("<"))) {
                                unsetStyle();
                                unsetStyle(endPos + 1, 1);
                                // }
                        }                        
                        //                        unsetStyle();
                    }

                    
                    elemStartUnsetter = false;
                    newStatus = "none";
                    this.setInElement(false);
                    this.setInAttribute(false);                    
                }                                 
            }
                       
            
            if ((this.getInElement()) && !(this.getInParam())) {
                int blankPos = preText.lastIndexOf(" ");
                if ((blankPos > -1) && (blankPos > elPos)) {
                    this.setInAttribute(true);
                    newStatus = "attribute";
                    
                }                                                 
            }
       
            
            if (this.getInAttribute()) {
                int paramStart = preText.lastIndexOf("=\"");
                int paramEnd = preText.lastIndexOf("\"");
                if ((paramStart > -1) && (paramStart > elPos)) {
                    this.setInParam(true);
                    if (!paramUnsetter) {
                        colorizeAll("param", paramStart + 1, 1);
                        paramUnsetter = true;
                    }                          
                    newStatus="param";
                }
                          

                if ((this.getInParam() && (paramEnd > paramStart+2))) {
                    this.setInParam(false);
                    this.setInAttribute(true);
                    paramUnsetter = false;               
                    newStatus="attribute";
                }
           }

        }



        if (!(this.status.equals(newStatus))) {

            if (newStatus.equals("none")) {
                if (this.getInElement()) {
                  this.setInElement(false);
                  this.prefixUnsetter = false;
                }
                
                if (this.getInAttribute()) {
                    this.setInAttribute(false);
                }

                if (this.getInParam()) {
                    this.setInParam(false);
                }

                if (this.getInComment()) {
                    this.setInComment(false);
                }
                
                
                
                unsetStyle(currentPos, 1);
                unsetStyle();
                if (this.elemStartUnsetter) {
                    this.elemStartUnsetter = false;
                    this.prefixUnsetter = false;
                }
                
                this.status = newStatus;
            }
            
            else {
                if (!newStatus.equals("comment")) {
                    this.colorizeAll(newStatus);
                    this.prefixUnsetter = false;
                }
                else {
                    setCommentStyle();
                }
                
                this.status = newStatus;
            }
            

            } 
        
    }


     public void hilightAll() {
        // unsetStyle();
        DefaultStyledDocument docNeed = (DefaultStyledDocument) textpane.getStyledDocument();
        String text = "";
        
        try {
            text = docNeed.getText(0, docNeed.getLength()); 
        }
        catch (Exception ex) {
            
        }

        
        unsetStyle(0, text.length());
        this.inComment = false;
        this.inElement = false;
        this.inParam = false;
        this.inAttribute = false;
        this.inUp = false;
        this.inBlank = false;
        
        for (int i=0; i<text.length(); i++) {
           
            Character car = new Character(text.charAt(i));



            if (car.toString().equals("<")) {

                if (((i+1 < text.length()) && (i+2 < text.length()) && (i+3 < text.length()))) {                                     
                    Character car1 = new Character(text.charAt(i+1));
                    Character car2 = new Character(text.charAt(i+2));
                    Character car3 = new Character(text.charAt(i+3));

                    if ((car1.toString().equals("!")) && (car2.toString().equals("-")) && (car3.toString().equals("-"))) {
                        setCommentStyle(i, 4);
                        this.inComment = true;
                        this.inElement = false;
                    }
                }
                
                
                
                                                    
                 
            }


            if (getInComment()) {   

                String restStr = text.substring(i, text.length());

                if (restStr.indexOf("-->") > 0) {
                    setCommentStyle(i, restStr.indexOf("-->") + 3);
                    i = i + restStr.indexOf("-->")+2;
                    unsetStyle();
                   
                }
                else {
                    setCommentStyle(i, restStr.length());
                    unsetStyle();
                    break;
                }
                
                
                               
            }
            
                                
            if (!(getInComment())) {
                if (car.toString().equals("<")) {
                    String endString    =   text.substring(i, text.length());
                    int closePos        =   endString.indexOf(">");
                    String tagValue     =   text.substring(i, i+closePos);
                    int startPos        =   i;
                    StringTokenizer str =   new StringTokenizer(tagValue);
                    
                    while (str.hasMoreTokens()) {
                        String elString = str.nextToken().toString();
                                            

                        int count=0;
                         if (elString.indexOf("\"") > 0) {

                            StringTokenizer paramStr = new StringTokenizer(elString, "\"");
                            int iPosNeu = tagValue.indexOf(elString);

                            // Must be set new, cuz String Tokenizer ignores multiple blanks
                            i = startPos + iPosNeu;

                            while (paramStr.hasMoreTokens()) {

                                // Attribute and Param must be separated
                                String attrString = paramStr.nextToken();
                                int posGleich = attrString.indexOf("=");
                                int neuPos = tagValue.indexOf(attrString);
                                if (posGleich > 0) {
                                    colorizeAll("attribute", i, posGleich + 1);
                                    unsetStyle();
                                    i = i + posGleich + 1;
                                    
                                }
                                else {                                    
                                    colorizeAll("param", i, attrString.length()+2);
                                    unsetStyle();
                                    i = i + attrString.length() + 3;
                                }
                                
                            }                                                        
                            
                            
                            
                         } else {

                             // Colorizing tag
                             colorizeAll("element", i, elString.length() + 1);
                             unsetStyle();
                             
                             // PrefixChecking
                             int posPreStart = elString.indexOf(":");
                             // 

                             // if Prefix, then colorize
                             if (posPreStart > 0) {
                                 String strPrefix = elString.substring(1, posPreStart);
                                 
                                 for (int j=0; j<prefixes.length; j++) {
                                     if (prefixes[j].equals(strPrefix)) {
                                         // setPrefixCol(i+1, posPreStart, j);
                                         
                                         setPrefixCol(i+1, elString.length(), j);
                                     }
                                     String closeFix = "/" + prefixes[j];
                                     if (closeFix.equals(strPrefix)) {
                                         // setPrefixCol(i+1, posPreStart, j);
                                         setPrefixCol(i+1, elString.length(), j);
                                          
                                     }
                                     
                                 }
                                 
                             }
                             i = i + elString.length() + 1;                               
                         }
                    }
                    colorizeAll("element", i-1, 1);
                    i=i-1;;

                    int preEndPos = tagValue.length() - 1;                    
                    Character preEndCar = new Character(tagValue.charAt(preEndPos));

                    if (preEndCar.toString().equals("/")) {
                        colorizeAll("element", startPos+preEndPos,  2);
                        // very ugly here ...
                        unsetStyle(startPos+preEndPos+2, 3);
                        i = i - 3;
                    }
                    


                   
                    unsetStyle();
                
                }
                

          
        }
            else {
                this.inComment = false;
            }
            


        }
        
    }


    public void hilightRegion(int startOffSet, int endOffSet) {

        // unsetStyle();
        DefaultStyledDocument docNeed = (DefaultStyledDocument) textpane.getStyledDocument();
        String text = "";
        
        try {
            text = docNeed.getText(0, docNeed.getLength()); 
        }
        catch (Exception ex) {
            
        }


         log.debug("-->-->-->Highlight Region <<- <<- <<--");
         log.debug("StartOffset " + startOffSet);
         log.debug("endOffSet " + endOffSet);
         log.debug("-->-->--> <<- --<-- <--< -");
        
        // unsetStyle(0, text.length());
        this.inComment = false;
        this.inElement = false;
        this.inParam = false;
        this.inAttribute = false;
        this.inUp = false;
        this.inBlank = false;
        
        for (int i=startOffSet; i< endOffSet; i++) {

            log.debug(" I - Start " + i);
            Character car = new Character(text.charAt(i));
            
            
            
            if (car.toString().equals("<")) {
                
                if (((i+1 < text.length()) && (i+2 < text.length()) && (i+3 < text.length()))) {                                     
                    Character car1 = new Character(text.charAt(i+1));
                    Character car2 = new Character(text.charAt(i+2));
                    Character car3 = new Character(text.charAt(i+3));
                    
                    if ((car1.toString().equals("!")) && (car2.toString().equals("-")) && (car3.toString().equals("-"))) {
                        setCommentStyle(i, 4);
                        this.inComment = true;
                        this.inElement = false;
                    }
                }
                
                
                
                
                
            }
            

            if (getInComment()) {   
                
                String restStr = text.substring(i, text.length());

                if (restStr.indexOf("-->") > 0) {
                    setCommentStyle(i, restStr.indexOf("-->") + 3);
                    i = i + restStr.indexOf("-->")+2;
                    unsetStyle();
                    
                }
                else {
                    setCommentStyle(i, restStr.length());
                    unsetStyle();
                    break;
                }
                
                
                
            }
            
                                
            if (!(getInComment())) {
                
                if (car.toString().equals("<")) {
                    String endString = text.substring(i, text.length());
                    int closePos = endString.indexOf(">");
                    log.debug("REAL CLOSE POS" + closePos);
                    // int closePos = endOffSet+1;
                    
                    log.debug("I " + i);
                    
                    int iPosNeu = i + closePos;
                    log.debug("ClosePos " + iPosNeu);

                    log.debug("TEXT " + text);

                    String tagValue = "";

                    if (closePos > -1) {
                         tagValue = text.substring(i, iPosNeu);
                    }
                                                                                
                    String tagValueSave = tagValue;
                    int startPos = i;



                    int eqPos = tagValue.indexOf("=");
                    
                    int j = 0;
                    
                    while (eqPos > -1) {                        
                        String tempValue = tagValue.substring(0, eqPos + 1);
                        log.debug("--------------------------- TEMP VALUE" + tempValue);

                        int startParam = tempValue.indexOf("\"");
                        int firstBlankPos = tempValue.lastIndexOf(" ");
                        int temp = tempValue.lastIndexOf("\"");
                                                  
                         if (j > 1 && tempValue.indexOf("\"") != 0) {
                             log.debug("---------");
                           
                             colorizeAll("element", i, tempValue.indexOf("\"") + 1);
                         }

                         
                         else {                                                                                                        
                             if (firstBlankPos < temp) {                                                                                     
                                 log.debug("Parameter found ");
                                 colorizeAll("param", i, tempValue.length());
                             }                            
                             else {
                                                                                                                
                                 int blankPos = tempValue.lastIndexOf(" ");

                                 if (blankPos < 0) {
                                     // colorizeAll("param", i, tagValue.length());
                                     break;
                                 }
                                 

                                 String preTag = tempValue.substring(0, blankPos);
                                 String afterTag = tempValue.substring(blankPos, tempValue.length());                                                                                                                                                                                                                        
                                 
                                 if (j==0) {
                                     int posPreStart = tempValue.indexOf(":");
                                     
                                     if (posPreStart > 0) {
                                         String strPrefix = tempValue.substring(1, posPreStart);
                                 
                                         for (int count=0; count<prefixes.length; count++) {
                                             if (prefixes[count].equals(strPrefix)) {
                                                 log.debug("i ---> " + i);
                                                 // setPrefixCol(i+1, posPreStart, j);
                                                 // setPrefixCol(i+1, elString.length(), j);
                                                 log.debug("elString: " + tempValue);
                                                 colorizeAll("element", i, 1);
                                                 setPrefixCol(i+1, tempValue.length()-1, count);
                                                 // break;
                                             }
                                             String closeFix = "/" + prefixes[j];
                                             log.debug("CLOSE FIX " + closeFix);
                                             log.debug("prefix " + prefixes[j]);
                                             if (closeFix.equals(strPrefix)) {
                                                 // setPrefixCol(i+1, posPreStart, j);
                                                 // setPrefixCol(i+1, elString.length(), j);
                                                 colorizeAll("element", i, 1);
                                                 setPrefixCol(i+1, tempValue.length(), count);
                                                 // break;
                                             }                                                    
                                         }
                                         log.debug("Doppelpunkt found");
                                     }
                                     
                                     else {
                                         colorizeAll("element", i-1, preTag.length()+1);    
                                     }                                                                                                                                                         
                                 }
                                 else {                                    
                                     log.debug("i --> " + i);
                                     log.debug("Length " + preTag.length());
                                     colorizeAll("param", i, preTag.length()); 
                                     
                                 }
                                                                                           
                                 j++;
                                 log.debug("After Tag" + afterTag);                                 
                                 
                                 colorizeAll("attribute", i + blankPos, afterTag.length());
                         
                          }

                         }
                         
                                                                      
                         tagValue = tagValue.substring(tempValue.length(), tagValue.length());
                         log.debug("New Value: + " + tagValue);
                         eqPos = tagValue.indexOf("=");
                
                         i = i + tempValue.length();
                         log.debug("I " + i);
                    
                    }
        
                    log.debug(tagValue);
                    log.debug("SCHLEIFEN ENDE ???");            
            
                    i++;
            
                    if (tagValue.indexOf("\"") == 0) {
                        log.debug("Tag Value IndexOf" + text.indexOf(tagValue));
                        log.debug("I " + i);
                        
                        colorizeAll("param", i-1, tagValue.lastIndexOf("\"") + 1);                        
                        i = i + tagValue.lastIndexOf("\"") + 1;
                        
                    }
                    
                    
                    if (j == 0) {
                        log.debug("NULL Round");
                                                
                        String tempValue = tagValue;
                        log.debug("TAGVALUE " + tempValue);
                        int posPreStart = tempValue.indexOf(":");
                                     
                        if (posPreStart > 0) {
                            String strPrefix = tempValue.substring(1, posPreStart);
                            
                            for (int count=0; count<prefixes.length; count++) {
                                if (prefixes[count].equals(strPrefix)) {
                                    // log.debug("i ---> " + i);
                                    // log.debug("elString: " + tempValue);
                                    colorizeAll("element", i-1, 1);
                                    log.debug("Anzahl " + count);
                                    setPrefixCol(i, tempValue.length()-1, count);
                                    // break;
                                }
                                String closeFix = "/" + prefixes[count];
                                log.debug("prefix " + prefixes[count]);
                                log.debug("CloseFix " + closeFix);
                                if (closeFix.equals(strPrefix)) {
                                    colorizeAll("element", i-1, 1);
                                    log.debug("Anzahl " + count);
                                    setPrefixCol(i, tempValue.length(), count);
                                    // break;
                                }                                                    
                            }
                        }
                        else {
                            colorizeAll("element", i-1, tagValue.length()+1);
                        }
                        i = i + tagValue.length();
                        
                    }
                                                                                                                        
                    colorizeAll("element", i-1, 1);
                    i=i-1;;
                    
                    int preEndPos = tagValueSave.length() - 1;
                    log.debug("I " + i);
                    log.debug("PREENDPOS " + preEndPos);

                    if (preEndPos > -1) {
                        Character preEndCar = new Character(tagValueSave.charAt(preEndPos));
                    
                        if (preEndCar.toString().equals("/")) {
                            colorizeAll("element", startPos+preEndPos,  2);
                            // very ugly here ...
                            unsetStyle(startPos+preEndPos+2, 3);
                            i = i - 3;
                        }
                        
                    }

                    unsetStyle();                    
                }                                
            }
            else {
                this.inComment = false;
            }
            
            
            
        }
        
    }
                                            
}
