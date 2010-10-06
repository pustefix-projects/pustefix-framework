package de.schlund.pfixcore.auth.conditions;

import de.schlund.pfixcore.auth.Condition;
import de.schlund.pfixcore.workflow.Context;

/**
 * Dumb condition returning a boolean as specified.
 * @author ffray
 *
 */
public final class BooleanCondition implements Condition {

    public static final BooleanCondition TRUE_CONDITION = new BooleanCondition(true);
    public static final BooleanCondition FALSE_CONDITION = new BooleanCondition(false);

    private final boolean result;

    public BooleanCondition(boolean result) {
        this.result = result;
    }

    /**
     * @returns the boolean value which has been specified via the constructor.
     */
    public boolean evaluate(Context context) {
        return result;
    }
}