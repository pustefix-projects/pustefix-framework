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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Category;
/**
 * 
 * This class handles incoming exceptions which can be checked by their type or
 * additionally by their stacktrace. <br/>
 * 
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker </a>
 */
class InstanceCheckerContainer {
	//~ Instance/static variables
	// ..............................................
	private int burst_ = 0;
	private Class clazz_ = null;
	private Integer code_ = null;
	private int currentburst_ = 0;
	private int fullcount_ = 0;
	private int limit_ = 0;
	private String limitdim_ = null;
	private String match_ = null;
	private int obsolete_ = 0;
	private String obsoletedim_ = null;
	private int oldcurrentburst_ = 0;
	private PFUtil pfutil_ = null;
	private long startmilli_ = 0;
	private Hashtable stracecontainerhash_ = null;
	private String type_ = null;
	private static Category CAT = Category
			.getInstance(InstanceCheckerContainer.class.getName());
	//~ Constructors
	// ...........................................................
	/**
	 * Create a new InstanceCheckerContainer object.
	 * 
	 * @param type
	 *                 the type of the exception against incoming exceptions will be
	 *                 checked.
	 * @param match
	 *                 determines if incoming exception should be checked only
	 *                 against the <see>type </see> or also against their
	 *                 stacktrace. If stacktraces should be checked the containing
	 *                 <see>StraceCheckerContainer </see> will be asked if they
	 *                 match the incoming exception.
	 * @param limit
	 *                 a number which specifies the maximum average of matches the
	 *                 allow per time rate <see>limit_dim </see>.
	 * @param limit_dim
	 *                 a String specifing the time rate <see>limit </see>.
	 * @param burst
	 *                 a number indicating the maximum burst before the above limit
	 *                 kicks in.
	 */
	InstanceCheckerContainer(String type, String match, int limit,
			String limitdim, int burst, int ob, String obdim) {
		this.type_ = type;
		this.match_ = match;
		this.limit_ = limit;
		this.burst_ = burst;
		this.limitdim_ = limitdim;
		this.obsolete_ = ob;
		this.obsoletedim_ = obdim;
		pfutil_ = PFUtil.getInstance();
		stracecontainerhash_ = new Hashtable();
		init();
	}
	//~ Methods
	// ................................................................
	/**
	 * Return the current burst of this instancecheckercontainer.
	 * 
	 * @return the current burst.
	 */
	int getCurrentburst() {
		if (match_.equals("strace")) {
			STraceCheckerContainer ch = null;
			if (stracecontainerhash_.containsKey(code_)) {
				ch = (STraceCheckerContainer) stracecontainerhash_.get(code_);
				return ch.getCurrentburst();
			}
		} else {
			return currentburst_;
		}
		return 0;
	}
	/**
	 * Return the number of not matched exceptions of this
	 * instancecheckercontainer and reset it.
	 * 
	 * @return the fullcount.
	 */
	int getFullcountnReset() {
		int tmp = 0;
		tmp = fullcount_;
		fullcount_ = 0;
		return tmp;
	}
	/**
	 * Retrieve report data. This is called from the <see>ReportGeneratorTask
	 * </see>-Thread so it must be synchronized because the main thread could
	 * modify the fullcount variable in the
	 * <see>InstanceCheckerContainer#doesMatch(Exception) </see> method.
	 */
	synchronized ArrayList getReports() {
		Hashtable hash = stracecontainerhash_;
		//int full = 0;
		//int count = 0;
		ArrayList reports = new ArrayList();
		//StringBuffer buf = new StringBuffer();
		if (hash.isEmpty()) { // NO stracecheckers exist
			int full = 0;
			if ((full = getFullcountnReset()) > 0) {
				String subject = getType() + "(" + full + ")";
				Report report = new Report(subject, "Stacktrace not available",
						full);
				reports.add(report);
			}
		} else {
			for (Enumeration enu = hash.keys(); enu.hasMoreElements();) {
				Integer key = (Integer) enu.nextElement();
				STraceCheckerContainer sch = (STraceCheckerContainer) hash
						.get(key);
				int full = 0;
				if ((full = sch.getFullcountnReset()) > 0) {
					//buf.append(getType() + " repeated " + full + " times
					// (Stacktrace as follows).\n");
					String message = sch.getMessage();
					StackTraceElement[] strace = sch.getStrace();
					if (message == null) {
						if (strace.length > 1) {
							message = strace[1].toString().trim();
						} else if (strace.length == 1) {
							// This case can happen when handling a
							// OutofMemoryError, where
							// the stracktrace has a length of only 1.
							message = strace[0].toString().trim();
						} else {
							// what's this?
							message = "No information found.";
						}
					}
					StringBuffer text = new StringBuffer();
					for (int i = 0; i < strace.length; i++) {
						text.append(" -> " + strace[i] + "\n");
					}
					text.append("\n");
					Report rep = new Report(getType() + ":" + message + "("
							+ full + ")", text.toString(), full);
					reports.add(rep);
				}
			}
		}
		//pfutil_.debug("InstanceChecker("+type_+")#getReport: found " + full
		// + " collected exceptions");
		return reports;
	}
	/**
	 * Return all STraceCheckerContainer of this instancecheckercontainer.
	 * 
	 * @return the stracecheckercontainers.
	 */
	Hashtable getSTraceCheckers() {
		return stracecontainerhash_;
	}
	/**
	 * Return the match-type of this instancecheckercontainer.
	 * 
	 * @return the type.
	 */
	String getType() {
		return type_;
	}
	/**
	 * Checks if the incoming exception will met the prerequisites. If for
	 * <see>match </see> attribute the exception type is given it looks if the
	 * incoming exception is an instance of the stored exception type specified
	 * by the <see>type </see> parameter. If for the <see>match </see>
	 * attribute the stacktrace is given, the containing
	 * <see>STraceCheckerContainer </see>will be asked if they match ( the
	 * exception type will be checked too o.c.). If no
	 * <see>STraceCheckerContainer </see> matches a new one will be created
	 * with the stracktrace of the current exception.
	 * 
	 * @param th
	 *                 the incomimg throwable.
	 * @return an int a result. Returns NO_MATCH if the exception type is
	 *             unkown. Returns FULL if the own limit or the limit of a
	 *             <see>STraceCheckerContainer </see> is exeeded. Returns MATCH if
	 *             itself or an <see>STraceCheckerContainer </see> is not exceeded.
	 *             Returns TRIGGER_MATCH if limit is not exceeded but the
	 *             generation of a report should be triggered.
	 */
	synchronized int doesMatch(Throwable th) {
		long nowmilli = 0;
		int diff = 0;
		int rate = 0;
		float sdiff = 0;
		int ds = 0;
		int status = PFUtil.FULL;
		PFUtil pfutil = PFUtil.getInstance();
		if (!clazz_.isInstance(th)) {
			status = PFUtil.NO_MATCH;
			return status;
		}
		if (match_.equals("strace")) {
			StackTraceElement[] strace = th.getStackTrace();
            code_ = new Integer(pfutil.getSTraceHashCode(strace));
			int tmpstat = PFUtil.NO_MATCH;
			// key found - call stracecheckercontainer.doesMatch()
			if (stracecontainerhash_.containsKey(code_)) {
				pfutil
						.debug("InstanceChecker: STraceChecker with strace.hashcode found! Calling STraceChecker.doesMatch().");
				STraceCheckerContainer checker = (STraceCheckerContainer) stracecontainerhash_
						.get(code_);
				tmpstat = checker.doesMatch();
				if (tmpstat == PFUtil.MATCH) {
					return tmpstat;
				}
				if (tmpstat == PFUtil.TRIGGER_MATCH) {
					return tmpstat;
				}
				if (tmpstat == PFUtil.FULL) {
					return tmpstat;
				}
			} // key not found - create a new stracechecker
			else {
				pfutil
						.debug("InstaceChecker: STraceChecker with strace.hashcode not found! Creating a new one.");
				STraceCheckerContainer ch = new STraceCheckerContainer(limit_,
						limitdim_, burst_);
				ch.setStrace(strace);
                
				ch.setMessage(th.getMessage());
				stracecontainerhash_.put(code_, ch);
				tmpstat = PFUtil.MATCH;
				return tmpstat;
			}
		}
		nowmilli = System.currentTimeMillis();
		diff = new Long(nowmilli - startmilli_).intValue();
		rate = pfutil.getRate(limitdim_);
		sdiff = limit_ * diff / rate;
		ds = new Double(Math.ceil(sdiff)).intValue();
		currentburst_ += ds;
		PFUtil.getInstance().debug(
				"STraceChecker: -->\nlastmatch=" + startmilli_ + "\n now="
						+ nowmilli + "\n diff=" + diff + "\n sdiff=" + sdiff
						+ "\n ds=" + ds + " currentburst=" + currentburst_);
		if (currentburst_ > burst_)
			currentburst_ = burst_;
		if (currentburst_ > 0) {
			currentburst_--;
			startmilli_ = System.currentTimeMillis();
			status = PFUtil.MATCH;
			if (oldcurrentburst_ == 0) {
				status = PFUtil.TRIGGER_MATCH;
			}
			oldcurrentburst_ = currentburst_;
		} else {
			fullcount_++;
			status = PFUtil.FULL;
		}
		pfutil.debug("InstanceChecker: currentburst: " + currentburst_);
		return status;
	}
	/**
	 * Remove the stracecheckerscontainers of this instancecheckercontainer if
	 * they did not match too long.
	 * 
	 * @return the number of removed STraceCheckerContainer.
	 */
	synchronized int removeObsoleteCheckers() {
		// NOTE: This method is called from the
		// <see>STraceCleanupTask</see>-thread, so it must be
		//synchronized because another thread might want to use the
		// <see>InstanceCheckerContainer</see>
		// objects a the same time.
		Hashtable scheckers = stracecontainerhash_;
		Integer i = null;
		STraceCheckerContainer schecker = null;
		int removed = 0;
		for (Enumeration enu = scheckers.keys(); enu.hasMoreElements();) {
			i = (Integer) enu.nextElement();
			schecker = (STraceCheckerContainer) scheckers.get(i);
			int last = new Double(schecker.getLastMatch()
					/ pfutil_.getRate(obsoletedim_)).intValue();
			if (last >= obsolete_ * pfutil_.getRate(obsoletedim_)) {
				pfutil_.debug("RemoveSTrace Checker: lastmatch=" + last
						+ " >= " + obsolete_ * pfutil_.getRate(obsoletedim_));
				scheckers.remove(i);
				removed++;
			}
		}
		return removed;
	}
	/**
	 * Initialises internal objects
	 */
	private void init() {
		currentburst_ = burst_;
		oldcurrentburst_ = currentburst_;
		startmilli_ = System.currentTimeMillis();
		try {
			clazz_ = Class.forName(type_);
		} catch (ClassNotFoundException e) {
			CAT.error("ERROR IN EXCEPTIONHANDLER!!!!!");
			CAT.error("--->" + type_ + "-->" + e.getClass().getName() + ":"
					+ e.getMessage());
			//This should never happen
		}
	}
} //InstanceCheckerContainer
