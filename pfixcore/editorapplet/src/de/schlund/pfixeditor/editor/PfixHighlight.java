package de.schlund.pfixeditor.editor;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;

public class PfixHighlight extends DefaultStyledDocument {

    PfixTextPane textpane;

    private Color colUnset = new Color(0,0,0);
    private Color colParam = new Color(0,200,100);
    private Color colElement = new Color(227,100,100);
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
        textpane.getStyledDocument().setCharacterAttributes(start, end, attr, false);
    }


    public void colorizeAll(String stati) {
        Color color = getStatiCol(stati);
        MutableAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setBold (attr, true);        
        StyleConstants.setForeground(attr, color);
        textpane.setCharacterAttributes(attr, false);
    }

    public void colorizeAll(String stati, int start, int end) {
        Color color = getStatiCol(stati);
        MutableAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setBold (attr, true);        
        StyleConstants.setForeground(attr, color);
        try {
             textpane.getStyledDocument().setCharacterAttributes(start, end, attr, false);
        } catch (Exception e) {
            System.out.println("Message " + e.getMessage());
        }
        
       
    }

    public Color getPrefixCol(int index) {
        Color color;

        switch (index) {
        case 0:
            color = new Color(39,167,244); 
            break;

        
        case 1:
            color = new Color(27,92,95);   
            break;

            
        case 2:
            color = new Color(172,4,165);   
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
        StyleConstants.setItalic (attr, true);
        StyleConstants.setForeground(attr, colComment);
        textpane.setCharacterAttributes(attr, false);       
    }

    public void setCommentStyle(int start, int end) {
        MutableAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setItalic (attr, true);
        StyleConstants.setForeground(attr, colComment);
        textpane.getStyledDocument().setCharacterAttributes(start, end, attr, false);
    }





    public void realtimeHilight() {
        int currentPos = textpane.getCaretPosition();
        boolean makegrey = false;

        String newStatus = "none";
        DefaultStyledDocument docNeed = (DefaultStyledDocument) textpane.getStyledDocument();
        String text = "";
        
        try {
            text = docNeed.getText(0, docNeed.getLength()); 
        }
        catch (Exception ex) {
            
        }


        String preText = text.substring(0, currentPos);
        String afterText = text.substring(currentPos, text.length());
        int comPos = preText.lastIndexOf("<!--");

        if (comPos > -1) {
            int endPos = preText.lastIndexOf("-->");
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
                // unsetStyle();
                newStatus="none";
                 
            }
            
            
            
        }
                        
        if (!this.getInComment()) {
            int elPos = preText.lastIndexOf("<");
            int endPos = preText.lastIndexOf(">");
            
            if (elPos > -1) {
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
                                    setPrefixCol(elPos+1, prePos, j);
                                    prefixUnsetter = true;
                                    // this.elemStartUnsetter = false;
                                
                                }
                                String endfix = "/" + prefixes[j];
                                
                                if (fix.equals(endfix)) {                                    
                                    setPrefixCol(elPos+1, prePos, j);
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
                    int neu = endPos + 1;                
                    if (currentPos == endPos + 1) {
                        unsetStyle();
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
                    String endString = text.substring(i, text.length());
                    int closePos = endString.indexOf(">");
                    String tagValue = text.substring(i, i+closePos);
                    int startPos = i;


                    StringTokenizer str = new StringTokenizer(tagValue);

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

                             // if Prefix, then colorize
                             if (posPreStart > 0) {
                                 String strPrefix = elString.substring(1, posPreStart);
                                 
                                 for (int j=0; j<prefixes.length; j++) {
                                     if (prefixes[j].equals(strPrefix)) {
                                         setPrefixCol(i+1, posPreStart, j);
                                     }
                                     String closeFix = "/" + prefixes[j];
                                     if (closeFix.equals(strPrefix)) {
                                         setPrefixCol(i+1, posPreStart, j);
                                          
                                     }
                                     
                                 }
                                 
                             }
                             i = i + elString.length() + 1;                               
                         }
                    }
                    colorizeAll("element", i-1, 1);
                    i=i-1;
                    unsetStyle();
                
                }
            
        }
            else {
                this.inComment = false;
            }
            


        }
        
    }









    

    
}
