/*
* This file is part of PFIXCORE.
*
* PFIXCORE is free software; you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* PFIXCORE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with PFIXCORE; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*
*/

/*
*
*/
package de.schlund.pfixxml.exceptionhandler;

import de.schlund.pfixxml.PfixServletRequest;

import de.schlund.util.FactoryInit;

import java.util.Properties;

import javax.servlet.http.*;


/**
 * The main class handling all exceptions occuring during work
 * of the Pustefix system. This class is implemented as a singleton.
 *
 *
 * Created: Wed Mar 14 14:47:07 2001
 *
 * @author <a href="mailto: jtl@schlund.de">Jens Lautenbacher</a>
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
public class ExceptionHandler implements FactoryInit {

    //~ Instance/static variables ..............................................

    private static ExceptionHandler instance_=null;
    private final String PROP_FILE_          ="exceptionhandler.propertyfile";
    private Cubbyhole cubbyhole_             =null;
    private String propfile_                 =null;
    private PropertyManager propman_         =null;
    private PFXThreadedHandler xhandler_     =null;

    //~ Constructors ...........................................................

    /**
     * Creates a new ExceptionHandler object.
     * Internal objects are initialised. 
     */
    private ExceptionHandler() {
        cubbyhole_=new Cubbyhole(100);
        propman_  =PropertyManager.getInstance();
        xhandler_ =new PFXThreadedHandler(cubbyhole_);
    }

    //~ Methods ................................................................

    /**
     * Returns the instance of this exceptionhandler.
     * @return ExceptionHandler the instance.
     */
    public static ExceptionHandler getInstance() {
        if(instance_==null) {
            instance_=new ExceptionHandler();
        }
        return instance_;
    }

    /**
     * Called from <see>de.schlund.pfixxml.ServletManager#callProcess</see> 
     * to handle all thrown exceptions. Pack the parameters in an <see>ExceptionContext</see>
     * and put this in the <see>Cubbyhole</see>.
     * @param e  the thrown exception value.
     * @param req the responsible request.
     * @param properties the current properties.
     */
    synchronized public void handle(Throwable t, PfixServletRequest req, 
                                    Properties properties, 
                                    HttpServletResponse res) {
        PFUtil.getInstance().debug("Handling a " + t.getClass().getName());
        // if propertyfile changed reload it, it's done in a tomcat thread (clumsy;-))
        // if it is the first time, skip reinitialisation
        // This is called from various threads, so everyone needs its own context !
        ExceptionContext excontext=new ExceptionContext(t, req, res, properties);
        excontext.init();
        if(propman_.needsReinitialisation()) {
            PFUtil.getInstance().debug("Reinitialization needed");
            try {
                propman_.init(propfile_);
                propman_.checkProperties();
                propman_.resetModTime();
                xhandler_.init();
                xhandler_.setErrorFlag(false);
            } catch(PFConfigurationException ex) {
                PFUtil.getInstance().fatal(
                        "Configuration of exceptionhandler failed: " + 
                        ex.getMessage() + " reason: " + 
                        ex.getExceptionCause().getClass()+":"+ex.getExceptionCause().getMessage());
                xhandler_.setErrorFlag(true);
            }
        } else
            PFUtil.getInstance().debug("No reinitialization needed");
        PFUtil.getInstance().debug(
                " Everything ok. I hope;-) Let's put the exception in the cubbyhole");
        try {
            cubbyhole_.put(excontext);
        } catch(InterruptedException ex) {
        }
    }

    /**
     * <see>de.schlund.util.FactoryInit#init(Properties)</see>. Create a new 
     * <see>PFXThreadHandler</see> and call its <see>PFXThreadHandler#init</see>
     * and <see>PFXThreadHandler#doit</see> method.
     */
    public void init(Properties properties) {
        PFUtil.getInstance().debug(
                "ExceptionHandler.init called from FactoryInit.");
        propfile_=(String) properties.getProperty(PROP_FILE_, "");
        try {
            propman_.init(propfile_);
            propman_.checkProperties();
            propman_.printConfig();
            xhandler_.init();
            xhandler_.doIt();
        } catch(PFConfigurationException e) {
            // This should never happen
            PFUtil.getInstance().fatal(
                    "Configuration of exceptionhandler failed: " + 
                    e.getMessage() + " reason: " + 
                    e.getExceptionCause().getClass()+":"+e.getExceptionCause().getMessage());
            xhandler_.setErrorFlag(true);
            xhandler_.doIt();
            return;
        }
    }
} //ExceptionHandler
