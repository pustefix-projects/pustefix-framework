package org.pustefixframework.maven.plugins.autoconfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Generates logback configuration fragment.
 * 
 * @author mleidig@schlund.de
 *
 */
public class LogbackConfigFragmentCreator extends ConfigFragmentCreator {

	@Override
	public File createBundle(File targetDir, Map<String,String> params) throws IOException {
		return createBundle(targetDir, null);
	}
	
	@Override
	public File createBundle(File targetDir, File configFile, Map<String,String> params) throws IOException {
		File file = new File(targetDir, "org.pustefixframework.logback.config-1.0.0.jar");
		OutputStream out = new FileOutputStream(file);
		createLogbackConfigBundle(out, configFile, params);
		return file;
	}
	
	private void createLogbackConfigBundle(OutputStream out, File configFile, Map<String,String> params) throws IOException {
	 
	    JarOutputStream jarOut = new JarOutputStream(out);
	    JarEntry entry = new JarEntry("META-INF/MANIFEST.MF");
	    jarOut.putNextEntry(entry);
	    createManifest(jarOut, "Logback Config", "org.pustefixframework.logback.config", "1.0.0", "com.springsource.ch.qos.logback.classic");
	    entry = new JarEntry("logback.xml");
	    jarOut.putNextEntry(entry);
	    if(configFile == null) createLogbackConfig(jarOut, params);
	    else copyFileToStream(configFile, jarOut);
	    jarOut.close();
	    
	}
	
	private void createLogbackConfig(OutputStream out, Map<String,String> params) throws IOException {
		Document doc;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException x) {
			throw new RuntimeException("Can't create DOM", x);
		}
		Element configuration = doc.createElement("configuration");
		doc.appendChild(configuration);
		Element appender = doc.createElement("appender");
		appender.setAttribute("name", "STDOUT"); 
		appender.setAttribute("class", "ch.qos.logback.core.ConsoleAppender");
		configuration.appendChild(appender);
		Element layout = doc.createElement("layout");
		layout.setAttribute("class", "ch.qos.logback.classic.PatternLayout");
		appender.appendChild(layout);
		Element pattern = doc.createElement("Pattern");
		pattern.setTextContent("%d{HH:mm:ss.SSS} XXX [%thread] %-5level %logger{36} - %msg%n");
		layout.appendChild(pattern);
		Element root = doc.createElement("root");
		String logLevel = params.get("logLevel");
		if(logLevel == null || logLevel.trim().equals("")) logLevel = "WARN";
		root.setAttribute("level", logLevel);
		configuration.appendChild(root);
		Element appenderRef = doc.createElement("appender-ref");
		appenderRef.setAttribute("ref", "STDOUT");
		root.appendChild(appenderRef);
		try {
			Transformer trf = TransformerFactory.newInstance().newTransformer();
			DOMSource src = new DOMSource(doc);
			StreamResult res = new StreamResult(out);
			trf.transform(src, res);
		} catch(TransformerException x) {
			throw new RuntimeException("Can't serialize DOM", x);
		}
	}
	
}
