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
package org.pustefixframework.maven.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.schlund.pfixxml.util.Xml;
import de.schlund.util.statuscodes.StatusCode;


public class GenerateSCodes {
    
    public static Result generateFromInfo(List<String> infoFiles, String docRoot, File genDir, String module, String targetPath) throws Exception {
        Result totalResult = new Result();
        for(String infoFile:infoFiles) {
            Result result = generate(infoFile, docRoot, genDir, module, targetPath);
            totalResult.addResult(result);
        }
        return totalResult;
    }
    
    public static Result generate(String infoFile, String docRoot, File genDir, String module, String targetPath) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        File file = new File(docRoot, infoFile);
        Document doc = db.parse(file);
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
                    path=path.substring(0,path.lastIndexOf('/'))+"/"+filePath;
                    File tmp = new File(docRoot, path);
                    if(tmp.exists()) res = path;
                }
                if(res==null) {
                    // try to get resource relative to docroot
                    File tmp = new File(docRoot, filePath);
                    if(tmp.exists()) res = filePath;
                }
                if(res==null) throw new RuntimeException("Statusmessage file not found: "+filePath);
                if (res.startsWith("/")) {
                    res = res.substring(1);
                }
                scXmlFiles.add(res);
            }
            boolean generated = generate(scXmlFiles, docRoot, genDir, className, module, targetPath);
            if(generated) genClasses.add(className);
            allClasses.add(className);
        }
        return new Result(allClasses, genClasses);
    }
    
    public static boolean generate(List<String> scXmlFiles, String docroot, File genDir, String className, String module, String targetPath) throws IOException, SAXException {
        
        String scLibPath = className.replace('.','/')+".java";
        File scLibFile = new File(genDir, scLibPath);
        if (scLibFile.exists()) {
            boolean differentFiles = false;
            List<String> targetXmlFiles = getModulePaths(scXmlFiles, module, targetPath);
            List<String> resPaths = getResourcePaths(scLibFile);
            if(resPaths.size() == targetXmlFiles.size()) {
                for(String resPath : resPaths) {
                    if(!targetXmlFiles.contains(resPath)) {
                        differentFiles = true;
                        break;
                    }
                }
            } else differentFiles=true;
            if(!differentFiles) {
                boolean newer = false;
                for (String pathStr: scXmlFiles) {
                    File path = new File(docroot, pathStr);
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
        for (String input: scXmlFiles) {
            String path = getModulePath(input,module,targetPath);
            docRelPaths.add(path);
        }
        createResources(writer, docRelPaths);
        
        for (String input: scXmlFiles) {
            Document doc = Xml.parseMutable(new File(docroot, input));
            createStatusCodes(writer, doc, docRelPaths.indexOf(getModulePath(input,module,targetPath)));
        }
            
        writer.write("}\n");
        writer.flush();
        writer.close();
            
        return true;
    }
    
    private static List<String> getResourcePaths(File scLibFile) throws IOException {
        List<String> paths = new ArrayList<String>();
        String regexp = "\\s*ResourceUtil.getFileResourceFromDocroot\\(\"([^\\)]+)\"\\).*";
        Pattern pattern = Pattern.compile(regexp);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(scLibFile), "ISO-8859-1"));
        String line;
        while((line=reader.readLine())!=null) {
            Matcher matcher = pattern.matcher(line);
            if(matcher.matches()) {
                paths.add(matcher.group(1));
            }
        }
        reader.close();
        return paths;
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

    
    private static String getModulePath(String relPath, String module, String targetPath) {
        if(module!=null) {
            int ind = relPath.lastIndexOf('.');
            if(ind == -1) throw new RuntimeException("Illegal file name: "+relPath);
            relPath = relPath.substring(0,ind)+"-merged"+relPath.substring(ind);
            String modulePath="";
            if(module.equals("pfixcore")) {
                if(!relPath.startsWith("core")) throw new RuntimeException("Illegal core file name: "+relPath);
                modulePath = "core-override/"+relPath.substring(5);
            } else {
                if(relPath.startsWith("PUSTEFIX-INF/")) relPath = relPath.substring(13);
                modulePath = "modules-override/"+module+"/"+relPath;
            }
            return modulePath;
        } else if(targetPath!=null) {
            int ind = relPath.lastIndexOf('/');
            if(ind>0) relPath=relPath.substring(ind+1);
            return targetPath+"/"+relPath;
        }
        return relPath;
    }
    
    private static List<String> getModulePaths(List<String> relPaths, String module, String targetPath) {
        List<String> newPaths = new ArrayList<String>();
        for(String relPath : relPaths) {
            newPaths.add(getModulePath(relPath, module, targetPath));
        }
        return newPaths;
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
