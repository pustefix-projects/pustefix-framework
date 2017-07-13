package org.pustefixframework.web.servlet.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.view.AbstractUrlBasedView;

/**
 * {@link org.springframework.web.servlet.View} implementation that renders the response
 * using a XSLT stylesheet from the Pustefix {@link de.schlund.pfixxml.targets.TargetGenerator}.
 */
public class XsltView extends AbstractUrlBasedView {

    @Override
    protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        //TODO implement TargetGenerator support
        throw new UnsupportedOperationException("TargetGenerator support not yet implemented");
    }

}
