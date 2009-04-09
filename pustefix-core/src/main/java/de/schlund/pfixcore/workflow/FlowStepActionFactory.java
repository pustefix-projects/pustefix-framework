/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package de.schlund.pfixcore.workflow;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;

/**
 * @author: jtl
 *
 */

public class FlowStepActionFactory {
    private final static Logger                LOG      = Logger.getLogger(FlowStepActionFactory.class);
    private static FlowStepActionFactory instance = new FlowStepActionFactory();
    private static String                JUMPTO   = "jumpto";
    private static String                SETFLOW  = "setflow";
    private static String                STOP     = "stop";
    
    private FlowStepActionFactory() {}
        
    public static FlowStepActionFactory getInstance() {
        return instance;
    }

    public FlowStepAction createAction(String action) {
        FlowStepAction act = null;
        if (action.equals(JUMPTO)) {
            act = new FlowStepJumpToAction();
        } else if (action.equals(SETFLOW)) {
            act = new FlowStepSetFlowAction();
        } else if (action.equals(STOP)) {
            act = new FlowStepForceStopAction();
        } else {
            try {
                Constructor<? extends FlowStepAction> constr = Class.forName(action).asSubclass(FlowStepAction.class).getConstructor();
                act = constr.newInstance((Object[])null);
            } catch (InstantiationException e) {
                LOG.error("unable to instantiate class [" + action + "]", e);
            } catch (IllegalAccessException e) {
                LOG.error("unable access class [" + action + "]", e);
            } catch (ClassNotFoundException e) {
                LOG.error("unable to find class [" + action + "]", e);
            } catch (NoSuchMethodException e) {
                LOG.error("unable to find constructor in [" + action + "]", e);
            } catch (InvocationTargetException e) {
                LOG.error("unable to invoke constructor in [" + action + "]", e);
            } catch (ClassCastException e) {
                LOG.error("class [" + action + "] does not implement the interface FlowStepAction", e);
            }
        }
        return act;
    }
}
