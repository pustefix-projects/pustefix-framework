package org.pustefixframework.http.internal;

import java.io.ByteArrayOutputStream;
import java.security.DigestOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.pustefixframework.http.RenderInterceptor;
import org.springframework.beans.factory.annotation.Autowired;

import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.RenderingException;
import de.schlund.pfixxml.SPDocument;

public class RenderInterceptorProcessor {

    @Autowired(required = false)
    List<RenderInterceptor> renderInterceptors = new ArrayList<>();

    public boolean preRender(SPDocument spdoc, String stylesheet, PfixServletRequest preq, 
            HttpServletResponse res, DigestOutputStream digestOutput, ByteArrayOutputStream byteOutput) throws RenderingException {
        for(RenderInterceptor renderInterceptor: renderInterceptors) {
            if(!renderInterceptor.preRender(spdoc, stylesheet, preq, res, digestOutput, byteOutput)) {
                return false;
            }
        }
        return true;
    }

    public void postRender(SPDocument spdoc, String stylesheet, PfixServletRequest preq, 
                      HttpServletResponse res, DigestOutputStream digestOutput, ByteArrayOutputStream byteOutput) throws RenderingException {
        for(RenderInterceptor renderInterceptor: renderInterceptors) {
            renderInterceptor.postRender(spdoc, stylesheet, preq, res, digestOutput, byteOutput);
        }
    }

}
