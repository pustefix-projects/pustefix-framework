package org.pustefixframework.maven.plugins.deepl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.owasp.encoder.Encode;
import org.pustefixframework.maven.plugins.deepl.DeepLClientException;
import org.pustefixframework.maven.plugins.deepl.DeepLRestClient;
import org.pustefixframework.maven.plugins.deepl.DeepLRestClientException;
import org.pustefixframework.maven.plugins.deepl.DeepLServerException;
import org.pustefixframework.maven.plugins.deepl.Translation;
import org.pustefixframework.maven.plugins.deepl.Translations;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DeepLRestClientTest  {

    private static Tomcat tomcat;
    private static URL endpoint;

    @Test
    public void testExceptions() {

        DeepLClientException clientEx = assertThrows(DeepLClientException.class, () -> {
            DeepLRestClient client = new DeepLRestClient(endpoint, "invalid_auth_key");
            client.translate(DeepLRestClient.Language.EN, DeepLRestClient.Language.DE, new String[] {"Hello world"});
        });
        assertEquals(403, clientEx.getStatus());

        clientEx = assertThrows(DeepLClientException.class, () -> {
            URL url = new URL("http://localhost:" + tomcat.getConnector().getPort() + "/XXX");
            DeepLRestClient client = new DeepLRestClient(url, "dummy");
            client.translate(DeepLRestClient.Language.EN, DeepLRestClient.Language.DE, new String[] {"Hello world"});
        });
        assertEquals(404, clientEx.getStatus());

        clientEx = assertThrows(DeepLClientException.class, () -> {
            DeepLRestClient client = new DeepLRestClient(endpoint, "dummy");
            client.translate(DeepLRestClient.Language.EN, DeepLRestClient.Language.DE, new String[0]);
        });
        assertEquals(400, clientEx.getStatus());

        DeepLServerException serverEx = assertThrows(DeepLServerException.class, () -> {
            URL url = new URL(endpoint.toExternalForm() + "?showerror");
            DeepLRestClient client = new DeepLRestClient(url, "dummy");
            client.translate(DeepLRestClient.Language.EN, DeepLRestClient.Language.DE, new String[] {"Hello world"});
        });
        assertEquals(500, serverEx.getStatus());

        assertThrows(DeepLRestClientException.class, () -> {
            URL url = new URL("http://localhost:0" + "/XXX");
            DeepLRestClient client = new DeepLRestClient(url, "dummy");
            client.translate(DeepLRestClient.Language.EN, DeepLRestClient.Language.DE, new String[] {"Hello world"});
        });
    }

    @Test
    public void testTranslation() {

        String[] textEN = new String[] {"Hello world", "Good morning"};
        String[] textDE = new String[] {"Hallo Welt", "Guten Morgen"};

        DeepLRestClient client = new DeepLRestClient(endpoint, "dummy");
        String[] result = client.translate(DeepLRestClient.Language.EN, DeepLRestClient.Language.DE, textEN);
        assertArrayEquals(textDE, result);

        client = new DeepLRestClient(endpoint, "dummy");
        result = client.translate(DeepLRestClient.Language.DE, DeepLRestClient.Language.EN, textDE);
        assertArrayEquals(textEN, result);

        String[] text = new String[] {"foobarbaz"};
        client = new DeepLRestClient(endpoint, "dummy");
        result = client.translate(DeepLRestClient.Language.DE, DeepLRestClient.Language.EN, text);
        assertArrayEquals(text, result);
    }

    @BeforeAll
    public static void startServer() throws Exception {
        int port = 0;
        try {
            ServerSocket server = new ServerSocket(port);
            port = server.getLocalPort();
            server.close();
        } catch(IOException x) {
            throw new RuntimeException("Can't get a free port", x);
        }
        tomcat = new Tomcat();
        tomcat.setBaseDir("target/tomcat");
        tomcat.setPort(port);
        tomcat.getConnector();
        Context context = tomcat.addContext("/", new File(".").getAbsolutePath());
        tomcat.addServlet("/", "TestServlet", new TranslationServlet());
        context.addServletMappingDecoded("/v2/translate", "TestServlet");
        tomcat.start();
        endpoint = new URL("http://localhost:" + tomcat.getConnector().getPort() + "/v2/translate");
    }

    @AfterAll
    public static void stopServer() throws Exception {
        tomcat.stop();
    }


    static class TranslationServlet extends HttpServlet {

        private static final long serialVersionUID = 1477366900700624397L;

        Map<String, String> dictionary;
        Map<String, String> reverseDictionary;

        TranslationServlet() {
            dictionary = new HashMap<>();
            dictionary.put("Hello world", "Hallo Welt");
            dictionary.put("Good morning", "Guten Morgen");
            reverseDictionary = new HashMap<>();
            dictionary.forEach((key, value) -> {reverseDictionary.put(value, key);});
        }

        @Override
        protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

            res.setContentType("text/plain");
            res.setCharacterEncoding("UTF-8");

            if(req.getParameter("showerror") != null) {
                sendError(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "test error");
                return;
            }

            String authKey = req.getParameter("auth_key");
            if(!"dummy".equals(authKey)) {
                sendError(res, HttpServletResponse.SC_FORBIDDEN, "no valid auth_key");
                return;
            }

            String targetLang = req.getParameter("target_lang");
            String sourceLang = req.getParameter("source_lang");

            PrintWriter writer = res.getWriter();
            String[] text = req.getParameterValues("text");
            if(text == null) {
                sendError(res, HttpServletResponse.SC_BAD_REQUEST, "missing text parameter");
                return;
            } else {
                Translations translations = new Translations();
                List<Translation> list = new ArrayList<>();
                for(String str: text) {
                    String translated = translate(sourceLang, targetLang, str);
                    list.add(new Translation(sourceLang, translated));
                }
                translations.setTranslations(list);
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(writer, translations);
            }
            writer.close();
        }

        private String translate(String sourceLang, String targetLang, String text) {
            if(sourceLang.equals("EN")) {
                return dictionary.getOrDefault(text, text);
            } else if(sourceLang.equals("DE")) {
                return reverseDictionary.getOrDefault(text, text);
            }
            return text;
        }

        private void sendError(HttpServletResponse res, int status, String msg) throws IOException {
            String json = "{\"message\": \"" + Encode.forJavaScript(msg) + "\"}";
            res.setContentType("text/plain");
            res.setStatus(status);
            res.getWriter().write(json);
        }
    }

    public static void main(String[] args) throws Exception {
        startServer();
        System.out.println(endpoint);
        while(true) {
            try {
                Thread.sleep(1000);
            } catch(InterruptedException x) {
            }
        }
    }

}
