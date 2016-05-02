package org.pustefixframework.xslt;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.Random;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.icl.saxon.om.NamePool;

public class CachedNamePoolTest {
    
    public static void main(String[] args) throws Exception {
        
        long t1 = System.currentTimeMillis();
        //test(new NamePool());
        test(new CachedNamePool());
        long t2 = System.currentTimeMillis();
        System.out.println("Time: " + (t2-t1) + "ms");
        
    }
    
    public static void test(NamePool namePool) throws Exception {
        
        namePool.loadStandardNames();
        Field field = NamePool.class.getDeclaredField("defaultNamePool");
        field.setAccessible(true);
        field.set(null, namePool);
        field.setAccessible(false);
        
        int no = 50;
        TransformerThread[] threads = new TransformerThread[no];
        for(int i = 0; i < no; i++) {
           threads[i] = new TransformerThread();
           threads[i].start();
        }
        for(int i = 0; i < no; i++) {
            threads[i].join();
            if(threads[i].getErrors() > 0) {
                throw new Exception("XSL transformations failed");
            }
        }
    }
    
    private static Document createRandomXML() throws Exception {
        
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = doc.createElement("elements");
        doc.appendChild(root);
        Random random = new Random();
        int no = random.nextInt(30);
        for(int i=0; i<no; i++) {
            addElements(root, random.nextInt(3));
        }
        return doc;
    }
    
    private static void addElements(Element root, int level) {
        
        Random random = new Random();
        int no = random.nextInt(20);
        for(int i=0; i<no; i++) {
            String elementName = "element" + random.nextInt(50);
            Element elem = root.getOwnerDocument().createElement(elementName);
            root.appendChild(elem);
            if(level > 0) {
                addElements(elem, level - 1);
            }
        }
    }
    
    private static Document createRandomXSL() throws Exception {
        
        String xslNamespace = "http://www.w3.org/1999/XSL/Transform";
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().newDocument();
        Element root = doc.createElementNS(xslNamespace, "xsl:stylesheet");
        root.setAttribute("version", "1.0");
        doc.appendChild(root);
        Random random = new Random();
        int no = random.nextInt(10);
        for(int i=0; i<no; i++) {
            Element templateElem = doc.createElementNS(xslNamespace, "xsl:template");
            templateElem.setAttribute("match", "element" + i);
            root.appendChild(templateElem);
            Element elem = doc.createElementNS(xslNamespace, "xsl:apply-templates");
            templateElem.appendChild(elem);
        }
        return doc;
    }
    
    private static class TransformerThread extends Thread {
        
        private int errors;
        
        @Override
        public void run() {
           
            for(int i = 0; i < 100; i++) {
            
                try {
                    Document xmlDoc = createRandomXML();
                    Document xslDoc = createRandomXSL();
                    
                    Source xml = new DOMSource(xmlDoc);
                    Source xsl = new DOMSource(xslDoc);
                    
                    Templates templates = TransformerFactory.newInstance().newTemplates(xsl);
                    Transformer t = templates.newTransformer();
                                      
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    Result res = new StreamResult(out);
                    t.transform(xml, res);
            
                } catch(Exception x) {
                    errors++;
                }
            
            }
        }
        
        int getErrors() {
            return errors;
        }
    
    }
    
}
