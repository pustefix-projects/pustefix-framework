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
class GeneralConfig {

    //~ Instance/static variables ..............................................

    private int cleanupschedule_      =0;
    private String cleanupscheduledim_=null;
    private int reportschedule_       =0;
    private String reportscheduledim_ =null;
    private int straceobsolete_       =0;
    private String straceobsoletedim_ =null;
    private boolean useme_            =false;

    //~ Constructors ...........................................................

    GeneralConfig(boolean useme, int clsched, String clscheddim, int repsched, 
                  String repscheddim, int stobsolete, String stobsoletedim) {
        this.useme_             =useme;
        this.cleanupschedule_   =clsched;
        this.cleanupscheduledim_=clscheddim;
        this.reportschedule_    =repsched;
        this.reportscheduledim_ =repscheddim;
        this.straceobsolete_    =stobsolete;
        this.straceobsoletedim_ =stobsoletedim;
    }

    //~ Methods ................................................................

    /**
     * Returns the cleanupschedule.
     * @return int
     */
    int getCleanupSchedule() {
        return cleanupschedule_;
    }

    /**
     * Returns the cleanupscheduledim.
     * @return String
     */
    String getCleanupScheduledim() {
        return cleanupscheduledim_;
    }

    int getReportSchedule() {
        return reportschedule_;
    }

    /**
     * Returns the reportscheduledim.
     * @return String
     */
    String getReportScheduledim() {
        return reportscheduledim_;
    }

    /**
     * Returns the straceobsolete.
     * @return int
     */
    int getStraceObsolete() {
        return straceobsolete_;
    }

    /**
     * Returns the straceobsoletedim.
     * @return String
     */
    String getStraceObsoleteDim() {
        return straceobsoletedim_;
    }

    /**
     * Returns the useme.
     * @return boolean
     */
    boolean isUseme() {
        return useme_;
    }
}