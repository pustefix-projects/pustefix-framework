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

package de.schlund.pfixcore.workflow;


import org.apache.log4j.Category;

import de.schlund.pfixxml.FactoryInitServlet;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author: jtl
 *
 */

public class FlowStepActionFactory {
    private static Category              CAT      = Category.getInstance(FlowStepActionFactory.class.getName());
    private static FlowStepActionFactory instance = new FlowStepActionFactory();
    private static String                JUMPTO   = "jumpto";
    
    private FlowStepActionFactory() {}
        
    public static FlowStepActionFactory getInstance() {
        return instance;
    }

    public FlowStepAction createAction(String action) {
        FlowStepAction act = null;
        if (action.equals(JUMPTO)) {
            act = new FlowStepJumpToAction();
        } else {
            try {
                Constructor constr = Class.forName(action).getConstructor(FactoryInitServlet.NO_CLASSES);
                act                = (FlowStepAction) constr.newInstance(FactoryInitServlet.NO_OBJECTS);
            } catch (InstantiationException e) {
                CAT.error("unable to instantiate class [" + action + "]", e);
            } catch (IllegalAccessException e) {
                CAT.error("unable access class [" + action + "]", e);
            } catch (ClassNotFoundException e) {
                CAT.error("unable to find class [" + action + "]", e);
            } catch (NoSuchMethodException e) {
                CAT.error("unable to find constructor in [" + action + "]", e);
            } catch (InvocationTargetException e) {
                CAT.error("unable to invoke constructor in [" + action + "]", e);
            } catch (ClassCastException e) {
                CAT.error("class [" + action + "] does not implement the interface FlowStepAction", e);
            }
        }
        return act;
    }
}
