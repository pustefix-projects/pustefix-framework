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
import java.util.Hashtable;
import org.apache.log4j.Category;
/**
 * Utility class for internal usage. It is a singleton. <br/>
 * 
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker </a>
 */
class PFUtil {
    //~ Instance/static variables
    // ..............................................
    static final int FULL = 2;
    static final int MATCH = 0;
    static final int NO_MATCH = 3;
    static final int TRIGGER_MATCH = 1;
    private static Category CAT_ = Category.getInstance(ExceptionHandler.class.getName());
    private static PFUtil instance_ = null;
    private Hashtable rates_ = null;
    /**
     * Creates a new TimeUtil object.
     */
    private PFUtil() {
        rates_ = new Hashtable();
        init();
    }
    //~ Methods
    // ................................................................
    /**
     * The getInstance method of a singleton.
     * 
     * @return the one and only instance.
     */
    static PFUtil getInstance() {
        if (instance_ == null) {
            instance_ = new PFUtil();
        }
        return instance_;
    }
    /*
     * void setMailConfig(String[] to, String from, String host, boolean doit) {
     * domail_=doit; if(! domail_) return; boolean error =false; StringBuffer
     * strerror=new StringBuffer(); toaddresses_ =new
     * InternetAddress[to.length]; for(int i=0; i <to.length; i++) { try {
     * toaddresses_[i]=new InternetAddress(to[i]); } catch(AddressException e) {
     * error=true; strerror.append( to[i] + " is not a valid address" +
     * e.getMessage()); } } try { fromaddress_=new InternetAddress(from); }
     * catch(AddressException e) { error=true; strerror.append(from + " is not
     * a valid address" + e.getMessage()); } Properties
     * props=System.getProperties(); props.put("mail.smtp.host", host);
     * mailsession_=Session.getDefaultInstance(props, null); if(error)
     * CAT_.fatal(strerror);
     */
    /**
     * Get util data.
     * 
     * @param dim
     *                 A String as a key.
     * @return a value belonging the key.
     */
    int getRate(String dim) {
        if (rates_.containsKey(dim)) {
            return ((Integer) rates_.get(dim)).intValue();
        }
        return 0;
    }
    /**
     * Get the hashcode of a stacktrace from the throwable object.
     * 
     * @return the hashcode as an int.
     */
    int getSTraceHashCode(StackTraceElement[] strace) {
        StringBuffer tmp = null;
        int code = 0;
        tmp = new StringBuffer();
        for (int i = 0; i < strace.length; i++) {
            tmp.append(strace[i].toString().trim());
        }
        code = tmp.toString().hashCode();
        return code;
    }
    synchronized void debug(String text) {
        if (CAT_.isDebugEnabled())
            CAT_.debug(text);
    }
    
    synchronized void info(String text) {
        if (CAT_.isInfoEnabled())
            CAT_.info(text);
    }
    
    synchronized void error(String text) {
        CAT_.error(text);
    }
    synchronized void fatal(String text) {
        CAT_.fatal(text);
    }
   
    /**
     * Get String representation for status codes.
     * 
     * @param result
     *                 the status code as an int.
     * @return the status code as a String.
     */
    String strByResult(int result) {
        switch (result) {
            case MATCH :
                return new String("MATCH");
            case TRIGGER_MATCH :
                return new String("TRIGGER_MATCH");
            case FULL :
                return new String("FULL");
            case NO_MATCH :
                return new String("NO_MATCH");
            default :
                return new String("UNKOWN_RESULT");
        }
    }
    /**
     * internal initialisation.
     */
    private void init() {
        rates_.put("milli", new Integer(1));
        rates_.put("sec", new Integer(1000));
        rates_.put("min", new Integer(60 * 1000));
        rates_.put("hour", new Integer(60 * 60 * 1000));
        rates_.put("day", new Integer(24 * 60 * 60 * 1000));
    }
} //PFUtil
