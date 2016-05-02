package de.schlund.pfixcore.auth;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import de.schlund.pfixcore.auth.conditions.BooleanCondition;
import de.schlund.pfixcore.auth.conditions.NavigationCase;
import de.schlund.pfixcore.workflow.ContextImpl;


@RunWith(Theories.class)
public class AuthConstraintImplTest {

    @DataPoints
    public static String[][] navAlternatives = {
        {},
        {"alt1of1"},
        {"alt1of2", "alt1of2"},
        {"alt1of10", "alt2of10", "alt3of10", "alt4of10", "alt5of10",
         "alt6of10", "alt7of10", "alt8of10", "alt9of10", "alt10of10"}};

    @Test
    public void testProperties() {
        final String id = "id";
        final Condition condition = BooleanCondition.TRUE_CONDITION;
        final String defaultAuthPage = "defPage";
        final String navPage1 = "navPage1";
        final String navPage2 = "navPage2";
        AuthConstraintImpl ac = new AuthConstraintImpl(id);
        ac.setCondition(condition);
        ac.setDefaultAuthPage(defaultAuthPage);
        int navCases = 0;
        
        ac.addNavigationCase(makeNavigationCase(navPage1, BooleanCondition.FALSE_CONDITION));
        navCases++;
        
        ac.addNavigationCase(makeNavigationCase(navPage2, BooleanCondition.FALSE_CONDITION));
        navCases++;
        
        Assert.assertEquals(id, ac.getId());
        Assert.assertEquals(condition, ac.getCondition());
        Assert.assertEquals(defaultAuthPage, ac.getDefaultAuthPage());
        List<NavigationCase> navigation = ac.getNavigation();
        Assert.assertEquals(navCases, navigation.size());
    }

    private NavigationCase makeNavigationCase(String page, Condition condition) {
        NavigationCase result = new NavigationCase(page);
        result.setCondition(condition);
        return result;
    }

    @Theory
    public void testNavigationAuthPage(String[] navAlternatives) {
        final String defPage = "defaultPage";

        AuthConstraintImpl ac = new AuthConstraintImpl("id");
        ac.setDefaultAuthPage(defPage);

        for (int i = 0; i < navAlternatives.length; ++i) {
            boolean lastNavCase = (i == (navAlternatives.length - 1));
            ac.addNavigationCase(makeNavigationCase(navAlternatives[i], new BooleanCondition(lastNavCase)));
        }

        // if there are no navigation-cases, the default-page must be returned
        if (navAlternatives.length == 0) {
            Assert.assertEquals(defPage, ac.getAuthPage(new ContextImpl()));
        } else {
            // otherwise the last nav-case has a condition evaluating to true,
            // thus the last nav-cases page must be returned
            Assert.assertEquals(navAlternatives[navAlternatives.length - 1], ac.getAuthPage(new ContextImpl()));
        }
    }
}
