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
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.plaf.*;
import java.util.*;
// import java.util.regex.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;



public class PfixTextPane extends JTextPane implements UndoableEditListener {
    PfixHighlight hilight;

    public PfixTextPane() {
        super();
        hilight = new PfixHighlight(this);
        setMargin(new Insets(0,0,0,0));
        setEditorKit(new StyledEditorKit());
        // setupPatterns();
        System.out.println("Created");
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
        hilight.hilightAll();
    }
    
    public void realtimeHilight() {
        hilight.realtimeHilight();     
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

        // setText(neu);
        try {
           docNeed. insertString(curPos, include, null);  
        } catch (Exception e) {
            System.out.println("Document not found");
            
        }                
        setCaretPosition(curPos);
                    
        
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
                System.out.println("tagStart " + tagStart);
                System.out.println(preString);
                
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
            System.out.println("ex " + exc.getMessage());
        }

        if (!(finalTag.equals(""))) {
            try {
                docNeed. insertString(curPos, finalTag, null);  
            } catch (Exception e) {
                System.out.println("Document not found");
            }

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
