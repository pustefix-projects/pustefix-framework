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

package de.schlund.pfixxml;

import de.schlund.pfixxml.*;
import de.schlund.pfixxml.serverutil.*;
import de.schlund.pfixxml.targets.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.apache.log4j.*;
import org.w3c.dom.*;

/**
 *
 *
 * -------------------------------------------------------------------------
 *
 * This class is at the top of the XML/XSLT System.
 * It serves as an abstract parent class for all servlets
 * needing access to the XML/XSL cache system povided by
 * de.schlund.pfixxml.TargetGenerator.<p><br>
 * Servlets inheriting from this class need to implement
 * getDom(HttpServletRequest req, HttpServletResponse res)
 * which returns a SPDocument. <br>
 */

public abstract class AbstractXMLServer extends ServletManager {
    private static DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
    static { dbfac.setNamespaceAware(true); }

    public  static final String SESS_LANG        = "__SELECTED_LANGUAGE__";
    public  static final String XML_CONTENT_TYPE = "text/xml; charset=iso-8859-1";

    public  static final String PARAM_XMLONLY    = "__xmlonly";
    public  static final String PARAM_ANCHOR     = "__anchor";
    public  static final String PARAM_EDITMODE   = "__editmode";
    public  static final String PARAM_LANG       = "__language";
    public  static final String PARAM_FRAME      = "__frame";
    public  static final String PARAM_NOSTORE    = "__nostore";
    public  static final String PARAM_REUSE      = "__reuse"; // internally used

    public  static final String XSLPARAM_LANG    = "lang";
    public  static final String XSLPARAM_SESSID  = "__sessid";
    public  static final String XSLPARAM_URI     = "__uri";
    public  static final String XSLPARAM_SERVP   = "__servletpath";
    public  static final String XSLPARAM_FRAME   = "__frame";
    public  static final String XSLPARAM_REUSE   = "__reusestamp";

    private static final String XSLPARAM_TG      = "__target_gen";
    private static final String XSLPARAM_TKEY    = "__target_key";
    private static final String VALUE_NONE       = "__NONE__";
    
    public  static final String SUFFIX_SAVEDDOM  = "_SAVED_DOM";

    public  static final String PROP_DEPEND      = "xmlserver.depend.xml";
    public  static final String PROP_NAME        = "xmlserver.servlet.name";
    public  static final String PROP_NOEDIT      = "xmlserver.noeditmodeallowed";
    public  static final String PROP_NOXML       = "xmlserver.noxmlonlyallowed";
    public  static final String PROP_RENDER_EXT  = "xmlserver.output.externalrenderer";
    
    /**
     * Holds the TargetGenerator which is the XML/XSL Cache for this
     * class of servlets.
     */
    protected TargetGenerator generator = null;
    /**
     * The unique Name of this servlet, needed to create a Namespace in
     * the HttpSession Session.
     */
    protected String servletname = null;
    /**
     * The configuration file for the TargetGeneratorFacory.
     */
    private        String   targetconf      = null;
    private        boolean  render_external = false;
    private static Category LOGGER_TRAIL    = Category.getInstance("LOGGER_TRAIL");
    private static Category CAT             = Category.getInstance(AbstractXMLServer.class.getName());
    /**
     * Init method of all servlets inheriting from AbstractXMLServers.
     * It calls super.init(Config) as a first step.
     * @param ServletConfig config. Passed in from the servlet container.
     * @return void
     * @exception ServletException thrown when the initialisation goes havoc somehow
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        CAT.debug("\n>>>> In init of AbstractXMLServer <<<<");
        initValues();
        CAT.debug("End of init AbstractXMLServer");
    }

    private void initValues() throws ServletException {
        if ((targetconf = getProperties().getProperty(PROP_DEPEND)) == null) {
            throw(new ServletException("Need property '" + PROP_DEPEND + "'"));
        }

        if ((servletname = getProperties().getProperty(PROP_NAME)) == null) {
            throw(new ServletException("Need property '" + PROP_NAME + "'"));
        }
        
        try {
            generator = TargetGeneratorFactory.getInstance().createGenerator(targetconf);
        } catch(Exception e){
            CAT.error("Error: ",e);
            throw(new ServletException("Couldn't get TargetGenerator: " + e.toString()));
        }
        
        String render_external_prop = getProperties().getProperty(PROP_RENDER_EXT);
        if ((render_external_prop != null) && render_external_prop.equals("true")) {
            render_external = true;
        }
        
    }

    protected boolean tryReloadProperties(PfixServletRequest preq) throws ServletException {
        if (super.tryReloadProperties(preq)) {
            initValues();
            return true;
        } else {
            try {
                generator.tryReinit();
            } catch (Exception e) {
                throw new ServletException("When trying to reinit generator: " + e);
            }
            return false;
        }
    }
    
    /**
     * A child of AbstractXMLServer must implement this method.
     * It is here where the final Dom tree and parameters for
     * applying to the stylesheet are put into SPDocument.
     * @param HttpServletRequest req:  the current request
     * @return SPDocument: The result Dom tree and parameters
     * @exception Exception Anything that can go wrong when constructing the resulting
     * SPDocument object
     */
    public abstract SPDocument getDom(PfixServletRequest preq)  throws Exception;
    
    /**
     * This is the method that is called for any servlet that inherits from ServletManager.
     * It calls getDom(req, res) to get the SPDocument doc.
     * This SPDocument is stored in the HttpSession so it can be reused if
     * the request parameter __reuse is set to "1". In other words, <b>if</b> the
     * request parameter __reuse is there and it is set to "1", getDom(req,res)
     * will <b>not</b> be called, instead the saved Dom tree from the previous request
     * to this servlet will be used.
     *
     * Request parameters that are put into the gen_params Hash:
     * <pre>__frame</pre><br>
     * <pre>__uri</pre><br>
     * <pre>__sessid</pre><br>
     * <pre>__editmode</pre><br>
     * <pre>__reusestamp</pre><br>
     * <pre>lang</pre><br>
     * @param PfixServletRequest req
     * @param HttpServletResponse res
     * @exception Exception
     */
    protected void process(PfixServletRequest preq, HttpServletResponse res) throws Exception {
        Properties        params     = new Properties();
        HttpSession       session    = preq.getSession(false);
        boolean           doreuse    = doReuse(preq);
        ContainerUtil     conutil    = getContainerUtil();
        SPDocument        spdoc      = null;
        Date              timestamp  = null;
        RequestParam      value;
        long              currtime;

        // We look for the request parameter __frame and __reuse.
        // These are needed for possible frame handling by the stylesheet;
        // they will be stored in the params properties and will be applied as stylesheet
        // parameters in apply Stylesheet
        // if __reuse is set, we will try to reuse a stored DomTree, if __reuse is
        // not set, we store the DomTree from getDom in the Session as servletname + _saved_dom

       
        if ((value = preq.getRequestParam(PARAM_FRAME)) != null)
            if (value.getValue() != null) {
                params.put(XSLPARAM_FRAME, value.getValue());
            }

        params.put(XSLPARAM_URI,   preq.getRequestURI(res));
        params.put(XSLPARAM_SERVP, preq.getContextPath() + preq.getServletPath());

        if (session != null) {
            params.put(XSLPARAM_SESSID, conutil.getSessionValue(session, ContainerUtil.SESSION_ID_URL));
            if (doreuse) {
                synchronized (session) {
                    spdoc = (SPDocument) conutil.getSessionValue(session, servletname + SUFFIX_SAVEDDOM);
                }
            }
            // Now look for the parameter __editmode, and store it in the
            // session if it's there. Get the parameter from the session, and hand it over to the
            // Stylesheet params. Do the same for the parameter __language.
            if ((value = preq.getRequestParam(PARAM_LANG)) != null) {
                if (value.getValue() != null) {
                    conutil.setSessionValue(session, SESS_LANG, value.getValue());
                }
            }

            if (conutil.getSessionValue(session, SESS_LANG) != null) {
                params.put(XSLPARAM_LANG, (String) conutil.getSessionValue(session, SESS_LANG));
            }
            
            if ((value = preq.getRequestParam(PARAM_EDITMODE)) != null) {
                if (value.getValue() != null) {
                    conutil.setSessionValue(session, PARAM_EDITMODE, value.getValue());
                }
            }
                
            if (conutil.getSessionValue(session, PARAM_EDITMODE) != null) {
                // first we check if the properties prohibit editmode
                String noedit = getProperties().getProperty(PROP_NOEDIT);
                if ((noedit == null) || noedit.equals("0") || noedit.equals("false") || noedit.equals("")) {
                    params.put(PARAM_EDITMODE, (String) conutil.getSessionValue(session, PARAM_EDITMODE));
                }
            }
        }

        
        if (spdoc == null) {
            currtime = System.currentTimeMillis();
            spdoc = getDom(preq);
            if (CAT.isDebugEnabled()) {
                CAT.debug("* Document for XMLServer is" + spdoc);
            }
            CAT.info(">>> Complete getDom(...) took " + (System.currentTimeMillis() - currtime) + "ms");
            RequestParam[] anchors   = preq.getAllRequestParams(PARAM_ANCHOR);
            Map            anchormap;
            if (anchors != null && anchors.length > 0) {
                anchormap = createAnchorMap(anchors);
                spdoc.storeFrameAnchors(anchormap);
            }
            PublicXSLTProcessor xsltproc = TraxXSLTProcessor.getInstance();
            currtime = System.currentTimeMillis();
            spdoc.setXmlObject(xsltproc.xmlObjectFromDocument(spdoc.getDocument()));
            spdoc.setDocument(null);
            CAT.info(">>> Complete xmlObjectFromDocument(...) took " + (System.currentTimeMillis() - currtime) + "ms");
            RequestParam store = preq.getRequestParam(PARAM_NOSTORE);
            if (store == null || store.getValue() == null || !store.getValue().equals("1")) {
                if (session != null) {
                    long stamp = System.currentTimeMillis();
                    spdoc.setTimestamp(stamp);
                    synchronized (session) {
                        conutil.setSessionValue(session, servletname + SUFFIX_SAVEDDOM, spdoc);
                    }
                }
            }
        }
        params.put(XSLPARAM_REUSE, "" + spdoc.getTimestamp());
        
        currtime = System.currentTimeMillis();
        handleDocument(preq, res, spdoc, params, doreuse);
        CAT.info(">>> Complete handleDocument(...) took " + (System.currentTimeMillis() - currtime) + "ms");
    }

    protected void handleDocument(PfixServletRequest preq, HttpServletResponse res,
                                  SPDocument spdoc, Properties params, boolean doreuse) throws Exception {

        boolean plain_xml = false;
        // if the document contains a error code, do errorhandling here and no further processing.
        int    err;
        String errtxt;
        if ((err = spdoc.getResponseError()) != 0) {
            if ((errtxt = spdoc.getResponseErrorText()) != null) {
                res.sendError(err, errtxt);
            } else {
                res.sendError(err);
            }
            return;
        }

        // So no error happened, let's go on with normal processing.
        
        HttpSession   session              = preq.getSession(false);
        ContainerUtil conutil              = getContainerUtil();
        TreeMap       paramhash            = constructParameters(spdoc, params);
        String        stylesheet           = extractStylesheetFromSPDoc(spdoc);

        if (stylesheet == null) {
            throw new XMLException("Wasn't able to extract any stylesheet specification... bailing out.");
        }
        if (!doreuse) {
            CAT.info(" *** Using stylesheet: " + stylesheet + " ***");
            if (session != null) {
                // we only want to update the Session hit when we are not handling a "reuse" request 
                SessionAdmin.getInstance().touchSession(servletname, stylesheet, session);
            }
            // Only process cookies if we don't reuse
            if (spdoc.getCookies() != null && !spdoc.getCookies().isEmpty() ) {
                CAT.debug("*** Sending cookies ***");
                // Now adding the Cookies from spdoc
                for (Iterator i = spdoc.getCookies().iterator(); i.hasNext(); ) {
                    Cookie cookie = (Cookie) i.next();
                    CAT.debug("    Add cookie: " + cookie);
                    res.addCookie(cookie);
                }
            }
        }

        // Check the document for supplied headers...
        HashMap headers = spdoc.getResponseHeader();
        if (headers != null) {
            for (Iterator i = headers.keySet().iterator(); i.hasNext();) {
                String key = (String) i.next();
                String val = (String) headers.get(key);
                CAT.debug("*** Setting custom supplied header: " + key + " -> " + val);
                res.setHeader(key, val);
            }
        } else {
            // set some default values to prohibit caching...
            res.setHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
            res.setHeader("Pragma", "no-cache");
            res.setHeader("Cache-Control", "no-cache, no-store, private, must-revalidate");
        }
        
        // Check if a content type was supplied
        String ctype;
        if ((ctype = spdoc.getResponseContentType()) != null) {
            res.setContentType(ctype);
        } else {
            res.setContentType(DEF_CONTENT_TYPE);
        }

        // Check if we are allowed and should just supply the xml doc

        RequestParam doplainxml = preq.getRequestParam(PARAM_XMLONLY);
        if (doplainxml != null && doplainxml.getValue().equals("1")) {
            String noxmlallowed = getProperties().getProperty(PROP_NOXML);
            if (noxmlallowed != null && (noxmlallowed.equals("false") || noxmlallowed.equals("0"))) {
                plain_xml = true;
            }
        }
        
        if (!render_external && !plain_xml) {
            PublicXSLTProcessor xsltproc = TraxXSLTProcessor.getInstance();
            try {
                xsltproc.applyTrafoForOutput(spdoc.getXmlObject(), 
                                             generator.getTarget(stylesheet).getValue(),
                                             paramhash, res.getOutputStream());
            } catch (TransformerException e) {
                CAT.warn("[Ignored TransformerException] ", e);
            }
                        
        } else if (plain_xml) {
            res.setContentType(XML_CONTENT_TYPE);
            TransformerFactory.newInstance().newTransformer().
                transform(new DOMSource(spdoc.getXmlObject()), new StreamResult(res.getOutputStream()));
        } else {
            Document ext_doc = dbfac.newDocumentBuilder().newDocument();
            Element  root    = ext_doc.createElement("render_external");
            ext_doc.appendChild(root);
            Element  ssheet  = ext_doc.createElement("stylesheet");
            root.appendChild(ssheet);
            ssheet.setAttribute("name", generator.getDisccachedir() + stylesheet);
            for (Iterator i = paramhash.keySet().iterator(); i.hasNext(); ) {
                String  key   = (String) i.next();
                String  val   = (String) paramhash.get(key);
                Element param = ext_doc.createElement("param");
                param.setAttribute("key", key);
                param.setAttribute("value", val);
                root.appendChild(param);
            }
            Node imported = ext_doc.importNode(spdoc.getXmlObject().getDocumentElement(), true);
            root.appendChild(imported);
            TransformerFactory.newInstance().newTransformer().
                transform(new DOMSource(ext_doc), new StreamResult(res.getOutputStream()));
        }
        
        if (!doreuse && session != null) {
            StringBuffer logbuff = new StringBuffer();
            logbuff.append(conutil.getSessionValue(session, VISIT_ID) + "|");
            logbuff.append(session.getId() + "|");
            logbuff.append(preq.getRemoteAddr() + "|");
            logbuff.append(preq.getServerName() + "|");
            logbuff.append(stylesheet + "|");
            logbuff.append(preq.getOriginalRequestURI());
            if (preq.getQueryString() != null) {
                logbuff.append("?" + preq.getQueryString());
            }
            LOGGER_TRAIL.warn(logbuff.toString());
        }
    }
    
    private TreeMap constructParameters(SPDocument spdoc, Properties gen_params) {
        TreeMap    paramhash = new TreeMap();
        Properties params    = spdoc.getProperties();
        // These are properties which have been set in the process method
        //  e.g. Frame handling is stored here 
        if (gen_params != null) {
            for (Enumeration e = gen_params.keys(); e.hasMoreElements();) {
                String name = (String) e.nextElement();
                String value= (String) gen_params.get(name);
                if (name != null && value != null) {
                    paramhash.put(name, value);
                }
            }
        }
        // These are the params that may be set by the DomTree producing
        // method of the servlet (something that implements the abstract method getDom)
        if (params != null) {
            for (Enumeration e = params.keys(); e.hasMoreElements();) {
                String name  = (String) e.nextElement();
                String value = (String) params.get(name);
                if (name != null && value != null) {
                    paramhash.put(name, value);
                }
            }
        }
        paramhash.put(XSLPARAM_TG,   targetconf);
        paramhash.put(XSLPARAM_TKEY, VALUE_NONE);
        return paramhash;
    }

    private String extractStylesheetFromSPDoc (SPDocument spdoc) {
        // First look if the pagename is set
        String pagename = spdoc.getPagename();
        if (pagename != null) {
            PageTargetTree pagetree = generator.getPageTargetTree();
            PageInfo       pinfo    = PageInfoFactory.getInstance().getPage(generator, pagename);
            Target         target   = pagetree.getTargetForPageInfo(pinfo);
            if (target == null) {
                CAT.warn("\n********************** NO TARGET ******************************");
                return null;
            } else {
                return target.getTargetKey();
            }
        } else {
            // other possibility: an explicit xslkey is given:
            return spdoc.getXSLKey();
        }
    }

    private boolean doReuse(PfixServletRequest preq) {
        HttpSession session = preq.getSession(false);
        if (session != null) {
            RequestParam reuse = preq.getRequestParam(PARAM_REUSE);
            if (reuse != null && reuse.getValue() != null) {
                SPDocument saved = (SPDocument) getContainerUtil().getSessionValue(session, servletname + SUFFIX_SAVEDDOM);
                if (saved == null) return false;
                String stamp = saved.getTimestamp() + "";
                if (reuse.getValue().equals(stamp)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private Map createAnchorMap(RequestParam[] anchors) {
        Map map = new HashMap();
        for (int i = 0; i < anchors.length; i++) {
            String value  = anchors[i].getValue();
            int    pos    = value.indexOf(":");
            if (pos < (value.length() - 1) && pos > 0) {
                String frame  = value.substring(0, pos);
                String anchor = value.substring(pos + 1);
                map.put(frame, anchor);
            }
        }
        return map;
    }
}

