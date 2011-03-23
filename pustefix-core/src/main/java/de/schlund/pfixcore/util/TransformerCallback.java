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

import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.pustefixframework.config.contextxmlservice.IWrapperConfig;
import org.pustefixframework.config.contextxmlservice.PageRequestConfig;
import org.pustefixframework.config.contextxmlservice.ProcessActionPageRequestConfig;
import org.pustefixframework.http.BotDetector;
import org.pustefixframework.util.FrameworkInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.schlund.pfixcore.auth.AuthConstraint;
import de.schlund.pfixcore.auth.Authentication;
import de.schlund.pfixcore.auth.Condition;
import de.schlund.pfixcore.auth.Role;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.generator.IWrapperInfo;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextImpl;
import de.schlund.pfixcore.workflow.IWrapperState;
import de.schlund.pfixcore.workflow.PageRequest;
import de.schlund.pfixcore.workflow.RequestTokenAwareState;
import de.schlund.pfixcore.workflow.State;
import de.schlund.pfixcore.workflow.context.AccessibilityChecker;
import de.schlund.pfixcore.workflow.context.RequestContextImpl;
import de.schlund.pfixxml.ResultDocument;
import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.util.ExtensionFunctionUtils;
import de.schlund.pfixxml.util.Xml;
import de.schlund.pfixxml.util.XsltVersion;

/**
 * Describe class TransformerCallback here.
 * 
 * 
 * Created: Tue Jul 4 14:45:43 2006
 * 
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class TransformerCallback {

    private final static Logger           LOG               = Logger.getLogger(TransformerCallback.class);
    private static DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();

    // public static void setNoStore(SPDocument spdoc) {
    // spdoc.setNostore(true);
    // }

    public static int isAccessible(RequestContextImpl requestcontext, TargetGenerator targetgen, String pagename) throws Exception {
        try {
            ContextImpl context = requestcontext.getParentContext();
            
            boolean pageExists = true;
            if(context.getContextConfig().getPageRequestConfig(pagename) == null) {
                pageExists = (targetgen.getPageTargetTree().getPageInfoForPageName(pagename) != null);
            }
            if(pageExists) {
                AccessibilityChecker check = (AccessibilityChecker) context;
                boolean retval;
                if (context.getContextConfig().isSynchronized()) {
                    synchronized (context) {
                        retval = check.isPageAccessible(pagename);
                    }
                } else {
                    retval = check.isPageAccessible(pagename);
                }
                if (retval) {
                    return 1;
                } else {
                    return 0;
                }
            }
            return -1;
        } catch (Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }

    public static int isVisited(RequestContextImpl requestcontext, String pagename) throws Exception {
        try {
            ContextImpl context = requestcontext.getParentContext();
            if (context.getContextConfig().getPageRequestConfig(pagename) != null) {
                AccessibilityChecker check = (AccessibilityChecker) context;
                boolean retval;
                if (context.getContextConfig().isSynchronized()) {
                    synchronized (context) {
                        retval = check.isPageAlreadyVisited(pagename);
                    }
                } else {
                    retval = check.isPageAlreadyVisited(pagename);
                }
                if (retval) {
                    return 1;
                } else {
                    return 0;
                }
            }
            return -1;
        } catch (Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }

    public static String getToken(RequestContextImpl requestContext, String tokenName) throws Exception {
        try {
            tokenName = tokenName.trim();
            if (tokenName.contains(":")) throw new IllegalArgumentException("Illegal token name: " + tokenName);
            String token = requestContext.getParentContext().getToken(tokenName);
            return token;
        } catch (Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }

    public static boolean requiresToken(RequestContextImpl requestContext, String pageName) throws Exception {
        try {
            ContextImpl context = requestContext.getParentContext();
            State state;
            if (pageName != null) {
                state = context.getPageMap().getState(pageName);
            } else {
                state = context.getPageMap().getState(context.getCurrentPageRequest());
            }
            if (state == null) {
                return false;
            }
            if (state instanceof RequestTokenAwareState) {
                RequestTokenAwareState rtaState = (RequestTokenAwareState) state;
                return rtaState.requiresToken();
            } else {
                return false;
            }
        } catch (Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }
    
    public static Node getIWrapperInfo(RequestContextImpl requestContext, Node docNode, String pageName, String prefix) {
        try {
            PageRequest pageRequest;
            ContextImpl context = requestContext.getParentContext();
            XsltVersion xsltVersion = Xml.getXsltVersion(docNode);
            if (pageName == null || pageName.equals("")) {
                pageRequest = requestContext.getCurrentPageRequest();
            } else {
                pageRequest = context.createPageRequest(pageName);
            }
            State state = context.getPageMap().getState(pageRequest);
            if (state != null && state instanceof IWrapperState) {
                IWrapperState iwState = (IWrapperState) state;
                Map<String, ? extends IWrapperConfig> iwrappers = iwState.getIWrapperConfigMap();
                IWrapperConfig iwrpConfig = iwrappers.get(prefix);
                if (iwrpConfig != null) {
                    Class<? extends IWrapper> iwrpClass = (Class<? extends IWrapper>) iwrpConfig.getWrapperClass();
                    if (iwrpClass != null) {
                        return IWrapperInfo.getDocument(iwrpClass, xsltVersion);
                    }
                }
            }
            return null;
        } catch (RuntimeException x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }
    
    public static Node getIWrappers(RequestContextImpl requestContext, Node docNode, String pageName) throws Exception {
        try {
            ContextImpl context = requestContext.getParentContext();
            XsltVersion xsltVersion = Xml.getXsltVersion(docNode);
            DocumentBuilder db = docBuilderFactory.newDocumentBuilder();
            Document doc = db.newDocument();
            Element root = doc.createElement("iwrappers");
            doc.appendChild(root);
            PageRequest pageRequest;
            if (pageName == null || pageName.equals("")) {
                pageRequest = requestContext.getCurrentPageRequest();
            } else {
                pageRequest = context.createPageRequest(pageName);
            }
            PageRequestConfig pageConfig = context.getContextConfig().getPageRequestConfig(pageRequest.getName());
            State state = context.getPageMap().getState(pageRequest);
            if (state != null && state instanceof IWrapperState) {
                IWrapperState iwState = (IWrapperState) state;
                Map<String, ? extends IWrapperConfig> iwrappers = iwState.getIWrapperConfigMap();
                for (String prefix : iwrappers.keySet()) {
                    Element elem = doc.createElement("iwrapper");
                    elem.setAttribute("prefix", prefix);
                    elem.setAttribute("class", iwrappers.get(prefix).getWrapperClass().getName());
                    elem.setAttribute("checkactive", "" + iwrappers.get(prefix).doCheckActive());
                    root.appendChild(elem);
                }
            }
            if(pageConfig != null) {
                Map<String, ? extends ProcessActionPageRequestConfig> actions = pageConfig.getProcessActions();
                if (actions != null && !actions.isEmpty()) {
                    Element actionelement = doc.createElement("actions");
                    root.appendChild(actionelement);
                    for (Iterator<? extends ProcessActionPageRequestConfig> iterator = actions.values().iterator(); iterator.hasNext();) {
                        ProcessActionPageRequestConfig action =  iterator.next();
                        ResultDocument.addObject(actionelement, "action", action);
                    }
                }
            }
            if (LOG.isDebugEnabled()) {
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer t = tf.newTransformer();
                t.setOutputProperty(OutputKeys.INDENT, "yes");
                StringWriter writer = new StringWriter();
                t.transform(new DOMSource(doc), new StreamResult(writer));
                LOG.debug(writer.toString());
            }
            Node iwrpDoc = Xml.parse(xsltVersion, doc);
            return iwrpDoc;
        } catch (Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }

    public static boolean hasRole(RequestContextImpl requestContext, String roleName) throws Exception {
        try {
            ContextImpl context = requestContext.getParentContext();
            Authentication auth = context.getAuthentication();
            if (auth != null) {
                return auth.hasRole(roleName);
            }
            return false;
        } catch (Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }
    
    public static boolean checkCondition(RequestContextImpl requestContext, String conditionId) throws Exception {
        try {
            ContextImpl context = requestContext.getParentContext();
            Condition condition = context.getContextConfig().getCondition(conditionId);
            if(condition != null) {
                return condition.evaluate(context);
            } else LOG.warn("CONDITION_NOT_FOUND|" + conditionId);
            return false;
        } catch (Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }
    
    public static boolean checkAuthConstraint(RequestContextImpl requestContext, String authConstraintId) throws Exception {
        try {
            ContextImpl context = requestContext.getParentContext();
            AuthConstraint constraint = context.getContextConfig().getAuthConstraint(authConstraintId);
            if(constraint != null) {
                return constraint.evaluate(context);
            } else LOG.warn("AUTHCONSTRAINT_NOT_FOUND|" + authConstraintId);
            return false;
        } catch (Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }

    public static Node getAllDefinedRoles(RequestContextImpl requestContext, Node docNode) throws Exception {
        try {
            Context context = requestContext.getParentContext();
            XsltVersion xsltVersion = Xml.getXsltVersion(docNode);
            DocumentBuilder db = docBuilderFactory.newDocumentBuilder();
            Document doc = db.newDocument();
            Element root = doc.createElement("roles");
            doc.appendChild(root);
            List<Role> configuredRoles = context.getContextConfig().getRoleProvider().getRoles();
            HashSet<Role> currentroles = new HashSet<Role>();
            if (context.getAuthentication() != null && context.getAuthentication().getRoles() != null) {
                currentroles.addAll(Arrays.asList(context.getAuthentication().getRoles()));
            }
            
            for (Role role : configuredRoles) {
                Element elem = doc.createElement("role");
                elem.setAttribute("name", role.getName());
                elem.setAttribute("initial", Boolean.toString(role.isInitial()));
                if (currentroles.contains(role)) {
                    elem.setAttribute("current", "true");
                } else {
                    elem.setAttribute("current", "false");
                }
                root.appendChild(elem);
            }
            
            if (LOG.isDebugEnabled()) {
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer t = tf.newTransformer();
                t.setOutputProperty(OutputKeys.INDENT, "yes");
                StringWriter writer = new StringWriter();
                t.transform(new DOMSource(doc), new StreamResult(writer));
                LOG.debug(writer.toString());
            }
            Node iwrpDoc = Xml.parse(xsltVersion, doc);
            return iwrpDoc;
        } catch (Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }
    
    /**
     * Checks authorization state of a page. Possible return values are:
     * 0 - no authorization required (no authconstraint defined)
     * 1 - already authorized (authconstraint defined and fulfilled)
     * 2 - not authorized, but authentication intended (authconstraint has login page)
     * 3 - not authorized, no authentication intended (authconstraint has no login page)
     */
    public static int checkAuthorization(RequestContextImpl requestContext, String pageName) throws Exception {
        try {
            int result = 0;
            ContextImpl context = requestContext.getParentContext();
            PageRequestConfig config = context.getContextConfig().getPageRequestConfig(pageName);
            if(config != null) {
                AuthConstraint authConst = config.getAuthConstraint();
                if(authConst==null) authConst = context.getContextConfig().getDefaultAuthConstraint();
                if(authConst != null) {
                    if(authConst.isAuthorized(context)) result = 1;
                    else if(authConst.getAuthPage(context)!=null) result = 2;
                    else result = 3;
                }
            }
            return result;
        } catch (Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }
    
    public static boolean isAuthorized(RequestContextImpl requestContext, String pageName) throws Exception {
        try {
            boolean result = true;
            ContextImpl context = requestContext.getParentContext();
            PageRequestConfig config = context.getContextConfig().getPageRequestConfig(pageName);
            if(config != null) {
                AuthConstraint authConst = config.getAuthConstraint();
                if(authConst==null) authConst = context.getContextConfig().getDefaultAuthConstraint();
                if(authConst != null) {
                    if(!authConst.isAuthorized(context)) result = false;
                }
            }
            return result;
        } catch (Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }
    
    public static boolean isBot(RequestContextImpl requestContext) {
        return BotDetector.isBot(requestContext.getPfixServletRequest().getRequest());
    }
    
    public static String getFrameworkVersion() {
        return FrameworkInfo.getVersion();
    }
    
    public static boolean needsLastFlow(RequestContextImpl requestContext, String pageName, String lastFlowName) throws Exception {
        try {
            ContextImpl context = requestContext.getParentContext();
            return context.needsLastFlow(pageName, lastFlowName);
        } catch (Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }
    
    public static String omitPage(RequestContextImpl requestContext, String pageName) throws Exception {
        try {
            ContextImpl context = requestContext.getParentContext();
            String defaultPage = context.getContextConfig().getDefaultPage(context.getVariant());
            return defaultPage.equals(pageName)?"":pageName;
        } catch (Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }
    
}
