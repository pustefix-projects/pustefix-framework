package de.schlund.pfixxml.util.xsltimpl;

import java.io.StringReader;

import javax.xml.transform.sax.SAXSource;

import org.pustefixframework.test.XmlAssert;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.schlund.pfixxml.util.WhiteSpaceStripping;
import de.schlund.pfixxml.util.XMLUtils;
import de.schlund.pfixxml.util.Xml;
import de.schlund.pfixxml.util.XmlSupport;
import de.schlund.pfixxml.util.XsltProvider;
import de.schlund.pfixxml.util.XsltVersion;
import junit.framework.TestCase;

public class XmlSupportTest extends TestCase {

    /**
     * Test include part whitespace stripping by parsing include parts with different global and
     * file local white space settings and comparing the resulting DOM to an expected reference DOM.
     */
    public void testWhitespaceStripping() throws Exception {

        //check global whitespace settings
        checkWhiteSpace("includes.xml", null);
        checkWhiteSpace("includes.xml", new WhiteSpaceStripping());
        checkWhiteSpace("includes_stripped1.xml", new WhiteSpaceStripping("*", null));
        checkWhiteSpace("includes_stripped1.xml", new WhiteSpaceStripping("include_parts part theme div p", null));
        checkWhiteSpace("includes_stripped2.xml", new WhiteSpaceStripping("div p", null));
        checkWhiteSpace("includes_stripped2.xml", new WhiteSpaceStripping("*", "include_parts part theme" ));
        checkWhiteSpace("includes_stripped2.xml", new WhiteSpaceStripping("theme div p", "theme"));

        //check file local whitespace settings
        WhiteSpaceStripping stripping = new WhiteSpaceStripping();
        checkWhiteSpace("includes.xml", "*", null, null);
        checkWhiteSpace("includes.xml", null, null, stripping);
        checkWhiteSpace("includes_stripped1.xml", "*", null, stripping);
        checkWhiteSpace("includes_stripped1.xml", "include_parts part theme div p", null, stripping);
        checkWhiteSpace("includes_stripped2.xml", "div p", null, stripping);
        checkWhiteSpace("includes_stripped2.xml", "*", "include_parts part theme", stripping);

        //check mixed global and file local whitespace settings
        checkWhiteSpace("includes_stripped2.xml", null, "include_parts part theme",
                new WhiteSpaceStripping("*", null));
        checkWhiteSpace("includes_stripped2.xml", "*", null,
                new WhiteSpaceStripping(null, "include_parts part theme"));
    }

    private void checkWhiteSpace(String refResource, WhiteSpaceStripping stripping) throws Exception {

        XmlSupport xmlSupport = XsltProvider.getXmlSupport(XsltVersion.XSLT1);
        Document strippedDoc = xmlSupport.createInternalDOM(new SAXSource(Xml.createXMLReader(),
                new InputSource(getClass().getResourceAsStream("includes.xml"))), stripping);
        Document refDoc = xmlSupport.createInternalDOM(new SAXSource(Xml.createXMLReader(),
                new InputSource(getClass().getResourceAsStream(refResource))));
        XmlAssert.assertEquals(refDoc.getDocumentElement(), strippedDoc.getDocumentElement());
    }

    private void checkWhiteSpace(String refResource, String stripSpace, String preserveSpace,
            WhiteSpaceStripping stripping) throws Exception {

        XmlSupport xmlSupport = XsltProvider.getXmlSupport(XsltVersion.XSLT1);
        Document origDoc = Xml.parseMutable(getClass().getResourceAsStream("includes.xml"));
        Document refDoc = Xml.parseMutable(getClass().getResourceAsStream(refResource));
        if(stripSpace != null) {
            origDoc.getDocumentElement().setAttribute("strip-space", stripSpace);
            refDoc.getDocumentElement().setAttribute("strip-space", stripSpace);
        }
        if(preserveSpace != null) {
            origDoc.getDocumentElement().setAttribute("preserve-space", preserveSpace);
            refDoc.getDocumentElement().setAttribute("preserve-space", preserveSpace);
        }
        Document strippedDoc = xmlSupport.createInternalDOM(new SAXSource(Xml.createXMLReader(),
                new InputSource(new StringReader(XMLUtils.serializeToString(origDoc)))), stripping);
        refDoc = xmlSupport.createInternalDOM(new SAXSource(Xml.createXMLReader(),
                new InputSource(new StringReader(XMLUtils.serializeToString(refDoc)))));
        XmlAssert.assertEquals(refDoc.getDocumentElement(), strippedDoc.getDocumentElement());
    }

}
