package de.schlund.pfixxml;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Category;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import de.schlund.pfixxml.util.Path;
import de.schlund.pfixxml.util.Xml;
import de.schlund.pfixxml.util.Xslt;


/**
 * IncludeDocument.java
 * 
 * 
 * Created: 20021031
 * 
 * @author <a href="mailto:haecker@schlund.de">Joerg Haecker</a>
 * 
 * 
 * This class encapsulates an include-module of the PUSTEFIX-system.
 * A IncludeDocument stores a Document created from a file. Currently
 * there are two types of Documents: mutable and immutable. The user
 * of this class must know which type he wants. 
 * Various administrative data like modification time
 * of the file from which it is created from and more are stored.  
 */
public class IncludeDocument {

    //~ Instance/static variables ..................................................................

    private Document              doc;
    private long                  modTime = 0;
    private static final Category CAT     = Category.getInstance(IncludeDocument.class.getName());
    
    //~ Methods ....................................................................................

    /**
     * Create the internal document.
     * @param path the path in the filesystem to create the document from.
     * @param mutable determine if the document is mutable or not. Any attempts
     * to modify an immutable document will cause an exception.
     */
    public void createDocument(Path path, boolean mutable) throws SAXException, IOException, TransformerException {
        File tmp = path.resolve();
        modTime  = tmp.lastModified();

        if (mutable) {
            doc = Xml.parse(tmp);
        } else {
            doc = Xslt.xmlObjectFromDisc(tmp);
        }
    }

    public Document getDocument() {
        return doc;
    }

    public long getModTime() {
        return modTime;
    }

    public void resetModTime() {
        modTime -= 1l;
    }
}
