package de.schlund.pfixxml.targets;

import java.io.File;
import java.io.FileInputStream;
import java.io.Writer;
import java.net.URI;
import java.util.List;
import java.util.Properties;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.pustefixframework.util.xml.DOMUtils;
import org.pustefixframework.util.xml.XPathUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import de.schlund.pfixcore.util.ModuleInfo;
import de.schlund.pfixxml.config.EnvironmentProperties;
import de.schlund.pfixxml.config.GlobalConfigurator;
import de.schlund.pfixxml.config.includes.IncludesResolver;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.Resource;
import de.schlund.pfixxml.resources.ResourceUtil;

/**
 * Class used by Maven plugin to generate targets.
 * 
 * @author mleidig@schlund.de
 *
 */
public class TargetGeneratorRunner {
    
    public boolean run(File docroot, File cache, String mode, Writer output, String logLevel) throws Exception {
        
        if(!docroot.exists()) throw new Exception("TargetGenerator docroot " + docroot.getAbsolutePath() + " doesn't exist");
        
        File webXml = new File(docroot, "WEB-INF/web.xml");
        if(!webXml.exists()) throw new Exception("Can't find web.xml: " + webXml.getAbsolutePath());
        
        String projectConfigLocation = getProjectConfigLocation(webXml);
        if(projectConfigLocation == null) throw new Exception("Can't get project config location from web.xml");
        
        ConsoleAppender appender = new ConsoleAppender(new PatternLayout("[%p] %c - %m\n"));
        Logger logger=Logger.getRootLogger();
        logger.setLevel(Level.toLevel(logLevel));
        logger.addAppender(appender);
        
        Properties props = new Properties();
        props.setProperty("mode", mode);
        EnvironmentProperties.setProperties(props);
        
        GlobalConfigurator.setDocroot(docroot.getPath());
        
        InputSource projectConfigInput = null;
        if(projectConfigLocation.startsWith("module:")) {
            Resource res = ResourceUtil.getResource(projectConfigLocation);
            projectConfigInput = new InputSource(res.getInputStream());
            projectConfigInput.setSystemId(projectConfigLocation);
        } else {
            File res = new File(docroot, projectConfigLocation);
            projectConfigInput = new InputSource(new FileInputStream(res));
            projectConfigInput.setSystemId(projectConfigLocation);
        }
        Resource confFile = setupProject(projectConfigInput);
       
        if(!cache.exists()) cache.mkdirs();
        FileResource cacheDir = ResourceUtil.getFileResource(cache.toURI());
        
        try { 
            TargetGenerator gen = new TargetGenerator(confFile, cacheDir, true);
            gen.setIsGetModTimeMaybeUpdateSkipped(false);
            gen.generateAll();
            output.write(gen.getReportAsString());
            return !gen.errorsReported();
        } catch(Exception x) {
            throw new Exception("Generating targets failed", x);
        }
        
    }
    
    private static Resource setupProject(InputSource projectConfig) throws Exception {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setXIncludeAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(projectConfig);
            IncludesResolver resolver = new IncludesResolver("http://www.pustefix-framework.org/2008/namespace/project-config", "config-include");
            resolver.resolveIncludes(doc);
            NamespaceContext nc = XPathUtils.createNamespaceContext("p", "http://www.pustefix-framework.org/2008/namespace/project-config");
            XPathFactory xpf = XPathFactory.newInstance();
            XPath xp = xpf.newXPath();
            xp.setNamespaceContext(nc);
            XPathExpression xpe = xp.compile("/p:project-config/p:dynamic-includes/p:default-search/p:module");
            NodeList nodes = (NodeList)xpe.evaluate(doc, XPathConstants.NODESET);
            ModuleInfo moduleInfo = ModuleInfo.getInstance();
            for(int i=0; i<nodes.getLength(); i++) {
                Element elem = (Element)nodes.item(i);
                String module = elem.getTextContent();
                moduleInfo.addDefaultSearchModule(module);
            }
            xp = xpf.newXPath();
            xp.setNamespaceContext(nc);
            xpe = xp.compile("/p:project-config/p:xml-generator/p:config-file");
            String dependXmlLocation = (String)xpe.evaluate(doc, XPathConstants.STRING);
            
            if(projectConfig.getSystemId().startsWith("module://") && !dependXmlLocation.matches("^\\w+:.*")) {
                URI moduleUri = new URI(projectConfig.getSystemId());
                String module = moduleUri.getAuthority();
                if(dependXmlLocation.startsWith("/")) dependXmlLocation = dependXmlLocation.substring(1);
                dependXmlLocation = "module://" + module + "/" + dependXmlLocation;
            }
            
            xp = xpf.newXPath();
            xp.setNamespaceContext(nc);
            xpe = xp.compile("/p:project-config/p:tenant");
            nodes = (NodeList)xpe.evaluate(doc, XPathConstants.NODESET);
            for(int i=0; i<nodes.getLength(); i++) {
                Element elem = (Element)nodes.item(i);
                String name = elem.getAttribute("name");
            }
            
            return ResourceUtil.getResource(dependXmlLocation);
        } catch(Exception x) {
            throw new Exception("Can't read project configuration", x);
        }
    }
    
    private static String getProjectConfigLocation(File webXml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(webXml);
        List<Element> servletElems = DOMUtils.getChildElementsByTagName(doc.getDocumentElement(), "servlet");
        for(Element servletElem: servletElems) {
            Element servletClassElem = DOMUtils.getFirstChildByTagName(servletElem, "servlet-class");
            if(servletClassElem != null && servletClassElem.getTextContent().trim().equals("org.springframework.web.servlet.DispatcherServlet")) {
                boolean isPustefix = false;
                String locations = null;
                List<Element> initParamElems = DOMUtils.getChildElementsByTagName(servletElem, "init-param");
                for(Element initParamElem: initParamElems) {
                    Element paramNameElem = DOMUtils.getFirstChildByTagName(initParamElem, "param-name");
                    if(paramNameElem != null) {
                        if(paramNameElem.getTextContent().trim().equals("contextConfigLocation")) {
                            Element paramValElem = DOMUtils.getFirstChildByTagName(initParamElem, "param-value");
                            if(paramValElem != null) {
                                locations = paramValElem.getTextContent().trim();
                            }
                        } else if(paramNameElem.getTextContent().trim().equals("contextClass")) {
                            Element paramValElem = DOMUtils.getFirstChildByTagName(initParamElem, "param-value");
                            if(paramValElem.getTextContent().trim().equals("org.pustefixframework.container.spring.beans.PustefixWebApplicationContext")) {
                                isPustefix = true;
                            }
                        }
                    } 
                }
                if(isPustefix && locations != null) {
                    String urls[] = locations.split("(\\s+)|(\\s*,\\s*)");
                    for(String url: urls) {
                        url = url.trim();
                        if(url.endsWith("project.xml")) {
                            return url;
                        }
                    }
                }
            }
        }
        return null;
    }

}
