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

package de.schlund.pfixxml.exceptionhandler;

/**
 * A threaded wrapper-class for the <see>PFXHandler</see>.
 * <br/>
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
class PFXThreadedHandler extends PFXHandler implements Runnable {

    //~ Instance/static variables ..............................................

    private Cubbyhole cubbyhole_=null;

    //~ Constructors ...........................................................

    /**
     * Create a new PFXThreadedHandler object and start the 'main' thread.
     * @param cubbyhole the cubbyhole to get objects from.
     */
    PFXThreadedHandler(Cubbyhole cubbyhole) {
        super();
        this.cubbyhole_=cubbyhole;
        PFUtil.getInstance().debug("Creating new PFXThreadedHandler object");
    }

    //~ Methods ................................................................

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        ExceptionContext excontext=null;
        PFUtil.getInstance().debug("PFXThreadedHandler thread running...");
        while(true) {
            try {
                excontext=(ExceptionContext) cubbyhole_.take();
                super.xhandle(excontext);
                Thread.sleep(1);
            } catch(InterruptedException ex) {
            }
        }
    }

    /**
     * Start the main thread
     */
    void doIt() {
        Thread t=null;
        t=new Thread(this);
        t.setName("ExceptionHandling-Thread");
        t.start();
    }
} //PFXThreadedHandler