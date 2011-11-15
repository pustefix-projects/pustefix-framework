/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package de.schlund.pfixxml.util;

import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Wraps a {@link org.xml.sax.helpers.DefaultHandler} around a
 * {@link javax.xml.transform.sax.TransformerHandler}. This adapter can be
 * used, when a DefaultHandler is required by a method, but you want to use a
 * TransformerHandler.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class TransformerHandlerAdapter extends DefaultHandler {
    private TransformerHandler handler;

    public TransformerHandlerAdapter(TransformerHandler handler) {
        this.handler = handler;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        this.handler.characters(ch, start, length);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#endDocument()
     */
    @Override
    public void endDocument() throws SAXException {
        this.handler.endDocument();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        this.handler.endElement(uri, localName, qName);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#endPrefixMapping(java.lang.String)
     */
    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        this.handler.endPrefixMapping(prefix);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#ignorableWhitespace(char[], int,
     *      int)
     */
    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        this.handler.ignorableWhitespace(ch, start, length);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#notationDecl(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void notationDecl(String name, String publicId, String systemId) throws SAXException {
        this.handler.notationDecl(name, publicId, systemId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#processingInstruction(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        this.handler.processingInstruction(target, data);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    @Override
    public void setDocumentLocator(Locator locator) {
        this.handler.setDocumentLocator(locator);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#skippedEntity(java.lang.String)
     */
    @Override
    public void skippedEntity(String name) throws SAXException {
        this.handler.skippedEntity(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#startDocument()
     */
    @Override
    public void startDocument() throws SAXException {
        this.handler.startDocument();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        this.handler.startElement(uri, localName, qName, attributes);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#startPrefixMapping(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        this.handler.startPrefixMapping(prefix, uri);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#unparsedEntityDecl(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException {
        this.handler.unparsedEntityDecl(name, publicId, systemId, notationName);
    }

}
