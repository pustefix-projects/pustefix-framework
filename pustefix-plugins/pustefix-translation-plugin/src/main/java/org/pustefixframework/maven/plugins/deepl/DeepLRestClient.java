/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.pustefixframework.maven.plugins.deepl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.pustefixframework.maven.plugins.Translator;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * DeepL translation API client implementation.
 */
public class DeepLRestClient implements Translator {

    private final static String PROPERTY_ENDPOINT = "deepl.endpoint";
    private final static String PROPERTY_AUTHKEY = "deepl.authkey";

    public enum Language {
        EN,
        DE,
        FR,
        ES,
        PT,
        IT,
        NL,
        PL,
        RU
    }

    private URL endpoint;
    private String authKey;

    /**
     * Constructs an uninitialized DeepL translation API client.
     * Requires that init method is called to initialize
     * required fields from properties.
     */
    public DeepLRestClient() {}

    /**
     * Constructs a DeepL translation API client with the specified
     * endpoint URL and authentication key.
     *
     * @param endpoint - DeepL API endpoint URL
     * @param authKey - DeepL API authentication key
     */
    public DeepLRestClient(URL endpoint, String authKey) {
        this.endpoint = endpoint;
        this.authKey = authKey;
    }

    @Override
    public void init(Properties properties) {
        String prop = properties.getProperty(PROPERTY_ENDPOINT);
        if(prop == null) {
            throw new IllegalArgumentException("Missing property: " + PROPERTY_ENDPOINT);
        } else {
            try {
                this.endpoint = new URL(prop);
            } catch(MalformedURLException x) {
                throw new IllegalArgumentException("Illegal property value: " + prop);
            }
        }
        prop = properties.getProperty(PROPERTY_AUTHKEY);
        if(prop == null) {
            throw new IllegalArgumentException("Missing property: " + PROPERTY_AUTHKEY);
        } else {
            this.authKey = prop;
        }
    }

    @Override
    public String[] translate(Locale sourceLocale, Locale targetLocale, String[] text) {
        Language sourceLang = Language.valueOf(sourceLocale.getLanguage().toUpperCase());
        Language targetLang = Language.valueOf(targetLocale.getLanguage().toUpperCase());
        return translate(sourceLang, targetLang, text);
    }

    /**
     * Translates text from source language to target language.
     *
     * @param sourceLang - source language
     * @param targetLang - target language
     * @param text - text to be translated
     * @return translated text
     */
    public String[] translate(Language sourceLang, Language targetLang, String[] text) {

        try {
            StringBuilder postData = new StringBuilder();
            postData.append("auth_key=" + URLEncoder.encode(authKey, "UTF-8")).append("&");
            postData.append("source_lang=" + sourceLang.name()).append("&");
            postData.append("target_lang=" + targetLang.name()).append("&");
            for(int i=0; i<text.length; i++) {
                postData.append("text=" + URLEncoder.encode(text[i], "UTF-8"));
                if(i < text.length - 1) {
                    postData.append("&");
                }
            }

            byte[] postDataBytes = postData.toString().getBytes("UTF-8");
            HttpURLConnection con = (HttpURLConnection)endpoint.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            con.setDoOutput(true);
            con.getOutputStream().write(postDataBytes);

            int status = con.getResponseCode();
            if(status >= 500) {
                throw new DeepLServerException(status, read(con.getErrorStream()));
            } else if(status >= 400) {
                    throw new DeepLClientException(status, read(con.getErrorStream()));
            } else if(status == 200) {
                ObjectMapper mapper = new ObjectMapper();
                Translations translations = mapper.readValue(con.getInputStream(), Translations.class);
                List<Translation> list = translations.getTranslations();
                String[] res = new String[list.size()];
                for(int i=0; i<list.size(); i++) {
                    res[i] = list.get(i).getText();
                }
                return res;
            } else {
                throw new DeepLClientException(status, "Unsupported response status code");
            }
        } catch(UnsupportedEncodingException x) {
            throw new RuntimeException(x);
        } catch(IOException x) {
            throw new DeepLRestClientException(x);
        }
    }

    private String read(InputStream in) throws IOException {
        if(in == null) {
            return null;
        }
        try (InputStreamReader reader = new InputStreamReader(in, "UTF-8")) {
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[4096];
            int i = 0;
            while ((i = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, i);
            }
            return sb.toString();
        }
    }

}
