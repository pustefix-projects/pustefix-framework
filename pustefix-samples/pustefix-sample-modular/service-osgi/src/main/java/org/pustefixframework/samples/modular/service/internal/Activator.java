package org.pustefixframework.samples.modular.service.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.pustefixframework.samples.modular.service.RandomNumberGenerator;

public class Activator implements BundleActivator {

    public void start(BundleContext context) throws Exception {
        RandomNumberGenerator generator = new MathRandomNumberGenerator();
        context.registerService(RandomNumberGenerator.class.getName(), generator, null);   
    }
    
    public void stop(BundleContext context) throws Exception {   
    }
    
}
