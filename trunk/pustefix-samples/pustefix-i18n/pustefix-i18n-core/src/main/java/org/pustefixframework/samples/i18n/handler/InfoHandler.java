package org.pustefixframework.samples.i18n.handler;


import org.pustefixframework.samples.i18n.context.TermsInfo;
import org.pustefixframework.samples.i18n.context.User;
import org.pustefixframework.samples.i18n.wrapper.Info;
import org.springframework.beans.factory.annotation.Autowired;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

public class InfoHandler implements IHandler {

    private TermsInfo termsInfo;
    private User user;

    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        Info info = (Info)wrapper;
        termsInfo.setAccepted(info.getAccepted());
    }

    public boolean isActive(Context context) throws Exception {
        return true;
    }

    public boolean needsData(Context context) throws Exception {
        return true;
    }

    public boolean prerequisitesMet(Context context) throws Exception {
        return user.getName() != null;
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        ((Info)wrapper).setAccepted(termsInfo.getAccepted());
    }

    @Autowired
    public void setTermsInfo(TermsInfo termsInfo) {
        this.termsInfo = termsInfo;
    }
    
    @Autowired
    public void setUser(User user) {
        this.user = user;
    }

}
