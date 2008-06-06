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
 */

package de.schlund.pfixxml.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.commons.digester.RulesBase;
import org.apache.commons.digester.WithDefaultsRulesWrapper;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.schlund.pfixxml.config.impl.DefaultMatchRule;
import de.schlund.pfixxml.resources.FileResource;

/**
 * Loads properties from a XML file
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public abstract class XMLPropertiesUtil {
    private static final String PROPS_NS = "http://pustefix.sourceforge.net/properties200401";

    private static final String CUS_NS = "http://www.schlund.de/pustefix/customize";

    private static final Logger LOG = Logger.getLogger(XMLPropertiesUtil.class);

    // Define PropertyRule inline
    public static class PropertyRule extends Rule {

        private Properties props;

        private String propName;

        private String propValue;

        public PropertyRule(Properties props) {
            this.props = props;
        }

        /* (non-Javadoc)
         * @see org.apache.commons.digester.Rule#begin(java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        public void begin(String namespace, String name, Attributes attributes) throws Exception {
            this.propName = attributes.getValue("name");
            this.propValue = "";
            if (propName == null) {
                throw new SAXException("Mandatory attribute \"name\" is missing!");
            }
        }

        /* (non-Javadoc)
         * @see org.apache.commons.digester.Rule#end(java.lang.String, java.lang.String)
         */
        public void end(String namespace, String name) throws Exception {
            if (props.getProperty(propName) != null) {
                LOG.warn("Overwriting already set property \"" + propName + "\" with value \"" + propValue.trim() + "\"!");
            }
            props.setProperty(propName, unesacpePropertyValue(propValue.trim()));
        }

        /* (non-Javadoc)
         * @see org.apache.commons.digester.Rule#body(java.lang.String, java.lang.String, java.lang.String)
         */
        public void body(String namespace, String name, String text) throws Exception {
            this.propValue += text;
        }

    }

    public static Properties loadPropertiesFromXMLFile(File file) throws SAXException, IOException {
        Properties props = new Properties();
        loadPropertiesFromXMLFile(file, props);
        return props;
    }

    public static Properties loadPropertiesFromXMLFile(FileResource file) throws SAXException, IOException {
        Properties props = new Properties();
        loadPropertiesFromXMLFile(file, props);
        return props;
    }

    public static void loadPropertiesFromXMLFile(FileResource file, Properties props) throws SAXException, IOException {
        loadPropertiesFromXMLStream(file.getInputStream(), props);
    }

    public static void loadPropertiesFromXMLFile(File file, Properties props) throws SAXException, IOException {
        loadPropertiesFromXMLStream(new FileInputStream(file), props);
    }

    public static void loadPropertiesFromXMLStream(InputStream input, Properties props) throws SAXException, IOException {
        Digester digester = new Digester();

        WithDefaultsRulesWrapper rules = new WithDefaultsRulesWrapper(new RulesBase());
        rules.addDefault(new DefaultMatchRule());
        digester.setRules(rules);
        digester.setRuleNamespaceURI(PROPS_NS);

        Rule propertyRule = new PropertyRule(props);
        Rule dummyRule = new Rule() {
        };

        digester.addRule("standardprops", dummyRule);
        digester.addRule("standardprops/properties", dummyRule);
        digester.addRule("standardprops/properties/prop", propertyRule);

        CustomizationHandler cushandler = new CustomizationHandler(digester, PROPS_NS, CUS_NS, new String[] { "/standardprops/properties" });
        SAXParser parser;
        try {
            SAXParserFactory spfac = SAXParserFactory.newInstance();
            spfac.setNamespaceAware(true);
            parser = spfac.newSAXParser();
            parser.parse(input, cushandler);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Could not initialize SAXParser!");
        }
    }

    protected static String unesacpePropertyValue(String value) {
        StringBuffer newValue = new StringBuffer(value.length());
        char aChar;
        int off = 0;
        int end = value.length();

        while (off < end) {
            aChar = value.charAt(off++);
            if (aChar == '\\') {
                aChar = value.charAt(off++);
                if (aChar == 'u') {
                    // Read the xxxx
                    int val = 0;
                    for (int i = 0; i < 4; i++) {
                        try  {
                            aChar = value.charAt(off++);
                        } catch (StringIndexOutOfBoundsException e) {
                            throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
                        }
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                val = (val << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                val = (val << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                val = (val << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
                        }
                    }
                    newValue.append((char) val);
                } else {
                    if (aChar == 't')
                        aChar = '\t';
                    else if (aChar == 'r')
                        aChar = '\r';
                    else if (aChar == 'n')
                        aChar = '\n';
                    else if (aChar == 'f')
                        aChar = '\f';
                    newValue.append(aChar);
                }
            } else {
                newValue.append(aChar);
            }
        }
        
        return newValue.toString();
    }
}
