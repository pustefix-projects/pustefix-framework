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

import de.schlund.pfixxml.jmx.JmxServer;
import de.schlund.pfixxml.jmx.TrailLogger;
import de.schlund.pfixxml.serverutil.*;
import de.schlund.pfixxml.targets.*;
import de.schlund.pfixxml.util.Path;
import de.schlund.pfixxml.util.Xml;
import de.schlund.pfixxml.util.Xslt;

import java.io.*;
import java.net.SocketException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.log4j.Category;
import org.w3c.dom.*;


/**
 * This class is at the top of the XML/XSLT System.
 * It serves as an abstract parent class for all servlets
 * needing access to the XML/XSL cache system povided by
 * de.schlund.pfixxml.TargetGenerator.<p><br>
 * Servlets inheriting from this class need to implement
 * getDom(HttpServletRequest req, HttpServletResponse res)
 * which returns a SPDocument. <br>
 */
public abstract class AbstractXMLServer extends ServletManager {

    //~ Instance/static variables ..................................................................
    // how to write xml to the result stream
    private static final int RENDER_NORMAL   = 0;
    private static final int RENDER_EXTERNAL = 1;
    private static final int RENDER_FONTIFY  = 2;
    private static final int RENDER_XMLONLY  = 3;

    private static final String   FONTIFY_SSHEET          = "core/xsl/xmlfontify.xsl";
    private static final String   SESS_LANG               = "__SELECTED_LANGUAGE__";
    private static final String   XML_CONTENT_TYPE        = "text/xml; charset=iso-8859-1";
    public  static final String   PARAM_XMLONLY           = "__xmlonly";
    public  static final String   PARAM_XMLONLY_FONTIFY   = "1"; // -> RENDER_FONFIFY
    public  static final String   PARAM_XMLONLY_XMLONLY   = "2"; // -> RENDER_XMLONLY
    public  static final String   PARAM_ANCHOR            = "__anchor";
    private static final String   PARAM_EDITMODE          = "__editmode";
    private static final String   PARAM_LANG              = "__language";
    private static final String   PARAM_FRAME             = "__frame";
    private static final String   PARAM_NOSTORE           = "__nostore";
    private static final String   PARAM_REUSE             = "__reuse"; // internally used
    private static final String   XSLPARAM_LANG           = "lang";
    private static final String   XSLPARAM_SESSID         = "__sessid";
    private static final String   XSLPARAM_URI            = "__uri";
    private static final String   XSLPARAM_SERVP          = "__servletpath";
    private static final String   XSLPARAM_REMOTE_ADDR    = "__remote_addr";
    private static final String   XSLPARAM_SERVER_NAME    = "__server_name";
    private static final String   XSLPARAM_REQUEST_SCHEME = "__request_scheme";
    private static final String   XSLPARAM_QUERYSTRING    = "__querystring";
    private static final String   XSLPARAM_FRAME          = "__frame";
    private static final String   XSLPARAM_REUSE          = "__reusestamp";
    private static final String   VALUE_NONE              = "__NONE__";
    private static final String   SUFFIX_SAVEDDOM         = "_SAVED_DOM";
    protected static final String PROP_DEPEND             = "xmlserver.depend.xml";
    protected static final String PROP_NAME               = "xmlserver.servlet.name";
    protected static final String PROP_NOEDIT             = "xmlserver.noeditmodeallowed";
    protected static final String PROP_RENDER_EXT         = "xmlserver.output.externalrenderer";
    protected static final String PROP_CLEANER_TO         = "sessioncleaner.timeout";
                                 
    // skip stat on all targets
    private static final String PROP_SKIP_GETMODTIMEMAYBEUPADTE_KEY           = "targetgenerator.skip_getmodtimemaybeupdate";
    private static final String PROP_SKIP_GETMODTIMEMAYBEUPADTE_ENABLED_VALUE = "true";

    private static final int XML_ONLY_ALLOWED    = 0;
    private static final int XML_ONLY_RESTRICTED = 1;
    private static final int XML_ONLY_PROHIBITED = 2;

    private static final String PROP_PROHIBITDEBUG = "xmlserver.prohibitdebug";
    private static final String PROP_PROHIBITINFO  = "xmlserver.prohibitinfo";
    
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
    private Path            targetconf        = null;
    private boolean         render_external   = false;
    private static Category LOGGER_TRAIL      = Category.getInstance("LOGGER_TRAIL");
    private static Category CAT               = Category.getInstance(AbstractXMLServer.class.getName());
    private boolean         editmodeAllowed   = false;

    private int scleanertimeout = 300;

    private boolean allowInfo  = true;
    private boolean allowDebug = true;
    
    //~ Methods ....................................................................................

    /**
     * Init method of all servlets inheriting from AbstractXMLServers.
     * It calls super.init(Config) as a first step.
     * @param ServletConfig config. Passed in from the servlet container.
     * @return void
     * @exception ServletException thrown when the initialisation goes havoc somehow
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        if (CAT.isDebugEnabled()) {
            CAT.debug("\n>>>> In init of AbstractXMLServer <<<<");
        }
        initValues();
        if (CAT.isDebugEnabled()) {
            CAT.debug("End of init AbstractXMLServer");
        }
    }

    private String getProperty(String name) throws ServletException {
        String value;
        
        value = getProperties().getProperty(name);
        if (value == null) {
            throw new ServletException("Need property '" + name + "'");
        }
        return value;
    }

    private void initValues() throws ServletException {
        targetconf  = PathFactory.getInstance().createPath(getProperty(PROP_DEPEND));
        servletname = getProperty(PROP_NAME);
        
        String prohibitDebug = getProperties().getProperty(PROP_PROHIBITDEBUG);
        if (prohibitDebug != null && (prohibitDebug.equals("true") || prohibitDebug.equals("1")))
            allowDebug = false;
        
        String prohibitInfo  = getProperties().getProperty(PROP_PROHIBITINFO);
        if (prohibitInfo != null && (prohibitInfo.equals("true") || prohibitInfo.equals("1")))
            allowInfo = false;
        
        String noedit = getProperties().getProperty(PROP_NOEDIT);
        editmodeAllowed = (noedit != null && (noedit.equals("false") || noedit.equals("0")));

		String timeout;
        if ((timeout = getProperties().getProperty(PROP_CLEANER_TO)) != null) {
            try {
                scleanertimeout = new Integer(timeout).intValue();
            } catch (NumberFormatException e) {
                throw new ServletException(e.getMessage());
            }
        }

        boolean skip_getmodtimemaybeupdate = handleSkipGetModTimeMaybeUpdateProps();
        
        try {
            generator = TargetGeneratorFactory.getInstance().createGenerator(targetconf);
        } catch (Exception e) {
            CAT.error("Error: ", e);
            throw new ServletException("Couldn't get TargetGenerator", e);
        }
        // tell targetgenerator to skip getModTimeMaybeUpdate or not
        generator.setIsGetModTimeMaybeUpdateSkipped(skip_getmodtimemaybeupdate);
        String render_external_prop = getProperties().getProperty(PROP_RENDER_EXT);
        if ((render_external_prop != null) && render_external_prop.equals("true")) {
            render_external = true;
        }
        if (isInfoEnabled()) {
            StringBuffer sb = new StringBuffer(255);
            sb.append("\n").append("AbstractXMLServer properties after initValues(): \n");
            sb.append("                targetconf = ").append(targetconf).append("\n");
            sb.append("               servletname = ").append(servletname).append("\n");
            sb.append("           editModeAllowed = ").append(editmodeAllowed).append("\n");
            sb.append("                   timeout = ").append(timeout).append("\n");
            sb.append("skip_getmodtimemaybeupdate = ").append(skip_getmodtimemaybeupdate).append("\n");
            sb.append("           render_external = ").append(render_external).append("\n");
            CAT.info(sb.toString());
        }
    }

    /**
     * Handle the properties concerning if the targetgenerator should skip
     * the check for modfied dependencies of targets (and the update of the respective target).
     * @return true if enabled, else false
     * @throws ServletException if the properties can not be found.
     */
    private boolean handleSkipGetModTimeMaybeUpdateProps() throws ServletException {
        boolean skip_getmodtimemaybeupdate = false;
        if (getProperties().getProperty(PROP_SKIP_GETMODTIMEMAYBEUPADTE_KEY) == null) {
            String msg = "Need property '" + PROP_SKIP_GETMODTIMEMAYBEUPADTE_KEY + "'";
            CAT.fatal(msg);
            throw new ServletException(msg);
        } else {
            String tmp = getProperties().getProperty(PROP_SKIP_GETMODTIMEMAYBEUPADTE_KEY);
            skip_getmodtimemaybeupdate = tmp.toUpperCase().equals(PROP_SKIP_GETMODTIMEMAYBEUPADTE_ENABLED_VALUE.toUpperCase()) ? true : false;
            if (isDebugEnabled()) {
                CAT.debug("SKIP_GETMODTIMEMAYBEUPDATE: " + skip_getmodtimemaybeupdate);
            }
        }
        return skip_getmodtimemaybeupdate;
    }

    protected boolean tryReloadProperties(PfixServletRequest preq) throws ServletException {
        if (super.tryReloadProperties(preq)) {
            initValues(); // this also does generator.tryReinit()
            return true;
        } else {
            try {
                // This is a fake. We also return true when only depend.xml change, but the properties not.
                // But we can only signal one type of "reload" event with the return value of this method,
                // so it's better to reload the properties one time too often.
                return generator.tryReinit(targetconf);
            } catch (Exception e) {
                throw new ServletException("When trying to reinit generator: " + e);
            }
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
    protected abstract SPDocument getDom(PfixServletRequest preq) throws Exception;

    /**
     * This is the method that is called for any servlet that inherits from ServletManager.
     * It calls getDom(req, res) to get the SPDocument doc.
     * This SPDocument is stored in the HttpSession so it can be reused if
     * the request parameter __reuse is set to a timestamp matching the timestamp of the saved SPDocument.
     * In other words, <b>if</b> the request parameter __reuse is
     * there and it is set to a matching timestamp, getDom(req,res)
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
        Properties   params      = new Properties();
        HttpSession  session     = preq.getSession(false);
        boolean      doreuse     = doReuse(preq);
        SPDocument   spdoc       = null;
        RequestParam value;
        
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
        params.put(XSLPARAM_URI, preq.getRequestURI());
        params.put(XSLPARAM_SERVP, preq.getContextPath() + preq.getServletPath());
        if (preq.getRemoteAddr() != null)
            params.put(XSLPARAM_REMOTE_ADDR, preq.getRemoteAddr());
        if (preq.getServerName() != null)
            params.put(XSLPARAM_SERVER_NAME, preq.getServerName());
        if (preq.getScheme() != null)
            params.put(XSLPARAM_REQUEST_SCHEME, preq.getScheme());
        if (preq.getQueryString() != null) {
            params.put(XSLPARAM_QUERYSTRING, preq.getQueryString());
        }

        if (session != null) {
            params.put(XSLPARAM_SESSID,
                       session.getAttribute(SessionHelper.SESSION_ID_URL));
            if (doreuse) {
                synchronized (session) {
                    spdoc = (SPDocument) session.getAttribute(servletname + SUFFIX_SAVEDDOM);
                }
            }
           
            // Now look for the parameter __editmode, and store it in the
            // session if it's there. Get the parameter from the session, and hand it over to the
            // Stylesheet params. Do the same for the parameter __language.
            if ((value = preq.getRequestParam(PARAM_LANG)) != null) {
                if (value.getValue() != null) {
                    session.setAttribute(SESS_LANG, value.getValue());
                }
            }
            if (session.getAttribute(SESS_LANG) != null) {
                params.put(XSLPARAM_LANG, session.getAttribute(SESS_LANG));
            }
            if ((value = preq.getRequestParam(PARAM_EDITMODE)) != null) {
                if (value.getValue() != null) {
                    session.setAttribute(PARAM_EDITMODE, value.getValue());
                }
            }
            if (session.getAttribute(PARAM_EDITMODE) != null) {
                // first we check if the properties prohibit editmode
                if (editmodeAllowed) {
                    params.put(PARAM_EDITMODE, session.getAttribute(PARAM_EDITMODE));
                }
            }
        }

        // Now we will store the time needed from the creation of the request up to now
        PerfEventType pet = PerfEventType.XMLSERVER_PREPROCESS;
        preq.endLogEntry(pet);
       
        if (spdoc == null) {
            preq.startLogEntry();
            spdoc           = getDom(preq);
            String pagename = spdoc.getPagename();
            PerfEventType et = PerfEventType.XMLSERVER_GETDOM;
            et.setPage(pagename);
         
            preq.endLogEntry(et);
            
            TrailLogger.log(preq, spdoc, session);
            if (isDebugEnabled()) {
                CAT.debug("* Generated document:" + spdoc);
            }
            RequestParam[] anchors   = preq.getAllRequestParams(PARAM_ANCHOR);
            Map            anchormap;
            if (anchors != null && anchors.length > 0) {
                anchormap = createAnchorMap(anchors);
                spdoc.storeFrameAnchors(anchormap);
            }
            if (spdoc.getDocument() == null) {
                // thats a request to an unkown page!
                // do nothing, cause we  want a 404 and no NPExpection
                if (CAT.isDebugEnabled()) {
                    CAT.debug("Having a null-document as parameter. Unkown page? Returning null...");
                }
            } else {
                spdoc.setDocument(Xml.parse(spdoc.getDocument()));
            }

            if (!spdoc.getNostore()) {
                RequestParam store = preq.getRequestParam(PARAM_NOSTORE);
                if (session != null && (store == null || store.getValue() == null || ! store.getValue().equals("1"))) {
                    SessionCleaner.getInstance().storeSPDocument(spdoc, session,
                                                                 servletname + SUFFIX_SAVEDDOM, scleanertimeout);
                }
            } else {
                CAT.info("*** Got NOSTORE from SPDocument! ****");
            }
            // this will remain at -1 when we don't have to enter the businesslogic codepath
            // (whenever there is a stored spdoc already)
        }
        params.put(XSLPARAM_REUSE, "" + spdoc.getTimestamp());
        handleDocument(preq, res, spdoc, params, doreuse);
    }

    protected void handleDocument(PfixServletRequest preq, HttpServletResponse res,
                                  SPDocument spdoc, Properties params, boolean doreuse) throws Exception {
        // Check the document for supplied headers...
        HashMap headers = spdoc.getResponseHeader();
        if (headers.size() != 0) {
            for (Iterator i = headers.keySet().iterator(); i.hasNext();) {
                String key = (String) i.next();
                String val = (String) headers.get(key);
                if (isDebugEnabled()) {
                    CAT.debug("*** Setting custom supplied header: " + key + " -> " + val);
                }
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
        HttpSession   session    = preq.getSession(false);
        TreeMap       paramhash  = constructParameters(spdoc, params, session);
        String        stylesheet = extractStylesheetFromSPDoc(spdoc);
        if (stylesheet == null) {
            throw new XMLException("Wasn't able to extract any stylesheet specification from page '" +
                                   spdoc.getPagename() + "' ... bailing out.");
        }
        preq.startLogEntry();
        if (! doreuse) {
            if (isInfoEnabled()) {
                CAT.info(" *** Using stylesheet: " + stylesheet + " ***");
            }
            // we only want to update the Session hit when we are not handling a "reuse" request
            if (session != null) {
                SessionAdmin.getInstance().touchSession(servletname, stylesheet, session);
            }
            // Only process cookies if we don't reuse
            if (spdoc.getCookies() != null && ! spdoc.getCookies().isEmpty()) {
                if (isDebugEnabled()) {
                    CAT.debug("*** Sending cookies ***");
                }
                // Now adding the Cookies from spdoc
                for (Iterator i = spdoc.getCookies().iterator(); i.hasNext();) {
                    Cookie cookie = (Cookie) i.next();
                    if (isDebugEnabled()) {
                        CAT.debug("    Add cookie: " + cookie);
                    }
                    res.addCookie(cookie);
                }
            }
        }

        render(spdoc, getRendering(preq), res, paramhash, stylesheet);
        
        if (! doreuse && session != null) {
            StringBuffer logbuff = new StringBuffer();
            logbuff.append(session.getAttribute(VISIT_ID) + "|");
            logbuff.append(session.getId() + "|");
            logbuff.append(preq.getRemoteAddr() + "|");
            logbuff.append(preq.getServerName() + "|");
            logbuff.append(stylesheet + "|");
            logbuff.append(preq.getOriginalRequestURI());
            if (preq.getQueryString() != null) {
                logbuff.append("?" + preq.getQueryString());
            }
            String flow = (String) paramhash.get("pageflow");
            if (flow != null) {
                logbuff.append("|" + flow);
            }
            LOGGER_TRAIL.warn(logbuff.toString());
        }
        PerfEventType et = PerfEventType.XMLSERVER_HANDLEDOCUMENT;
        et.setPage(spdoc.getPagename());
        preq.endLogEntry(et);
    }

    private void render(SPDocument spdoc, int rendering, HttpServletResponse res, TreeMap paramhash, String stylesheet) throws TargetGenerationException, IOException, TransformerException, TransformerConfigurationException, TransformerFactoryConfigurationError {
        switch (rendering) {
            case RENDER_NORMAL:
                renderNormal(spdoc, res, paramhash, stylesheet);
                break;
            case RENDER_FONTIFY:
                renderFontify(spdoc, res);
                break;
        	case RENDER_EXTERNAL:
        	    renderExternal(spdoc, res, paramhash, stylesheet);
        	    break;
        	case RENDER_XMLONLY:
        	    renderXmlonly(spdoc, res);
        	    break;
        	default:
        	    throw new IllegalArgumentException("unkown rendering: " + rendering);
        }
    }

    private void renderXmlonly(SPDocument spdoc, HttpServletResponse res) throws IOException {
        Xml.serialize(spdoc.getDocument(), res.getOutputStream(), true, true);
    }

    private void renderNormal(SPDocument spdoc, HttpServletResponse res, TreeMap paramhash, String stylesheet) throws TargetGenerationException, IOException, TransformerException {
        Templates stylevalue;
        stylevalue = (Templates) generator.getTarget(stylesheet).getValue();
        if ( stylevalue == null ) { // AH 2004-09-21 added for bugtracing 
            CAT.warn("stylevalue must not be null; stylevalue="+stylevalue+"; stylesheet="+stylesheet+"; spdoc.getPagename()="+((spdoc!=null)?spdoc.getPagename():"spdoc==null")+" spdoc.getXSLKey()="+((spdoc!=null)?spdoc.getXSLKey():"spdoc==null"));
        }
        try {
            Xslt.transform(spdoc.getDocument(), stylevalue, paramhash, new StreamResult(res.getOutputStream()));
        } catch (TransformerException e) {
            if (e.getException() instanceof SocketException) {
                CAT.warn("[Ignored TransformerException] : " + e.getMessage());
                if (isInfoEnabled()) {
                    CAT.info("[Ignored TransformerException]", e);
                }
            } else if(e.getException() != null &&  e.getException().getClass().getName().equals("org.apache.catalina.connector.ClientAbortException")) {
                CAT.warn("[Ignored TransformerException] : " + e.getMessage());
            } else
                throw e;
        }
    }

    private void renderExternal(SPDocument spdoc, HttpServletResponse res, TreeMap paramhash, String stylesheet) throws TransformerException, TransformerConfigurationException, TransformerFactoryConfigurationError, IOException {
        Document ext_doc = Xml.createDocument();
        Element  root    = ext_doc.createElement("render_external");
        ext_doc.appendChild(root);
        Element  ssheet  = ext_doc.createElement("stylesheet");
        root.appendChild(ssheet);
        ssheet.setAttribute("name", generator.getDisccachedir() + File.separator + stylesheet);
        for (Iterator i = paramhash.keySet().iterator(); i.hasNext();) {
            String  key   = (String) i.next();
            String  val   = (String) paramhash.get(key);
            Element param = ext_doc.createElement("param");
            param.setAttribute("key", key);
            param.setAttribute("value", val);
            root.appendChild(param);
        }
        Node imported = ext_doc.importNode(spdoc.getDocument().getDocumentElement(), true);
        root.appendChild(imported);
        TransformerFactory.newInstance().newTransformer().transform(new DOMSource(ext_doc),
                                                                new StreamResult(res.getOutputStream()));
    }

    private void renderFontify(SPDocument spdoc, HttpServletResponse res) throws TargetGenerationException, IOException {
        Templates stylevalue = (Templates) generator.createXSLLeafTarget(FONTIFY_SSHEET).getValue();
        try {
            Xslt.transform(spdoc.getDocument(), stylevalue, null, new StreamResult(res.getOutputStream()));
        } catch (TransformerException e) {
            CAT.warn("*** Ignored exception when trying to render XML tree ***");
        }
        
    }
    private int getRendering(PfixServletRequest pfreq) {
        String value;
        int rendering;
        RequestParam xmlonly;
        
        if (render_external) {
            return RENDER_EXTERNAL;
        }
        xmlonly = pfreq.getRequestParam(PARAM_XMLONLY);
        if (xmlonly == null) {
            return RENDER_NORMAL;
        }
        value = xmlonly.getValue();
        if (value.equals(PARAM_XMLONLY_XMLONLY)) {
            rendering = RENDER_XMLONLY;
        } else if (value.equals(PARAM_XMLONLY_FONTIFY)) {
            rendering = RENDER_FONTIFY;
        } else {
            throw new IllegalArgumentException("invalid value for " + PARAM_XMLONLY + ": " + value);
        }
        if (editmodeAllowed || JmxServer.getInstance().isKnownClient(pfreq.getRemoteAddr())) {
            return rendering;
        } else {
            return RENDER_NORMAL;
        }
    }

    private TreeMap constructParameters(SPDocument spdoc, Properties gen_params, HttpSession session) {
        TreeMap    paramhash = new TreeMap();
        Properties params = spdoc.getProperties();
        // These are properties which have been set in the process method
        //  e.g. Frame handling is stored here
        if (gen_params != null) {
            for (Enumeration e = gen_params.keys(); e.hasMoreElements();) {
                String name  = (String) e.nextElement();
                String value = (String) gen_params.get(name);
                if (name != null && value != null) {
                    paramhash.put(name, value);
                }
            }
        }
        // These are the parameters that may be set by the DOM tree producing
        // method of the servlet (something that implements the abstract method getDom())
        if (params != null) {
            for (Enumeration e = params.keys(); e.hasMoreElements();) {
                String name  = (String) e.nextElement();
                String value = (String) params.get(name);
                if (name != null && value != null) {
                    paramhash.put(name, value);
                }
            }
        }
        paramhash.put(TargetGenerator.XSLPARAM_TG, targetconf.getRelative());
        paramhash.put(TargetGenerator.XSLPARAM_TKEY, VALUE_NONE);

        String session_to_link_from_external = "NOSUCHSESSION";
        if (session != null) {
            Boolean secure   = (Boolean) session.getAttribute(SESSION_IS_SECURE);
            String  parentid = (String) session.getAttribute(SessionAdmin.PARENT_SESS_ID); 
            if (secure != null && secure.booleanValue() && parentid != null) {
                session_to_link_from_external = parentid;
            } else if (secure == null || !secure.booleanValue()) {
                session_to_link_from_external = session.getId();
            }
        }
        paramhash.put("__external_session_ref",session_to_link_from_external);
        return paramhash;
    }

    private String extractStylesheetFromSPDoc(SPDocument spdoc) {
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
                SPDocument saved = (SPDocument) session.getAttribute(servletname + SUFFIX_SAVEDDOM);
                if (saved == null)
                    return false;
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
            String value     = anchors[i].getValue();
            int    pos       = value.indexOf("|");
            if (pos < 0) pos = value.indexOf(":"); // This is for backwards compatibility, but should not be used anymore!
            if (pos < (value.length() - 1) && pos > 0) {
                String frame  = value.substring(0, pos);
                String anchor = value.substring(pos + 1);
                map.put(frame, anchor);
            }
        }
        return map;
    }


    private boolean isDebugEnabled() {
        return CAT.isDebugEnabled() && allowDebug;
    }
    

    private boolean isInfoEnabled() {
        return CAT.isInfoEnabled() && allowInfo;
    }

}


