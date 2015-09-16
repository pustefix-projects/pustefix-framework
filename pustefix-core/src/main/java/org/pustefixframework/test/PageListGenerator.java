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
package org.pustefixframework.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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
import org.pustefixframework.util.LocaleUtils;
import org.pustefixframework.util.xml.DOMUtils;
import org.pustefixframework.util.xml.XPathUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import de.schlund.pfixcore.util.ModuleInfo;
import de.schlund.pfixcore.workflow.SiteMap;
import de.schlund.pfixxml.LanguageInfo;
import de.schlund.pfixxml.Tenant;
import de.schlund.pfixxml.TenantInfo;
import de.schlund.pfixxml.config.EnvironmentProperties;
import de.schlund.pfixxml.config.GlobalConfigurator;
import de.schlund.pfixxml.config.includes.IncludesResolver;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.Resource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.targets.PageInfo;
import de.schlund.pfixxml.targets.TargetGenerator;

/**
 * Generates list with display names of all pages.
 */
public class PageListGenerator {

    private TenantInfo tenantInfo;
    private LanguageInfo languageInfo;
    private Resource confFile;

    public List<File> generate(File docroot, File outputDir, String mode) throws Exception {
        
        if(!docroot.exists()) throw new Exception("TargetGenerator docroot " + docroot.getAbsolutePath() + " doesn't exist");
        
        File webXml = new File(docroot, "WEB-INF/web.xml");
        if(!webXml.exists()) throw new Exception("Can't find web.xml: " + webXml.getAbsolutePath());
        
        String projectConfigLocation = getProjectConfigLocation(webXml);
        if(projectConfigLocation == null) throw new Exception("Can't get project config location from web.xml");
        
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.toLevel("error"));
        ConsoleAppender rootAppender = new ConsoleAppender(new PatternLayout("[%p] %c - %m\n"));
        rootLogger.addAppender(rootAppender);
        
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
        setupProject(projectConfigInput);
       
        try {
            FileResource cacheDir = ResourceUtil.getFileResource(outputDir.toURI());
            if(!cacheDir.exists()) cacheDir.mkdirs();
            TargetGenerator gen = new TargetGenerator(confFile, cacheDir, false, false);
            gen.setIsGetModTimeMaybeUpdateSkipped(true);
            gen.setTenantInfo(tenantInfo);
            gen.afterPropertiesSet();
            Set<PageInfo> pageInfos = gen.getPageTargetTree().getPageInfos();
            List<String> pageNames = new ArrayList<String>();
            for(PageInfo pageInfo: pageInfos) {
                pageNames.add(pageInfo.getName());
            }
            
            SiteMap siteMap = gen.getSiteMap();
            List<File> generatedFiles = new ArrayList<File>();
            
            if(tenantInfo != null) {
                for(Tenant tenant : tenantInfo.getTenants()) {
                    File file = new File(outputDir, "pagelist" + tenant.getName() + ".txt");
                    Set<String> pages = new HashSet<String>();
                    try ( PrintWriter writer = new PrintWriter(file, "utf8"); ) {
                        for(String pageName : pageNames) {
                            for(String language: tenant.getSupportedLanguages()) {
                                String langPart = LocaleUtils.getLanguagePart(language);
                                String pathPrefix = "";
                                if(!language.equals(tenant.getDefaultLanguage())) {
                                    pathPrefix = langPart + "/";
                                }
                                String aliasName = pathPrefix + siteMap.getAlias(pageName, language);
                                if(pages.add(aliasName)) {
                                    writer.println(aliasName);
                                }
                                List<String> aliases = siteMap.getPageAlternativeAliases(pageName, language);
                                if(aliases != null) {
                                    for(String alias: aliases) {
                                        aliasName = pathPrefix + alias;
                                        if(pages.add(aliasName)) {
                                            writer.println(aliasName);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    generatedFiles.add(file);
                }
            } else {
                File file = new File(outputDir, "pagelist.txt");
                Set<String> pages = new HashSet<String>();
                try ( PrintWriter writer = new PrintWriter(file, "utf8"); ) {
                    for(String pageName : pageNames) {
                        if(languageInfo != null) {
                            for(String language : languageInfo.getSupportedLanguages()) {
                                String langPart = LocaleUtils.getLanguagePart(language);
                                String pathPrefix = "";
                                if(!language.equals(languageInfo.getDefaultLanguage())) {
                                    pathPrefix = langPart + "/";
                                }
                                String aliasName = pathPrefix + siteMap.getAlias(pageName, language);
                                if(pages.add(aliasName)) {
                                    writer.println(aliasName);
                                }
                                List<String> aliases = siteMap.getPageAlternativeAliases(pageName, language);
                                if(aliases != null) {
                                    for(String alias : aliases) {
                                        aliasName = pathPrefix + alias;
                                        if(pages.add(aliasName)) {
                                            writer.println(aliasName);
                                        }
                                    }
                                }
                            }
                        } else {
                            String aliasName = siteMap.getAlias(pageName, null);
                            if(pages.add(aliasName)) {
                                writer.println(aliasName);
                            }
                            List<String> aliases = siteMap.getPageAlternativeAliases(pageName, null);
                            if(aliases != null) {
                                for(String alias: aliases) {
                                    if(pages.add(alias)) {
                                        writer.println(alias);
                                    }
                                }
                            }
                        }
                    }
                }
                generatedFiles.add(file);
            }
            return generatedFiles;
        } catch(Exception x) {
            throw new Exception("Generating pagelist failed", x);
        }
    }
    
    private void setupProject(InputSource projectConfig) throws Exception {
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
            confFile = ResourceUtil.getResource(dependXmlLocation);
            
            xp = xpf.newXPath();
            xp.setNamespaceContext(nc);
            xpe = xp.compile("/p:project-config/p:tenant");
            nodes = (NodeList)xpe.evaluate(doc, XPathConstants.NODESET);
            if(nodes != null && nodes.getLength() > 0) {
                List<Tenant> tenants = new ArrayList<Tenant>();
                for(int i=0; i<nodes.getLength(); i++) {
                    Element elem = (Element)nodes.item(i);
                    String name = elem.getAttribute("name");
                    Tenant tenant = new Tenant(name);
                    List<Element> langElems = DOMUtils.getChildElementsByTagName(elem, "lang");
                    for(Element langElem: langElems) {
                        tenant.addSupportedLanguage(langElem.getTextContent().trim());
                    }
                    tenants.add(tenant);
                }
                tenantInfo = new TenantInfo();
                tenantInfo.setTenants(tenants);
            }
            
            xp = xpf.newXPath();
            xp.setNamespaceContext(nc);
            xpe = xp.compile("/p:project-config/p:project/p:lang");
            nodes = (NodeList)xpe.evaluate(doc, XPathConstants.NODESET);
            if(nodes != null && nodes.getLength() > 0) {
                List<String> languages = new ArrayList<String>();
                String defaultLanguage = null;
                for(int i=0; i<nodes.getLength(); i++) {
                    Element elem = (Element)nodes.item(i);
                    String language = elem.getTextContent().trim();
                    languages.add(language);
                    if(elem.getAttribute("default").equals("true")) {
                        defaultLanguage = language;
                    }
                }
                languageInfo = new LanguageInfo();
                languageInfo.setSupportedLanguages(languages);
                languageInfo.setDefaultLanguage(defaultLanguage);
            }
            
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