package org.pustefixframework.util.i18n;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

public class PODataTest extends TestCase {

    public void test() {

        List<POMessage> messages = new ArrayList<>();
        messages.add(new POMessage("hello", "hallo"));
        messages.add(new POMessage("context", "day", "days", new String[] {"Tag", "Tage"}));
        Map<String, String> headers = new HashMap<>();
        headers.put("Plural-Forms", "nplurals=2; plural=n != 1;");
        POData data = new POData(messages, headers);

        assertEquals("hallo", data.getText("hello"));
        assertEquals("Tage", data.getText("day", "days", 0));
        assertEquals("Tag", data.getText("day", "days", 1));
        assertEquals("Tage", data.getText("day", "days", 2));
        assertEquals("years", data.getText("year", "years", 0));
    }

    public void testPluralFormExpressions() throws Exception {

        POData.PluralForms plural = new POData.PluralForms("nplurals=2; plural=n == 1 ? 0 : 1;");
        assertEquals(1, plural.getIndex(0));
        assertEquals(0, plural.getIndex(1));
        assertEquals(1, plural.getIndex(2));

        plural = new POData.PluralForms("nplurals=2; plural=n != 1;");
        assertEquals(1, plural.getIndex(0));
        assertEquals(0, plural.getIndex(1));
        assertEquals(1, plural.getIndex(2));

        plural = new POData.PluralForms("nplurals=1; plural=0;");
        assertEquals(0, plural.getIndex(0));
        assertEquals(0, plural.getIndex(1));
        assertEquals(0, plural.getIndex(2));

        plural = new POData.PluralForms(
                "nplurals=6; plural=n==0 ? 0 : n==1 ? 1 : n==2 ? 2 : n%100>=3 && n%100<=10 ? 3 : n%100>=11 ? 4 : 5;");
        assertEquals(0, plural.getIndex(0));
        assertEquals(1, plural.getIndex(1));
        assertEquals(2, plural.getIndex(2));
        assertEquals(3, plural.getIndex(3));
        assertEquals(3, plural.getIndex(103));
        assertEquals(3, plural.getIndex(1405));
        assertEquals(3, plural.getIndex(23409));
        assertEquals(4, plural.getIndex(11));
        assertEquals(4, plural.getIndex(1099));
        assertEquals(4, plural.getIndex(278));
        assertEquals(5, plural.getIndex(100));
        assertEquals(5, plural.getIndex(202));
        assertEquals(5, plural.getIndex(3001));

        plural = new POData.PluralForms(
                "nplurals=6; plural=n==0 ? 5 : n==1 ? 0 : n==2 ? 1 : n%100>=3 && n%100<=10 ? 2 : n%100>=11 ? 3 : 4");
        assertEquals(5, plural.getIndex(0));
        assertEquals(0, plural.getIndex(1));
        assertEquals(1, plural.getIndex(2));
        assertEquals(4, plural.getIndex(3001));
    }

}
