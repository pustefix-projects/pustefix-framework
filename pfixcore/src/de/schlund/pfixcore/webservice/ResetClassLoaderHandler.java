package de.schlund.pfixcore.webservice;

import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;

import de.schlund.pfixxml.loader.AppLoader;


public class ResetClassLoaderHandler extends BasicHandler {

   protected void resetClassLoader(MessageContext msgContext) {
       AppLoader apploader=AppLoader.getInstance();
       if(apploader.isEnabled()) {
      ClassLoader loader = (ClassLoader) msgContext.getProperty(Constants.OLD_CLASSLOADER_PROPERTY);
      if (loader != null) {
         msgContext.setProperty(Constants.OLD_CLASSLOADER_PROPERTY, null);
         Thread.currentThread().setContextClassLoader(loader);
         //msgContext.setClassLoader(loader);
      }
       }
   }

   public void invoke(MessageContext msgContext) {
      resetClassLoader(msgContext);
   }

   public void onFault(MessageContext msgContext) {
      resetClassLoader(msgContext);
   }

}