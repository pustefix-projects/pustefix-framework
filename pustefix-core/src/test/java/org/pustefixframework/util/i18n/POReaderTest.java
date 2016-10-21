package org.pustefixframework.util.i18n;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

public class POReaderTest extends TestCase {

    public void test() throws IOException {

        InputStream input = getClass().getResourceAsStream("messages.po");
        POReader reader = new POReader();
        POData messages = reader.read(input, "utf8");
        assertEquals("Unbekannter Systemfehler", messages.getText("Unknown system error"));
        assertEquals("%d fataler Fehler gefunden", messages.getText("found %d fatal error"));
        assertEquals("%d fataler Fehler gefunden", messages.getText("found %d fatal error", "found %d fatal errors", 1));
        assertEquals("%d fatale Fehler gefunden", messages.getText("found %d fatal error", "found %d fatal errors", 0));
        assertEquals("%d fatale Fehler gefunden", messages.getText("found %d fatal error", "found %d fatal errors", 2));
        assertEquals("unknown", messages.getText("unknown"));
        assertEquals("unknown", messages.getText("unknown", "unknown plural", 1));
        assertEquals("unknown plural", messages.getText("unknown", "unknown plural", 0));
        assertEquals("unknown plural", messages.getText("unknown", "unknown plural", 2));
        assertEquals("Mehrzeiliges\nBeispiel\n", messages.getText("multiline\nexample\n"));
        assertEquals("FOO {0}", messages.getText("foo"));
        assertEquals("BAR", messages.getText("bar"));
        assertEquals("BAZ", messages.getText("baz"));
        assertEquals("TEST", messages.getText("test"));
    }

    public void testUnescape() {

        POReader reader = new POReader();
        assertEquals("foo", reader.unescape("foo"));
        assertEquals("f", reader.unescape("f"));
        assertEquals("", reader.unescape(""));
        assertEquals("\n", reader.unescape("\\n"));
        assertEquals("\\", reader.unescape("\\\\"));
        assertEquals("\"", reader.unescape("\\\""));
        assertEquals("\ta\nb\\", reader.unescape("\\ta\\nb\\\\"));
    }

}

