package de.schlund.lucefix.core;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import de.schlund.pfixxml.PathFactory;

/**
 * @author schuppi
 * @date Jun 7, 2005
 */
public class IncludeFileHandler extends DefaultHandler implements LexicalHandler {


    public IncludeFileHandler(String path, long lastTouchOfFile) {
        this.lasttouch = lastTouchOfFile;
        this.path = PathFactory.getInstance().makePathStringRelative(path);
        alleDokumente = new Vector();
    }

    private static final String PRODUCT           = "product";
    private static final String PART              = "part";
    private static final String INCLUDE_PARTS     = "include_parts";

    private int                 includepart_count = 0;
    private int                 part_count        = 0;
    private int                 product_count     = 0;
    private Part                currentPart       = null;
    private PreDoc              currentDoc        = null;
    private Collection          alleDokumente     = null;
    private String              path;
    private long                lasttouch;



    public void comment(char[] arg0, int arg1, int arg2) throws SAXException {
        if (currentDoc != null) for (int i = 0; i < arg0.length; i++) {
            currentDoc.addComment(arg0[i]);
        }
    }

    public void startElement(String uri, String name, String qName, Attributes atts) throws SAXException {
        super.startElement(uri, name, qName, atts);
        //        System.out.println("new element " + uri + " | " + name + " | " +
        // qName + " | " + print(atts));
        //        String internal = ("".equals(uri) ? qName : (uri + name));
        String internal = qName;
        if (INCLUDE_PARTS.equals(internal)) {
            includepart_count++;
            //            System.out.println("includepartcount: " + includepart_count);
            return;
        } else if (PART.equals(internal)) {
            part_count++;
            //            System.out.println("partcount: " + part_count);
            if (part_count == 1) {
//                System.out.println("starting new part " + print(atts));
                currentPart = new Part(path, atts.getValue("name"));
                return;
            }
        } else if (PRODUCT.equals(internal)) {
            product_count++;
            //            System.out.println("productcount: " + product_count);
            if (product_count == 1) {
//                System.out.println("starting new product " + print(atts));
                currentDoc = new PreDoc(currentPart.getFilename(), currentPart.getName(), atts.getValue("name"),
                        lasttouch);
                return;
            }
        }
        if (currentDoc != null) {
            // if we are here, this is just a plain odd element
            currentDoc.addTag(internal);
            addAttribs(atts);
        }
    }

    private void addAttribs(Attributes atts) {
        for (int i = 0; i < atts.getLength(); i++) {
            currentDoc.addAttribKey(atts.getLocalName(i));
            currentDoc.addAttribValue(atts.getValue(i));
        }
    }

    public Document[] getScannedDocuments() {
        return (Document[]) getScannedDocumentsAsVector().toArray(new Document[0]);
    }
    
    public Vector getScannedDocumentsAsVector(){
        Vector retval = new Vector();
        for (Iterator iter = alleDokumente.iterator(); iter.hasNext();) {
            PreDoc element = (PreDoc) iter.next();
            retval.add(element.toLuceneDocument());
        }
        return retval;
    }




    public void endElement(String uri, String name, String qName) throws SAXException {
        super.endElement(uri, name, qName);
        //        System.out.println("end element: " + uri + " | " + name + " | " +
        // qName);
        //        String internal = ("".equals(uri) ? qName : (uri + name));
        String internal = qName;
        if (INCLUDE_PARTS.equals(internal)) {
            includepart_count--;
            //            if (includepart_count == 0) System.out.println("ending
            // includepart");
            //            System.out.println("includepartcount: " + includepart_count);
        } else if (PART.equals(internal)) {
            part_count--;
            //            if (part_count == 0) System.out.println("ending part");
            //            System.out.println("partcount: " + part_count);
        } else if (PRODUCT.equals(internal)) {
            product_count--;
            if (product_count == 0) {
                //                System.out.println("ending product");
//                System.out.println(currentDoc);
                alleDokumente.add(currentDoc);
            }
            //            System.out.println("partcount: " + part_count);
        }
        //        System.out.println("start element " + ("".equals(uri) ? qName : (uri
        // +
        // name)));
    }

    public void characters(char ch[], int start, int length) {
        //        System.out.print("Characters: \"");
        for (int i = start; i < start + length; i++) {
            switch (ch[i]) {
                case '\\' :
                    //                    System.out.print("\\\\");
                    break;
                case '"' :
                    //                    System.out.print("\\\"");
                    break;
                case '\n' :
                    //                    System.out.print("\\n");
                    break;
                case '\r' :
                    //                    System.out.print("\\r");
                    break;
                case '\t' :
                    //                    System.out.print("\\t");
                    break;
                default :
                    if (currentDoc != null) currentDoc.addContent(ch[i]);
                    //                    System.out.print(ch[i]);
                    break;
            }
        }
        //        System.out.print("\"\n");
    }



    /**
     * @param atts
     * @return
     */
    private String print(Attributes atts) {
        StringBuffer retv = new StringBuffer();
        for (int i = 0; i < atts.getLength(); i++) {
            retv.append(atts.getLocalName(i));
            retv.append("-");
            retv.append(atts.getValue(i));
        }
        return retv.toString();
    }


    // bla

    public void endCDATA() throws SAXException {
        // TODO Auto-generated method stub

    }

    public void endDTD() throws SAXException {
        // TODO Auto-generated method stub

    }

    public void endEntity(String arg0) throws SAXException {
        // TODO Auto-generated method stub

    }

    public void startCDATA() throws SAXException {
        // TODO Auto-generated method stub

    }

    public void startDTD(String arg0, String arg1, String arg2) throws SAXException {
        // TODO Auto-generated method stub

    }

    public void startEntity(String arg0) throws SAXException {
        // TODO Auto-generated method stub

    }
    
    
    
    
}