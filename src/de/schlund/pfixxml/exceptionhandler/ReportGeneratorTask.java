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

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Enumeration;
import java.util.Locale;
import java.util.TimeZone;
import java.util.TimerTask;
import java.util.Vector;

import de.schlund.pfixcore.util.email.EmailSender;
import de.schlund.pfixcore.util.email.EmailSenderException;


/**
 * Generates a report mail by collecting data from all 
 * instancecheckercontainer.
 * 
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
class ReportGeneratorTask extends TimerTask {

    //~ Instance/static variables ..............................................

    private Vector icheckers_=null;
    private String info_     =null;
    private PFUtil pfutil_   =null;
    private final String COUNTRY_ ="DE";
    private final String LANGUAGE_="de";
    private final String TIMEZONE_="Europe/Berlin";

    //~ Constructors ...........................................................

    /**
    * Create a new ReportGenerator.
    * @param icheckers a Vector of <see>InstanceContainer</see> objects.
    * @param info a String to indicate the kind of report.
    */
    ReportGeneratorTask(Vector icheckers, String info) {
        this.icheckers_=icheckers;
        this.info_     =info;
        pfutil_        =PFUtil.getInstance();
    }

    //~ Methods ................................................................

    /** 
    * @see java.lang.Runnable#run()
    */
    public void run() {
        Thread.currentThread().setName("ReportGeneratorTask-Thread");
        pfutil_.debug("ReportGeneratorTask (" + info_ + ") started.");
        String buf=createReport();
        if(buf!=null) {
            TimeZone tz        =TimeZone.getTimeZone(TIMEZONE_);
            Locale loc         =new Locale(LANGUAGE_, COUNTRY_);
            Calendar cal       =Calendar.getInstance(tz, loc);
            SimpleDateFormat df=new SimpleDateFormat("H:mm:ss");
            String date        =df.format(cal.getTime());
            String subject = date + ":Report of collected exceptions (" + info_ + ")";
            MailConfig mailconfig = MailConfig.getInstance();
            try {
                EmailSender.sendMail(subject, buf.toString(), 
                                    mailconfig.getTo(),
                                    mailconfig.getFrom(),
                                    mailconfig.getHost());
            } catch (EmailSenderException e) {
                pfutil_.fatal("Sending of errormail failed!!! "+e.getMessage());
            }
            pfutil_.debug("ReportGeneratorTask (" + info_ + ") mail sent");
        } else
            pfutil_.debug(" ReportGeneratorTask: no need to send mail");
        pfutil_.debug("ReportGeneratorTask (" + info_ + ") ended.");
    }

    /**
    * Create Report.
    * @return text containing report data.
    */
    private String createReport() {
        StringBuffer buf=new StringBuffer();
        boolean doit    =false;
        for(Enumeration enum=icheckers_.elements(); enum.hasMoreElements();) {
            InstanceCheckerContainer ich=(InstanceCheckerContainer) enum.nextElement();
            String b                    =ich.getReport();
            if(b!=null) {
                buf.append(b + "\n");
                doit=true;
            }
        }
        if(doit)
            return buf.toString();
        return null;
    }
} // ReportGeneratorTask