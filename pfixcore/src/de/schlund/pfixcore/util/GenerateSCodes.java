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
 *
 */
package de.schlund.pfixcore.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.pustefixframework.config.generic.PropertyFileReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixxml.config.GlobalConfig;
import de.schlund.pfixxml.config.GlobalConfigurator;
import de.schlund.pfixxml.resources.DocrootResource;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.util.Xml;
import de.schlund.util.statuscodes.StatusCode;


public class GenerateSCodes {

    private static final String SCODEFILES = "partindex.scodefile";
    
    public static void main(String[] args) throws Exception {

        if (args.length != 4) {
            throw new IOException("expected 4 arguments, got " + args.length);
        }
        
        File configFile = new File(args[0]);
        String docroot = args[1];
        File genDir = new File(args[2]);
        String className = args[3];
        GenerateSCodes.generate (configFile, docroot, genDir, className);
        
    }
    
    public static Result generateFromInfo(List<DocrootResource> infoFiles, String docRoot, File genDir, String module) throws Exception {
        Result totalResult = new Result();
        for(DocrootResource infoFile:infoFiles) {
            Result result = generate(infoFile, docRoot, genDir, module);
            totalResult.addResult(result);
        }
        return totalResult;
    }
    
    public static Result generate(DocrootResource infoFile, String docRoot, File genDir, String module) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(infoFile.getInputStream());
        NodeList scElems = doc.getDocumentElement().getElementsByTagName("statuscodes");
        List<String> genClasses = new ArrayList<String>();
        List<String> allClasses = new ArrayList<String>();
        for(int i=0; i<scElems.getLength(); i++) {
            Element scElem = (Element)scElems.item(i);
            String className = scElem.getAttribute("class");
            List<DocrootResource> scXmlFiles = new ArrayList<DocrootResource>();
            NodeList fileElems = scElem.getElementsByTagName("file");
            for(int j=0; j<fileElems.getLength(); j++) {
                Element fileElem = (Element)fileElems.item(j);
                String filePath = fileElem.getTextContent();
                DocrootResource res = null;
                if(!filePath.startsWith("/")) {
                    // try to get resource relative to info file
                    String path = infoFile.getRelativePath();
                    path=path.substring(0,path.lastIndexOf('/'))+"/"+filePath;
                    DocrootResource tmp = ResourceUtil.getFileResourceFromDocroot(path);
                    if(tmp.exists()) res = tmp;
                }
                if(res==null) {
                    // try to get resource relative to docroot
                    DocrootResource tmp = ResourceUtil.getFileResourceFromDocroot(filePath);
                    if(tmp.exists()) res = tmp;
                }
                if(res==null) throw new RuntimeException("Statusmessage file not found: "+filePath);
                scXmlFiles.add(res);
            }
            boolean generated = generate(scXmlFiles, docRoot, genDir, className, module);
            if(generated) genClasses.add(className);
            allClasses.add(className);
        }
        return new Result(allClasses, genClasses);
    }
    
   
    
    public static void generate(File configFile, String docroot, File genDir, String className) throws IOException, SAXException, ParserException {
        GlobalConfigurator.setDocroot(docroot);
        Properties props = new Properties();
        if (configFile.getName().endsWith(".prop") || configFile.getName().endsWith(".properties")) {
            props.load(new FileInputStream(configFile));
        } else {
            PropertyFileReader.read(configFile, props);
        }
        generate(props, docroot, genDir, className);
    }
    
    public static void generate(Properties props, String docroot, File genDir, String className) throws IOException, SAXException {
        if(GlobalConfig.getDocroot()==null) GlobalConfigurator.setDocroot(docroot);
        
        HashSet<DocrootResource> scfiles = new HashSet<DocrootResource>();
        HashSet<String> propfiles = new HashSet<String>(PropertiesUtils.selectProperties(props, SCODEFILES).values());
        for (String tmp: propfiles) {
            scfiles.add(ResourceUtil.getFileResourceFromDocroot(tmp));
        }
        
        String scLibPath = className.replace('.','/')+".java";
        File scLibFile = new File(genDir, scLibPath);
        if (scLibFile.exists()) {
            boolean newer = false;
            for (FileResource path: scfiles) {
                if (path.exists() && path.lastModified() > scLibFile.lastModified()) {
                    newer = true;
                    break;
                }
            }
            if(!newer) return;
        } else {
            if (!scLibFile.getParentFile().exists()) {
                scLibFile.getParentFile().mkdirs();
            }
        }

        Writer writer = new OutputStreamWriter(new FileOutputStream(scLibFile), "ascii");
        createHeader(writer, className);
        
        List<String> docRelPaths = new ArrayList<String>();
        for (DocrootResource input: scfiles) docRelPaths.add(input.getRelativePath());
        createResources(writer, docRelPaths);
        
        for (DocrootResource input: scfiles) {
            Document doc = Xml.parseMutable(input);
            createStatusCodes(writer, doc, docRelPaths.indexOf(input.getRelativePath()));
        }
            
        writer.write("}\n");
        writer.flush();
        writer.close();
            
    }
    
    public static boolean generate(List<DocrootResource> scXmlFiles, String docroot, File genDir, String className, String module) throws IOException, SAXException {
        
        if(GlobalConfig.getDocroot()==null) GlobalConfigurator.setDocroot(docroot);
        
        String scLibPath = className.replace('.','/')+".java";
        File scLibFile = new File(genDir, scLibPath);
        if (scLibFile.exists()) {
            boolean differentFiles = false;
            try {
                Class<?> clazz = Class.forName(className);
                Field field = clazz.getDeclaredField("__RES");
                DocrootResource[] resArr = (DocrootResource[])field.get(null);
                if(resArr.length == scXmlFiles.size()) {
                    for(DocrootResource res:resArr) {
                        if(!scXmlFiles.contains(res)) {
                            differentFiles = true;
                            break;
                        }
                    }
                } else differentFiles = true;
            } catch(ClassNotFoundException x) {
                //statuscode class not available -> can't check if built from same files -> continue   
            } catch(Exception x) {
                throw new RuntimeException("Can't detect from which files statuscode class was built.",x);
            }
            if(!differentFiles) {
                boolean newer = false;
                for (FileResource path: scXmlFiles) {
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
        List<String> docRelPaths = new ArrayList<String>();
        for (DocrootResource input: scXmlFiles) {
            String path = getModulePath(input.getRelativePath(),module);
            docRelPaths.add(path);
        }
        createResources(writer, docRelPaths);
        
        for (DocrootResource input: scXmlFiles) {
            Document doc = Xml.parseMutable(input);
            createStatusCodes(writer, doc, docRelPaths.indexOf(getModulePath(input.getRelativePath(),module)));
        }
            
        writer.write("}\n");
        writer.flush();
        writer.close();
            
        return true;
    }
    
    public static void generate(File scXmlFile, File genDir, String className, String docRelPath) throws IOException, SAXException {
      
        if (!scXmlFile.exists()) throw new IOException("statuscode file doesn't exist: "+scXmlFile.getAbsolutePath());
        
        String scLibPath = className.replace('.','/')+".java";
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
            
        List<String> docRelPaths = new ArrayList<String>();
        docRelPaths.add(docRelPath);
        createResources(writer, docRelPaths);
        
        Document doc = Xml.parseMutable(scXmlFile);
        createStatusCodes(writer, doc, 0);
                    
        writer.write("\n}\n");
        writer.flush();
        writer.close();
        
    }
    
    private static void createResources(Writer writer, List<String> docRelPaths) throws IOException {
        writer.write("    public static final DocrootResource[] __RES = {\n");
        Iterator<String> it = docRelPaths.iterator();
        while(it.hasNext()) {
            writer.write("        ResourceUtil.getFileResourceFromDocroot(\""+it.next()+"\")");
            if(it.hasNext()) writer.write(",");
            writer.write("\n");
        }
        writer.write("    };\n\n");
    }
    
    private static void createStatusCodes(Writer writer, Document doc, int resIndex) throws IOException {
        NodeList list  = doc.getElementsByTagName("part");
        for (int i = 0; i < list.getLength() ; i++) {
            Element node      = (Element) list.item(i);
            String  name      = node.getAttribute("name");
            String  classname = StatusCode.convertToFieldName(name);
            writer.write("    public static final StatusCode " + classname +
                    " = new StatusCode(\"" + name + "\", __RES["+resIndex+"]);\n");
        }
    }

    private static void createHeader(Writer writer, String className) throws IOException {
        int ind = className.lastIndexOf('.');
        if(ind == -1) throw new RuntimeException("Class name must contain package: "+className);
        String pkgName = className.substring(0,ind);
        String simpleName = className.substring(ind+1);
        writer.write("/*\n");
        writer.write(" * This file is AUTOGENERATED. Do not change by hand.\n");
        writer.write(" */\n");
        writer.write("\n");
        writer.write("\n");
        writer.write("package "+pkgName+";\n\n");
        writer.write("import de.schlund.pfixxml.resources.DocrootResource;\n");
        writer.write("import de.schlund.pfixxml.resources.ResourceUtil;\n");
        if(!pkgName.equals("de.schlund.util.statuscodes")) {
            writer.write("import de.schlund.util.statuscodes.StatusCode;\n");
            writer.write("import de.schlund.util.statuscodes.StatusCodeException;\n");
        }
        writer.write("import java.lang.reflect.Field;\n");
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

    
    private static String getModulePath(String relPath, String module) {
        if(module==null) return relPath;
        int ind = relPath.lastIndexOf('.');
        if(ind == -1) throw new RuntimeException("Illegal file name: "+relPath);
        relPath = relPath.substring(0,ind)+"-merged"+relPath.substring(ind);
        String modulePath="";
        if(module.equals("pfixcore")) {
            if(!relPath.startsWith("core")) throw new RuntimeException("Illegal core file name: "+relPath);
            modulePath = "core-override/"+relPath.substring(5);
        } else {
            modulePath = "modules-override/"+module+"/"+relPath;
        }
        return modulePath;
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
    
}
