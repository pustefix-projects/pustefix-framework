package org.pustefixframework.agent;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;

import javax.management.ObjectName;

/**
 * Java agent which adds a ClassFileTransfomer which replaces bytecode
 * of classes loaded from jar files by bytecode from live fallback locations
 * (local Maven projects detected by reading the live.xml or automatically 
 * searched under a configurable directory).
 * 
 * @author mleidig@schlund.de
 *
 */
public class LiveAgent implements LiveAgentMBean {
    
    private static String PROP_LIVEROOT = "pustefix.liveroot";
    private static String PROP_LIVEROOT_MAXDEPTH = "pustefix.liveroot.maxdepth";
    private static int DEFAULT_LIVEROOT_MAXDEPTH = 4;
    
    private LiveClassFileTransformer transformer;
    
    public LiveAgent(LiveClassFileTransformer transformer) {
        this.transformer = transformer;
    }
    
    public String getLiveLocation(String moduleName) {
        return transformer.getLiveLocation(moduleName);
    }
    
    public static void premain(String agentArguments, Instrumentation instrumentation) {    
       
        File liveRootDir = null;
        int liveRootMaxDepth = DEFAULT_LIVEROOT_MAXDEPTH;
        
        String liveRoot = System.getProperty(PROP_LIVEROOT);
        if(liveRoot != null) {
            liveRootDir = new File(liveRoot);
            try {
                liveRootDir = liveRootDir.getCanonicalFile();
            } catch (IOException e) {
                fail("Can't read liveroot dir '" + liveRootDir.getPath() + "'.");
            }
            if(!liveRootDir.exists()) fail("Liveroot dir '" + liveRootDir.getPath() + "' doesn't exist.");
            
            String value = System.getProperty(PROP_LIVEROOT_MAXDEPTH);
            if(value != null) {
                liveRootMaxDepth = Integer.parseInt(value);
            }
        }
        
        LiveInfo liveInfo = null;
        if(liveRootDir == null) {
            try {
                liveInfo = new LiveInfo();
            } catch(Exception x) {
                fail("Error reading live information [" + x.getMessage() + "]");
            }
        } else {
            liveInfo = new LiveInfo(liveRootDir, liveRootMaxDepth);
        }
        LiveClassFileTransformer transformer = new LiveClassFileTransformer(liveInfo);
        instrumentation.addTransformer(transformer);          
        
        LiveAgent agent = new LiveAgent(transformer);
        try {
            ObjectName name = new ObjectName("Pustefix:type=LiveAgent");
            ManagementFactory.getPlatformMBeanServer().registerMBean(agent, name);
        } catch(Exception x) {
            throw new RuntimeException("Can't register LiveAgent", x);
        }
    }
        
    public static void agentmain(String args, Instrumentation inst) throws Exception {
        premain(args, inst);
    }
    
    private static void fail(String msg) {
        System.err.println("ERROR: " + msg);
        System.exit(1);
    }
   
}
