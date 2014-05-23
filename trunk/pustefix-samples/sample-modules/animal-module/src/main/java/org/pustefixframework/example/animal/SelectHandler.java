package org.pustefixframework.example.animal;


import org.springframework.beans.factory.annotation.Autowired;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

public class SelectHandler implements IHandler {

    private ContextAnimal contextAnimal;
    
    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        Select select = (Select)wrapper;
        contextAnimal.setSelectedAnimal(select.getAnimal());
    }
    
    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        if(contextAnimal.getSelectedAnimal() != null) {
            ((Select)wrapper).setAnimal(contextAnimal.getSelectedAnimal());
        }
    }
    
    public boolean needsData(Context context) throws Exception {
        return contextAnimal.getSelectedAnimal() == null;
    }
    
    public boolean prerequisitesMet(Context context) throws Exception {
        return true;
    }
    
    public boolean isActive(Context context) throws Exception {
        return true;
    }
    
    @Autowired
    public void setContextAnimal(ContextAnimal contextAnimal) {
        this.contextAnimal = contextAnimal;
    }

}
