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
package org.pustefixframework.maven.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


public class GenerateSCodes {
    
    private final static String DEPRECATED_NS_STATUSCODEINFO = "http://pustefix-framework.org/statuscodeinfo";
    private final static String NS_STATUSCODEINFO = "http://www.pustefix-framework.org/2008/namespace/statuscodeinfo";


    public static Result generateFromInfo(List<String> infoFiles, String resDir, File genDir, String module, boolean dynamic) throws Exception {
        Result totalResult = new Result();
        for(String infoFile:infoFiles) {
            Result result = generate(infoFile, resDir, genDir, module, dynamic);
            totalResult.addResult(result);
        }
        return totalResult;
    }
    
    public static Result generate(String infoFile, String resDir, File genDir, String module, boolean dynamic) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        File file = new File(resDir, infoFile);
        Document doc = db.parse(file);
        if(DEPRECATED_NS_STATUSCODEINFO.equals(doc.getDocumentElement().getNamespaceURI()) || 
                DEPRECATED_NS_STATUSCODEINFO.equals(doc.getDocumentElement().getAttribute("xmlns"))) {
            String msg = "[DEPRECATED] Statuscode info file '" + infoFile + "' uses deprecated namespace '" + 
                         DEPRECATED_NS_STATUSCODEINFO + "'. It should be replaced by '" + NS_STATUSCODEINFO + "'.";
            System.out.println("[WARNING] " + msg);
        }
        NodeList scElems = doc.getDocumentElement().getElementsByTagName("statuscodes");
        List<String> genClasses = new ArrayList<String>();
        List<String> allClasses = new ArrayList<String>();
        for(int i=0; i<scElems.getLength(); i++) {
            Element scElem = (Element)scElems.item(i);
            String className = scElem.getAttribute("class");
            List<String> scXmlFiles = new ArrayList<String>();
            NodeList fileElems = scElem.getElementsByTagName("file");
            for(int j=0; j<fileElems.getLength(); j++) {
                Element fileElem = (Element)fileElems.item(j);
                String filePath = fileElem.getTextContent();
                String res = null;
                if(!filePath.startsWith("/")) {
                    // try to get resource relative to info file
                    String path = infoFile;
                    path=path.substring(0,path.lastIndexOf(File.separatorChar))+File.separator+filePath;
                    File tmp = new File(resDir, path);
                    if(tmp.exists()) res = path;
                }
                if(res==null) {
                    // try to get resource relative to the resource dir
                    File tmp = new File(resDir, filePath);
                    if(tmp.exists()) res = filePath;
                }
                if(res==null) throw new RuntimeException("Statusmessage file not found: "+filePath);
                if (res.startsWith(File.separator)) {
                    res = res.substring(1);
                }
                scXmlFiles.add(res);
            }
            boolean generated = generate(scXmlFiles, resDir, genDir, className, module, dynamic);
            if(generated) genClasses.add(className);
            allClasses.add(className);
        }
        return new Result(allClasses, genClasses);
    }
    
    public static boolean generate(List<String> scXmlFiles, String resDir, File genDir, String className, String module, boolean dynamic) throws IOException, SAXException {
        
        String scLibPath = className.replace('.',File.separatorChar)+".java";
        File scLibFile = new File(genDir, scLibPath);
        if (scLibFile.exists()) {
            boolean differentFiles = false;
            List<URI> targetXmlFiles = getModuleURIs(scXmlFiles, module, dynamic);
            List<URI> resPaths = getResourceURIs(scLibFile);
            if(resPaths.size() == targetXmlFiles.size()) {
                for(URI resPath : resPaths) {
                    if(!targetXmlFiles.contains(resPath)) {
                        differentFiles = true;
                        break;
                    }
                }
            } else differentFiles=true;
            if(!differentFiles) {
                boolean newer = false;
                for (String pathStr: scXmlFiles) {
                    File path = new File(resDir, pathStr);
                    if (path.exists() && path.lastModified() > scLibFile.lastModified()) {
                        newer = true;
                        break;
                    }
                }
                if(!newer) return false;
            }
        } else {
            if (!scLibFile.getParentFile().exists()) {
                scLibFile.getParentFile().mkdirs();
            }
        }
        Writer writer = new OutputStreamWriter(new FileOutputStream(scLibFile), "ascii");
        createHeader(writer, className);
        List<URI> uris = new ArrayList<URI>();
        for (String input: scXmlFiles) {
            URI uri = getModuleURI(input, module, dynamic);
            uris.add(uri);
        }
        createResources(writer, uris);
        
        for (String input: scXmlFiles) {
            Document doc = parseMutable(new File(resDir, input));
            createStatusCodes(writer, doc, uris.indexOf(getModuleURI(input, module, dynamic)));
        }
            
        writer.write("}\n");
        writer.flush();
        writer.close();
            
        return true;
    }
    
    private static List<URI> getResourceURIs(File scLibFile) throws IOException {
        List<URI> paths = new ArrayList<URI>();
        String regexp = "\\s*new URI\\(\"([^\\)]+)\"\\).*";
        Pattern pattern = Pattern.compile(regexp);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(scLibFile), "ISO-8859-1"));
        String line;
        while((line=reader.readLine())!=null) {
            Matcher matcher = pattern.matcher(line);
            if(matcher.matches()) {
                URI uri;
                try {
                    uri = new URI(matcher.group(1));
                } catch (URISyntaxException e) {
                    throw new RuntimeException("Illegal URI: "+matcher.group(1), e);
                }
                paths.add(uri);
            }
        }
        reader.close();
        return paths;
    }
    
    public static void generate(File scXmlFile, File genDir, String className, URI uri) throws IOException, SAXException {
      
        if (!scXmlFile.exists()) throw new IOException("statuscode file doesn't exist: "+scXmlFile.getAbsolutePath());
        
        String scLibPath = className.replace('.',File.separatorChar)+".java";
        File scLibFile = new File(genDir, scLibPath);
        if (scLibFile.exists()) {
            if(scXmlFile.lastModified() < scLibFile.lastModified()) return;
        } else {
            if (!scLibFile.getParentFile().exists()) {
                scLibFile.getParentFile().mkdirs();
            }
        }

        Writer writer = new OutputStreamWriter(new FileOutputStream(scLibFile), "ascii");
        createHeader(writer, className);
            
        List<URI> uris = new ArrayList<URI>();
        uris.add(uri);
        createResources(writer, uris);
        
        Document doc = parseMutable(scXmlFile);
        createStatusCodes(writer, doc, 0);
                    
        writer.write("\n}\n");
        writer.flush();
        writer.close();
        
    }
    
    private static void createResources(Writer writer, List<URI> uris) throws IOException {
        writer.write("    public static final URI[] __URI;\n\n");
        writer.write("    static {\n");
        writer.write("        try {\n");
        writer.write("            __URI = new URI[] {\n");
        Iterator<URI> it = uris.iterator();
        while(it.hasNext()) {
            writer.write("                new URI(\""+it.next().toASCIIString()+"\")");
            if(it.hasNext()) writer.write(",");
            writer.write("\n");
        }
        writer.write("            };\n");
        writer.write("        } catch (URISyntaxException e) {\n");
        writer.write("            throw new RuntimeException(\"Illegal URI\", e);\n");
        writer.write("        }\n");
        writer.write("    };\n\n");
    }
    
    private static void createStatusCodes(Writer writer, Document doc, int resIndex) throws IOException {
        NodeList list  = doc.getElementsByTagName("part");
        for (int i = 0; i < list.getLength() ; i++) {
            Element node      = (Element) list.item(i);
            String  name      = node.getAttribute("name");
            String  classname = convertToFieldName(name);
            writer.write("    public static final StatusCode " + classname +
                    " = new StatusCode(\"" + name + "\", __URI["+resIndex+"]);\n");
        }
    }

    private static void createHeader(Writer writer, String className) throws IOException {
        int ind = className.lastIndexOf('.');
        if(ind == -1) throw new RuntimeException("Class name must contain package: "+className);
        String pkgName = className.substring(0,ind);
        String simpleName = className.substring(ind+1);
        writer.write("/*\n" +
                " * This file is part of Pustefix.\n" + 
                " *\n" +
                " * Pustefix is free software; you can redistribute it and/or modify\n" +
                " * it under the terms of the GNU Lesser General Public License as published by\n" + 
                " * the Free Software Foundation; either version 2 of the License, or\n" +
                " * (at your option) any later version.\n" + 
                " *\n" + 
                " * Pustefix is distributed in the hope that it will be useful,\n" + 
                " * but WITHOUT ANY WARRANTY; without even the implied warranty of\n" + 
                " * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" + 
                " * GNU Lesser General Public License for more details.\n" + 
                " *\n" + 
                " * You should have received a copy of the GNU Lesser General Public License\n" + 
                " * along with Pustefix; if not, write to the Free Software\n" + 
                " * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA\n" +
                " */\n");
        writer.write("package "+pkgName+";\n\n");
        if(!pkgName.equals("de.schlund.util.statuscodes")) {
            writer.write("import de.schlund.util.statuscodes.StatusCode;\n");
            writer.write("import de.schlund.util.statuscodes.StatusCodeException;\n");
        }
        writer.write("import java.lang.reflect.Field;\n");
        writer.write("import java.net.URI;\n");
        writer.write("import java.net.URISyntaxException;\n");
        writer.write("\n");
        writer.write("public class "+simpleName+" {\n\n");
        writer.write("    public static StatusCode getStatusCodeByName(String name) throws StatusCodeException {\n");
        writer.write("        return getStatusCodeByName(name, false);\n");
        writer.write("    }\n");        
        writer.write("\n");        
        writer.write("    public static StatusCode getStatusCodeByName(String name, boolean optional) throws StatusCodeException {\n");
        writer.write("        String     fieldname = StatusCode.convertToFieldName(name);\n");
        writer.write("        StatusCode scode     = null;\n");
        writer.write("        try {\n");
        writer.write("            Field field = "+simpleName+".class.getField(fieldname);\n");
        writer.write("            scode = (StatusCode) field.get(null);\n");
        writer.write("        } catch (NoSuchFieldException e) {\n");
        writer.write("            //\n");
        writer.write("        } catch (SecurityException e) {\n");
        writer.write("            //\n");
        writer.write("        } catch (IllegalAccessException e) {\n");
        writer.write("            //\n");
        writer.write("        }\n");
        writer.write("        if (scode == null && optional == false) {\n");
        writer.write("            throw new StatusCodeException(\"StatusCode \" + name + \" is not defined.\");\n");
        writer.write("        }\n");
        writer.write("        return scode;\n");
        writer.write("    }\n\n");
    }

    
    private static URI getModuleURI(String relPath, String module, boolean dynamic) {
        String uriStr;

        //Fix for Windows '\' in relPath.
        relPath = relPath.replace(File.separatorChar, '/');

        if(module != null) {
            if(dynamic) {
                uriStr = "dynamic://"+module+"/"+relPath;
            } else {
                int ind = relPath.lastIndexOf('.');
                if(ind == -1) throw new RuntimeException("Illegal file name: "+relPath);
                relPath = relPath.substring(0,ind)+"-merged"+relPath.substring(ind);
                uriStr = "docroot:/modules-override/" + module + "/" + relPath;
            }
        } else {
            uriStr = "docroot:/" + relPath;
        }
        try {
            return new URI(uriStr);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Illegal URI: " + uriStr);
        }
    }
    
    private static List<URI> getModuleURIs(List<String> relPaths, String module, boolean dynamic) {
        List<URI> newURIs = new ArrayList<URI>();
        for(String relPath : relPaths) {
            newURIs.add(getModuleURI(relPath, module, dynamic));
        }
        return newURIs;
    }
    
    public static class Result {
        
        public List<String> allClasses;
        public List<String> generatedClasses;
        
        Result() {
            allClasses = new ArrayList<String>();
            generatedClasses = new ArrayList<String>();
        }
        
        Result(List<String> allClasses, List<String> generatedClasses) {
            this.allClasses = allClasses;
            this.generatedClasses = generatedClasses;
        }
        
        void addResult(Result result) {
            allClasses.addAll(result.allClasses);
            generatedClasses.addAll(result.generatedClasses);
        }
    
    }

    //-- copied from core to avoid cyclic dependency:

    public static String convertToFieldName(String part) {
        return part.replace('.', '_').replace(':', '_').toUpperCase();
    }
    
    public static Document parseMutable(File file) throws IOException, SAXException {
        try {
            return createDocumentBuilder().parse(file);
        } catch (SAXParseException e) {
            StringBuffer buf = new StringBuffer(100);
            buf.append("Caught SAXParseException!\n");
            buf.append("  Message  : ").append(e.getMessage()).append("\n");
            buf.append("  SystemID : ").append(e.getSystemId()).append("\n");
            buf.append("  Line     : ").append(e.getLineNumber()).append("\n");
            buf.append("  Column   : ").append(e.getColumnNumber()).append("\n");
            throw exception(buf, e);
        } catch (SAXException e) {
            StringBuffer buf = new StringBuffer(100);
            buf.append("Caught SAXException!\n");
            buf.append("  Message  : ").append(e.getMessage()).append("\n");
            buf.append("  SystemID : ").append(file.getPath()).append("\n");
            throw exception(buf, e);
        } catch (IOException e) {
            StringBuffer buf = new StringBuffer(100);
            buf.append("Caught IOException!\n");
            buf.append("  Message  : ").append(e.getMessage()).append("\n");
            buf.append("  SystemID : ").append(file.getPath()).append("\n");
            throw exception(buf, e);
        }
    }

    private static IOException exception(StringBuffer buf, Throwable cause) {
        IOException result = new IOException(buf.toString());
        result.initCause(cause);
        return result;
    }
    
    private static final DocumentBuilderFactory factory = createDocumentBuilderFactory();
    private static String DEFAULT_DOCUMENTBUILDERFACTORY = "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl";
    
    private static DocumentBuilderFactory createDocumentBuilderFactory() {
        DocumentBuilderFactory fact = null;
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl == null) cl = GenerateSCodes.class.getClassLoader();
            Class<?> clazz = Class.forName(DEFAULT_DOCUMENTBUILDERFACTORY, true, cl);
            fact = (DocumentBuilderFactory)clazz.newInstance();
        } catch(Exception x) {
            //ignore and try to get DocumentBuilderFactory via factory finder in next step
        }
        if (fact == null) {
            try {
                fact = DocumentBuilderFactory.newInstance();
            } catch(FactoryConfigurationError x) {
                throw new RuntimeException("Can't get DocumentBuilderFactory",x);
            }
        }
        if (!fact.isNamespaceAware()) {
            fact.setNamespaceAware(true);
        }
        if (fact.isValidating()) {
            fact.setValidating(false);
        }
        return fact;
    }

    public static DocumentBuilder createDocumentBuilder() {
        DocumentBuilder result;
        try {
            result = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("createDocumentBuilder failed", e);
        }
        result.setErrorHandler(ERROR_HANDLER);
        return result;
    }

    // make sure that output is not polluted by prinlns:
    private static final ErrorHandler ERROR_HANDLER = new ErrorHandler() {
            public void error(SAXParseException exception) throws SAXException {
                report(exception);
            }

            public void fatalError(SAXParseException exception) throws SAXException {
                report(exception);
            }

            public void warning(SAXParseException exception) throws SAXException {
                report(exception);
            }

            private void report(SAXParseException exception) throws SAXException {
                throw exception;
            }
        };
}
