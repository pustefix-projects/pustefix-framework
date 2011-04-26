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

package de.schlund.pfixxml.config;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Properties;

import javax.xml.xpath.XPathExpressionException;

import org.pustefixframework.config.customization.PropertiesBasedCustomizationInfo;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import de.schlund.pfixcore.exception.PustefixRuntimeException;
import de.schlund.pfixcore.util.FileUriTransformer;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;

/**
 * Handler for conditional processing. This handler takes care that only the
 * parts matching certain conditions are parsed, where applicable. Please note
 * that this implementation is NOT thread-safe,
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class CustomizationHandler extends DefaultHandler {
    private final static String DEFAULT_CUS_NS = "http://www.schlund.de/pustefix/customize";

    private final static String DEFAULT_CHOOSE_ELEMENTNAME = "choose";

    private final static String DEFAULT_DOCROOT_ELEMENTNAME = "docroot";

    private final static String DEFAULT_FQDN_ELEMENTNAME = "fqdn";

    private final static String DEFAULT_UID_ELEMENTNAME = "uid";

    private final static String DEFAULT_MACHINE_ELEMENTNAME = "machine";

    private class ParsingInfo implements Cloneable {
        private boolean parsingActive = true;

        private boolean inChoose = false;

        private boolean foundActiveTree = false;

        private boolean triggerEndElement = false;

        @Override
        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private PropertiesBasedCustomizationInfo customizationInfo;

    private DefaultHandler targetHandler;

    private ArrayList<ParsingInfo> stack;

    private String docroot;

    private String fqdn;

    private String machine;

    private String uid;

    private String namespace;

    private String namespaceContent;

    private String elementChoose = DEFAULT_CHOOSE_ELEMENTNAME;

    private String elementDocroot = DEFAULT_DOCROOT_ELEMENTNAME;

    private String elementFqdn = DEFAULT_FQDN_ELEMENTNAME;

    private String elementMachine = DEFAULT_MACHINE_ELEMENTNAME;

    private String elementUid = DEFAULT_UID_ELEMENTNAME;

    private StringBuffer xmlPath = new StringBuffer();

    private String[] matchingPaths = null;

    /**
     * Creates a new customization handler with default configuration
     * 
     * @param targetHandler Handler XML events are forwarded to
     */
    public CustomizationHandler(DefaultHandler targetHandler) {
        this(targetHandler, DEFAULT_CUS_NS);
    }

    /**
     * Creates a new customization handler using the supplied arguments
     * 
     * @param targetHandler Handler events are forwared to
     * @param namespace URI specifying the namespace to expect the customization
     *        elements within
     */
    public CustomizationHandler(DefaultHandler targetHandler, String namespace) {
        this(targetHandler, EnvironmentProperties.getProperties(), namespace,
                namespace);
    }

    /**
     * Creates a new customization handler using the supplied arguments
     * 
     * @param targetHandler Handler events are forwared to
     * @param namespace URI specifying the namespace to expect the customization
     *        elements within
     * @param namespaceContent URI expected for docroot and fqdn tag, can be
     *                         <code>null</code> if this tags should not be
     *                         matched
     */
    public CustomizationHandler(DefaultHandler targetHandler, String namespace,
            String namespaceContent) {
        this(targetHandler, EnvironmentProperties.getProperties(), namespace,
                namespaceContent);
    }

    /**
     * Creates a new customization handler using the supplied arguments
     * 
     * @param targetHandler Handler events are forwared to
     * @param namespace URI specifying the namespace to expect the customization
     *        elements within
     * @param namespaceContent URI expected for docroot and fqdn tag
     * @param pathsToMatch Array containing paths where the choose element is
     *        expected (e.g. "/root/element/otherlement")
     */
    public CustomizationHandler(DefaultHandler targetHandler, String namespace,
            String namespaceContent, String[] pathsToMatch) {
        this(targetHandler, EnvironmentProperties.getProperties(), namespace,
                namespaceContent);
        this.matchingPaths = pathsToMatch;
    }

    /**
     * Creates a new customization handler using the supplied arguments
     * 
     * @param targetHandler Handler events are forwared to
     * @param environmentProps Properties to read variables from
     * @param namespace URI specifying the namespace to expect the customization
     *        elements within
     * @param namespaceContent URI expected for docroot and fqdn tag, can be
     *                         <code>null</code> if this tags should not be
     *                         matched
     */
    public CustomizationHandler(DefaultHandler targetHandler,
            Properties environmentProps, String namespace, String namespaceContent) {
        this.namespace = namespace;
        this.namespaceContent = namespaceContent;
        this.targetHandler = targetHandler;
        Properties props = new Properties(environmentProps);
        // Beware of org.apache.log4j.helpers.OptionConverter.convertSpecialChars(String s),
        // it deletes all \\ !!! So it's impossible to use a windows path here. 
        String docroot = GlobalConfig.getDocroot() + "/";
        docroot = FileUriTransformer.getUriWithoutWindowsDriveSpecifier(docroot);
        if (docroot != null) {
            props.setProperty("docroot", docroot);
        }
        this.docroot = docroot;
        this.fqdn = props.getProperty("fqdn");
        this.machine = props.getProperty("machine");
        this.uid = props.getProperty("uid");
        this.customizationInfo = new PropertiesBasedCustomizationInfo(props);
    }

    public void setFallbackDocroot() {
        // Support for running webapps from source with 'mvn tomcat:run'
        // Set the docroot to the docroot where the core dir is located if running from source
        if(docroot.endsWith("src/main/webapp/")) {
            FileResource doc = ResourceUtil.getFileResourceFromDocroot("/core/");
            try {
                String path = doc.toURL().toURI().getPath();
                if(path.endsWith("/")) path = path.substring(0, path.length()-1);
                path = path.substring(0, path.lastIndexOf("/")+1);
                docroot = path;
                ((PropertiesBasedCustomizationInfo)customizationInfo).getProperties().setProperty("docroot", docroot);
            } catch(URISyntaxException x) {
                throw new PustefixRuntimeException("Error while building docroot path", x);
            } catch(MalformedURLException x) {
                throw new PustefixRuntimeException("Error while building docroot path", x);
            }
        }
    }
    
    private ParsingInfo peekParsingInfo() {
        return (ParsingInfo) this.stack.get(0);
    }

    private void popParsingInfo() {
        this.stack.remove(0);
    }

    private void pushParsingInfo() {
        this.stack.add(0, (ParsingInfo) peekParsingInfo().clone());
        this.peekParsingInfo().triggerEndElement = false;
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        this.targetHandler.setDocumentLocator(locator);
    }

    @Override
    public void startDocument() throws SAXException {
        // Do initialization
        this.stack = new ArrayList<ParsingInfo>();
        this.stack.add(0, new ParsingInfo());

        this.targetHandler.startDocument();
    }

    @Override
    public void endDocument() throws SAXException {
        this.targetHandler.endDocument();
    }

    @Override
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        if (this.peekParsingInfo().parsingActive) {
            this.targetHandler.startPrefixMapping(prefix, uri);
        }
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        if (this.peekParsingInfo().parsingActive) {
            this.targetHandler.endPrefixMapping(prefix);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        // Always push stack for new element
        ParsingInfo parentInfo = this.peekParsingInfo();
        this.pushParsingInfo();

        if (this.peekParsingInfo().parsingActive) {
            if (this.peekParsingInfo().inChoose) {
                if (localName.equals("when") && uri.equals(this.namespace)) {
                    if (!this.peekParsingInfo().foundActiveTree) {
                        String testExp = atts.getValue("test");
                        if (testExp == null) {
                            throw new SAXException(
                                    "Element \"when\" must have \"test\" attribute set!");
                        }
                        if (this.evalXPathExpression(testExp)) {
                            parentInfo.foundActiveTree = true;
                            this.peekParsingInfo().inChoose = false;
                        } else {
                            this.peekParsingInfo().parsingActive = false;
                            this.peekParsingInfo().inChoose = false;
                        }
                    } else {
                        this.peekParsingInfo().parsingActive = false;
                        this.peekParsingInfo().inChoose = false;
                    }
                } else if (localName.equals("otherwise")
                        && uri.equals(this.namespace)) {
                    if (!this.peekParsingInfo().foundActiveTree) {
                        parentInfo.foundActiveTree = true;
                        this.peekParsingInfo().inChoose = false;
                    } else {
                        this.peekParsingInfo().parsingActive = false;
                        this.peekParsingInfo().inChoose = false;
                    }
                } else {
                    this.peekParsingInfo().inChoose = false;
                    throw new SAXException(
                            "Illegal element \""
                                    + qName
                                    + "\": Only elements \"when\" and \"otherwise\" are allowed as children of element \""
                                    + this.elementChoose + "\"!");
                }
            } else if (localName.equals(this.elementChoose)
                    && uri.equals(this.namespace) && currentPathMatches()) {
                this.peekParsingInfo().inChoose = true;
                this.peekParsingInfo().foundActiveTree = false;
            } else if (this.namespaceContent != null
                    && localName.equals(this.elementDocroot)
                    && uri.equals(this.namespaceContent)) {
                if (docroot != null) {
                    targetHandler.characters(docroot.toCharArray(), 0, docroot.length());
                } else {
                    throw new SAXException("Element \"" + qName + "\" is not allowed in packed WAR mode. Please change your configuration to use relative paths instead.");
                }
            } else if (this.namespaceContent != null
                    && localName.equals(this.elementFqdn)
                    && uri.equals(this.namespaceContent)) {
                targetHandler.characters(fqdn.toCharArray(), 0, fqdn.length());
            } else if (this.namespaceContent != null
                    && localName.equals(this.elementMachine)
                    && uri.equals(this.namespaceContent)) {
                targetHandler.characters(machine.toCharArray(), 0, machine
                        .length());
            } else if (this.namespaceContent != null
                    && localName.equals(this.elementUid)
                    && uri.equals(this.namespaceContent)) {
                targetHandler.characters(uid.toCharArray(), 0, uid.length());
            } else {
                this.targetHandler.startElement(uri, localName, qName, atts);
                this.peekParsingInfo().triggerEndElement = true;
                this.xmlPath.append("/");
                this.xmlPath.append(localName);
            }
        }
    }

    private boolean evalXPathExpression(String testExp) throws SAXException {
        try {
            return customizationInfo.evaluateXPathExpression(testExp);
        } catch (XPathExpressionException e) {
            throw new SAXException("Error while evaluating XPath expression \"" + testExp + "\": " + e.getMessage(), e);
        }
    }

    private boolean currentPathMatches() {
        if (matchingPaths == null) {
            return true;
        }
        for (int i = 0; i < matchingPaths.length; i++) {
            if (xmlPath.toString().equals(matchingPaths[i])) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (this.peekParsingInfo().triggerEndElement) {
            // Pass through
            this.targetHandler.endElement(uri, localName, qName);
            this.xmlPath.delete(xmlPath.lastIndexOf("/"), xmlPath.length());
        }
        // Always pop stack
        this.popParsingInfo();
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (this.peekParsingInfo().parsingActive
                && !this.peekParsingInfo().inChoose) {
            this.targetHandler.characters(ch, start, length);
        }
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        if (this.peekParsingInfo().parsingActive) {
            this.targetHandler.ignorableWhitespace(ch, start, length);
        }
    }

    @Override
    public void processingInstruction(String target, String data)
            throws SAXException {
        if (this.peekParsingInfo().parsingActive) {
            this.targetHandler.processingInstruction(target, data);
        }
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
        if (this.peekParsingInfo().parsingActive) {
            this.targetHandler.skippedEntity(name);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#error(org.xml.sax.SAXParseException)
     */
    @Override
    public void error(SAXParseException e) throws SAXException {
        if (this.peekParsingInfo().parsingActive) {
            this.targetHandler.error(e);
        } else {
            super.fatalError(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#fatalError(org.xml.sax.SAXParseException)
     */
    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        if (this.peekParsingInfo().parsingActive) {
            this.targetHandler.fatalError(e);
        } else {
            super.fatalError(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#notationDecl(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void notationDecl(String name, String publicId, String systemId)
            throws SAXException {
        if (this.peekParsingInfo().parsingActive) {
            this.targetHandler.notationDecl(name, publicId, systemId);
        } else {
            super.notationDecl(name, publicId, systemId);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#resolveEntity(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId)
            throws IOException, SAXException {
        if (this.peekParsingInfo().parsingActive) {
            return this.targetHandler.resolveEntity(publicId, systemId);
        } else {
            return super.resolveEntity(publicId, systemId);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#unparsedEntityDecl(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void unparsedEntityDecl(String name, String publicId,
            String systemId, String notationName) throws SAXException {
        if (this.peekParsingInfo().parsingActive) {
            this.targetHandler.unparsedEntityDecl(name, publicId, systemId,
                    notationName);
        } else {
            super.unparsedEntityDecl(name, publicId, systemId, notationName);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#warning(org.xml.sax.SAXParseException)
     */
    @Override
    public void warning(SAXParseException e) throws SAXException {
        if (this.peekParsingInfo().parsingActive) {
            this.targetHandler.warning(e);
        } else {
            super.warning(e);
        }
    }

}
