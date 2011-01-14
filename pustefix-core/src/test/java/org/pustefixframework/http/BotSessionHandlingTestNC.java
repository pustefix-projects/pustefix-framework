package org.pustefixframework.http;

public class BotSessionHandlingTestNC extends BotSessionHandlingTest {
    
    static {
        HTTP_PORT = findFreePort();
        HTTPS_PORT = findFreePort();
        try {
            server = createServer(HTTP_PORT, HTTPS_PORT, BotSessionTrackingStrategy.class, true);
        } catch(Exception x) {
            throw new RuntimeException("Error creating embedded server", x);
        }
    }
    
}
