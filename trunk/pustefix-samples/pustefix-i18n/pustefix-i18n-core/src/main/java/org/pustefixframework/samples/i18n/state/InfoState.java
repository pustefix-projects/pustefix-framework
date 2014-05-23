package org.pustefixframework.samples.i18n.state;

import org.pustefixframework.samples.i18n.context.TermsInfo;
import org.springframework.beans.factory.annotation.Autowired;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.app.StaticState;
import de.schlund.pfixxml.PfixServletRequest;

public class InfoState extends StaticState {
    
    private TermsInfo termsInfo;
    
    @Override
    public boolean isAccessible(Context context, PfixServletRequest preq)
            throws Exception {
        return termsInfo.getAccepted();
    }
    
    @Autowired
    public void setTermsInfo(TermsInfo termsInfo) {
        this.termsInfo = termsInfo;
    }

}
