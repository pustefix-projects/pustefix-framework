package de.schlund.pfixeditor.test;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.plaf.*;
import java.util.*;
import java.util.regex.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;



public class PfixTextPane extends JTextPane {



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
           }
           else {
              int tagEnd = 0;                   
              tagEnd = preString.lastIndexOf(">");
               
              String tag = preString.substring(tagStart, tagEnd) + ">";

                   if (tag.indexOf(" ") > 0) {
                       int blank = tag.indexOf(" ");
                       int endStr = tag.length();
                       StringBuffer strNeu = new StringBuffer(tag);
                       strNeu = strNeu.delete(blank, endStr-1);
                       tag = strNeu.toString();                   
                   }
              
              
              if (stack.contains(tag)) {
                  stack.removeElement(tag);
              }
              else {
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

           // generating new PreString;
           preString = preString.substring(0, tagStart);
                                                       
             }

        }
        catch (Exception exc) {
            // l_error.setText("Not possible here");
            System.out.println("ex " + exc.getMessage());
        }

        if (!(finalTag.equals(""))) {
            String textNeu = saveString + finalTag + endString;
            setText(textNeu);
            // this.checkString();
            setCaretPosition(curPos);
        }
            
        

    }

   



    
    
}
