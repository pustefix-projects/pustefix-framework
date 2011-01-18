package org.pustefixframework.http;

import junit.framework.TestCase;

public class BotDetectorTest extends TestCase {
    
    public void testBotDetection() {
        
        String googleBot = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)";
        String yandexBot = "Mozilla/5.0 (compatible; YandexBot/3.0; MirrorDetector; +http://yandex.com/bots)";
        String yahooBot = "Mozilla/5.0 (compatible; Yahoo! Slurp; http://help.yahoo.com/help/us/ysearch/slurp)";
        String bingBot = "Mozilla/5.0 (compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm)";
        String baiduBot = "Baiduspider+(+http://www.baidu.com/search/spider.htm)";
        String msnBot = "msnbot/2.0b (+http://search.msn.com/msnbot.htm)._";
        String alexaBot = "ia_archiver (+http://www.alexa.com/site/help/webmasters; crawler@alexa.com)";
        
        String ffWin = "Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.9.2.3) Gecko/20100401 Firefox/3.6.3";
        String ffLin = "Mozilla/5.0 (X11; U; Linux i686; de; rv:1.9.2.13) Gecko/20101206 Ubuntu/10.10 (maverick) Firefox/3.6.13";
        String ieWin = "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Mozilla/4.0 (compatible; MSIE 7.0; Win32; 1&1); .NET CLR 3.5.30729)";
        
        assertTrue(BotDetector.isBot(googleBot));
        assertTrue(BotDetector.isBot(yandexBot));
        assertTrue(BotDetector.isBot(yahooBot));
        assertTrue(BotDetector.isBot(bingBot));
        assertTrue(BotDetector.isBot(baiduBot));
        assertTrue(BotDetector.isBot(msnBot));
        assertTrue(BotDetector.isBot(alexaBot));
        
        assertTrue(!BotDetector.isBot(ffWin));
        assertTrue(!BotDetector.isBot(ffLin));
        assertTrue(!BotDetector.isBot(ieWin));
        
    }
    
}
