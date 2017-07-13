package org.pustefixframework.web.servlet.view;

import java.util.Locale;

import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.UrlBasedViewResolver;


/**
 * {@link org.springframework.web.servlet.ViewResolver} implementation that resolves
 * instances of {@link XsltView} by translating the supplied view name into the target
 * XSLT stylesheet from the Pustefix {@link de.schlund.pfixxml.targets.TargetGenerator}.
 */
public class XsltViewResolver extends UrlBasedViewResolver {

   @Override
   public View resolveViewName(String viewName, Locale locale) throws Exception {
       //currently only supports redirect URLs
       //TODO implement TargetGenerator support
       if(viewName.startsWith(REDIRECT_URL_PREFIX)) {
           return super.resolveViewName(viewName, locale);
       }
       return null;
   }

}
