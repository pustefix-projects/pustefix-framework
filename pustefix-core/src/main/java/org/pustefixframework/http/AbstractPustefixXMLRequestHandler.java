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
package org.pustefixframework.http;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.SocketException;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.pustefixframework.config.contextxmlservice.AbstractXMLServletConfig;
import org.pustefixframework.config.contextxmlservice.ServletManagerConfig;
import org.w3c.dom.Document;

import de.schlund.pfixcore.exception.PustefixApplicationException;
import de.schlund.pfixcore.exception.PustefixCoreException;
import de.schlund.pfixcore.workflow.NavigationFactory;
import de.schlund.pfixcore.workflow.NavigationInitializationException;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.RenderingException;
import de.schlund.pfixxml.RequestParam;
import de.schlund.pfixxml.SPDocument;
import de.schlund.pfixxml.SessionCleaner;
import de.schlund.pfixxml.Variant;
import de.schlund.pfixxml.config.EnvironmentProperties;
import de.schlund.pfixxml.perflogging.AdditionalTrailInfo;
import de.schlund.pfixxml.serverutil.SessionHelper;
import de.schlund.pfixxml.targets.PageInfo;
import de.schlund.pfixxml.targets.PageInfoFactory;
import de.schlund.pfixxml.targets.PageTargetTree;
import de.schlund.pfixxml.targets.Target;
import de.schlund.pfixxml.targets.TargetGenerationException;
import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.testrecording.TestRecording;
import de.schlund.pfixxml.testrecording.TrailLogger;
import de.schlund.pfixxml.util.CacheValueLRU;
import de.schlund.pfixxml.util.MD5Utils;
import de.schlund.pfixxml.util.Xml;
import de.schlund.pfixxml.util.Xslt;


/**
 * This class is at the top of the XML/XSLT System.
 * It serves as an abstract parent class for all servlets
 * needing access to the XML/XSL cache system povided by
 * de.schlund.pfixxml.TargetGenerator.<p><br>
 * Servlets inheriting from this class need to implement
 * getDom(HttpServletRequest req, HttpServletResponse res)
 * which returns a SPDocument. <br>
 */
public abstract class AbstractPustefixXMLRequestHandler extends AbstractPustefixRequestHandler {

    //~ Instance/static variables ..................................................................
    // how to write xml to the result stream
    private enum RENDERMODE { RENDER_NORMAL, RENDER_EXTERNAL, RENDER_FONTIFY, RENDER_XMLONLY };

    public static final String DEF_PROP_TMPDIR = "java.io.tmpdir";
    private static final String FONTIFY_SSHEET        = "module://pustefix-core/xsl/xmlfontify.xsl";
    public  static final String SESS_LANG             = "__SELECTED_LANGUAGE__";
    public  static final String PARAM_XMLONLY         = "__xmlonly";
    public  static final String PARAM_XMLONLY_FONTIFY = "1"; // -> RENDER_FONFIFY
    public  static final String PARAM_XMLONLY_XMLONLY = "2"; // -> RENDER_XMLONLY
    public  static final String PARAM_ANCHOR          = "__anchor";
    private static final String PARAM_EDITMODE        = "__editmode";
    private static final String PARAM_LANG            = "__language";
    private static final String PARAM_FRAME           = "__frame";
    private static final String PARAM_REUSE           = "__reuse"; // internally used
    
    private static final String   XSLPARAM_LANG           = "lang";
    private static final String   XSLPARAM_SESSID         = "__sessid";
    private static final String   XSLPARAM_URI            = "__uri";
    private static final String   XSLPARAM_SERVP          = "__servletpath";
    private static final String   XSLPARAM_CONTEXTPATH    = "__contextpath";
    private static final String   XSLPARAM_REMOTE_ADDR    = "__remote_addr";
    private static final String   XSLPARAM_SERVER_NAME    = "__server_name";
    private static final String   XSLPARAM_REQUEST_SCHEME = "__request_scheme";
    private static final String   XSLPARAM_QUERYSTRING    = "__querystring";
    private static final String   XSLPARAM_FRAME          = "__frame";
    private static final String   XSLPARAM_REUSE          = "__reusestamp";
    private static final String   XSLPARAM_EDITOR_URL     = "__editor_url";
    private static final String   XSLPARAM_EDITOR_INCLUDE_PARTS_EDITABLE_BY_DEFAULT = "__editor_include_parts_editable_by_default";
    private static final String   XSL_PARAM_APP_URL       = "__application_url";
    private static final String   VALUE_NONE              = "__NONE__";
    private static final String   SUFFIX_SAVEDDOM         = "_SAVED_DOM";
    private static final String   ATTR_SHOWXMLDOC         = "__ATTR_SHOWXMLDOC__";

    public static final String PREPROCTIME = "__PREPROCTIME__";
    public static final String GETDOMTIME  = "__GETDOMTIME__";
    public static final String TRAFOTIME   = "__TRAFOTIME__";
    
    public final static String SESS_CLEANUP_FLAG_STAGE1 = "__pfx_session_cleanup_stage1";
    public final static String SESS_CLEANUP_FLAG_STAGE2 = "__pfx_session_cleanup_stage2";
    
    private int maxStoredDoms;
    
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
    
    protected String editorLocation;
    
    @Override
    protected ServletManagerConfig getServletManagerConfig() {
        return this.getAbstractXMLServletConfig();
    }
    
    protected abstract AbstractXMLServletConfig getAbstractXMLServletConfig();
    
    private boolean      renderExternal            = false;
    private boolean      editmodeAllowed            = false;
    private boolean      includePartsEditableByDefault = true;
    private boolean      checkModtime               = true;
    private boolean xmlOnlyAllowed = false;
    
    private final static Logger LOGGER_TRAIL = Logger.getLogger("LOGGER_TRAIL");
    private final static Logger LOGGER       = Logger.getLogger(AbstractPustefixXMLRequestHandler.class);
    
    private AdditionalTrailInfo additionalTrailInfo = null;
    
    private TestRecording testRecording;
    
    private SessionCleaner sessionCleaner;
    
    //~ Methods ....................................................................................
    /**
     * Init method of all servlets inheriting from AbstractXMLServlets.
     * It calls super.init(Config) as a first step.
     * @param ContextXMLServletConfig config. Passed in from the servlet container.
     * @return void
     * @exception ServletException thrown when the initialisation goes havoc somehow
     */
    @Override
    public void init() throws ServletException {
        super.init();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("\n>>>> In init of AbstractXMLServlet <<<<");
        }
        initValues();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("End of init AbstractXMLServlet");
        }
        verifyDirExists(System.getProperty(DEF_PROP_TMPDIR));
    }

    private void initValues() throws ServletException {
        servletname = this.getAbstractXMLServletConfig().getServletName();

        String mode = EnvironmentProperties.getProperties().getProperty("mode");
        if(!"prod".equals(mode)) xmlOnlyAllowed = true; 
        
        if (generator == null) {
            LOGGER.error("Error: TargetGenerator has not been set.");
            throw new ServletException("TargetGenerator is not set");
        }
        
        generator.setIsGetModTimeMaybeUpdateSkipped(!checkModtime);
        
        if (LOGGER.isInfoEnabled()) {
            StringBuffer sb = new StringBuffer(255);
            sb.append("\n").append("AbstractXMLServlet properties after initValues(): \n");
            sb.append("                targetconf = ").append(generator.getConfigPath()).append("\n");
            sb.append("               servletname = ").append(servletname).append("\n");
            sb.append("           editModeAllowed = ").append(editmodeAllowed).append("\n");
            sb.append("            xmlOnlyAllowed = ").append(xmlOnlyAllowed).append("\n");
            sb.append("             maxStoredDoms = ").append(maxStoredDoms).append("\n");
            sb.append("                   timeout = ").append(sessionCleaner.getTimeout()).append("\n");
            sb.append("              checkModtime = ").append(checkModtime).append("\n");
            sb.append("           render_external = ").append(renderExternal).append("\n");
            LOGGER.info(sb.toString());
        }
    }

    /**
     * A child of AbstractXMLServlet must implement this method.
     * It is here where the final Dom tree and parameters for
     * applying to the stylesheet are put into SPDocument.
     * @param HttpServletRequest req:  the current request
     * @return SPDocument: The result Dom tree and parameters
     * @exception Exception Anything that can go wrong when constructing the resulting
     * SPDocument object
     */
    protected abstract SPDocument getDom(PfixServletRequest preq) throws PustefixApplicationException, PustefixCoreException;

    
    @SuppressWarnings("unchecked")
    private CacheValueLRU<String,SPDocument> getLRU(HttpSession session) {
        return (CacheValueLRU<String,SPDocument>) session.getAttribute(servletname + SUFFIX_SAVEDDOM);
    }
    
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
    @Override
    protected void process(PfixServletRequest preq, HttpServletResponse res) throws Exception {
        Properties  params     = new Properties();
        HttpSession session    = preq.getSession(false);
        CacheValueLRU<String,SPDocument> storeddoms = null;
        if (session != null) {
            storeddoms = getLRU(session);
            if (storeddoms == null) {
                storeddoms = new CacheValueLRU<String,SPDocument>(maxStoredDoms);
                session.setAttribute(servletname + SUFFIX_SAVEDDOM, storeddoms);
            }
        }
        SPDocument spdoc   = doReuse(preq, storeddoms);
        boolean    doreuse = false;
        if (spdoc != null) {
            doreuse = true;
        }
        
        // Check if session has been scheduled for invalidation
        if (!doreuse && session.getAttribute(SESS_CLEANUP_FLAG_STAGE2) != null) {
            HttpServletRequest req = preq.getRequest();
            String redirectUri = SessionHelper.getClearedURL(req.getScheme(), getServerName(req), req, getAbstractXMLServletConfig().getProperties());
            relocate(res, redirectUri);
            return;
        }

        RequestParam value;
        long         currtime;
        long         preproctime = -1;
        long         getdomtime  = -1;
        
        // Reset the additional trail info before processing the request
        if(additionalTrailInfo!=null) additionalTrailInfo.reset();

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
        params.put(XSLPARAM_CONTEXTPATH, preq.getContextPath());
        if (preq.getRemoteAddr() != null)
            params.put(XSLPARAM_REMOTE_ADDR, preq.getRemoteAddr());
        if (preq.getServerName() != null)
            params.put(XSLPARAM_SERVER_NAME, preq.getServerName());
        if (preq.getScheme() != null)
            params.put(XSLPARAM_REQUEST_SCHEME, preq.getScheme());
        if (preq.getQueryString() != null)
            params.put(XSLPARAM_QUERYSTRING, preq.getQueryString());

        if (editorLocation != null) {
            params.put(XSLPARAM_EDITOR_URL, editorLocation);
        }
        params.put(XSL_PARAM_APP_URL, getApplicationURL(preq));
        
        if (session != null) {
            params.put(XSLPARAM_SESSID, session.getAttribute(SessionHelper.SESSION_ID_URL));
            if (doreuse) {
                synchronized (session) {
                    // Make sure redirect is only done once
                    // See also: SSL redirect implementation in Context
                    spdoc.resetRedirectURL();
                }
            }
           
            if ((value = preq.getRequestParam(PARAM_LANG)) != null) {
                if (value.getValue() != null) {
                    session.setAttribute(SESS_LANG, value.getValue());
                }
            }
            // Now look for the parameter __editmode, and store it in the
            // session if it's there. Get the parameter from the session, and hand it over to the
            // Stylesheet params.
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
        preproctime = System.currentTimeMillis() - preq.getCreationTimeStamp();
        preq.getRequest().setAttribute(PREPROCTIME, preproctime);
        
        if (spdoc == null) {
            
            currtime        = System.currentTimeMillis();
            
            // Now get the document
            try {
                spdoc = getDom(preq);
            } catch (PustefixApplicationException e) {
                // get original Exception thrown by Application
                // and rethrow it so that it can be catched by the
                // exception processor
                if (e.getCause() != null && e.getCause() instanceof Exception) {
                    throw (Exception) e.getCause();
                } else {
                    throw e;
                }
            }
	    
            
            TrailLogger.log(preq, spdoc, session);
            RequestParam[] anchors   = preq.getAllRequestParams(PARAM_ANCHOR);
            Map<String, String> anchormap;
            if (anchors != null && anchors.length > 0) {
                anchormap = createAnchorMap(anchors);
                spdoc.storeFrameAnchors(anchormap);
            }
            if (spdoc.getDocument() == null) {
                // thats a request to an unkown page!
                // do nothing, cause we  want a 404 and no NPExpection
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Having a null-document in the SPDoc. Unkown page? Returning 404...");
                }
            }

            // this will remain at -1 when we don't have to enter the businesslogic codepath
            // (whenever there is a stored spdoc already)
            getdomtime = System.currentTimeMillis() - currtime;
            spdoc.setCreationTime(getdomtime);
        }
        preq.getRequest().setAttribute(GETDOMTIME, spdoc.getCreationTime());
        params.put(XSLPARAM_REUSE, "" + spdoc.getTimestamp());
        if (session != null && session.getAttribute(SESS_LANG) != null) {
            params.put(XSLPARAM_LANG, session.getAttribute(SESS_LANG));
        }
        
        handleDocument(preq, res, spdoc, params, doreuse);

        if (session != null && (getRendering(preq) != RENDERMODE.RENDER_FONTIFY) && !doreuse) {
            // This will store just the last dom, but only when editmode is allowed (so this normally doesn't apply to production mode)
            // This is a seperate place from the SessionCleaner as we don't want to interfere with this, nor do we want to use 
            // the whole queue of possible stored SPDocs only for the viewing of the DOM during development.
            if (xmlOnlyAllowed) {
                session.setAttribute(ATTR_SHOWXMLDOC, spdoc);
            }

//            if (!spdoc.getNostore() || spdoc.isRedirect()) {
            if (spdoc.isRedirect()) {
                    LOGGER.info("**** Storing for redirection! ****");
//                } else {
//                    LOGGER.info("**** Storing because SPDoc wants us to. ****");
//                }
                sessionCleaner.storeSPDocument(spdoc, storeddoms);
            } else {
                LOGGER.info("**** Not storing because no redirect... ****");
            }
        }
        
        if (session != null && !doreuse && (session.getAttribute(SESS_CLEANUP_FLAG_STAGE1) != null
                && session.getAttribute(SESS_CLEANUP_FLAG_STAGE2) == null)) {
            if (/* !spdoc.getNostore() || */ spdoc.isRedirect()) {
                // Delay invalidation
                
                // This is obviously not thread-safe. However, no problems should
                // arise if the session is invalidated twice.
                session.setAttribute(SESS_CLEANUP_FLAG_STAGE2, true);
                
                sessionCleaner.invalidateSession(session);
            } else {
                // Invalidate immediately
                if (session.getAttribute(SESS_CLEANUP_FLAG_STAGE1) != null
                        && session.getAttribute(SESS_CLEANUP_FLAG_STAGE2) == null) {
                    session.invalidate();
                }
            }
        }
    }

    private String getApplicationURL(PfixServletRequest preq) {
        HttpServletRequest req = preq.getRequest();
        StringBuffer sb = new StringBuffer();
        
        sb.append(req.getScheme());
        sb.append("://");
        sb.append(req.getServerName());
        if ((req.getScheme().equals("http") && req.getLocalPort() != 80)
                || (req.getScheme().equals("https") && req.getLocalPort() != 443)) {
            sb.append(':');
            sb.append(req.getLocalPort());
        }
        sb.append(req.getContextPath());
        return sb.toString();
    }

    protected boolean isPageDefined(String name) {
    	return true;
    }
   
    protected void handleDocument(PfixServletRequest preq, HttpServletResponse res,
                                  SPDocument spdoc, Properties params, boolean doreuse) throws IOException, PustefixCoreException {
        long currtime = System.currentTimeMillis();
        
        // Check the document for supplied headers...
        HashMap<String, String> headers = spdoc.getResponseHeader();
        if (headers.size() != 0) {
            for (Iterator<String> i = headers.keySet().iterator(); i.hasNext();) {
                String key = i.next();
                String val = headers.get(key);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("*** Setting custom supplied header: " + key + " -> " + val);
                }
                res.setHeader(key, val);
            }
        } 
        // set some default values to force generating new requests every time...
        if(!headers.containsKey("Expires")) {
            res.setHeader("Expires", "Mon, 05 Jul 1970 05:07:00 GMT");
        }
        if(!headers.containsKey("Cache-Control")) {
            res.setHeader("Cache-Control", "private");
        }
        // Check if a content type was supplied
        String ctype;
        if ((ctype = spdoc.getResponseContentType()) != null) {
            res.setContentType(ctype);
        } else {
            res.setContentType(DEF_CONTENT_TYPE);
        }
        if (spdoc.getResponseError() == HttpServletResponse.SC_NOT_FOUND && spdoc.getDocument() != null) {
            String stylesheet = extractStylesheetFromSPDoc(spdoc);
            if (generator.getTarget(stylesheet) != null) {
                spdoc.setResponseError(0);
                spdoc.setResponseErrorText(null);
            }
        }
        
        // if the document contains a error code, do errorhandling here and no further processing.
        if(spdoc.getResponseError() != 0) {
        	sendError(spdoc, res);
        	return;
        }

        // So no error happened, let's go on with normal processing.
        HttpSession   session    = preq.getSession(false);
        TreeMap<String, Object> paramhash  = constructParameters(spdoc, params, session);
        String        stylesheet = extractStylesheetFromSPDoc(spdoc);
        if (stylesheet == null) {
        	if(spdoc.getPagename()!=null && !isPageDefined(spdoc.getPagename())) {
        		spdoc.setResponseError(HttpServletResponse.SC_NOT_FOUND);
        		spdoc.setResponseErrorText(null);
        		sendError(spdoc, res);
        		return;
        	} else {
        		throw new PustefixCoreException("Wasn't able to extract any stylesheet specification from page '" +
                                   spdoc.getPagename() + "' ... bailing out.");
        	}
        }
        
        if (spdoc.docIsUpdateable()) {
            if (stylesheet.indexOf("::") > 0) {
                spdoc.getDocument().getDocumentElement().setAttribute("used-pv", stylesheet);
            }
            spdoc.setDocument(Xml.parse(generator.getXsltVersion(), spdoc.getDocument()));
            spdoc.setDocIsUpdateable(false);
        }

        if(!spdoc.getTrailLogged()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("*** Using document:" + spdoc);
            }
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(" *** Using stylesheet: " + stylesheet + " ***");
            }
        }
        
        if (! doreuse) {
            // we only want to update the Session hit when we are not handling a "reuse" request
            if (session != null) {
                getSessionAdmin().touchSession(servletname, stylesheet, session);
            }
            // Only process cookies if we don't reuse
            setCookies(spdoc,res);
        }
        
        ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
        
        boolean modified_or_no_etag = doHandleDocument(spdoc, stylesheet, paramhash, preq, res, session, output);

        long handletime = System.currentTimeMillis() - currtime;
        preq.getRequest().setAttribute(TRAFOTIME, handletime);
        
        Map<String, Object> addinfo = null;
        if(additionalTrailInfo!=null) addinfo = additionalTrailInfo.getData(preq);
        
        if (session != null && !spdoc.getTrailLogged()) {
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
            if(addinfo!=null) {
                for (Iterator<String> keys = addinfo.keySet().iterator(); keys.hasNext(); ) {
                    logbuff.append("|" + addinfo.get(keys.next()));
                }
            }
            LOGGER_TRAIL.warn(logbuff.toString());
            spdoc.setTrailLogged();
        }
        
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(">>> Complete handleDocument(...) took " + handletime + "ms" + 
                    " (needed xslt: " + modified_or_no_etag + ")");
        }
     
        if (modified_or_no_etag && 
                (spdoc.getResponseContentType() == null || spdoc.getResponseContentType().startsWith("text/html"))) {
            try {
                OutputStream       out          = res.getOutputStream();
                OutputStreamWriter writer       = new OutputStreamWriter(out, res.getCharacterEncoding());
                writer.write("\n<!--");
                if(addinfo != null) {
                    for (Iterator<String> keys = addinfo.keySet().iterator(); keys.hasNext(); ) {
                        String key = keys.next();
                        writer.write(" " + key + ": " + addinfo.get(key));
                    }
                }
                writer.write(" -->");
            } catch (Exception e) {
                LOGGER.warn("Error adding info data to page", e);
            }
            res.flushBuffer();
            if(getRendering(preq) == RENDERMODE.RENDER_NORMAL) {
                hookAfterDelivery(preq, spdoc, output);
            }
        }

    }

    private void sendError(SPDocument spdoc, HttpServletResponse res) throws IOException {
         String errtxt;
         int err = spdoc.getResponseError();
         setCookies(spdoc,res);
         if ((errtxt = spdoc.getResponseErrorText()) != null) {
             res.sendError(err, errtxt);
         } else {
             res.sendError(err);
         }
    }
    
    private boolean doHandleDocument(SPDocument spdoc, String stylesheet, TreeMap<String, Object> paramhash, 
            PfixServletRequest preq, HttpServletResponse res, HttpSession session, ByteArrayOutputStream output) throws IOException, PustefixCoreException {
        
        boolean modified_or_no_etag = true;
        String etag_incoming = preq.getRequest().getHeader("If-None-Match");
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch(NoSuchAlgorithmException x) {
            throw new RuntimeException("Can't create message digest", x);
        }
        DigestOutputStream digestOutput = new DigestOutputStream(output, digest);
        try {
            hookBeforeRender(preq, spdoc, paramhash, stylesheet);
            render(spdoc, getRendering(preq), res, paramhash, stylesheet, digestOutput);
        } finally {
            hookAfterRender(preq, spdoc, paramhash, stylesheet);
        }
        
        byte[] digestBytes = digest.digest();
        String etag_outgoing = MD5Utils.byteToHex(digestBytes);
        res.setHeader("ETag", etag_outgoing);
        
        if (getRendering(preq) == RENDERMODE.RENDER_NORMAL && etag_incoming != null && etag_incoming.equals(etag_outgoing)) {
            //it's important to reset the content type, because old Apache versions in conjunction
            //with mod_ssl and mod_deflate (here enabled for text/html) gzip 304 responses and thus
            //cause non-empty response bodies which confuses the browsers
            res.setContentType(null);
            res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            LOGGER.info("*** Reusing UI: " + spdoc.getPagename());
            modified_or_no_etag = false;
        } else {
            output.writeTo(res.getOutputStream());
        }
        
        return modified_or_no_etag;
    }
    
    private void render(SPDocument spdoc, RENDERMODE rendering, HttpServletResponse res, TreeMap<String, Object> paramhash, String stylesheet, OutputStream output) throws RenderingException {
        try {
        switch (rendering) {
            case RENDER_NORMAL:
                renderNormal(spdoc, res, paramhash, stylesheet, output);
                break;
            case RENDER_FONTIFY:
                renderFontify(spdoc, res, paramhash);
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
        } catch (TransformerConfigurationException e) {
            throw new RenderingException("Exception while rendering page " + spdoc.getPagename() + " with stylesheet " + stylesheet, e);
        } catch (TargetGenerationException e) {
            throw new RenderingException("Exception while rendering page " + spdoc.getPagename() + " with stylesheet " + stylesheet, e);
        } catch (IOException e) {
            throw new RenderingException("Exception while rendering page " + spdoc.getPagename() + " with stylesheet " + stylesheet, e);
        } catch (TransformerException e) {
            throw new RenderingException("Exception while rendering page " + spdoc.getPagename() + " with stylesheet " + stylesheet, e);
        }
    }

    private void renderXmlonly(SPDocument spdoc, HttpServletResponse res) throws IOException {
        Xml.serialize(spdoc.getDocument(), res.getOutputStream(), true, true);
    }

    private void renderNormal(SPDocument spdoc, HttpServletResponse res, TreeMap<String, Object> paramhash, String stylesheet, OutputStream output) throws
        TargetGenerationException, IOException, TransformerException {
        Templates stylevalue;
        Target    target = generator.getTarget(stylesheet);
        paramhash.put("themes", target.getThemes().getId());
        stylevalue = (Templates) target.getValue();
        if (stylevalue == null) { // AH 2004-09-21 added for bugtracing 
            LOGGER.warn("stylevalue MUST NOT be null: stylesheet=" + stylesheet + "; " +
                     ((spdoc != null) ? ("pagename=" +  spdoc.getPagename()) : "spdoc==null")); 
        }
        try {
            Xslt.transform(spdoc.getDocument(), stylevalue, paramhash, new StreamResult(output), getServletEncoding());
        } catch (TransformerException e) {
            Throwable inner = e.getException();
            Throwable cause = null;
            if (inner != null) {
                cause = inner.getCause();
            }
            
            if ((inner != null && inner instanceof SocketException) ||
                (cause != null && cause instanceof SocketException)) {
                LOGGER.warn("[Ignored TransformerException] : " + e.getMessage());
            } else {
                throw e;
            }
        }
    }


    // FIXME Remove external render mode as it is NOT WORKING
    private void renderExternal(SPDocument spdoc, HttpServletResponse res, TreeMap<String, Object> paramhash, String stylesheet) throws
        TransformerException, TransformerConfigurationException, TransformerFactoryConfigurationError, IOException {
        Document doc  = spdoc.getDocument();
        Document ext  = doc;
        if (/* !spdoc.getNostore() || */ spdoc.isRedirect()) {
            ext = Xml.createDocument();
            ext.appendChild(ext.importNode(doc.getDocumentElement(), true));
        }
        
        ext.insertBefore(ext.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" media=\"all\" href=\"file: //"
                                                         + generator.getName() + "/" + stylesheet + "\""),
                         ext.getDocumentElement()); 
        for (Iterator<String> i = paramhash.keySet().iterator(); i.hasNext();) {
            String key = i.next();
            String val = (String) paramhash.get(key);
            ext.insertBefore(ext.createProcessingInstruction("modxslt-param", "name=\"" + key + "\" value=\"" + val + "\""),
                             ext.getDocumentElement());
        }
        res.setContentType("text/xml");
        TransformerFactory.newInstance().newTransformer().transform(new DOMSource(ext),
                                                                    new StreamResult(res.getOutputStream()));
    }

    private void renderFontify(SPDocument spdoc, HttpServletResponse res, TreeMap<String, Object> paramhash) throws TargetGenerationException, TransformerException, IOException {
        Templates stylevalue = (Templates) generator.createXSLLeafTarget(FONTIFY_SSHEET).getValue();
        Xslt.transform(spdoc.getDocument(), stylevalue, paramhash, new StreamResult(res.getOutputStream()));
    }

    private RENDERMODE getRendering(PfixServletRequest pfreq) {
        String value;
        RENDERMODE rendering;
        RequestParam xmlonly;
        
        if (renderExternal) {
            return RENDERMODE.RENDER_EXTERNAL;
        }
        xmlonly = pfreq.getRequestParam(PARAM_XMLONLY);
        if (xmlonly == null) {
            return RENDERMODE.RENDER_NORMAL;
        }
        value = xmlonly.getValue();
        if (value.equals(PARAM_XMLONLY_XMLONLY)) {
            rendering = RENDERMODE.RENDER_XMLONLY;
        } else if (value.equals(PARAM_XMLONLY_FONTIFY)) {
            rendering = RENDERMODE.RENDER_FONTIFY;
        } else {
            throw new IllegalArgumentException("invalid value for " + PARAM_XMLONLY + ": " + value);
        }
        if (xmlOnlyAllowed || (testRecording!=null && testRecording.isKnownClient(pfreq.getRemoteAddr()))) {
            return rendering;
        } else {
            return RENDERMODE.RENDER_NORMAL;
        }
    }

    private TreeMap<String, Object> constructParameters(SPDocument spdoc, Properties gen_params, HttpSession session) throws NavigationInitializationException {
        TreeMap<String, Object> paramhash = new TreeMap<String, Object>();
        HashMap<String, Object> params = spdoc.getProperties();
        // These are properties which have been set in the process method
        //  e.g. Frame handling is stored here
        if (gen_params != null) {
            for (Enumeration<?> e = gen_params.keys(); e.hasMoreElements();) {
                String name  = (String) e.nextElement();
                String value = gen_params.getProperty(name);
                if (name != null && value != null) {
                    paramhash.put(name, value);
                }
            }
        }
        // These are the parameters that may be set by the DOM tree producing
        // method of the servlet (something that implements the abstract method getDom())
        if (params != null) {
            for (Iterator<String> iter = params.keySet().iterator(); iter.hasNext(); ) {
                String name  = iter.next();
                Object value = params.get(name);
                if (name != null && value != null) {
                    paramhash.put(name, value);
                }
            }
        }
        paramhash.put(TargetGenerator.XSLPARAM_TG, generator.getConfigPath().toURI().toString());
        paramhash.put(TargetGenerator.XSLPARAM_TKEY, VALUE_NONE);
        paramhash.put(TargetGenerator.XSLPARAM_NAVITREE, NavigationFactory.getInstance().getNavigation(generator.getConfigPath(), generator.getXsltVersion()).getNavigationXMLElement());
        paramhash.put(XSLPARAM_EDITOR_INCLUDE_PARTS_EDITABLE_BY_DEFAULT, Boolean.toString(includePartsEditableByDefault));

        String session_to_link_from_external = getSessionAdmin().getExternalSessionId(session);
        paramhash.put("__external_session_ref", session_to_link_from_external);
        paramhash.put("__spdoc__", spdoc);
        paramhash.put("__register_frame_helper__", new RegisterFrameHelper(getLRU(session), spdoc));
        return paramhash;
    }
    
    

    private String extractStylesheetFromSPDoc(SPDocument spdoc) {
        // First look if the pagename is set
        String pagename             = spdoc.getPagename();
        if (pagename != null) {
            Variant        variant    = spdoc.getVariant();
            PageTargetTree pagetree   = generator.getPageTargetTree();
            PageInfo       pinfo      = null;
            Target         target     = null;
            String         variant_id = null;
            if (variant != null && variant.getVariantFallbackArray() != null) {
                String[] variants = variant.getVariantFallbackArray();
                for (int i = 0; i < variants.length; i++) {
                    variant_id = variants[i];
                    LOGGER.info("   ** Trying variant '" + variant_id + "' **");
                    pinfo   = PageInfoFactory.getInstance().getPage(generator, pagename, variant_id);
                    target  = pagetree.getTargetForPageInfo(pinfo);
                    if (target != null) {
                        return target.getTargetKey();
                    }
                }
            }
            if (target == null) {
                LOGGER.info("   ** Trying root variant **");
                pinfo = PageInfoFactory.getInstance().getPage(generator, pagename, null);
                target = pagetree.getTargetForPageInfo(pinfo);
            }
            if (target == null) {
                LOGGER.warn("\n********************** NO TARGET ******************************");
                return null;
            } else {
                return target.getTargetKey();
            }
        } else {
            // other possibility: an explicit xslkey is given:
            return spdoc.getXSLKey();
        }
    }

    private SPDocument doReuse(PfixServletRequest preq, CacheValueLRU<String,SPDocument> storeddoms) {
        HttpSession session = preq.getSession(false);
        if (session != null) {
            RequestParam reuse = preq.getRequestParam(PARAM_REUSE);
            if (reuse != null && reuse.getValue() != null) {
                SPDocument saved = storeddoms.get(reuse.getValue());
                if (preq.getPageName() != null && saved!=null && saved.getPagename() != null && !preq.getPageName().equals(saved.getPagename())) {
                    if (LOGGER.isDebugEnabled()) 
                        LOGGER.debug("Don't reuse SPDocument because pagenames differ: " + preq.getPageName() + " -> " + saved.getPagename());
                    saved = null;
                }
                if(LOGGER.isDebugEnabled()) {
                    if(saved!=null) LOGGER.debug("Reuse SPDocument "+saved.getTimestamp()+" restored with key "+reuse.getValue());
                    else LOGGER.debug("No SPDocument with key "+reuse.getValue()+" found");
                }
                return saved;
            } else if (getRendering(preq) == RENDERMODE.RENDER_FONTIFY) {
                return (SPDocument) session.getAttribute(ATTR_SHOWXMLDOC);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private Map<String, String> createAnchorMap(RequestParam[] anchors) {
        Map<String, String> map = new HashMap<String, String>();
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
    private void verifyDirExists(String tmpdir) {
        File temporary_dir = new File(tmpdir);
        if(!temporary_dir.exists()) {
            boolean ok = temporary_dir.mkdirs();
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(temporary_dir.getPath() + " did not exist. Created now. Sucess:" + ok);
            }
        }
    }
    
    private void setCookies(SPDocument spdoc,HttpServletResponse res) {
        if (spdoc.getCookies() != null && ! spdoc.getCookies().isEmpty()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("*** Sending cookies ***");
            }
            // Now adding the Cookies from spdoc
            for (Iterator<Cookie> i = spdoc.getCookies().iterator(); i.hasNext();) {
                Cookie cookie = i.next();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("    Add cookie: " + cookie);
                }
                res.addCookie(cookie);
            }
        }
    }
    
    
    /**
     * Called before the XML result tree is rendered. This method can
     * be overidden in sub-implementations to do initializiation stuff,
     * which might be needed for XSLT callback methods.
     * 
     * @param preq current servlet request
     * @param spdoc XML document going to be rendered
     * @param paramhash parameters supplied to XSLT code
     * @param stylesheet name of the stylesheet being used
     */
    protected void hookBeforeRender(PfixServletRequest preq, SPDocument spdoc, TreeMap<String, Object> paramhash, String stylesheet) {
        // Empty in default implementation
    }

    /**
     * Called after the XML result tree is rendered. This method can
     * be overidden in sub-implementations to do de-initializiation stuff.
     * This method is guaranteed to be always called, when the corresponding
     * before method has been called - even if an error occurs. 
     * 
     * @param preq current servlet request
     * @param spdoc XML document going to be rendered
     * @param paramhash parameters supplied to XSLT code
     * @param stylesheet name of the stylesheet being used
     */
    protected void hookAfterRender(PfixServletRequest preq, SPDocument spdoc, TreeMap<String, Object> paramhash, String stylesheet) {
        // Empty in default implementation
    }
    
    protected void hookAfterDelivery(PfixServletRequest preq, SPDocument spdoc, ByteArrayOutputStream output) {
        // Empty in default implementation
    }
    
    public class RegisterFrameHelper {
        private CacheValueLRU<String,SPDocument> storeddoms;
        private SPDocument spdoc;

        private RegisterFrameHelper(CacheValueLRU<String,SPDocument> storeddoms, SPDocument spdoc) {
            this.storeddoms = storeddoms;
            this.spdoc = spdoc;
        }
        
        public void registerFrame(String frameName) {
            sessionCleaner.storeSPDocument(spdoc, frameName, storeddoms);
        }
        
        public void unregisterFrame(String frameName) {
            String reuseKey = spdoc.getTimestamp() + "";
            if (frameName.equals("_top")) {
                frameName = null;
            }
            if (frameName != null && frameName.length() > 0) {
                reuseKey += "." + frameName;
            }
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("Remove SPDocument stored under "+reuseKey);
            }
            storeddoms.remove(reuseKey);
        }
    }
    
    public void setTargetGenerator(TargetGenerator tgen) {
        this.generator = tgen;
    }
    
    public void setTestRecording(TestRecording testRecording) {
        this.testRecording = testRecording;
    }
    
    public void setEditorLocation(String editorLocation) {
        if (editorLocation.endsWith("/")) {
            editorLocation = editorLocation.substring(0, editorLocation.length() - 1);
        }
        this.editorLocation = editorLocation;
    }
    
    public void setCheckModtime(boolean checkModtime) {
        this.checkModtime = checkModtime;
    }
    
    public void setSessionCleaner(SessionCleaner sessionCleaner) {
        this.sessionCleaner = sessionCleaner;
    }
    
    public void setRenderExternal(boolean renderExternal) {
        this.renderExternal = renderExternal;
    }
    
    public void setAdditionalTrailInfo(AdditionalTrailInfo additionalTrailInfo) {
        this.additionalTrailInfo = additionalTrailInfo;
    }
    
    public void setMaxStoredDoms(int maxStoredDoms) {
        this.maxStoredDoms = maxStoredDoms;
    }
    
    public void setEditModeAllowed(boolean editModeAllowed) {
        this.editmodeAllowed = editModeAllowed;
    }

    public void setIncludePartsEditableByDefault(boolean includePartsEditableByDefault) {
        this.includePartsEditableByDefault = includePartsEditableByDefault;
    }
    
}
