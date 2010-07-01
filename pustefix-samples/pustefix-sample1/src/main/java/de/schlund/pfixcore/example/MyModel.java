package de.schlund.pfixcore.example;

import java.util.Date;

import de.schlund.pfixcore.generator.UseHandlerClass;
import de.schlund.pfixcore.generator.annotation.Param;

@UseHandlerClass(MyModelHandler.class)
public class MyModel {

    private boolean adult;
    private Date date;
    
    public boolean getAdult() {
        return adult;
    }
    
    public void setAdult(boolean adult) {
        this.adult = adult;
    }
    
    @Param(mandatory=false)
    public Date getDate() {
        return date;
    }
    
    public void setDate(Date date) {
        this.date = date;
    }
    
}
