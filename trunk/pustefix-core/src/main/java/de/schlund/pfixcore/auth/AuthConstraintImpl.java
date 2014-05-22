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
 */
package de.schlund.pfixcore.auth;

import java.util.LinkedList;
import java.util.List;

import de.schlund.pfixcore.auth.conditions.NavigationCase;
import de.schlund.pfixcore.workflow.Context;

/**
 * 
 * @author mleidig@schlund.de
 * 
 */
public class AuthConstraintImpl implements AuthConstraint {

    private Condition condition;
    private String defaultAuthPage;
    private String id;
    private List<NavigationCase> navigation = new LinkedList<NavigationCase>();
    private boolean authJump = true;
    
    public AuthConstraintImpl(String id) {
        this.id = id;
    }
    
    public void addNavigationCase(NavigationCase navigationCase) {
        navigation.add(navigationCase);
    }

    public List<NavigationCase> getNavigation() {
        return navigation;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public Condition getCondition() {
        return condition;
    }

    public String getId() {
        return id;
    }

    public String getDefaultAuthPage() {
        return defaultAuthPage;
    }

    public void setDefaultAuthPage(String authPage) {
        this.defaultAuthPage = authPage;
    }

    public String getAuthPage(Context context) {
        for (NavigationCase navcase : navigation) {
            if (navcase.evaluate(context)) {
                return navcase.getPage();
            }
        }
        return defaultAuthPage;
    }
    
    public void setAuthJump(boolean authJump) {
        this.authJump = authJump;
    }

    public boolean getAuthJump() {
        return authJump;
    }
    
    public boolean isAuthorized(Context context) {
        return evaluate(context);
    }

    public boolean evaluate(Context context) {
        if (condition != null) {
            return condition.evaluate(context);
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("authconstraint");
        sb.append("{");
        sb.append("id=" + id);
        sb.append(",authpage=" + defaultAuthPage);
        sb.append(",authjump=" + authJump);
        sb.append("}");
        sb.append("[");
        sb.append(condition);
        sb.append("]");
        return sb.toString();
    }

}
