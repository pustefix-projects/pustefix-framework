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

package de.schlund.pfixxml.loader;

import java.util.*;
import org.apache.log4j.Category;

/**
 * CommandProcessor.java 
 * 
 * Created: 29.04.2003
 * 
 * @author mleidig
 */
public class CommandProcessor {

    private Category CAT=Category.getInstance(getClass().getName());

    public String process(String cmd) throws IllegalCommandException {
	   AppLoader loader=AppLoader.getInstance();
	   //cmd=cmd.trim().toLowerCase();
	   StringTokenizer st=new StringTokenizer(cmd," ");
	   if(st.countTokens()==0) throw new IllegalCommandException(IllegalCommandException.NO_CMD,cmd);
	   if(st.countTokens()>2) throw new IllegalCommandException(IllegalCommandException.ARG_NO,cmd);
	   String cmdStr=st.nextToken();
	   if(cmdStr.equals("reload")) {
	       if(st.hasMoreTokens()) throw new IllegalCommandException(IllegalCommandException.NO_ARG,cmd);
	       boolean reloaded=loader.reload();
           if(reloaded && StateTransfer.getInstance().getInconsistencyType()==AppLoader.INCONSISTENCY_PROBABLE) {
                HashSet classes=new HashSet();
                Iterator it=StateTransfer.getInstance().getExceptions(AppLoader.INCONSISTENCY_PROBABLE);
                while(it.hasNext()) {
                    StateTransferException ste=(StateTransferException)it.next();
                    String name=ste.getClassName();
                    classes.add(name);
                }
                StringBuffer sb=new StringBuffer();
                sb.append("[Warning]\nState consistency can't be ensured due to important changes affecting the following classes:\n");
                it=classes.iterator();
                while(it.hasNext()) {
                    sb.append("'"+it.next()+"'");
                    if(it.hasNext()) sb.append(", ");
                }
                sb.append("\nLook in the AppLoader's logfile for details. You are recommended to do a AppLoader restart.");
                return sb.toString();
           }
           
	   } else if(cmdStr.equals("restart")) {
	       if(st.hasMoreTokens()) throw new IllegalCommandException(IllegalCommandException.NO_ARG,cmd);
	       loader.restart();
	   } else if(cmdStr.equals("auto")) {
	       if(!st.hasMoreTokens()) throw new IllegalCommandException(IllegalCommandException.ARG_NO,cmd);
	       String argStr=st.nextToken();
	       if(argStr.equals("on")) {
		      loader.setTrigger(AppLoader.AUTO_TRIGGER);
	       } else if(argStr.equals("off")) {
		      loader.setTrigger(AppLoader.MANUAL_TRIGGER);
	       } else {
		      throw new IllegalCommandException(IllegalCommandException.ILL_ARG,cmd);
	       }
	   } else if(cmdStr.equals("interval")) {
	       if(!st.hasMoreTokens()) throw new IllegalCommandException(IllegalCommandException.ARG_NO,cmd);
	       String argStr=st.nextToken();
	       try {
		      int val=Integer.parseInt(argStr);
		      if(val<1 || val>Integer.MAX_VALUE) throw new IllegalCommandException(IllegalCommandException.ILL_ARG,cmd);
		      loader.setInterval(val);
	       } catch(NumberFormatException x) {
		      throw new IllegalCommandException(IllegalCommandException.ILL_ARG,cmd);
	       }
       } else if(cmdStr.equals("typecheck")) {
           Profiler profiler=new Profiler();
           profiler.doTypeCheck();
           return profiler.getTypeCheckInfo();
       } else if(cmdStr.equals("classinfo")) {
           if(!st.hasMoreTokens()) throw new IllegalCommandException(IllegalCommandException.ARG_NO,cmd);
           String argStr=st.nextToken();
           Profiler profiler=new Profiler();
           return profiler.getClassInfo(argStr);
	   } else {
	        throw new IllegalCommandException(IllegalCommandException.INV_CMD,cmd);
	   }
       return null;
    }
   
}
