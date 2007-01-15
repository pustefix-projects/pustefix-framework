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

package de.schlund.pfixxml.config.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.commons.digester.RulesBase;
import org.apache.commons.digester.WithDefaultsRulesWrapper;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.schlund.pfixcore.scriptedflow.ScriptedFlowConfig;
import de.schlund.pfixcore.scriptedflow.ScriptedFlowConfigImpl;
import de.schlund.pfixxml.config.ContextXMLServletConfig;
import de.schlund.pfixxml.config.CustomizationHandler;
import de.schlund.pfixxml.config.includes.FileIncludeEvent;
import de.schlund.pfixxml.config.includes.FileIncludeEventListener;
import de.schlund.pfixxml.config.includes.IncludesResolver;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.util.Xml;

/**
 * Stores configuration for a Pustefix servlet
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ContextXMLServletConfigImpl extends AbstractXMLServletConfigImpl implements SSLOption, CommonServletConfig, ContextXMLServletConfig {
    private final static Class DEFAULT_IHANDLER_STATE = de.schlund.pfixcore.workflow.app.DefaultIWrapperState.class;

    private final static Class DEFAULT_STATIC_STATE = de.schlund.pfixcore.workflow.app.StaticState.class;

    public final static String CONFIG_NS = "http://pustefix.sourceforge.net/properties200401";

    private final static String CUS_NS = "http://www.schlund.de/pustefix/customize";

    private Class defaultStateClass = DEFAULT_STATIC_STATE;

    private Class defaultIHandlerStateClass = DEFAULT_IHANDLER_STATE;

    private ContextConfigImpl contextConfig;

    private ScriptedFlowConfigImpl scriptedFlowConfig = new ScriptedFlowConfigImpl();

    private Set<FileResource> fileDependencies = new HashSet<FileResource>();

    private long loadTime = 0;

    public static ContextXMLServletConfigImpl readFromFile(FileResource file, Properties globalProperties) throws SAXException, IOException {
        final ContextXMLServletConfigImpl config = new ContextXMLServletConfigImpl();

        // Initialize configuration properties with global default properties
        config.setProperties(globalProperties);

        // Create digester and register default rule
        Digester digester = new Digester();
        WithDefaultsRulesWrapper rules = new WithDefaultsRulesWrapper(new RulesBase());
        digester.setRules(rules);
        rules.addDefault(new DefaultMatchRule());
        digester.setRuleNamespaceURI(CONFIG_NS);

        Rule servletInfoRule = new ServletInfoRule(config);
        Rule servletInfoEditModeRule = new ServletInfoEditModeRule(config);
        Rule servletInfoDefaultStateRule = new ServletInfoDefaultStateRule(config);
        Rule servletInfoDefaultIHandlerStateRule = new ServletInfoDefaultIHandlerStateRule(config);
        Rule sslRule = new SSLRule();
        Rule contextRule = new ContextRule(config);
        Rule contextResourceRule = new ContextResourceRule(config);
        Rule contextResourceInterfaceRule = new ContextResourceInterfaceRule(config);
        Rule contextResourcePropertyRule = new ContextResourcePropertyRule(config);
        Rule scriptedFlowRule = new ScriptedFlowRule(config);
        Rule pageflowRule = new PageflowRule(config);
        Rule pageflowVariantRule = new PageflowVariantRule(config);
        Rule pageflowStepRule = new PageflowStepRule(config);
        Rule pageflowStepOnContinueRule = new PageflowStepOnContinueRule(config);
        Rule pageflowStepConditionRule = new PageflowStepConditionRule(config);
        Rule pageflowStepActionRule = new PageflowStepActionRule(config);
        Rule pagerequestRule = new PagerequestRule(config);
        Rule pagerequestVariantRule = new PagerequestVariantRule(config);
        Rule pagerequestStateRule = new PagerequestStateRule(config);
        Rule pagerequestFinalizerRule = new PagerequestFinalizerRule(config);
        Rule pagerequestInputRule = new PagerequestInputRule(config);
        Rule pagerequestInputInterfaceRule = new PagerequestInputInterfaceRule(config);
        Rule pagerequestAuthInterfaceRule = new PagerequestAuthInterfaceRule(config);
        Rule pagerequestAuxInterfaceRule = new PagerequestAuxInterfaceRule(config);
        Rule pagerequestOutputResourceRule = new PagerequestOutputResourceRule(config);
        Rule pagerequestPropertyRule = new PagerequestPropertyRule(config);
        Rule contextStartInterceptorRule = new ContextInterceptorRule(config, "start");
        Rule contextEndInterceptorRule = new ContextInterceptorRule(config, "end");
        Rule servletPropertyRule = new ServletPropertyRule(config);

        // Rule doing nothing
        Rule dummyRule = new Rule() {
        };

        digester.addRule("contextxmlserver", dummyRule);
        digester.addRule("contextxmlserver/servletinfo", servletInfoRule);
        digester.addRule("contextxmlserver/servletinfo/editmode", servletInfoEditModeRule);
        digester.addRule("contextxmlserver/servletinfo/defaultstate", servletInfoDefaultStateRule);
        digester.addRule("contextxmlserver/servletinfo/defaultihandlerstate", servletInfoDefaultIHandlerStateRule);
        digester.addRule("contextxmlserver/servletinfo/ssl", sslRule);
        digester.addRule("contextxmlserver/context", contextRule);
        digester.addRule("contextxmlserver/context/resource", contextResourceRule);
        digester.addRule("contextxmlserver/context/resource/implements", contextResourceInterfaceRule);
        digester.addRule("contextxmlserver/context/resource/properties", dummyRule);
        digester.addRule("contextxmlserver/context/resource/properties/prop", contextResourcePropertyRule);
        digester.addRule("contextxmlserver/scriptedflow", scriptedFlowRule);
        digester.addRule("contextxmlserver/pageflow", pageflowRule);
        digester.addRule("contextxmlserver/pageflow/default", dummyRule);
        digester.addRule("contextxmlserver/pageflow/variant", pageflowVariantRule);
        digester.addRule("contextxmlserver/pageflow/flowstep", pageflowStepRule);
        digester.addRule("contextxmlserver/pageflow/default/flowstep", pageflowStepRule);
        digester.addRule("contextxmlserver/pageflow/variant/flowstep", pageflowStepRule);
        digester.addRule("contextxmlserver/pageflow/flowstep/oncontinue", pageflowStepOnContinueRule);
        digester.addRule("contextxmlserver/pageflow/default/flowstep/oncontinue", pageflowStepOnContinueRule);
        digester.addRule("contextxmlserver/pageflow/variant/flowstep/oncontinue", pageflowStepOnContinueRule);
        digester.addRule("contextxmlserver/pageflow/flowstep/oncontinue/when", pageflowStepConditionRule);
        digester.addRule("contextxmlserver/pageflow/default/flowstep/oncontinue/when", pageflowStepConditionRule);
        digester.addRule("contextxmlserver/pageflow/variant/flowstep/oncontinue/when", pageflowStepConditionRule);
        digester.addRule("contextxmlserver/pageflow/flowstep/oncontinue/when/action", pageflowStepActionRule);
        digester.addRule("contextxmlserver/pageflow/default/flowstep/oncontinue/when/action", pageflowStepActionRule);
        digester.addRule("contextxmlserver/pageflow/variant/flowstep/oncontinue/when/action", pageflowStepActionRule);
        digester.addRule("contextxmlserver/pagerequest", pagerequestRule);
        digester.addRule("contextxmlserver/pagerequest/default", dummyRule);
        digester.addRule("contextxmlserver/pagerequest/variant", pagerequestVariantRule);
        digester.addRule("contextxmlserver/pagerequest/ssl", sslRule);
        digester.addRule("contextxmlserver/pagerequest/default/ssl", sslRule);
        digester.addRule("contextxmlserver/pagerequest/variant/ssl", sslRule);
        digester.addRule("contextxmlserver/pagerequest/state", pagerequestStateRule);
        digester.addRule("contextxmlserver/pagerequest/default/state", pagerequestStateRule);
        digester.addRule("contextxmlserver/pagerequest/variant/state", pagerequestStateRule);
        digester.addRule("contextxmlserver/pagerequest/finalizer", pagerequestFinalizerRule);
        digester.addRule("contextxmlserver/pagerequest/default/finalizer", pagerequestFinalizerRule);
        digester.addRule("contextxmlserver/pagerequest/variant/finalizer", pagerequestFinalizerRule);
        digester.addRule("contextxmlserver/pagerequest/input", pagerequestInputRule);
        digester.addRule("contextxmlserver/pagerequest/default/input", pagerequestInputRule);
        digester.addRule("contextxmlserver/pagerequest/variant/input", pagerequestInputRule);
        digester.addRule("contextxmlserver/pagerequest/input/interface", pagerequestInputInterfaceRule);
        digester.addRule("contextxmlserver/pagerequest/default/input/interface", pagerequestInputInterfaceRule);
        digester.addRule("contextxmlserver/pagerequest/variant/input/interface", pagerequestInputInterfaceRule);
        digester.addRule("contextxmlserver/pagerequest/auth", dummyRule);
        digester.addRule("contextxmlserver/pagerequest/default/auth", dummyRule);
        digester.addRule("contextxmlserver/pagerequest/variant/auth", dummyRule);
        digester.addRule("contextxmlserver/pagerequest/auth/authinterface", pagerequestAuthInterfaceRule);
        digester.addRule("contextxmlserver/pagerequest/default/auth/authinterface", pagerequestAuthInterfaceRule);
        digester.addRule("contextxmlserver/pagerequest/variant/auth/authinterface", pagerequestAuthInterfaceRule);
        digester.addRule("contextxmlserver/pagerequest/auth/auxinterface", pagerequestAuxInterfaceRule);
        digester.addRule("contextxmlserver/pagerequest/default/auth/auxinterface", pagerequestAuxInterfaceRule);
        digester.addRule("contextxmlserver/pagerequest/variant/auth/auxinterface", pagerequestAuxInterfaceRule);
        digester.addRule("contextxmlserver/pagerequest/output", dummyRule);
        digester.addRule("contextxmlserver/pagerequest/output/resource", pagerequestOutputResourceRule);
        digester.addRule("contextxmlserver/pagerequest/default/output", dummyRule);
        digester.addRule("contextxmlserver/pagerequest/default/output/resource", pagerequestOutputResourceRule);
        digester.addRule("contextxmlserver/pagerequest/variant/output", dummyRule);
        digester.addRule("contextxmlserver/pagerequest/variant/output/resource", pagerequestOutputResourceRule);
        digester.addRule("contextxmlserver/pagerequest/properties", dummyRule);
        digester.addRule("contextxmlserver/pagerequest/properties/prop", pagerequestPropertyRule);
        digester.addRule("contextxmlserver/pagerequest/default/properties", dummyRule);
        digester.addRule("contextxmlserver/pagerequest/default/properties/prop", pagerequestPropertyRule);
        digester.addRule("contextxmlserver/pagerequest/variant/properties", dummyRule);
        digester.addRule("contextxmlserver/pagerequest/variant/properties/prop", pagerequestPropertyRule);
        digester.addRule("contextxmlserver/interceptors", dummyRule);
        digester.addRule("contextxmlserver/interceptors/start", dummyRule);
        digester.addRule("contextxmlserver/interceptors/end", dummyRule);
        digester.addRule("contextxmlserver/interceptors/start/interceptor", contextStartInterceptorRule);
        digester.addRule("contextxmlserver/interceptors/end/interceptor", contextEndInterceptorRule);
        digester.addRule("contextxmlserver/properties", dummyRule);
        digester.addRule("contextxmlserver/properties/prop", servletPropertyRule);

        CustomizationHandler cushandler = new CustomizationHandler(digester, CONFIG_NS, CUS_NS, new String[] { "/contextxmlserver/servletinfo", "/contextxmlserver/pagerequest", "/contextxmlserver/pagerequest/properties", "/contextxmlserver/pagerequest/default", "/contextxmlserver/pagerequest/variant", "/contextxmlserver/pagerequest/default/properties", "/contextxmlserver/pagerequest/variant/properties", "/contextxmlserver/properties" });
        String confDocXml = null;
        config.loadTime = System.currentTimeMillis();

        Document confDoc = Xml.parseMutable(file);
        IncludesResolver iresolver = new IncludesResolver(CONFIG_NS, "config-include");
        // Make sure list of dependencies only contains the file itself
        config.fileDependencies.clear();
        config.fileDependencies.add(file);
        FileIncludeEventListener listener = new FileIncludeEventListener() {

            public void fileIncluded(FileIncludeEvent event) {
                config.fileDependencies.add(event.getIncludedFile());
            }

        };
        iresolver.registerListener(listener);
        iresolver.resolveIncludes(confDoc);
        confDocXml = Xml.serialize(confDoc, false, true);

        SAXParser parser;
        try {
            SAXParserFactory spfac = SAXParserFactory.newInstance();
            spfac.setNamespaceAware(true);
            parser = spfac.newSAXParser();
            parser.parse(new InputSource(new StringReader(confDocXml)), cushandler);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Could not initialize SAXParser!");
        }

        // Set edit mode property for compatibility reasons
        if (config.isEditMode()) {
            config.getProperties().setProperty("xmlserver.noeditmodeallowed", "false");
        }

        // Set depend.xml proeprty for compatibility with exception processors
        config.getProperties().setProperty("xmlserver.depend.xml", config.getDependFile());

        // Set reference to server properties in context config
        config.getContextConfig().setProperties(config.getProperties());

        // Do some finishing
        config.getContextConfig().doFinishing();

        return config;
    }

    public void setDefaultStaticState(Class clazz) {
        this.defaultStateClass = clazz;
    }

    public Class getDefaultStaticState() {
        return this.defaultStateClass;
    }

    public void setDefaultIHandlerState(Class clazz) {
        this.defaultIHandlerStateClass = clazz;
    }

    public Class getDefaultIHandlerState() {
        return this.defaultIHandlerStateClass;
    }

    public void setContextConfig(ContextConfigImpl config) {
        this.contextConfig = config;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.ContextXMLServletConfig#getContextConfig()
     */
    public ContextConfigImpl getContextConfig() {
        return this.contextConfig;
    }

    public ScriptedFlowConfigImpl getScriptedFlowConfig() {
        return this.scriptedFlowConfig;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.ContextXMLServletConfig#needsReload()
     */
    public boolean needsReload() {
        for (FileResource file : fileDependencies) {
            if (file.lastModified() > loadTime) {
                return true;
            }
        }
        return false;
    }
}
