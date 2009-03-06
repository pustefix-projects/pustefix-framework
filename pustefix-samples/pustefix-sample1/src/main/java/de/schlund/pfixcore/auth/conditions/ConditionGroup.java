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
 */
package de.schlund.pfixcore.auth.conditions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.schlund.pfixcore.auth.Condition;
import de.schlund.pfixcore.workflow.Context;

/**
 * 
 * @author mleidig@schlund.de
 * 
 */
public abstract class ConditionGroup implements Condition {

    protected List<Condition> conditions;

    public ConditionGroup() {
        conditions = new ArrayList<Condition>();
    }

    public ConditionGroup(Condition... conditions) {
        this();
        for (Condition condition : conditions)
            this.conditions.add(condition);
    }

    public void add(Condition condition) {
        conditions.add(condition);
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public abstract boolean evaluate(Context context);

    public abstract String getOperatorString();

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("( ");
        Iterator<Condition> it = conditions.iterator();
        while (it.hasNext()) {
            Condition condition = it.next();
            sb.append(condition);
            if (it.hasNext()) sb.append(" " + getOperatorString() + " ");
        }
        sb.append(" )");
        return sb.toString();
    }

}
