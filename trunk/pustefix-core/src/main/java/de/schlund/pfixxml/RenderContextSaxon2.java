package de.schlund.pfixxml;

import java.util.Properties;

import javax.xml.transform.Result;


public class RenderContextSaxon2 extends RenderContext {

    private Result result;
    private Properties properties;
    
    public void setResult(Result result) {
        this.result = result;
    }
    
    public Result getResult() {
        return result;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
    
}
