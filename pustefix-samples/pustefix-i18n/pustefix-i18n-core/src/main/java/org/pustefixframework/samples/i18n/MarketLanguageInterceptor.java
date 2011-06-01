package org.pustefixframework.samples.i18n;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextInterceptor;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.Variant;

public class MarketLanguageInterceptor implements ContextInterceptor {

    public void process(Context context, PfixServletRequest preq) {
        String serverName = preq.getRequest().getServerName();
        if(serverName.startsWith("us.")) {
            context.setLanguage("en_US");
            context.setPageSelector("lang", "en");
            context.setPageSelector("country", "US");
        } else if(serverName.startsWith("ca.")) {
            context.setLanguage("en_CA");
            context.setPageSelector("lang", "en");
            context.setPageSelector("country", "CA");
        } else {
            context.setLanguage("de_DE");
            context.setPageSelector("lang", "de");
            context.setPageSelector("country", "DE");
        }
    }
    
}
