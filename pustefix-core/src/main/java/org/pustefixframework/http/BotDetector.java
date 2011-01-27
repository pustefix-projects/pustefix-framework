package org.pustefixframework.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

/**
 * Detects if 'User-Agent' header comes from a well-known search engine bot.
 * This is done by matching regular expressions against the header. The
 * regular expressions are provided by a configuration file which is read
 * from the classpath. The list of regular expressions can be extended by
 * just putting additional files under the pre-defined META-INF path.
 * 
 * @author mleidig@schlund.de
 *
 */
public class BotDetector {

    private static String CONFIG = "META-INF" + File.separator + "org" + File.separator + "pustefixframework" + File.separator + "http" + File.separator + "bot-user-agents.txt";
    private static Pattern pattern = getBotPattern();
    
    public static boolean isBot(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if(userAgent == null) {
            return false;
        } else {
            return isBot(userAgent);
        }
    }
    
    public static boolean isBot(String userAgent) {
        return pattern.matcher(userAgent).matches();
    }
    
    private static Pattern getBotPattern() {
        //matching against one big composed pattern is a little bit faster and
        //less memory consuming than matching against each bot pattern individually
        StringBuilder sb = new StringBuilder();
        try {
            Enumeration<URL> urls = AbstractPustefixRequestHandler.class.getClassLoader().getResources(CONFIG);
            while(urls.hasMoreElements()) {
                URL url = urls.nextElement();
                InputStream in = url.openStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "utf8"));
                String line;
                while((line = reader.readLine()) != null) {
                    line = line.trim();
                    if(!line.startsWith("#") && !line.equals("")) {
                        if(sb.length() > 0) sb.append("|");
                        sb.append("(").append(line).append(")");
                    }
                }
                in.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading bot user-agent configuration", e);
        }
        return Pattern.compile(sb.toString());
    }
    
}
