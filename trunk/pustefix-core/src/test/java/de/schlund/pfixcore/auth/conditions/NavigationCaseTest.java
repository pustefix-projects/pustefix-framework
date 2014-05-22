package de.schlund.pfixcore.auth.conditions;

import junit.framework.Assert;

import org.junit.Test;

import de.schlund.pfixcore.auth.Condition;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextImpl;

public class NavigationCaseTest {

    @Test
    public void testProperties() {
        final String pageName = "page";
        final Condition condition = BooleanCondition.FALSE_CONDITION;
        NavigationCase navcase = new NavigationCase(pageName);
        navcase.setCondition(condition);
        
        Assert.assertEquals(pageName, navcase.getPage());
        Assert.assertEquals(condition, navcase.getCondition());
    }

    @Test
    public void testEvaluate() {
        testCondition(new NavigationCase("foo"), BooleanCondition.TRUE_CONDITION);
        testCondition(new NavigationCase("foo"), BooleanCondition.FALSE_CONDITION);
    }

    private void testCondition(NavigationCase navcase, Condition condition) {
        navcase.setCondition(condition);
        Context context = new ContextImpl();
        Assert.assertEquals(condition.evaluate(context), navcase.evaluate(context));
    }

}
