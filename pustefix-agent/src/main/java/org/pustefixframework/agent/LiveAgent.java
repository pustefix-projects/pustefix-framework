package org.pustefixframework.agent;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;

import javax.management.ObjectName;

public class LiveAgent implements LiveAgentMBean {
     
    private LiveClassFileTransformer transformer;
    
    public LiveAgent(LiveClassFileTransformer transformer) {
        this.transformer = transformer;
    }
    
    public String getLiveLocation(String moduleName) {
        return transformer.getLiveLocation(moduleName);
    }
    
    public static void premain(String agentArguments, Instrumentation instrumentation) {    
        
        LiveInfo liveInfo = new LiveInfo();
        LiveClassFileTransformer transformer = new LiveClassFileTransformer(liveInfo);
        instrumentation.addTransformer(transformer);          
        
        LiveAgent agent = new LiveAgent(transformer);
        try {
            ObjectName name = new ObjectName("Pustefix:type=LiveAgent");
            ManagementFactory.getPlatformMBeanServer().registerMBean(agent, name);
        } catch(Exception x) {
            x.printStackTrace();
        }
    }

  
    
}
