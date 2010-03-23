package de.schlund.pfixxml.targets;

import java.io.File;
import java.io.Writer;
import java.util.Properties;

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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.schlund.pfixcore.util.ModuleInfo;
import de.schlund.pfixxml.config.BuildTimeProperties;
import de.schlund.pfixxml.config.GlobalConfigurator;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;

/**
 * Class used by Maven plugin to generate targets.
 * 
 * @author mleidig@schlund.de
 *
 */
public class TargetGeneratorRunner {
    
    public boolean run(File docroot, File config, File projectConfig, File cache, String mode, Writer output, String logLevel) throws Exception {
        
        if(!docroot.exists()) throw new Exception("TargetGenerator docroot " + docroot.getAbsolutePath() + " doesn't exist");
        if(!config.exists()) throw new Exception("TargetGenerator configuration " + config.getAbsolutePath() + " doesn't exist");
        
        ConsoleAppender appender = new ConsoleAppender(new PatternLayout("[%p] %c - %m\n"));
        Logger logger=Logger.getRootLogger();
        logger.setLevel(Level.toLevel(logLevel));
        logger.addAppender(appender);
        
        Properties props = new Properties();
        props.setProperty("mode", mode);
        BuildTimeProperties.setProperties(props);
        
        GlobalConfigurator.setDocroot(docroot.getPath());
        
        setupDynamicIncludes(projectConfig);
        
        FileResource confFile = ResourceUtil.getFileResource(config.toURI());
        if(!cache.exists()) cache.mkdirs();
        FileResource cacheDir = ResourceUtil.getFileResource(cache.toURI());
        
        try { 
            TargetGeneratorFactory genFactory = TargetGeneratorFactory.getInstance();
            TargetGenerator gen = genFactory.createGenerator(confFile, cacheDir);
            gen.setIsGetModTimeMaybeUpdateSkipped(false);
            gen.generateAll();
            output.write(TargetGenerator.getReportAsString());
        } catch(Exception x) {
            throw new Exception("Generating targets failed", x);
        }
        return !TargetGenerator.errorsReported();
    }
    
    private static void setupDynamicIncludes(File projectConfig) throws Exception {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(projectConfig);
            XPathFactory xpf = XPathFactory.newInstance();
            XPath xp = xpf.newXPath();
            XPathExpression xpe = xp.compile("/project-config/dynamic-includes/default-search/module");
            NodeList nodes = (NodeList)xpe.evaluate(doc, XPathConstants.NODESET);
            ModuleInfo moduleInfo = ModuleInfo.getInstance();
            for(int i=0; i<nodes.getLength(); i++) {
                Element elem = (Element)nodes.item(i);
                String module = elem.getTextContent();
                moduleInfo.addDefaultSearchModule(module);
            }
        } catch(Exception x) {
            throw new Exception("Can't read project configuration", x);
        }
    }
    
}
