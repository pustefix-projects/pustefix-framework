/*
 * Place license here
 */

package org.pustefixframework.config.contextxml.parser;

import java.io.IOException;
import java.util.Properties;

import org.xml.sax.SAXException;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixxml.config.XMLPropertiesUtil;
import de.schlund.pfixxml.config.impl.ContextXMLServletConfigImpl;
import de.schlund.pfixxml.resources.ResourceUtil;

/**
 * 
 * @author mleidig
 *
 */
public class ContextXMLParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
        
        ContextXMLServletConfigImpl ctxConfig = new ContextXMLServletConfigImpl();
        context.getObjectTreeElement().addObject(ctxConfig);
        
        Properties properties = new Properties(System.getProperties());
        try {
            XMLPropertiesUtil.loadPropertiesFromXMLFile(ResourceUtil.getFileResourceFromDocroot("common/conf/pustefix.xml"), properties);
        } catch (SAXException e) {
            throw new ParserException("Error while reading common/conf/pustefix.xml", e);
        } catch (IOException e) {
            throw new ParserException("Error while reading common/conf/pustefix.xml", e);
        }
        ctxConfig.setProperties(properties);
       
    }

}
