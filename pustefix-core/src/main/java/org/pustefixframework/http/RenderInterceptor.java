package org.pustefixframework.http;

import java.io.ByteArrayOutputStream;
import java.security.DigestOutputStream;

import javax.servlet.http.HttpServletResponse;

import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.RenderingException;
import de.schlund.pfixxml.SPDocument;

public interface RenderInterceptor {

    boolean preRender(SPDocument spdoc, String stylesheet, PfixServletRequest preq, 
                      HttpServletResponse res, DigestOutputStream digestOutput, ByteArrayOutputStream byteOutput) throws RenderingException;

    void postRender(SPDocument spdoc, String stylesheet, PfixServletRequest preq, 
                    HttpServletResponse res, DigestOutputStream digestOutput, ByteArrayOutputStream byteOutput) throws RenderingException;

}
