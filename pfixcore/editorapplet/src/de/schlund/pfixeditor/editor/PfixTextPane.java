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






/*
* TabSetting Beta-Version included.
* - The Real-Time Tab-Setting after pressing the Return-Key should work.
*
* - Tabbing the whole document or a single row isn't implemented yet,
*   because there are a few bugs. I will implement these methods later.
*
* - I had to change the Font-Types
*
* - Some functions are not in use at the moment, i will remove them later
*
* - in the next step, I will write an own Tab-Setting class, because PfixTextPane
*   is too large and too complex
*
*   (VZaich, 05.11.03);
*/


package de.schlund.pfixeditor.editor;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.plaf.*;
import java.util.*;
// import java.util.regex.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import java.io.*;








public class PfixTextPane extends JTextPane implements UndoableEditListener {

    PfixHighlight hilight;
    PfixDebug log;
    PfixCheckXml xmlChecker;

    public PfixTextPane() {
        
        super();
        log = new PfixDebug(false);
        xmlChecker = new PfixCheckXml(this);
      
        hilight = new PfixHighlight(this);
        setMargin(new Insets(0,0,0,0));
        setEditorKit(new StyledEditorKit());
        Font fontB = new Font("Monospaced",Font.PLAIN, 12);
        setFont(fontB);
        DefaultStyledDocument docNeed = (DefaultStyledDocument) getStyledDocument();


        // pane.setTabSize(0);
        
        // setupPatterns();
        log.debug("Created");
    }

    public boolean getScrollableTracksViewportWidth() {
        return false;
    }
    
    /**
     * overridden from JEditorPane
     * to suppress line wraps
     *
     * @see getScrollableTracksViewportWidth
     */
    public void setSize(Dimension d) {
        if(d.width < getParent().getSize().width) {
            d.width = getParent().getSize().width;
        }
        super.setSize(d);
        //super.setSize(getMaximumSize());
    }

    public void hilightAll() {
        DefaultStyledDocument docNeed = (DefaultStyledDocument) getStyledDocument();
        log.debug("DOC LENGTH " + docNeed.getLength());
        hilight.hilightRegion(0, docNeed.getLength());
        // hilight.hilightAll();
    }
    
    public void realtimeHilight(int code) {
        log.debug("CODE" + code);

        
        if (code == 10) {
            realTabSet(getCaretPosition());
        }

        if (code==113) {
            // indentDocument();
        }
        

        if (code == 112) {
            int selStart = getSelectionStart();
            int selEnd = getSelectionEnd();
            
            if (selStart == selEnd) {
                // indentRegion(0, selStart, selEnd);
            }
            else {
                // indentRegion(0, selStart, selEnd);                
            }                                                
            // realTabSet(getCaretPosition());
        }                        
        hilight.realtimeHilight(code);     
    }
    
    public String removePreBlankActRow(String newDocument) {
        String newString = newDocument.substring(1, newDocument.length());
        log.debug("--> NEW STRING GONNA BE RETURNED " + newString);
                            
        return newString;
        
    }

    public String removeBlankDoc(String newDocument) {
        
        int breakLast       = newDocument.lastIndexOf("\n");
        String preString    = newDocument.substring(0, breakLast + 1);
        String afterString = newDocument.substring(breakLast + 2, newDocument.length());
    
        newDocument = preString + afterString;
        
        return newDocument;

    }


    public String addBlankDoc(String newDocument) {

        int breakLast    = newDocument.lastIndexOf("\n");
        String preString = newDocument.substring(0, breakLast + 1);
        
        preString = preString + " ";
        
        String afterString = newDocument.substring(breakLast + 1, newDocument.length());
        
        newDocument = preString + afterString;                        
        
        return newDocument;
        
    }
    


    
    // Using the older XML-Parser --> gonna be replaced soon
    public void parseStream(String stream) throws Exception {
        log.debug("Start parsing Steam ");
        org.w3c.dom.Document doc;
        
        DocumentBuilderFactory docBuilderFactory;
        DocumentBuilder        docBuilder;
            
        docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setNamespaceAware(true);
        docBuilderFactory.setValidating(false);
        docBuilder       = docBuilderFactory.newDocumentBuilder();
        
        // InputStream is = new InputStream();
        StringBufferInputStream is = new StringBufferInputStream(stream);

        
        doc = docBuilder.parse(is);
        doc.createElementNS("pfx:", "pfx:");

                
    }

    public int getTabPositionActRow(String row) {
        return countBlank(row);
    }

    public int getTabPositionLastRow(String row) {
        int preBlank = 0;
        
        if (row.lastIndexOf("<") > -1) {
            preBlank = row.lastIndexOf("<");
            int preBlankTemp = row.lastIndexOf("</");            
            
            // checking if Last Element's a Close Tag
            if (preBlankTemp == preBlank) {
                preBlank = row.indexOf("<");
            }
        }
        else {
            preBlank = countBlank(row);
            
        }
        
        return preBlank;                
    }
    

    // Method gets the Character-Start of the Actually Row
    public int getTabPosition(String preString, boolean act) {

        int preBlank = 0;
        int preBlankTempOld = 0;

        // checking if road has an elelemnt
        if (preString.lastIndexOf("<") > -1) {
            preBlank = preString.lastIndexOf("<");

            int preBlankTemp = preString.lastIndexOf("</");            

            // checking if Last Element's a Close Tag
            if (preBlankTemp == preBlank) {
                preBlank = preString.indexOf("<");
            }


        }
        
        else {                                   
            preBlankTempOld = countBlank(preString);

            // Getting the Blanks of the actually row        
            if (preBlankTempOld > -1) {
                if (preBlankTempOld < preBlank) {
                    preBlank = preBlankTempOld;
                }
                
            }
        }                          
        return preBlank;        
    }

    

    // getting the Text of The Document
    public String getDocText() {
        DefaultStyledDocument docNeed = (DefaultStyledDocument) getStyledDocument();        
        String text = "";
                        
        try {
            text = docNeed.getText(0, docNeed.getLength());            
        } catch (Exception e) {
            log.debug("Couldnt get Text");            
        } // end try

        return text;
    }
    

    // I will modify this function l8ter. It works, but I don't like it
    public void checkingWrongTags(int prePosTag, String text, int rowBreak, int pos, int endTagPos, String zeile) {

        log.debug("SEARCH TAG FOUND AT " + prePosTag);
        
        String newString = text.substring(0, prePosTag);        
        int    brPos     = newString.lastIndexOf("\n");        
        log.debug("brPos " + brPos);
        
        int preBlankCountNeu = prePosTag - brPos;        
        log.debug("ROW BREAK " + preBlankCountNeu);                                                
        
        // put the tag into the Right Position
        String finalStr = text.substring(0, pos - 1);
        log.debug("FinalStr: " + finalStr);
        
        int preLastBreak = finalStr.lastIndexOf("\n");
        
        int checkSize = endTagPos - preLastBreak;        
        log.debug("CHECKSIZE " + checkSize);
        log.debug("PRE BLANK COUNT " + preBlankCountNeu);        
        log.debug("PreLastBreak " + preLastBreak);

        if (checkSize == preLastBreak) {
            // I'm not sure what this means...
        }
        else {
            if (checkSize > 0) {
                if (checkSize > 4) {                    
                    log.debug("Im gonna remove you");
                    // int diff = preBlankCountNeu - zaehlerPos;

                    String newLine = zeile.substring(1, zeile.length());
                    int zaehlerPos = countBlank(newLine);
                    
                    int diff = zaehlerPos - preBlankCountNeu;
                    log.debug("ZAEHLER " + zaehlerPos);
                   
                    for (int i=0; i < diff + 1; i ++) {
                        removeBlankOnly(preLastBreak + 1); 
                    }
                    
                    // removeBlankOnly(preLastBreak + 1);
                    // setTabIndent(getCaretPosition()-1);
                    // int diff = preBlankCountNeu - zaehlerPos;
                    // for (int i = 0; i < zaehlerPos - 4; i++) {
                    for (int i = 0; i < preBlankCountNeu - 1; i ++) {                                                     
                        log.debug("i " + i);
                        setBlank(getCaretPosition()-1);                        
                    }                                                            
                }
            }
            else {
                // setTabIndent(rowBreak);
                log.debug("ERREURE FOUND");
                String newLine = zeile.substring(1, zeile.length());
                // int zaehlerPos = countBlank(newLine);
                int zaehlerPos = newLine.indexOf("<");
                boolean addTab = true;

                if (zaehlerPos < 1) {
                     zaehlerPos = countBlank(newLine);
                     addTab = false;
                     
                }                                
                log.debug("ZAEHLER-POS " + zaehlerPos);
                for (int i = 0; i < zaehlerPos; i++) {
                    log.debug("i " + i);
                    setBlank(rowBreak);                    
                }
                if (addTab) {
                     setTabIndent(rowBreak);
                }                                
            }                                                
        }                
    }    

    // This should work, but the XML-Parser have to be replaced
    public void realTabSet(int pos) {
        log.debug("--->--->--->--->STARTING Real Tab Indent <<---<<-----<<----");
        DefaultStyledDocument docNeed = (DefaultStyledDocument) getStyledDocument();        
        String zeile = "";
        String text = "";
                        
        try {
            text = docNeed.getText(0, pos);            
        } catch (Exception e) {
            log.debug("Couldnt get Text");            
        } // end try

        int rowBreak = text.lastIndexOf("\n");
        int startBreak = text.indexOf("\n");
        log.debug("END: " + rowBreak);
       

        if (rowBreak == startBreak) {
            startBreak = 0;
        }
        else {
            String tempString = text.substring(0, rowBreak - 1);
            startBreak = tempString.lastIndexOf("\n");            
        } // end if rowBreak == startBreak
        
       
        if (rowBreak > -1 && startBreak > -1) {
            log.debug("START " + startBreak);
            log.debug("ROW " + rowBreak);
            zeile = text.substring(startBreak, rowBreak);
            log.debug("Zeile: " + zeile);
            
            StringTokenizer token = new StringTokenizer(text, "\n");
            
            log.debug("TOKEN FOUND " + token.countTokens());

            // Counting Blanks in every row
                int preBlankCount = zeile.lastIndexOf("<");
                int preBlankTemp = zeile.lastIndexOf("</");
                log.debug("PREBLANKCOUNT " + preBlankCount);
                log.debug("PREBLANKTEMP " + preBlankTemp);

                if (preBlankTemp == preBlankCount) {
                     preBlankCount = zeile.indexOf("<");
                }
                else {
                    
                    
                }
                                                
                // int preBlankCount = zeile.IndexOf("<");
            
                log.debug("PRE BLANK COUNT !" + preBlankCount);
                                                                            
                try {
                    parseStream(zeile);
                    for (int i = 0; i < preBlankCount-1; i++) {
                        setBlank(rowBreak);
                    } // end try

            
                } catch (Exception ex ) {
                    log.debug("WRONG !");            
                    log.debug("EXCEPTION " + ex.getLocalizedMessage());
                    log.debug("Message " + ex.getStackTrace().toString());

                    if (ex.getMessage().toString().indexOf("Dokumentwurzelelement fehlt") > -1) {

                    int endTagPos = text.lastIndexOf("</");

                    if (endTagPos > -1) {
                        String endTag = text.substring(endTagPos, text.length());
                
                        try {
                            endTag = endTag.substring(2, endTag.indexOf(">"));
                        } catch (Exception e) {
                            log.debug("NO END TAG FOUND");
                        }
                        
                        log.debug("END TAG" + endTag);
                    
                        String searchTag = "<" + endTag;                    
                        log.debug("TAG TO SEARCH for:" + searchTag);
                        
                        int prePosTag = text.lastIndexOf(searchTag);                        
                        String preEndTag = text.substring(0, endTagPos);

                        log.debug("PREENDTAG " + preEndTag);


                        if (prePosTag > -1) {                             
                            String help = text.substring(0, prePosTag);                            
                            log.debug("HELP " + help);
                            log.debug("PREENDTAG.lenge " + preEndTag.length());                        
                            log.debug("SEARCH TAG FOUND AT --> OLD" + prePosTag);
                        
                            int rePrePosTag = preEndTag.lastIndexOf("</" + endTag);

                            // int tempTag = endTagPos;                            
                            // int tempPosTag = prePosTag;
                            // String neuString = text.substring(0, rePrePosTag);
                            // int preNeu = help.lastIndexOf(endTag);
                            // int closeNeu = neuString.lastIndexOf("</" + endTag);

                            String realEndTag = "</" + endTag;
                            String helpText = text;
                            String openHelpText = text.substring(0, helpText.lastIndexOf(realEndTag));;

                            int count = 0;

                            while (helpText.lastIndexOf(realEndTag ) >  -1) {
                            
                                log.debug("An End Tag was found");
                                helpText = helpText.substring(0,helpText.lastIndexOf(realEndTag));

                                if (helpText.lastIndexOf(searchTag) > helpText.lastIndexOf(realEndTag)) {
                                    log.debug("Everythings ok");
                                    break;
                               
                                }

                                count ++;
                                // prePosTag = doof.lastIndexOf(searchTag);

                            
                            }
                            
                            log.debug("COUNT " + count);
                            if (count > 0) {
                             

                        
                                for (int i=0; i<count + 1;i++) {
                                    prePosTag = openHelpText.lastIndexOf(searchTag);
                                    log.debug("PrePosTag " + prePosTag);
                                    if (prePosTag > -1) {
                                         openHelpText = openHelpText.substring(0, openHelpText.lastIndexOf(searchTag));
                                    }
                                    
                                    
                                    
                                    
                                }
                            }
                                                
                            log.debug("SEARCH TAG FOUND AT " + prePosTag);
                        
                    
                            if (prePosTag > -1) {
                                checkingWrongTags(prePosTag, text, rowBreak, pos, endTagPos, zeile);
                            }
                            else {
                                log.debug("---");
                            }
                        }
                        else {
                            log.debug("XML-ERROR-FOUND ! CHECK YOUR CODE !"); 
                            
                        }
                        
                        
                    } // end if entTagPos > 1
                    
                    // else
                    else {
                         if (preBlankCount == -1) {
                             String newLine = zeile.substring(1, zeile.length());
                             int zaehlerPos = countBlank(newLine);                           
                             log.debug("ZAEHLER POS" + zaehlerPos);                            
                             for (int i = 0; i < zaehlerPos; i++) {
                                 setBlank(rowBreak);
                             } // end for Schleife                             
                             
                         } // if preBlankCout == - 1
                         else {
                             setTabIndent(rowBreak);
                             for (int i = 0; i < preBlankCount-1; i++) {
                                 setBlank(rowBreak);
                             }
                         }

                    } // ende else if entTag > 1
                } // end if ex.getMessage()
                else {
                    setTabIndent(rowBreak);
                    log.debug("MOEP DUMM DI DUMM");
                    for (int i = 0; i < preBlankCount-1; i++) {
                        setBlank(rowBreak);
                    }                                        
                } // ende else if ex.getMessage();

            } // end First try Block
        } // end if (rowBreak > -1 && startBreak > -1)
        else {

        }
        
    }                                                   

    public int countBlank(String zeile) {
        log.debug("Row BLANK-COUNT: " + zeile);
        int zaehlerPos = 0;
      
        for (int i = 0; i < zeile.length(); i ++) {
            Character car = new Character(zeile.charAt(i));
            // log.debug("CHAR"+car.toString()+"Char");
            // log.debug("CAR" + car.toString() + "TEST");
            
            if (!(car.toString().equals(" "))) {
                // log.debug("CHAR"+car.toString()+"Char");
                if (car.toString().equals("\n")) {
                    log.debug("EMPTY LINE !");
                    zaehlerPos = 0;
                    break;
                }
                
                break;
            }
            zaehlerPos ++;                        
        }

        log.debug("ZEILE LAENGE " + zeile.length());

        if (zeile.length() == zaehlerPos) {
            zaehlerPos = -1;
        }
                                                     
        return zaehlerPos;
    }
    
    public void setTabIndent(int pos) {
        DefaultStyledDocument docNeed = (DefaultStyledDocument) getStyledDocument();
         try {
             docNeed.insertString(pos + 1, "    ", null);
         } catch (Exception e) {
             log.debug("Doenst go as I like it");
             log.debug(e.toString());
         }        
    }


    public void removeBlank(int pos) {
        DefaultStyledDocument docNeed = (DefaultStyledDocument) getStyledDocument();
        try {
            docNeed.remove(pos, 4);
            
        } catch (Exception e) {
            
        }                
    }

    public String setTabIndent() {
        return "    ";
    }

    public String setBlank() {
        return " ";
    }
    


    public void removeBlankOnly(int pos) {
        DefaultStyledDocument docNeed = (DefaultStyledDocument) getStyledDocument();
        try {
            docNeed.remove(pos, 1);            
        } catch (Exception e) {
            
        }                
    }
    

    
    public void setBlank(int pos) {
        DefaultStyledDocument docNeed = (DefaultStyledDocument) getStyledDocument();
        try {
            docNeed.insertString(pos + 1, " ", null);
        } catch (Exception e) {
            log.debug("Exception FOUND !");
        }
        
    }
    
    
    public String getCloseTag(String zeile) {
        log.debug("in GET CLOSE TAG");
        log.debug("ZEILE !!!! " + zeile);
        int startCloseTag = zeile.indexOf("</");

        if (startCloseTag > -1) {
            String endString = zeile.substring(startCloseTag, zeile.length());
            log.debug("endZeile: " + endString);
            
            int closePos = endString.indexOf(">");

            if (closePos > - 1) {                
                String tag = "<" + endString.substring(2, closePos) + ">";                
                return tag;                 
            }
            else {
                return null;
                
            }                                                
        }
        else {
            return null;
        }                        
    }


    // looking for the open - Tag of the Close-Tag
    // and return the number of blanks
    public int getOpenTagBlank(String closeTag, String text) {

        log.debug("in get Open Blank Tag");
        int startOpenTag = text.lastIndexOf(closeTag);
        log.debug("TEXT" + text);

        if (startOpenTag > -1) {
            String preString = text.substring(0, startOpenTag);
            log.debug("PRE STRING + " + preString);
            
            int retBreak = preString.lastIndexOf("\n");
            if (retBreak > - 1) {                
                String zeile = text.substring(retBreak + 1, text.length());
                log.debug("ZEILE " + zeile);
                int countBlanks = countBlank(zeile);
                log.debug("BLANKER COUNT " + countBlanks);
                return countBlanks;                
            }
            else {                
                return -1;     
            }                                     
        }
        else {
            
            return -1;
        }
        
        
        


        
    }
    

    public boolean getSameRow(String zeile, String tag) {
        if (zeile.indexOf(tag) > - 1) {
            return true;
        }
        else {
            return false;
            
        }
    }
    



    public int countCloseTags(String helpText, String realEndTag, String openTag) {

        int count = 0;

        log.debug("in COUNT CLOSE TAG **********************************************");
        log.debug("REAL END TAG " + realEndTag);
        log.debug("REAL OPEN TAG " + openTag);
        log.debug("TEXT -->  " + helpText);
        
        while (helpText.lastIndexOf(realEndTag ) >  -1) {
            
            log.debug("Endtag Found !!!!");
            helpText = helpText.substring(0, helpText.lastIndexOf(realEndTag));
            log.debug("HELP TEXT ! " + helpText);

            if (helpText.lastIndexOf(openTag) > helpText.lastIndexOf(realEndTag)) {
                log.debug("Everythings ok !");
                break;                               
            }            
            count ++;                                         
        }

        return count;        
    }



    // Will be removed l8ter
    public int getDepth2(String preDocument, String openHelpText, String openTag, int count) {
        int stufe = 0;
        int depth = 0;
        int tempStufe = 0;

        
        for (int i=0; i<count;i++) {
            stufe = openHelpText.lastIndexOf(openTag);
            log.debug("OpenHelText " + openHelpText);
            if (openHelpText.lastIndexOf(openTag) > -1) {
                log.debug("HELPTAG FOUND !!!!");
                openHelpText = openHelpText.substring(0, openHelpText.lastIndexOf(openTag));
                tempStufe = stufe;
            }
            else {
                break;
                
            }                                    
        }

        stufe = tempStufe;
                                
        if (stufe > 0) {
            String preHelp = preDocument.substring(0, stufe);
            log.debug("PRE HELP " + preHelp);
            int breakPosNew = preHelp.lastIndexOf("\n");
            log.debug("BREAK POS NEW = " + breakPosNew);
            depth = stufe - breakPosNew - 1;            
        }
        
        log.debug("STUFE !" + stufe);
        log.debug("DEPTH " + depth);

        return depth;
    }

    


    public int getDepth(String preDocument, String openHelpText, String openTag, int count) {
        int stufe = 0;
        int depth = 0;

        
        for (int i=0; i<count + 1;i++) {
            stufe = openHelpText.lastIndexOf(openTag);
            log.debug("OpenHelText " + openHelpText);
            if (openHelpText.lastIndexOf(openTag) > -1) {
                log.debug("HELPTAG FOUND !!!!");
               openHelpText = openHelpText.substring(0, openHelpText.lastIndexOf(openTag));  
            }
            else {
                break;
                
            }                                    
        }

                                
        if (stufe > 0) {
            String preHelp = preDocument.substring(0, stufe);
            int breakPosNew = preHelp.lastIndexOf("\n");
            depth = stufe - breakPosNew - 1;            
        }
        
        log.debug("STUFE !" + stufe);
        log.debug("DEPTH " + depth);

        return depth;
    }



    public void insertTag(String include) {
        int curPos = getCaretPosition();
        String text = "";
        DefaultStyledDocument docNeed = (DefaultStyledDocument) getStyledDocument();
        
        try {
            text = docNeed.getText(0, docNeed.getLength()); 
        }
        catch (Exception ex) {
            
        }

        String saveString = text.substring(0, curPos);
        String endString = text.substring(curPos, text.length());

        String neu = saveString + include + endString;

        setText(neu);
        setCaretPosition(curPos);
        hilightAll();
                    
        
    }
    


    
    public void closeFinalTag() {
        int curPos = getCaretPosition();
        
        DefaultStyledDocument docNeed = (DefaultStyledDocument) getStyledDocument();
        String text = "";
        
        try {
            text = docNeed.getText(0, docNeed.getLength()); 
        }
        catch (Exception ex) {
            
        }
        
        Stack stack = new Stack();
        
        stack.push(new String("hallo"));
        
        int tagStart = 2;
        String preString = "";
        String saveString = text.substring(0, curPos);
        String endString = text.substring(curPos, text.length());
        String finalTag = "";
        
        try {
            preString = text.substring(0, curPos);   
            
            while (tagStart > 1) {
                
                tagStart = preString.lastIndexOf("<");
                log.debug("tagStart " + tagStart);
                log.debug(preString);
                
                int tagStartNext = tagStart + 1;           
                Character car = new Character(preString.charAt(tagStartNext));
                
                if (car.toString().equals("/")) {
                    int tagEnd = preString.lastIndexOf(">");
                    // Building closeTag and put it on stack;                              
                    String closeTag = "<" + preString.substring(tagStart+2, tagEnd)+ ">";
                    stack.addElement(closeTag);
                } else {
                    int tagEnd = 0;                   
                    tagEnd = preString.lastIndexOf(">");
                    
                    String tag = preString.substring(tagStart, tagEnd) + ">";
                    
                    // tag Break --> Tests if Tag already closed
                    int tagBreak = tag.indexOf("/");
                    Character carCommentStart = new Character(tag.charAt(1));
                    
                    if (!(carCommentStart.toString().equals("!"))) {
                        if (tagBreak < 0) {
                            if (tag.indexOf(" ") > 0) {
                                int blank = tag.indexOf(" ");
                                int endStr = tag.length();
                                StringBuffer strNeu = new StringBuffer(tag);
                                strNeu = strNeu.delete(blank, endStr-1);
                                tag = strNeu.toString();                   
                            }
                            
                            if (stack.contains(tag)) {
                                stack.removeElement(tag);
                            } else {
                                StringBuffer strBuf = new StringBuffer(tag);
                                strBuf.insert(1, "/");
                                finalTag = strBuf.toString();
                                
                                if (finalTag.indexOf(" ") > 0) {
                                    int blank = finalTag.indexOf(" ");
                                    int endStr = finalTag.length();
                                    StringBuffer strNeu = new StringBuffer(finalTag);
                                    strNeu = strNeu.delete(blank, endStr-1);
                                    finalTag = strNeu.toString();                                            
                                }
                                break;
                            }
                        }
                    }
                }
                
                // generating new PreString;
                preString = preString.substring(0, tagStart);
            }
        } catch (Exception exc) {
            // l_error.setText("Not possible here");
            log.debug("ex " + exc.getMessage());
        }

        if (!(finalTag.equals(""))) {
            String textNeu = saveString + finalTag + endString;
            setText(textNeu);
            // this.checkString();
            setCaretPosition(curPos);
        }
    }

    public void undoableEditHappened(UndoableEditEvent e) {
        /* if (!getOperation())
           {
           if (compoundEdit == null)
           {
           currentEdit.addEdit(e.getEdit());
           //undo.addEdit(e.getEdit());
           } else
           compoundEdit.addEdit(e.getEdit());
           }*/
    }




  




        


  





    
    
}
