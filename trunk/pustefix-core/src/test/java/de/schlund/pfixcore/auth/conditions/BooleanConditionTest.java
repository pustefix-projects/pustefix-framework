package de.schlund.pfixcore.auth.conditions;

import junit.framework.Assert;

import org.junit.Test;


public class BooleanConditionTest {

    @Test
    public void testConditions() {
        Assert.assertEquals(true, BooleanCondition.TRUE_CONDITION.evaluate(null));
        Assert.assertEquals(false, BooleanCondition.FALSE_CONDITION.evaluate(null));
    }

}
