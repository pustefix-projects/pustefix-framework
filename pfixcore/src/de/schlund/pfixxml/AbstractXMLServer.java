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




import de.schlund.pfixcore.util.PropertiesUtils;
import de.schlund.pfixxml.serverutil.*;
import de.schlund.pfixxml.targets.*;
import de.schlund.pfixxml.testenv.*;
import java.io.*;
import java.net.SocketException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
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

    private static DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
    
    public static final String  SESS_LANG                = "__SELECTED_LANGUAGE__";
    public static final String  SESS_RECORDMODE          = "__RECORDMODE__";
    public static final String  XML_CONTENT_TYPE         = "text/xml; charset=iso-8859-1";
    public static final String  PARAM_XMLONLY            = "__xmlonly";
    public static final String  PARAM_ANCHOR             = "__anchor";
    public static final String  PARAM_EDITMODE           = "__editmode";
    public static final String  PARAM_LANG               = "__language";
    public static final String  PARAM_FRAME              = "__frame";
    public static final String  PARAM_NOSTORE            = "__nostore";
    public static final String  PARAM_REUSE              = "__reuse"; // internally used
    public static final String  PARAM_RECORDMODE         = "__recordmode";
    private static final String PARAM_VAL_RECORDMODE_OFF = "0";
    public static final String  XSLPARAM_LANG            = "lang";
    public static final String  XSLPARAM_SESSID          = "__sessid";
    public static final String  XSLPARAM_URI             = "__uri";
    public static final String  XSLPARAM_SERVP           = "__servletpath";
    public static final String  XSLPARAM_REMOTE_ADDR     = "__remote_addr";
    public static final String  XSLPARAM_SERVER_NAME     = "__server_name";
    public static final String  XSLPARAM_FRAME           = "__frame";
    public static final String  XSLPARAM_REUSE           = "__reusestamp";
    private static final String XSLPARAM_TG              = "__target_gen";
    private static final String XSLPARAM_TKEY            = "__target_key";
    private static final String VALUE_NONE               = "__NONE__";
    public static final String  SUFFIX_SAVEDDOM          = "_SAVED_DOM";
    public static final String  PROP_DEPEND              = "xmlserver.depend.xml";
    public static final String  PROP_NAME                = "xmlserver.servlet.name";
    public static final String  PROP_NOEDIT              = "xmlserver.noeditmodeallowed";
    public static final String  PROP_RENDER_EXT          = "xmlserver.output.externalrenderer";
    public static final String  PROP_CLEANER_TO          = "sessioncleaner.timeout";

    // xml output only, no transformation
    private static final String PROP_NOXML_KEY                    = "xmlserver.noxmlonlyallowed";
    private static final String PROP_XMLONLY_NOT_ENABLED_VALUE    = "true";
    private static final String PROP_XMLONLY_ENABLED_VALUE        = "false";
    private static final String PROP_XMLONLY_RESTRICTED_VALUE     = "restricted";
    private static final String PROP_XMLONLY_RESTRICTED_HOSTS_KEY = "xmlserver.xmlonlyallowed.host";
    // record mode
    private static final String PROP_RECORDMODE_KEY               = "xmlserver.recordmode_allowed";
    private static final String PROP_RECORDMODE_ALLOWED_VALUE     = "true";
   
    // skip stat on all targets
    private static final String PROP_SKIP_GETMODTIMEMAYBEUPADTE_KEY           = "targetgenerator.skip_getmodtimemaybeupdate";
    private static final String PROP_SKIP_GETMODTIMEMAYBEUPADTE_ENABLED_VALUE = "true";

    private static final int XML_ONLY_ALLOWED    = 0;
    private static final int XML_ONLY_RESTRICTED = 1;
    private static final int XML_ONLY_PROHIBITED = 2;

    private static final String PROP_PROHIBITDEBUG = "xmlserver.prohibitdebug";
    private static final String PROP_PROHIBITINFO  = "xmlserver.prohibitinfo";
    
    // error handling
    private static final String ERROR_STYLESHEET = "core/xsl/errorrepresentation.xsl";
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
    private String          targetconf        = null;
    private boolean         render_external   = false;
    private static Category LOGGER_TRAIL      = Category.getInstance("LOGGER_TRAIL");
    private static Category CAT               = Category.getInstance(AbstractXMLServer.class.getName());
    private boolean         editmodeAllowed   = false;
    private boolean         recordmodeAllowed = false;
    private int             isXMLOnlyAllowed  = XML_ONLY_PROHIBITED;
    private String[]        xmlOnlyValidHosts = null;

    private int scleanertimeout = 300;

    private boolean allowInfo  = true;
    private boolean allowDebug = true;
    
    //~ Initializers ...............................................................................

    static {
        dbfac.setNamespaceAware(true);
    }

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

    private void initValues() throws ServletException {
        if ((targetconf = getProperties().getProperty(PROP_DEPEND)) == null) {
            throw (new ServletException("Need property '" + PROP_DEPEND + "'"));
        }
        if ((servletname = getProperties().getProperty(PROP_NAME)) == null) {
            throw (new ServletException("Need property '" + PROP_NAME + "'"));
        }

        String prohibitDebug = getProperties().getProperty(PROP_PROHIBITDEBUG);
        if (prohibitDebug != null && (prohibitDebug.equals("true") || prohibitDebug.equals("1")))
            allowDebug = false;
        
        String prohibitInfo  = getProperties().getProperty(PROP_PROHIBITINFO);
        if (prohibitInfo != null && (prohibitInfo.equals("true") || prohibitInfo.equals("1")))
            allowInfo = false;
        
        String noedit = getProperties().getProperty(PROP_NOEDIT);
        if (noedit != null && (noedit.equals("false") || noedit.equals("0"))) {
            editmodeAllowed = true;
        } else {
            editmodeAllowed = false;
        }
        String timeout;
        if ((timeout = getProperties().getProperty(PROP_CLEANER_TO)) != null) {
            try {
                scleanertimeout = new Integer(timeout).intValue();
            } catch (NumberFormatException e) {
                throw new ServletException(e.getMessage());
            }
        }

        handleRecordModeProps();
       
        boolean skip_getmodtimemaybeupdate = handleSkipGetModTimeMaybeUpdateProps();
        
        handleXMLOnlyProps();
        
        try {
            generator = TargetGeneratorFactory.getInstance().createGenerator(targetconf);
        } catch (Exception e) {
            CAT.error("Error: ", e);
            throw (new ServletException("Couldn't get TargetGenerator: " + e.toString()));
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
            sb.append("        recordmode allowed = ").append(recordmodeAllowed).append("\n");
            String str = null;
            if (isXMLOnlyAllowed == XML_ONLY_ALLOWED) {
                str = "allowed";
            } else if (isXMLOnlyAllowed == XML_ONLY_RESTRICTED) {
                str = "restricted";
            } else if (isXMLOnlyAllowed == XML_ONLY_PROHIBITED) {
                str = "prohibited";
            } else str = "???";
            sb.append("            xmlOnlyAllowed = ").append(str).append("\n");
            if (isXMLOnlyAllowed == XML_ONLY_RESTRICTED) {
                if (xmlOnlyValidHosts!=null) {
                    for (int i=0; i<xmlOnlyValidHosts.length; i++) {
                        sb.append("                       host = ").append(xmlOnlyValidHosts).append("\n");
                    }
                }
            }
            sb.append("skip_getmodtimemaybeupdate = ").append(skip_getmodtimemaybeupdate).append("\n");
            sb.append("           render_external = ").append(render_external).append("\n");
            CAT.info(sb.toString());
        }
    }

    /**
     * Handle the properties concerning if a client can retrieve plain xml.
     * Makes write access to the <see>isXMLOnlyAllowed</see> and to the
     * <see>xmlOnlyValidHosts</see> fields if mode is restricted.
     * @throws ServletException if the properties can not be found.
     */
    private void handleXMLOnlyProps() throws ServletException {
        // analyze the NO_XMLONLY_ALLOWED property
        if (getProperties().getProperty(PROP_NOXML_KEY) == null) {
            String msg = "Need property '" + PROP_NOXML_KEY + "'";
            CAT.fatal(msg);
            throw new ServletException(msg);
        } else {
            String tmp = getProperties().getProperty(PROP_NOXML_KEY);
            if (tmp.toUpperCase().equals(PROP_XMLONLY_NOT_ENABLED_VALUE.toUpperCase())) {
                isXMLOnlyAllowed = XML_ONLY_PROHIBITED;
            } else if (tmp.toUpperCase().equals(PROP_XMLONLY_ENABLED_VALUE.toUpperCase())) {
                isXMLOnlyAllowed = XML_ONLY_ALLOWED;
            } else if (tmp.toUpperCase().equals(PROP_XMLONLY_RESTRICTED_VALUE.toUpperCase())) {
                isXMLOnlyAllowed = XML_ONLY_RESTRICTED;
            }
            if (isDebugEnabled()) {
                CAT.debug("\nXML only is: "
                          + (isXMLOnlyAllowed == XML_ONLY_ALLOWED ? "allowed" : "")
                          + (isXMLOnlyAllowed == XML_ONLY_RESTRICTED ? "restricted" : "")
                          + (isXMLOnlyAllowed == XML_ONLY_PROHIBITED ? "prohibited" : ""));
            }
            if (isXMLOnlyAllowed == XML_ONLY_RESTRICTED) { // get valid hosts form properties
                TreeMap      map  = PropertiesUtils.selectPropertiesSorted(getProperties(), PROP_XMLONLY_RESTRICTED_HOSTS_KEY);
                xmlOnlyValidHosts = new String[map.entrySet().size()];
                Iterator     iter = map.keySet().iterator();
                int          i    = 0;
                StringBuffer sb   = null;
                while (iter.hasNext()) {
                    Object key = iter.next();
                    xmlOnlyValidHosts[i] = map.get(key).toString();
                    if (isDebugEnabled()) {
                        if (sb == null) {
                            sb = new StringBuffer();
                        }
                        sb.append("   " + xmlOnlyValidHosts[i]).append("\n");
                    }
                    i++;
                }
                if (isDebugEnabled()) {
                    sb.insert(0, "\nValid hosts for xml only are: \n");
                    CAT.debug(sb.toString());
                }
            }
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

    /**
     * Handle the properties concerning if the record mode is accessible.
     * Sets the <see>recormodeAllowed</see> field if enabled.
     * @throws ServletException if the properties can not be found.
     */
    private void handleRecordModeProps() throws ServletException {
        // analyze the RECORDMODE property
        if (getProperties().getProperty(PROP_RECORDMODE_KEY) == null) {
            String msg = "Need property '" + PROP_RECORDMODE_KEY + "'";
            CAT.fatal(msg);
            throw new ServletException(msg);
        } else {
            String tmp = getProperties().getProperty(PROP_RECORDMODE_KEY);
            recordmodeAllowed = tmp.toUpperCase().equals(PROP_RECORDMODE_ALLOWED_VALUE.toUpperCase()) ? true : false;
            if (isDebugEnabled()) {
                CAT.debug("RecordModeAllowed is: " + recordmodeAllowed);
            }
        }
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
                return generator.tryReinit();
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
    public abstract SPDocument getDom(PfixServletRequest preq) throws Exception;

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
        long         currtime;
        long         preproctime = -1;
        long         getdomtime  = -1;
        long         handletime  = -1;
        
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
                params.put(XSLPARAM_LANG, (String)session.getAttribute(SESS_LANG));
            }
            if ((value = preq.getRequestParam(PARAM_EDITMODE)) != null) {
                if (value.getValue() != null) {
                    session.setAttribute(PARAM_EDITMODE, value.getValue());
                }
            }
            if (session.getAttribute(PARAM_EDITMODE) != null) {
                // first we check if the properties prohibit editmode
                if (editmodeAllowed) {
                    params.put(PARAM_EDITMODE, (String)session.getAttribute(PARAM_EDITMODE));
                }
            }
        }

        // Set stylesheet parameters for editconsole
        if (recordmodeAllowed) {
            String name = RecordManagerFactory.getInstance().createRecordManager(targetconf).getTestcaseName(session);         
            if (name != null) {
                params.put(PARAM_RECORDMODE, name);            
            }
            boolean allowed = recordmodeAllowed && RecordManagerFactory.getInstance().createRecordManager(targetconf).isRecordmodeAllowed();
            params.put("recordmode_allowed", new Boolean(allowed).toString());
        }

        // Now we will store the time needed from the creation of the request up to now
        currtime    = System.currentTimeMillis();
        preproctime = currtime - preq.getCreationTimestamp();
       
        if (spdoc == null) {
            preq.startLogEntry();
            spdoc           = getDom(preq);
            String pagename = spdoc.getPagename();
            if (pagename == null) {
                preq.endLogEntry("GETDOM", 0);
            } else {
                preq.endLogEntry("GETDOM (" + pagename + ")", 0);
            }
            
            // start recording if allowed and enabled
            if (recordmodeAllowed) {
                RecordManager recorder = RecordManagerFactory.getInstance().createRecordManager(targetconf);
                recorder.tryRecord(preq, res, spdoc, session);
            }
            if (isDebugEnabled()) {
                CAT.debug("* Document for XMLServer is" + spdoc);
            }
            if (isInfoEnabled()) {
                CAT.info(">>> Complete getDom(...) took " + getdomtime + "ms");
            }
            RequestParam[] anchors   = preq.getAllRequestParams(PARAM_ANCHOR);
            Map            anchormap;
            if (anchors != null && anchors.length > 0) {
                anchormap = createAnchorMap(anchors);
                spdoc.storeFrameAnchors(anchormap);
            }
            PublicXSLTProcessor xsltproc = TraxXSLTProcessor.getInstance();
            currtime = System.currentTimeMillis();
            spdoc.setDocument(xsltproc.xmlObjectFromDocument(spdoc.getDocument()));
            if (isInfoEnabled()) {
                CAT.info(">>> Complete xmlObjectFromDocument(...) took "
                         + (System.currentTimeMillis() - currtime) + "ms");
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
            getdomtime = System.currentTimeMillis() - currtime;
        }
        currtime = System.currentTimeMillis();
        params.put(XSLPARAM_REUSE, "" + spdoc.getTimestamp());
        handleDocument(preq, res, spdoc, params, doreuse);
        handletime = System.currentTimeMillis() - currtime;
        if (isInfoEnabled()) {
            CAT.info(">>> Complete handleDocument(...) took " + handletime + "ms");
        }
        try {
            if (spdoc.getResponseContentType() == null || spdoc.getResponseContentType().startsWith("text/html")) {
                OutputStream       out          = res.getOutputStream();
                OutputStreamWriter writer       = new OutputStreamWriter(out, res.getCharacterEncoding());
                writer.write("\n<!-- PRE_PROC: " + preproctime +
                             " GET_DOM: " + getdomtime +
                             " HDL_DOC: " + handletime + " -->");
                writer.flush();
            }
        } catch (Exception e) {
            // ignore
        }
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
        HttpSession   session    = preq.getSession(false);
        TreeMap       paramhash  = constructParameters(spdoc, params);
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
            if (session != null) {
                // we only want to update the Session hit when we are not handling a "reuse" request
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
        // Check the document for supplied headers...
        HashMap headers = spdoc.getResponseHeader();
        if (headers != null) {
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
        // Check if we are allowed and should just supply the xml doc
        plain_xml = isXMLOnlyCurrentlyEnabled(preq);
        if (! render_external && ! plain_xml) {
            TraxXSLTProcessor xsltproc = TraxXSLTProcessor.getInstance();
            Object stylevalue = null;
            
            try {
                stylevalue = generator.getTarget(stylesheet).getValue();
            } catch (TargetGenerationException targetex) {
                CAT.error("AbstractXMLServer caught Exception!", targetex);
                Document errordoc = targetex.toXMLRepresentation();
                errordoc = xsltproc.xmlObjectFromDocument(errordoc);
                Object   stvalue = generator.createXSLLeafTarget(ERROR_STYLESHEET).getValue();
                xsltproc.applyTrafoForOutput(errordoc, stvalue, null, res.getOutputStream());
                return;
            }
            try {
                xsltproc.applyTrafoForOutput(spdoc.getDocument(), 
                                             stylevalue, paramhash, 
                                             res.getOutputStream());
            } catch (TransformerException e) {
                if(e.getException() instanceof SocketException) {
                    CAT.warn("[Ignored TransformerException] : " + e.getMessage());
                    if (isInfoEnabled()) {
                        CAT.info("[Ignored TransformerException]", e);
                    }
                } else {
                    throw e;
                }
            }
        } else if (plain_xml) {
            res.setContentType(XML_CONTENT_TYPE);
            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(spdoc.getDocument()), 
                                                                        new StreamResult(res.getOutputStream()));
        } else {
            Document ext_doc = dbfac.newDocumentBuilder().newDocument();
            Element  root    = ext_doc.createElement("render_external");
            ext_doc.appendChild(root);
            Element  ssheet  = ext_doc.createElement("stylesheet");
            root.appendChild(ssheet);
            ssheet.setAttribute("name", generator.getDisccachedir() + stylesheet);
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
            LOGGER_TRAIL.warn(logbuff.toString());
        }
        preq.endLogEntry("HANDLEDOCUMENT (" + stylesheet + ")", 0);
    }

   
    /**
     * Check if the current request will retrieve plain xml.
     * @param the {@link PfixServletRequest} containing all
     * submitted parameters.
     * @return true if the <see>PARAM_XMLONLY</see> is 1 and
     * plain xml is allowed or the client host is valid in
     * restricted mode, false if plain xml is prohibited.
     */
    boolean isXMLOnlyCurrentlyEnabled(PfixServletRequest pfreq) {
        if (isXMLOnlyAllowed == XML_ONLY_ALLOWED) {
            RequestParam doplainxml = pfreq.getRequestParam(PARAM_XMLONLY);
            if (doplainxml != null && doplainxml.getValue().equals("1")) {
                return true;
            }
        } else if (isXMLOnlyAllowed == XML_ONLY_RESTRICTED) {
            RequestParam doplainxml = pfreq.getRequestParam(PARAM_XMLONLY);
            if (doplainxml != null && doplainxml.getValue().equals("1")) {
                String client_ip = pfreq.getRemoteAddr();
                for (int i = 0; i < xmlOnlyValidHosts.length; i++) {
                    if (client_ip.equals(xmlOnlyValidHosts[i])) {
                        if (isInfoEnabled()) {
                            CAT.info("\nEnabling plain xml for client " + client_ip);
                        }
                        return true;
                    } else {
                        CAT.warn("\n The host " + client_ip
                                 + " is NOT allowed to retrieve plain xml!");
                    }
                }
            }
        } else if (isXMLOnlyAllowed == XML_ONLY_PROHIBITED) {
            return false;
        }
        return false;
    }

    private TreeMap constructParameters(SPDocument spdoc, Properties gen_params) {
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
        paramhash.put(XSLPARAM_TG, targetconf);
        paramhash.put(XSLPARAM_TKEY, VALUE_NONE);
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
