package org.pustefixframework.util.i18n;

import java.io.IOException;
import java.util.Locale;

import junit.framework.TestCase;

public class POMessageSourceTest extends TestCase {

    public void test() throws IOException {
        POMessageSource src = new POMessageSource();
        //src.setBasename("org/pustefixframework/util/i18n/messages");
        src.setBasenames("org/pustefixframework/util/i18n/messages", "org/pustefixframework/util/i18n/errors");
        Locale locale = new Locale("de","DE","foo");
        
        System.out.println(src.getMessage("foo", new String[] {"kkk"}, locale));
    }
    
}
