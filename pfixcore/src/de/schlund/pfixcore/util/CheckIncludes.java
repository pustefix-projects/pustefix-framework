package de.schlund.pfixcore.util;
import com.icl.saxon.expr.*;
import de.schlund.pfixxml.*;
import de.schlund.pfixxml.targets.*;
import de.schlund.pfixxml.xpath.*;
import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.apache.log4j.xml.*;
import org.apache.xml.serialize.*;
import org.apache.xpath.*;
import org.w3c.dom.*;

/**
 * CheckIncludes.java
 *
 *
 * Created: Tue Apr  8 15:42:11 2003
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */

public class CheckIncludes {
    private HashMap generators       = new HashMap();
    private TreeSet includefilenames = new TreeSet();
    private TreeSet includes;
    private static final String XPATH = "/include_parts/part/product";
    private String  pwd;
    private static DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
    
    private void usuage() {
        System.out.println("");
        System.out.println("");
        System.out.println("");
    }
    
    public CheckIncludes(String pwd, File allprj, File allincs) throws Exception {
        this.pwd = pwd;

        LineNumberReader input = new LineNumberReader(new FileReader(allprj));
        String           line;
        
        while ((line = input.readLine()) != null) {
            line = pwd + line;
            TargetGenerator gen = new TargetGenerator(new File(line));
            generators.put(line, gen);
        }
        input.close();

        includes = AuxDependencyFactory.getInstance().getAllAuxDependencies();

        // for (Iterator i = includes.iterator(); i.hasNext();) {
        //      AuxDependency aux = (AuxDependency) i.next();
        // }

        input = new LineNumberReader(new FileReader(allincs));
        while ((line = input.readLine()) != null) {
            line = new File(pwd + line).getCanonicalPath();
            includefilenames.add(line);
        }
        input.close();
    }

    
    
    public static void main(String[] args) throws Exception {
        String allprjarg = args[0];
        String allincarg = args[1];

        File   dir = new File(".");

        String pwd = dir.getCanonicalPath() + "/";

        DOMConfigurator.configure(pwd + "core/conf/generator_quiet.xml");
        
        CheckIncludes instance = new CheckIncludes(pwd, new File(allprjarg), new File(allincarg));

        Document doc = instance.checkForUnusedIncludes();

        OutputFormat out_format = new OutputFormat("xml", "ISO-8859-1", true);
        out_format.setIndent(2);
        out_format.setPreserveSpace(false);
        FileOutputStream out_stream;
        out_stream = new FileOutputStream("CheckOutput.xml");
        XMLSerializer ser = new XMLSerializer(out_stream, out_format);
        ser.serialize(doc);

    }

    private Document checkForUnusedIncludes() throws Exception {
        IncludeDocumentFactory incfac = IncludeDocumentFactory.getInstance();
        Document result   = dbfac.newDocumentBuilder().newDocument();
        Element  res_root = result.createElement("checkresult");
        result.appendChild(res_root);
        
        for (Iterator i = includefilenames.iterator(); i.hasNext();) {
            String path = (String) i.next();
            String simplepath = path.substring(pwd.length());
            Document doc;

            Element res_incfile = result.createElement("incfile");
            res_root.appendChild(res_incfile);
            res_incfile.setAttribute("name", path);
            
            try {
                IncludeDocument incdoc = incfac.getIncludeDocument(path, true);
                doc    = incdoc.getDocument();
            } catch (Exception e) {
                Element error = result.createElement("ERROR");
                res_incfile.appendChild(error);
                error.setAttribute("cause", e.getMessage());
                continue;
            }
            
            Element root = doc.getDocumentElement();
            if (!root.getNodeName().equals("include_parts")) {
                Element error = result.createElement("ERROR");
                res_incfile.appendChild(error);
                error.setAttribute("cause", "not an include file (root != include_parts)");
                continue;
            }
            
            NodeList nl = root.getChildNodes();
            for (int j = 0; j < nl.getLength(); j++) {
                if (nl.item(j) instanceof Element) {
                    Element partelem = (Element) nl.item(j);
                    
                    if (!partelem.getNodeName().equals("part")) {
                        Element error = result.createElement("ERROR");
                        res_incfile.appendChild(error);
                        error.setAttribute("cause", "invalid node in include file (child of root != part): " + simplepath + "/" + partelem.getNodeName());
                        continue;
                    }

                    Element res_part = result.createElement("part");
                    res_incfile.appendChild(res_part);
                    res_part.setAttribute("name", partelem.getAttribute("name"));
                    
                    NodeList prodchilds = partelem.getChildNodes();
                    for (int k = 0; k < prodchilds.getLength(); k++) {
                        if (prodchilds.item(k) instanceof Element) {
                            Element productelem = (Element) prodchilds.item(k);
                            if (!productelem.getNodeName().equals("product")) {
                                Element error = result.createElement("ERROR");
                                res_part.appendChild(error);
                                error.setAttribute("cause", "invalid node in part (child of part != product): " +
                                                   simplepath + "/" + partelem.getNodeName() + "/" + productelem.getNodeName());
                                continue;
                            }

                            String part    = partelem.getAttribute("name");
                            String product = productelem.getAttribute("name");

                            Element res_product = result.createElement("product");
                            res_part.appendChild(res_product);
                            res_product.setAttribute("name", product);
                            
                            AuxDependency aux =
                                AuxDependencyFactory.getInstance().getAuxDependency(DependencyType.TEXT, path, part, product);
                            if (!includes.contains(aux)) {
                                res_product.setAttribute("UNUSED", "true");
                                continue;
                            }

                            NodeList langchilds = productelem.getChildNodes();
                            for (int l = 0; l < langchilds.getLength(); l++) {
                                if (langchilds.item(l) instanceof Element) {
                                    Element langelem = (Element) langchilds.item(l);
                                    if (!langelem.getNodeName().equals("lang")) {
                                        Element error = result.createElement("ERROR");
                                        res_product.appendChild(error);
                                        error.setAttribute("cause", "invalid node in product (child of product != lang): " +
                                                           simplepath + "/" + partelem.getNodeName() + "/" +
                                                           productelem.getNodeName() + "/" + langelem.getNodeName());
                                        continue;
                                    }
                                    
                                    String langname = langelem.getAttribute("name");
                                    
                                    Element res_lang = result.createElement("lang");
                                    res_product.appendChild(res_lang);
                                    res_lang.setAttribute("name", langname);
                                    
                                    NodeList     textnodes = XPathAPI.selectNodeList(langelem, ".//text()");
                                    StringBuffer textbuff  = new StringBuffer("");
                                    for (int x = 0; x < textnodes.getLength(); x++) {
                                        String text = ((Text) textnodes.item(x)).getNodeValue();
                                        text = text.trim();
                                        textbuff.append(text + " ");
                                    }
                                    
                                    String alltext = textbuff.toString().trim();
                                    
                                    if (!alltext.equals("")) {
                                        Element textelm = result.createElement("text");
                                        Text    textval = result.createTextNode(alltext);
                                        textelm.appendChild(textval);
                                        res_lang.appendChild(textelm);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }
    
} // CheckIncludes
