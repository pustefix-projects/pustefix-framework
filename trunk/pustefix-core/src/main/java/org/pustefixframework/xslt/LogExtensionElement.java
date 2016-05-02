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

package org.pustefixframework.xslt;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.icl.saxon.Context;
import com.icl.saxon.output.Emitter;
import com.icl.saxon.output.Outputter;
import com.icl.saxon.output.XMLEmitter;
import com.icl.saxon.style.StyleElement;
import com.icl.saxon.tree.AttributeCollection;

import de.schlund.pfixxml.config.EnvironmentProperties;

/**
 * XSL extension element for logging out messages. Compared to the XSL standard
 * message element, it has additional features like specifying a log level and
 * logging to a Log4J logger instead to standard output.
 * 
 */
public class LogExtensionElement extends StyleElement {

    private Level maxLevel;    
    private Level level;
    private Logger logger;

    public boolean isInstruction() {
        return true;
    }

    public boolean mayContainTemplateBody() {
        return true;
    }

    public void prepareAttributes() throws TransformerConfigurationException {

        AttributeCollection atts = getAttributeList();        
        for(int i=0; i<atts.getLength(); i++) {
            String name = atts.getQName(i);
            String value = atts.getValue(i);
            if(name.equals("level")) {
                level = Level.toLevel(value);
            } else if(name.equals("logger")) {
                logger = Logger.getLogger(value);
            } else {
                compileError("Attribute " + name + " is not allowed on this element");
            }
        }
        if(level == null) {
            level = Level.INFO;
        }
        String localName = getLocalName();
        if(!localName.equals("log")) {
            level = Level.toLevel(localName);
        }
        if(logger == null) {
            String mode = EnvironmentProperties.getProperties().getProperty("mode");
            if("prod".equals(mode)) {
                maxLevel = Level.WARN;
            } else if("test".equals(mode) || "devel".equals("mode")) {
                maxLevel = Level.DEBUG;
            } else {
                maxLevel = Level.INFO;
            }
        } else {
            maxLevel = logger.getEffectiveLevel();
        }
    }

    public void validate() throws TransformerConfigurationException {
        checkWithinTemplate();
    }

    public void process(Context context) throws TransformerException {

        if(level.isGreaterOrEqual(maxLevel)) {

            if(logger == null) {

                Emitter emitter = context.getController().getMessageEmitter();
                if (emitter==null) {
                    emitter = context.getController().makeMessageEmitter();
                }
                if (emitter.getWriter()==null) {
                    emitter.setWriter(new OutputStreamWriter(System.err));
                }
                output(context, emitter);

            } else {

                Emitter emitter = new XMLEmitter();
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                emitter.setWriter(pw);
                output(context, emitter);
                logger.log(level, sw.toString());

            }
        }
    }

    private void output(Context context, Emitter emitter) throws TransformerException {

        Outputter old = context.getController().getOutputter();
        Properties props = new Properties();
        props.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
        context.getController().changeOutputDestination(props, emitter);
        processChildren(context);
        context.getController().resetOutputDestination(old);
    }

}
