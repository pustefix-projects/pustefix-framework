package de.schlund.pfixcore.webservice;

import org.apache.axis.MessageContext;
import de.schlund.pfixxml.loader.AppLoader;

public class SetClassLoaderHandler extends ResetClassLoaderHandler {

   public void invoke(MessageContext msgContext) { 
       
      AppLoader loader=AppLoader.getInstance();
      if(loader.isEnabled()) {
          ClassLoader newLoader=loader.getAppClassLoader();
          if(newLoader!=null) {
              ClassLoader currentLoader=Thread.currentThread().getContextClassLoader();
             
              if(!newLoader.equals(currentLoader)) {
                  msgContext.setProperty(Constants.OLD_CLASSLOADER_PROPERTY,currentLoader);
                  Thread.currentThread().setContextClassLoader(newLoader);
                  msgContext.setClassLoader(newLoader);
               
              }
          }	
        
	  }
   }	  
		   
}
