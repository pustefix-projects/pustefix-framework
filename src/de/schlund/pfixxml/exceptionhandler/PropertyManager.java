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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.oro.text.perl.MalformedPerl5PatternException;
import org.apache.oro.text.perl.Perl5Util;


/*
 *
 */


/**
 * @author jh
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
class PropertyManager {

    //~ Instance/static variables ..............................................

    private static PropertyManager instance_=null;
    private int exrulecount_                =0;
    private File file_                      =null;
    private boolean initialised_            =false;
    private long mtime_                     =0;
    private Properties properties_          =null;
    private final Hashtable validdimensions = new Hashtable();

    //~ Constructors ...........................................................

    /**
     * Creates a new PropertyManager object.
     */
    private PropertyManager() {
        validdimensions.put("sec", "");
        validdimensions.put("min", "");
        validdimensions.put("hour", "");
        validdimensions.put("day", "");
    }

    //~ Methods ................................................................

    static PropertyManager getInstance() {
        if(instance_==null) {
            instance_=new PropertyManager();
        }
        return instance_;
    }
    
    /**
     * Return the configuration for all exceptions.
     * @return an array of exceptionconfig objects ordered in
     * the same way as in the property file.
     */
    ExceptionConfig[] getExceptionConfig() {
        ExceptionConfig[] config=new ExceptionConfig[exrulecount_];
        for(int i=1; i<=exrulecount_; i++) {
            String type         =(String) properties_.getProperty(
                                         "rule." + i + ".type");
            String match        =(String) properties_.getProperty(
                                         "rule." + i + ".match");
            int limit           =Integer.parseInt(
                                         (String) properties_.getProperty(
                                                 "rule." + i + ".limit"));
            String dim          =(String) properties_.getProperty(
                                         "rule." + i + ".dim");
            int burst           =Integer.parseInt(
                                         (String) properties_.getProperty(
                                                 "rule." + i + ".burst"));
            ExceptionConfig conf=new ExceptionConfig(type, match, limit, dim, 
                                                     burst);
            config[i - 1]       =conf;
        }
        return config;
    }

    /**
     * Return the general configuration of the exceptionhandler. 
     * @returns a generalconfig object.
     */
    GeneralConfig getGeneralConfig() {
        String use               =(String) properties_.get(
                                          "rule.general.use_me");
        boolean useme            =Boolean.valueOf(use).booleanValue();
        String cs                =(String) properties_.get(
                                          "rule.general.cleanuptask_schedule");
        int cleanupschedule      =Integer.parseInt(cs);
        String cleanupscheduledim=(String) properties_.get(
                                          "rule.general.cleanuptask_dim");
        String rs                =(String) properties_.get(
                                          "rule.general.reporttask_schedule");
        int reportschedule       =Integer.parseInt(rs);
        String reportscheduledim =(String) properties_.get(
                                          "rule.general.reporttask_dim");
        String stobs             =(String) properties_.get(
                                          "rule.general.stracech_obsolete");
        int straceobs            =Integer.parseInt(stobs);
        String straceobsdim      =(String) properties_.get(
                                          "rule.general.stracech_obsolete_dim");
        GeneralConfig config     =new GeneralConfig(useme, cleanupschedule, 
                                                    cleanupscheduledim, 
                                                    reportschedule, 
                                                    reportscheduledim, 
                                                    straceobs, straceobsdim);
        return config;
    }

    /**
     * Returns the initialised flag.
     * @return boolean
     */
    boolean isInitialised() {
        return initialised_;
    }

    /**
     * Return the mail configuration of the exception handler.
     * @return a mailconfig object.
     */

    MailConfig getMailConfig() {
        StringBuffer strerror=new StringBuffer();
        boolean send         =Boolean.valueOf(
                                      (String) properties_.get("rule.mail.send"))
               .booleanValue();
        if(send==true) {
            String from   =(String) properties_.getProperty("rule.mail.from");
            String to     =(String) properties_.get("rule.mail.to");
            Perl5Util perl=new Perl5Util();
            Vector v      =new Vector();
            try {
                perl.split(v, "/,/", to);
            } catch(MalformedPerl5PatternException e) {
                strerror.append("Wrong Perl5 pattern" + e.getMessage() + 
                                "\n");
            }
            String[] tos=new String[v.size()];
            int i       =0;
            for(Enumeration enum=v.elements(); enum.hasMoreElements();) {
                String ele=(String) enum.nextElement();
                tos[i++]=ele.trim();
            }
            String host      =(String) properties_.getProperty("rule.mail.host");
            MailConfig config=new MailConfig(tos, from, host, send);
            return config;
        } else {
            MailConfig config=new MailConfig(null, null, null, false);
            return config;
        }
    }


    /**
     * Check if the properties are valid.
     * @exception PFConfigurationException if any property is invalid.
     */
    void checkProperties() throws PFConfigurationException {
        initialised_=false;
        try {
            checkGeneralProps();
            checkMailProps();
            checkRulesProps();
        } catch(PFConfigurationException e) {
            throw new PFConfigurationException(e.getMessage(), e.getCause());
        }
        initialised_=true;
    }


    /**
     * Initialise the propertymanager. Load and read the propertyfile
     * into an internal Properties object.
     * @exception PFConfigurationException if any errors occur.
     */
    void init(String propfile) throws PFConfigurationException {
        initialised_=false;
        file_       =new File(propfile);
        FileInputStream istream=null;
        try {
            istream=new FileInputStream(file_);
        } catch(FileNotFoundException e) {
            throw new PFConfigurationException("File Not Found :" + propfile, e);
        }
        try {
            properties_=new Properties();
            properties_.load(istream);
        } catch(IOException e) {
            throw new PFConfigurationException("Could Not Load Properties ", e);
        }
        mtime_=file_.lastModified();
    }


    /**
     * Check if property file changed since last reading.
     * @return true if propertyfile changed, else false.
     */
    boolean needsReinitialisation() {
        if(file_==null)
            return false;
        long cmtime=file_.lastModified();
        if(cmtime>mtime_)
            return true;
        return false;
    }

    /**
     * Set the saved modification time to the current mod. time of the property file.
     */
    void resetModTime() {
        mtime_=file_.lastModified();
    }

    /**
     * Read and check all gerneral properties.
     * @exception PFConfiguartionException if any property is inavlid.
     */
    private void checkGeneralProps() throws PFConfigurationException {
        // read general rules in form rule.general...
        String currentrule   ="rule.general.";
        String useme         =properties_.getProperty(currentrule + "use_me", 
                                                      "");
        
        if(useme.equals("")) {
            throw new PFConfigurationException(currentrule+"'use_me' not found\n", null);
        } else {
            Boolean.valueOf(useme).booleanValue();
        }
        String schedule=properties_.getProperty(
                                currentrule + "cleanuptask_schedule", "");
        if(schedule.equals("")) {
            throw new PFConfigurationException(currentrule+"'cleanuptask_schedule' not found\n", null);
        } else {
            try {
                Integer.parseInt(schedule);
            } catch(NumberFormatException e) {
                throw new PFConfigurationException(currentrule+"'cleanuptask_schedule' is not an int: "+schedule, e);
            }
        }
        String cleanupscheduledim=properties_.getProperty(
                                          currentrule + "cleanuptask_dim", "");
        if(cleanupscheduledim.equals("")) {
            throw new PFConfigurationException(currentrule+"'cleanuptask_dim' not found'", null);
        }
        if(!validdimensions.containsKey(cleanupscheduledim)) {
            throw new PFConfigurationException(currentrule+"'cleanuptask_dim' not valid", null);
        }
        
        String straceob=properties_.getProperty(
                                currentrule + "stracech_obsolete", "");
        if(straceob.equals("")) {
            throw new PFConfigurationException(currentrule+"'stracech_obsolete' not found", null);
        } else {
            try {
                Integer.parseInt(straceob);
            } catch(NumberFormatException e) {
                throw new PFConfigurationException(currentrule+"'stracech_obsolete' is not an int: "+straceob, null);
            }
        }
        String straceobsoletedim=properties_.getProperty(
                                         currentrule + 
                                         "stracech_obsolete_dim", "");
        if(straceobsoletedim.equals("")) {
            throw new PFConfigurationException(currentrule+"'stracech_obsolete_dim' not found", null);
        }
        if(!validdimensions.containsKey(straceobsoletedim)) {
            throw new PFConfigurationException(currentrule+"''stracech_obsolete_dim' not valid", null);
        }
        
        String rep=properties_.getProperty(currentrule + 
                                           "reporttask_schedule", "");
        if(rep.equals("")) {
            throw new PFConfigurationException(currentrule+"'reporttask_schedule' not found", null);
        } else {
            try {
                Integer.parseInt(rep);
            } catch(NumberFormatException e) {
                throw new PFConfigurationException(currentrule+"'reporttask_schedule' is not an int:"+rep, null);  
            }
        }
        String reportscheduledim=properties_.getProperty(
                                         currentrule + "reporttask_dim", "");
        if(reportscheduledim.equals("")) {
            throw new PFConfigurationException(currentrule+"'reporttask_dim' not found", null);
        }
        if(!validdimensions.containsKey(reportscheduledim)) {
            throw new PFConfigurationException(currentrule+"'reporttask_dim' not valid", null);
        }
    }

    /**
     * Read and check all mail properties. 
     * @exception PFConfigurationException if any property is invalid.
     */
    private void checkMailProps() throws PFConfigurationException {
        String currentrule   ="rule.mail.";
        String send          =properties_.getProperty(currentrule + "send", "");
        if(send.equals("")) {
            throw new PFConfigurationException(currentrule+"'send' not found", null);
        }
        if(! send.equals("true")) {
            return;
        }
        String from=properties_.getProperty(currentrule + "from", "");
        if(from.equals("")) {
            throw new PFConfigurationException(currentrule+"'from' not found", null);
        }
        String to=properties_.getProperty(currentrule + "to", "");
        if(to.equals("")) {
            throw new PFConfigurationException(currentrule+"to not found", null);
        }
        Perl5Util perl=new Perl5Util();
        Vector v      =new Vector();
        try {
            perl.split(v, "/,/", to);
        } catch(MalformedPerl5PatternException e) {
            throw new PFConfigurationException(currentrule+"Wrong Perl5 pattern", e);
        }
        for(Enumeration enum=v.elements(); enum.hasMoreElements();) {
            String str=(String) enum.nextElement();
            try {
                new InternetAddress(str.trim());
            } catch(AddressException ex) {
                throw new PFConfigurationException(currentrule+str.trim()+"is not a valid address", ex);
            }
        }
        String host=properties_.getProperty(currentrule + "host");
        if(host.equals("")) {
            throw new PFConfigurationException(currentrule+"host not found", null);
        }
    }

    /**
     * Read and check all exception properties.
     * @exception PFConfigurationException if any property is invalid.
     */
    private void checkRulesProps() throws PFConfigurationException {
        boolean end          =false;
        int key              =1;
        // read rules in form rule.N...
        while(! end) {
            String currentrule="rule." + key + ".";
            String ty         =properties_.getProperty(currentrule + "type", "");
            if(ty.equals("")) {
                end=true;
                continue;
            }
            if(! ty.equals("")) {
                try {
                    Class clazz=Class.forName(ty);
                    if(clazz==null) {
                        throw new PFConfigurationException(currentrule+"'type' not found", null);
                    }
                } catch(ClassNotFoundException e) {
                    throw new PFConfigurationException("class Not Found for "+currentrule+ ty, e);
                }
            }
            String ma=properties_.getProperty(currentrule + "match", "");
            if(ma.equals("")) {
                throw new PFConfigurationException(currentrule + " 'match' not found", null);
            }
            String li=properties_.getProperty(currentrule + "limit", "");
            if(li.equals("")) {
                throw new PFConfigurationException(currentrule + "'limit' not found", null);
            } else {
                try {
                    Integer.parseInt(li);
                } catch(NumberFormatException e) {
                    throw new PFConfigurationException(currentrule+li+": limit is not an int: ", null);
                }
            }
            String di=properties_.getProperty(currentrule + "dim", "");
            if(di.equals("")) {
                throw new PFConfigurationException(currentrule+"'dim' not found", null);
            }
            if(!validdimensions.containsKey(di)) {
                throw new PFConfigurationException(currentrule+"'dim' is not valid", null);   
            }   
            String bu=properties_.getProperty(currentrule + "burst", "");
            if(bu.equals("")) {
                throw new PFConfigurationException(currentrule+"'burst' not found", null);
            } else {
                try {
                    Integer.parseInt(bu);
                } catch(NumberFormatException e) {
                    throw new PFConfigurationException(currentrule+bu+": is not an int", e); 
                }
            }
            if(! end) {
                exrulecount_=key++;
            }
        }
    }
}