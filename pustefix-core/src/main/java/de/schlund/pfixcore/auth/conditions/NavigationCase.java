package de.schlund.pfixcore.auth.conditions;

import de.schlund.pfixcore.auth.Condition;
import de.schlund.pfixcore.workflow.Context;

public class NavigationCase implements Condition {

    private String page;
    private Condition condition;
    
    public NavigationCase(String page) {
        if (page == null || page.equals("")) {
            throw new IllegalArgumentException("Parameter 'page' must not be null or empty!");
        }
        this.page = page;
    }

    public String getPage() {
        return page;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    @Override
    public boolean evaluate(Context context) {
        if (condition == null) {
            return true;
        } else {
            return condition.evaluate(context);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("navigation");
        sb.append("{");
        sb.append("page=" + page);
        sb.append("}[");
        sb.append(condition);
        sb.append("]");
        return sb.toString();
    }

}
