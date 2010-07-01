package de.schlund.pfixcore.example;

import org.pustefixframework.container.annotations.Inject;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

public class MyModelHandler implements IHandler {

    private ContextAdultInfo cai;
    
    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        MyModel              info    = (MyModel) wrapper;
        cai.setAdult(info.getAdult());
        System.out.println(">>>>>>>>>>>>>>>>> "+info.getAdult());
        cai.setDate(info.getDate());
    }
    
    public boolean isActive(Context context) throws Exception {
        return true;
    }
    
    public boolean needsData(Context context) throws Exception {
        return cai.needsData();
    }
    
    public boolean prerequisitesMet(Context context) throws Exception {
        return true;
    }
    
    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        MyModel              info = (MyModel) wrapper;
        if (!cai.needsData()) {
            info.setAdult(cai.getAdult());
        }
        info.setDate(cai.getDate());
    }
    
    @Inject
    public void setContextAdultInfo(ContextAdultInfo cai) {
        this.cai = cai;
    }
    
}
