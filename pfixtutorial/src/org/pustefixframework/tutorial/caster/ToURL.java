package org.pustefixframework.tutorial.caster;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.pustefixframework.tutorial.StatusCodeLib;

import de.schlund.pfixcore.generator.IWrapperParamCaster;
import de.schlund.pfixcore.generator.SimpleCheck;
import de.schlund.pfixxml.RequestParam;
import de.schlund.util.statuscodes.StatusCode;

public class ToURL extends SimpleCheck implements IWrapperParamCaster {

    private URL[] value = null;
    private StatusCode scode;
    
    public ToURL() {
        scode = StatusCodeLib.CASTER_URL_INVALID;
    }
    
    public void castValue(RequestParam[] requestParams) {
        List<URL> out = new ArrayList<URL>();
        URL url;
        for (RequestParam param : requestParams) {
            try {
                url = new URL(param.getValue());
                out.add(url);
            } catch(MalformedURLException ex) {
                addSCode(scode);
            }
        }
        if (!errorHappened()) {
            value = out.toArray(new URL[] {});
        }
    }

    public Object[] getValue() {
        return value;
    }
}
