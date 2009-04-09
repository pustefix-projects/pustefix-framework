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
package de.schlund.pfixcore.util;
import gnu.getopt.Getopt;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.BasicConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.schlund.pfixxml.util.Xml;
/**
 * MultiTransform.java
 * 
 * 
 * Created: Wed Apr 24 20:07:52 2002
 * 
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher </a>
 * 
 *  
 */
public class MultiTransform {
    private static TransformerFactory trfac;
    private String srcdir = null;
    private String xslfile = null;
    private String action = null;
    private String out_ext = null;
    private HashMap<String, String> params = new HashMap<String, String>();
    private List<String> infiles = new LinkedList<String>();
    
    private MultiTransform() {
        BasicConfigurator.configure();
        trfac = TransformerFactory.newInstance();
    }
    
    public static void main(String[] args) throws Exception {
        if (args == null || args.length == 0) {
            System.out.println("Usage:");
            System.out.println(">>>     java de.schlund.pfixcore.util.MultiTransform -a action [std|iwrp] -x XSL-File");
            System.out.println("                                                    (-s sourcedir)? (-o out_extension)?");
            System.out.println("                                                    (-p key=val)* XML-Infile (XML-Infile)*");
            System.exit(-1);
        }
        MultiTransform instance = new MultiTransform();
        instance.scanOptions(args);
        instance.callAction();
    }
    public void callAction() throws Exception {
        boolean error = false;
        if (action != null) {
            if (action.equals("std")) {
                doStdfiles();
            } else if (action.equals("iwrp")) {
                doIWrappers();
            } else if (action.equals("zoneprops")) {
                doServletZone();
            } else if (action.equals("docu")) {
                doDocumentation();
            } else {
                error = true;
            }
        } else {
            error = true;
        }
        if (error) {
            System.out.println("action needs to be one of 'std', 'zoneprops' or 'iwrp'");
            System.exit(-1);
        }
        System.out.print("\n");
    }
    private void doDocumentation() throws Exception {
        Templates xsl = trfac.newTemplates(new StreamSource(xslfile));
        for (Iterator<String> i = infiles.iterator(); i.hasNext();) {
            String infile = i.next();
            // Remove last extension xsl.in
            String outfile = infile.substring(0, infile.lastIndexOf("xsl"));
            // Add doku.xml for generating documentation files
            outfile = outfile + "doku.xml";
            doTransformMaybe(null, xsl, infile, outfile, xslfile);
        }
    }
    private void doStdfiles() throws Exception {
        File f = new File(xslfile);
        Templates xsl = trfac.newTemplates(new StreamSource(f));
        for (Iterator<String> i = infiles.iterator(); i.hasNext();) {
            String infile = i.next();
            // Remove the last extension part: .foo.in => .foo
            String outfile = infile.substring(0, infile.lastIndexOf("."));
            // maybe add an output extension
            if ((out_ext != null)) {
                outfile = outfile + "." + out_ext;
            }
            doTransformMaybe(null, xsl, infile, outfile, xslfile);
        }
    }
    private void doIWrappers() throws Exception {
        File f = new File(xslfile);
        Templates xsl = trfac.newTemplates(new StreamSource(f));
        for (Iterator<String> i = infiles.iterator(); i.hasNext();) {
            String iwrp = i.next();
            String pkg = iwrp.substring(0, iwrp.lastIndexOf("/"));
            if (srcdir != null && pkg.startsWith(srcdir)) {
                pkg = pkg.substring(srcdir.length());
            }
            pkg = pkg.replace(File.separatorChar, '.');
            String outfile = iwrp.substring(0, iwrp.lastIndexOf("."));
            outfile = outfile + ".java";
            String klass = iwrp.substring(iwrp.lastIndexOf("/") + 1, iwrp.lastIndexOf("."));
            params.put("classname", klass);
            params.put("package", pkg);
            doTransformMaybe(null, xsl, iwrp, outfile, xslfile);
        }
    }
    private void doServletZone() throws Exception {
        String outdir_tomcat = (String) params.get("outdir-tomcat");
        String xslfile_tomcat = (String) params.get("xslfile-tomcat");
        if (outdir_tomcat == null) {
            System.out.println("*** Need to have a parameter 'outdir-tomcat'");
            System.exit(-1);
        }
        if (xslfile_tomcat == null) {
            System.out.println("*** Need to have a parameter 'xslfile-tomcat'");
            System.exit(-1);
        }
        Templates xsltomcat = trfac.newTemplates(new StreamSource(xslfile_tomcat));
        Document doc = Xml.parseMutable((String) infiles.get(0));
        NodeList nl = doc.getElementsByTagName("project");
        for (int i = 0; i < nl.getLength(); i++) {
            Element prj = (Element) nl.item(i);
            String name = prj.getAttribute("name");
            params.put("prjname", name);
            File dir_tomcat = new File(outdir_tomcat + "/webapps/" + name + "/WEB-INF");
            dir_tomcat.mkdirs();
            String outfile_tomcat = dir_tomcat.getAbsolutePath() + "/web.xml";
            doTransformMaybe(doc, xsltomcat, (String) infiles.get(0), outfile_tomcat, xslfile_tomcat);
        }
    }
    private void doTransformMaybe(Document indoc, Templates tmpl, String infile, String outfile, String xslfile) throws TransformerConfigurationException, TransformerException {
        File in = new File(infile);
        File out = new File(outfile);
        File xsl = new File(xslfile);
        if (in.exists() && in.isFile() && in.canRead() && (!out.exists() || out.canWrite())) {
            if ((out.exists() && ((in.lastModified() > out.lastModified()) || xsl.lastModified() > out.lastModified())) || !out.exists()) {
                System.out.println(">>> " + infile + " ==> " + outfile);
                Transformer trafo = tmpl.newTransformer();
                for (Iterator<String> i = params.keySet().iterator(); i.hasNext();) {
                    String key = i.next();
                    String val = params.get(key);
                    trafo.setParameter(key, val);
                }
                if (indoc != null) {
                    trafo.transform(new DOMSource(indoc), new StreamResult(outfile));
                } else {
                    File f = new File(infile);
                    trafo.transform(new StreamSource(f), new StreamResult(outfile));
                }
            } else {
                System.out.print(".");
            }
        } else {
            StringBuffer buf = new StringBuffer();
            buf.append("File error: ").append(infile).append(" ==> ").append(outfile).append("\n");
            buf.append(infile + " isFile: ").append(in.isFile()).append("\n");
            buf.append(infile + " canRead: ").append(in.canRead()).append("\n");
            buf.append(outfile + " exists: ").append(in.exists()).append("\n");
            buf.append(outfile + " canWrite: ").append(in.exists()).append("\n");
            throw new RuntimeException(buf.toString());
        }
    }
    public void scanOptions(String[] args) throws Exception {
        Getopt g = new Getopt("MultiTransform", args, "s:x:p:a:");
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'x' :
                    xslfile = g.getOptarg();
                    break;
                case 's' :
                    srcdir = g.getOptarg();
                    break;
                case 'o' :
                    out_ext = g.getOptarg();
                    break;
                case 'a' :
                    action = g.getOptarg();
                    break;
                case 'p' :
                    String par = g.getOptarg();
                    if (par.indexOf("=") <= 0) {
                    }
                    String key = par.substring(0, par.indexOf("="));
                    String val = par.substring(par.indexOf("=") + 1);
                    params.put(key, val);
                    break;
                case '?' :
                    System.exit(-1);
            }
        }
        boolean error = false;
        if (xslfile == null) {
            System.out.println("Error: Need to give a xsl file");
            error = true;
        }
        if (action == null) {
            System.out.println("Error: Need to give an action");
            error = true;
        }
        if (g.getOptind() == args.length) {
            System.out.println("Error: Need at last one input file");
            error = true;
        }
        if (error)
            System.exit(-1);
        for (int i = g.getOptind(); i < args.length; i++) {
            infiles.add(args[i]);
        }
    }
} // MultiTransform
