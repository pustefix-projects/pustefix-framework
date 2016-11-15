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

package de.schlund.pfixxml;

import java.io.StringWriter;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Node;

import com.icl.saxon.Context;
import com.icl.saxon.Controller;
import com.icl.saxon.expr.FragmentValue;
import com.icl.saxon.expr.NodeSetValue;
import com.icl.saxon.expr.StaticContext;
import com.icl.saxon.expr.TextFragmentValue;
import com.icl.saxon.expr.Value;
import com.icl.saxon.om.NodeEnumeration;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.output.Outputter;
import com.icl.saxon.output.XMLEmitter;

import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.util.ExtensionFunctionUtils;
import de.schlund.pfixxml.util.XsltContext;
import de.schlund.pfixxml.util.xsltimpl.XsltContextSaxon1;

/**
 * @author mleidig@schlund.de
 */
public class IncludeDocumentExtensionSaxon1 {
    
    public static Object get(Context context,String path_str,String part,TargetGenerator targetgen,String targetkey,
            String parent_part_in,String parent_theme_in,String computed_inc,String module,String search, 
            String tenant, String language) throws Exception {    
        try {
            XsltContext xsltContext=new XsltContextSaxon1(context);
            return IncludeDocumentExtension.get(xsltContext,path_str,part,targetgen,targetkey,
                    parent_part_in,parent_theme_in,computed_inc,module,search, tenant, language);
        } catch(Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }
    
    public static boolean exists(Context context,String path_str,String part,TargetGenerator targetgen,String targetkey,
            String module,String search, String tenant, String language) throws Exception {    
        try {
            XsltContext xsltContext=new XsltContextSaxon1(context);
            return IncludeDocumentExtension.exists(xsltContext,path_str,part,targetgen,targetkey,
                                                   module,search, tenant, language);
        } catch(Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }
    
    public static String getSystemId(Context context) {
        XsltContext xsltContext=new XsltContextSaxon1(context);
        return IncludeDocumentExtension.getSystemId(xsltContext);
    }
    
    public static final String getRelativePathFromSystemId(Context context) {
        XsltContext xsltContext=new XsltContextSaxon1(context);
        return IncludeDocumentExtension.getRelativePathFromSystemId(xsltContext);
    }
    
    public static final String getModuleFromSystemId(Context context) {
        XsltContext xsltContext = new XsltContextSaxon1(context);
        return IncludeDocumentExtension.getModuleFromSystemId(xsltContext);
    }
    
    public static boolean isIncludeDocument(Context context) {
        XsltContext xsltContext=new XsltContextSaxon1(context);
        return IncludeDocumentExtension.isIncludeDocument(xsltContext);
    }
    
    public static String getResolvedURI() {
        return IncludeDocumentExtension.getResolvedURI();
    }
    
    public static String getDynIncInfo(String part, String theme, String path, String resolvedModule, String requestedModule, String tenant, String language) {
        return IncludeDocumentExtension.getDynIncInfo(part, theme, path, resolvedModule, requestedModule, tenant, language);
    }

    public static String getLocation(Context context) {
        StringBuilder sb = new StringBuilder();
        NodeInfo currentNode = context.getCurrentNodeInfo();
        if(currentNode != null) {
            sb.append(currentNode.getSystemId() + ":" + currentNode.getParent().getParent().getLineNumber());
        }
        StaticContext staticContext = context.getStaticContext();
        sb.append(" "+staticContext.getSystemId()+ ":"+staticContext.getLineNumber());
        return sb.toString();
    }
    
    public static Node getIncludeInfo(Context context, String path, String module, String search, 
            String tenant, String language, String targetkey, TargetGenerator targetgen) throws Exception {
        XsltContext xsltContext=new XsltContextSaxon1(context);
        return IncludeDocumentExtension.getIncludeInfo(xsltContext, path, module, search, tenant, language, targetkey, targetgen);
    }

    public static String getMessage(Context context, TargetGenerator targetGen, String key, String lang, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) throws Exception {
        arg1 = stringify(arg1, context);
        arg2 = stringify(arg2, context);
        arg3 = stringify(arg3, context);
        arg4 = stringify(arg4, context);
        arg5 = stringify(arg5, context);
        return IncludeDocumentExtension.getMessage(targetGen, key, lang, arg1, arg2, arg3, arg4, arg5);
    }

    private static Object stringify(Object arg, Context context) throws TransformerException {
        if(arg instanceof Value) {
            Controller controller = context.getController();
            XMLEmitter emitter = new XMLEmitter();
            StringWriter writer = new StringWriter();
            emitter.setWriter(writer);
            Outputter old = controller.getOutputter();
            Properties props = new Properties();
            props.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
            controller.changeOutputDestination(props, emitter);
            try {
                if(arg instanceof FragmentValue) {
                    ((FragmentValue)arg).copy(controller.getOutputter());
                } else if(arg instanceof TextFragmentValue) {
                    ((TextFragmentValue)arg).copy(controller.getOutputter());
                } else if(arg instanceof NodeSetValue) {
                    NodeEnumeration enm = ((NodeSetValue)arg).enumerate(context, true);
                    while(enm.hasMoreElements()) {
                        NodeInfo node = enm.nextElement();
                        node.copy(controller.getOutputter());
                    }
                } else if(arg instanceof Value) {
                    controller.getOutputter().writeContent(((Value)arg).asString());
                } else {
                    return arg;
                }
            } finally {
                controller.resetOutputDestination(old);
            }
            return writer.toString();
        } else {
            return arg;
        }
    }

    public static boolean messageExists(TargetGenerator targetGen, String key, String lang) {
        return IncludeDocumentExtension.messageExists(targetGen, key, lang);
    }

}
