package de.schlund.pfixcore.webservice;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.description.ServiceDesc;
import org.apache.axis.encoding.TypeMappingRegistry;
import org.apache.axis.enum.Scope;
import org.apache.axis.handlers.*;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.providers.java.RPCProvider;
import org.apache.axis.utils.ClassUtils;
import org.apache.axis.utils.Messages;
import org.apache.commons.logging.Log;
import java.util.HashMap;
import de.schlund.pfixcore.example.webservices.DataImpl;
import de.schlund.pfixxml.loader.*;

public class AppLoaderHandler extends BasicHandler
{
    protected static Log log =
        LogFactory.getLog(JWSHandler.class.getName());

    protected static HashMap soapServices = new HashMap();

    AppClassLoader current;
    
    public void invoke(MessageContext msgContext) throws AxisFault {
        try {
            setupService(msgContext);
        } catch (Exception e) {
            log.error( Messages.getMessage("exception00"), e );
            throw AxisFault.makeFault(e);
        }
    }
    
    protected void setupService(MessageContext msgContext) throws Exception {
        AppLoader loader=AppLoader.getInstance();
        if(loader.isEnabled()) {
            String clsName=DataImpl.class.getName();
            if(current!=loader.getAppClassLoader()) {
                ClassUtils.removeClassLoader(clsName);
                soapServices.remove(clsName);
                current=loader.getAppClassLoader();
            }
            msgContext.setClassLoader(current);  
            /* Create a new RPCProvider - this will be the "service"   */
            /* that we invoke.                                                */
            /******************************************************************/
            // Cache the rpc service created to handle the class.  The cache
            // is based on class name, so only one .jws/.jwr class can be active
            // in the system at a time.
            SOAPService rpc = (SOAPService)soapServices.get(clsName);
            if (rpc == null) {
                rpc = new SOAPService(new RPCProvider());
                rpc.setOption(RPCProvider.OPTION_CLASSNAME, clsName );
                rpc.setEngine(msgContext.getAxisEngine());
                
                // Support specification of "allowedMethods" as a parameter.
                String allowed = (String)getOption(RPCProvider.OPTION_ALLOWEDMETHODS);
                if (allowed == null) allowed = "*";
                rpc.setOption(RPCProvider.OPTION_ALLOWEDMETHODS, allowed);
                // Take the setting for the scope option from the handler
                // parameter named "scope"
                String scope = (String)getOption(RPCProvider.OPTION_SCOPE);
                if (scope == null) scope = Scope.DEFAULT.getName();
                rpc.setOption(RPCProvider.OPTION_SCOPE, scope);
                
                // Set up service description
                ServiceDesc sd = rpc.getServiceDescription();
                
                TypeMappingRegistry tmr = msgContext.getAxisEngine().getTypeMappingRegistry();
                sd.setTypeMappingRegistry(tmr);
                sd.setTypeMapping(msgContext.getTypeMapping());
                
                rpc.getInitializedServiceDesc(msgContext);
                
                soapServices.put(clsName, rpc);
                
            }
            
            // Set engine, which hooks up type mappings.
            rpc.setEngine(msgContext.getAxisEngine());
            
            rpc.init();   // ??

            // OK, this is now the destination service!
            msgContext.setService( rpc );
            System.out.println("RPC");
        }
      
    }
    
}
