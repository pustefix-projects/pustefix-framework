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

package org.pustefixframework.http.dereferer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.pustefixframework.config.contextxmlservice.ServletManagerConfig;
import org.pustefixframework.http.AbstractPustefixRequestHandler;

import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.RequestParam;
import de.schlund.pfixxml.serverutil.SessionHelper;
import de.schlund.pfixxml.util.Base64Utils;

/**
 * This class implements a "Dereferer" servlet to get rid of Referer
 * headers. <b>ALL LINKS THAT GO TO AN OUTSIDE DOMAIN MUST USE THIS SERVLET!</b>
 * If this servlet is bound to e.g. /deref than every outside link
 * (say to http://www.gimp.org) must be written like <a href="/deref?link=http://www.gimp.org">Gimp</a>
 *
 */

public class DerefRequestHandler extends AbstractPustefixRequestHandler {
   
    private static final long serialVersionUID = 4003807093421866709L;
    
    protected final static Logger   DEREFLOG        = Logger.getLogger("LOGGER_DEREF");
    protected final static Logger   LOG             = Logger.getLogger(DerefRequestHandler.class);
    
    private long validTime;
    private boolean mustSign;
    
    private ServletManagerConfig config;
    
    public void setValidTime(long validTime) {
        this.validTime = validTime;
    }
    
    public void setMustSign(boolean mustSign) {
        this.mustSign = mustSign;
    }
    
    @Override
    public boolean allowSessionCreate() {
        return (false);
    }

    @Override
    public boolean needsSession() {
        return (false);
    }

    private String signString(String input, long timeStamp) {
        return SignUtil.getSignature(input, timeStamp);
    }

    private boolean checkSign(String input, long timeStamp, long validTime, String sign) {
        if (input == null || sign == null) {
            return false;
        }
        if( (System.currentTimeMillis()-timeStamp) > validTime) {
            return false;
        }
        return SignUtil.checkSignature(input, timeStamp, sign);
    }
    
    @Override
    protected void process(PfixServletRequest preq, HttpServletResponse res) throws Exception {
        RequestParam linkparam    = preq.getRequestParam("link");
        RequestParam enclinkparam = preq.getRequestParam("__enclink");
        RequestParam signparam    = preq.getRequestParam("__sign");
        RequestParam tsparam      = preq.getRequestParam("__ts");

        HttpServletRequest req     = preq.getRequest();
        String             referer = req.getHeader("Referer");

        LOG.debug("===> Referer: " + referer);
        
        long timeStamp = 0;
        if(tsparam!=null && tsparam.getValue()!=null) {
            try {
                timeStamp=Long.parseLong(tsparam.getValue());
            } catch(NumberFormatException x) {
                LOG.warn("Request contains invalid deref timestamp value: " + tsparam.getValue());
                res.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return; 
            }
        }        

        if (linkparam != null && linkparam.getValue() != null) {
            LOG.debug(" ==> Handle link: " + linkparam.getValue());
            if (signparam != null && signparam.getValue() != null) {
                LOG.debug("     with sign: " + signparam.getValue());
            }
            handleLink(linkparam.getValue(), signparam, timeStamp, validTime, mustSign, preq, res);
            return;
        } else if (enclinkparam != null && enclinkparam.getValue() != null &&
                   signparam != null && signparam.getValue() != null) {
            LOG.debug(" ==> Handle enclink: " + enclinkparam.getValue());
            LOG.debug("     with sign: " + signparam.getValue());
            handleEnclink(enclinkparam.getValue(), timeStamp, validTime, signparam.getValue(), preq, res);
            return;
        } else {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
    }


    private void handleLink(String link, RequestParam signparam, long timeStamp, long validTime, boolean mustSign,
                            PfixServletRequest preq, HttpServletResponse res) throws Exception {
        boolean checked = false;
        boolean signed  = false;
        boolean addallparams = false;
        
        if (link.startsWith("/") || link.startsWith("addallparams:/")) {
            // This is a "relative absolute" link, no other domain.
            // It doesn't matter if any JS tricks or other stuff is played here, because
            // the link will only be used in the second stage when we do relocate via 302
            mustSign = false;
        }

        if  (signparam != null && signparam.getValue() != null) {
            signed = true;
        }
        if (signed && checkSign(link, timeStamp, validTime, signparam.getValue())) {
            checked = true;
        }

        if (link.startsWith("addallparams:")) {
            addallparams = true;
            link = link.substring("addallparams:".length());
        }        
        
        // We don't currently enforce the signing at this stage. We may change this to enforcing mode,
        // or maybe we will use some clear warning pages in the case of a not signed request.
        if (checked || (!signed && !mustSign)) {
            OutputStream       out    = res.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(out, res.getCharacterEncoding());
            if (addallparams) {
                String[] allparamnames = preq.getRequestParamNames();
                StringBuffer urlextension = new StringBuffer();
                for (String tmpname : allparamnames) {
                    if (tmpname.equals("link") || tmpname.equals("__sign") || tmpname.equals("__enclink") || tmpname.equals("__ts")) {
                        continue;
                    }
                    RequestParam tmpparam = preq.getRequestParam(tmpname);
                    if (tmpparam.getValue() != null) {
                        urlextension.append("&" + URLEncoder.encode(tmpname, preq.getRequest().getCharacterEncoding()) + "=" + 
                                URLEncoder.encode(tmpparam.getValue(), preq.getRequest().getCharacterEncoding()));
                    }
                }
                String urltail = urlextension.toString();
                if (urltail != null && urltail.length() > 0) {
                    if (link.contains("?")) {
                        link = link + urlextension;
                    } else {
                        link = link + "?" + urlextension.substring(1);
                    }
                }
            }
            String             enclink  = Base64Utils.encode(link.getBytes("utf8"),false);
            if(!signed && !mustSign) timeStamp = System.currentTimeMillis();
            String             reallink = getServerURL(preq) +
                SessionHelper.getClearedURI(preq) + SignUtil.getFakeSessionIdArgument(preq.getRequestedSessionId()) + "?__enclink=" + URLEncoder.encode(enclink, "utf8") +
                "&__sign=" + signString(enclink, timeStamp) + "&__ts=" + timeStamp;
            
            LOG.debug("===> Meta refresh to link: " + reallink);
            DEREFLOG.info(preq.getServerName() + "|" + link + "|" + preq.getRequest().getHeader("Referer"));
            
            writer.write("<html><head>");
            writer.write("<meta http-equiv=\"refresh\" content=\"0; URL=" + reallink +  "\">");

            writer.write("<script language=\"JavaScript\" type=\"text/javascript\">\n");
            writer.write("<!--\n");
            writer.write("function redirect() { setTimeout(\"window.location.replace('" + reallink + "')\", 10); }\n");
            writer.write("-->\n");
            writer.write("</script>\n");
            
            writer.write("</head><body onload=\"redirect()\" bgcolor=\"#ffffff\"><center>");
            writer.write("<a style=\"color:#cccccc;\" href=\"" + reallink + "\">" + "-> Redirect ->" + "</a>");
            writer.write("</center></body></html>");
            writer.flush();
        } else {
            LOG.warn("===> No meta refresh because signature is wrong.");
            sendInvalidLink(preq, res);
            return;
        }
    }
    
    private String getServerURL(PfixServletRequest preq) {
        String url = preq.getScheme() + "://" + preq.getServerName();
        //only add port if non-default
        if (!((preq.getScheme().equals("http") && preq.getServerPort() == 80) || (preq.getScheme().equals("https") && preq.getServerPort() == 443))) {
            url += ":" + preq.getServerPort();
        }
        return url;
    }

    private void handleEnclink(String enclink, long timeStamp, long validTime, String sign, PfixServletRequest preq, HttpServletResponse res) throws Exception {
        if (checkSign(enclink, timeStamp, validTime, sign)) {
            String link = new String( Base64Utils.decode(enclink), "utf8");
            if (link.startsWith("/")) {
                link = getServerURL(preq) + link;
            }
            LOG.debug("===> Relocate to link: " + link);

            res.setHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
            res.setHeader("Pragma", "no-cache");
            res.setHeader("Cache-Control", "no-cache, no-store, private, must-revalidate");
            res.setHeader("Location", link);
            res.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        } else {
            LOG.warn("===> Won't relocate because signature is wrong.");
            sendInvalidLink(preq, res);
            return;
        }
    }
    
    @Override
    public boolean wantsCheckSessionIdValid() {
        return false;
    }

    @Override
    public ServletManagerConfig getServletManagerConfig() {
        return this.config;
    }
    
    public void setConfiguration(ServletManagerConfig config) {
        this.config = config;
    }
    
    private void sendInvalidLink(PfixServletRequest req, HttpServletResponse res) throws IOException {
        String redirectUrl = getServerURL(req) + req.getContextPath();
        res.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        res.setHeader("Location", redirectUrl);
    }
    
    @Override
    public String[] getRegisteredURIs() {
        return new String[] { handlerURI, handlerURI + "/**" };
    }
    
}
