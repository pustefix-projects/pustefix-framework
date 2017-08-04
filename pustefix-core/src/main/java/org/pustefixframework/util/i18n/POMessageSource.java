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
package org.pustefixframework.util.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import de.schlund.pfixxml.config.EnvironmentProperties;

/**
 * MessageSource implementation for gettext PO files. 
 * 
 * Basically works like Spring's property-based implementation 
 * {@link org.springframework.context.support.ReloadableResourceBundleMessageSource},
 * but reads messages directly from .po instead of property files.
 */
public class POMessageSource implements HierarchicalMessageSource, ResourceLoaderAware {

    private final static String PO_FILE_SUFFIX = ".po";

    private Logger LOG = LoggerFactory.getLogger(POMessageSource.class);

    private long cacheMillis = -1;
    private MessageSource parent;
    private ResourceLoader resourceLoader = new DefaultResourceLoader();
    private final Set<String> basenames = new LinkedHashSet<>();
    private String defaultEncoding = "UTF-8";

    private ConcurrentMap<String, CachedPOData> cache = new ConcurrentHashMap<>();
    private ConcurrentMap<Locale, CachedLocaleData> localeCache = new ConcurrentHashMap<>();

    /**
     * Resolves message. Returns the default message if no message found.
     * 
     * @param code - message code
     * @param args - message arguments
     * @param defaultMessage - default message
     * @param locale - message locale
     * @return resolved message or default message if not found
     */
    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {

        String message = getMessageInternal(code, args, locale);
        if(message == null) {
            return renderMessage(defaultMessage, args);
        } else {
            return message;
        }  
    }

    /**
     * Resolves message. Throws NoSuchMessageException if no message found.
     * 
     * @param code - message code
     * @param args - message arguments
     * @param locale - message locale
     * @return resolved message
     * @throws NoSuchMessageException
     */
    @Override
    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {

        String message = getMessageInternal(code, args, locale);
        if(message == null) {
            throw new NoSuchMessageException(code, locale);
        } else {
            return message;
        }   
    }

    /**
     * Resolves message using the MessageSourceResolvable attributes. Throws NoSuchMessageException
     * if not matching message is found.
     * 
     * @param resolvable - attributes for message matching
     * @param locale - message locale
     * @return resolved message
     * @throws NoSuchMessageException
     */
    @Override
    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {

        String[] codes = resolvable.getCodes();
        if(codes != null) {
            for(String code: codes) {
                String msg = getMessageInternal(code, resolvable.getArguments(), locale);
                if(msg != null) {
                    return msg;
                }
            }
        }
        if(resolvable.getDefaultMessage() != null) {
            return renderMessage(resolvable.getDefaultMessage(), resolvable.getArguments());
        }
        throw new NoSuchMessageException(codes.length > 0 ? codes[codes.length - 1] : null, locale);
    }

    private String getMessageInternal(String code, Object[] args, Locale locale) {

        CachedLocaleData localeData = getCachedLocaleData(locale);
        for(String fileName: localeData.fileNames) {
            CachedPOData cachedData;
            try {
                boolean doCheck = cacheMillis > -1 && System.currentTimeMillis() - localeData.lastCheck > cacheMillis;
                cachedData = getCachedPOData(fileName, doCheck);
                if(cachedData.data != null) {
                    String text = cachedData.data.findText(code);
                    if(text != null) {
                        return renderMessage(text, args);
                    }
                }
            } catch(IOException x) {
                LOG.warn("Error getting PO message", x);
            }
        }
        if(parent != null) {
            if(parent instanceof POMessageSource) {
                return ((POMessageSource)parent).getMessageInternal(code, args, locale);
            }
            return parent.getMessage(code, args, null, locale);
        }
        return null;
    }

    private String renderMessage(String message, Object[] args) {

        if(message != null) {
            if(message.contains("{")) {
                MessageFormat format = new MessageFormat(message);
                return format.format(args);
            } else {
                return message;
            }
        }
        return null;
    }

    /**
     * Set the parent MessageSource which will be used to resolve
     * a message if it can't be found by this MessageSource.
     * 
     * @param parent - parent MessageSource
     */
    @Override
    public void setParentMessageSource(MessageSource parent) {
        this.parent = parent;
    }

    /**
     * Get the parent MessageSource or null if none is set.
     * 
     * @return parent MessageSource
     */
    @Override
    public MessageSource getParentMessageSource() {
        return parent;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * Set base name for looking up PO files, e.g. "WEB-INF/messages" will result
     * in searching "WEB-INF/messages_de_DE.po", "WEB-INF/messages_de.po" and
     * "WEB-INF/messages.po" for locale "de_DE".
     * 
     * @param basename - PO file base name
     */
    public void setBasename(String basename) {
        setBasenames(basename);
    }

    /**
     * Set multiple base names for looking up PO files, e.g. "WEB-INF/messages" and
     * "WEB-INF/errors" will result in searching "WEB-INF/messages_de_DE.po",
     * "WEB-INF/messages_de.po", "WEB-INF/messages.po", "WEB-INF/errors_de_DE.po",
     * "WEB-INF/errors_de.po" and "WEB-INF/errors.po".
     * 
     * @param basenames - PO file base names
     */
    public void setBasenames(String... basenames) {
        this.basenames.clear();
        for(String basename: basenames) {
            this.basenames.add(basename.trim());
        }
    }

    /**
     * Set the number of milliseconds to cache loaded PO files, i.e. when
     * to check for modifications.
     * 
     * @param cacheMillis - number of milliseconds to cache PO files
     */
    public void setCacheMillis(long cacheMillis) {
        if(!"prod".equals(EnvironmentProperties.getProperties().getProperty("mode"))) {
            this.cacheMillis = cacheMillis;
        }
    }

    /**
     * Set the number of seconds to cache loaded PO files, i.e. when
     * to check for modifications.
     * 
     * @param cacheSeconds - number of seconds to cache PO files
     */
    public void setCacheSeconds(int cacheSeconds) {
        if(!"prod".equals(EnvironmentProperties.getProperties().getProperty("mode"))) {
            this.cacheMillis = cacheSeconds * 1000;
        }
    }

    /**
     * Set the default encoding used to read PO files.
     * By default "UTF-8" will be used.
     * 
     * @param defaultEncoding - default PO file encoding
     */
    public void setDefaultEncoding(String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
    }

    private List<String> getFileNames(Locale locale) {

        String language = locale.getLanguage();
        String country = locale.getCountry();
        String variant = locale.getVariant();
        List<String> fileNames = new ArrayList<>();
        for(String basename: basenames) {
            int ind = fileNames.size();
            fileNames.add(ind, basename + PO_FILE_SUFFIX);
            StringBuilder sb = new StringBuilder();
            sb.append(basename).append("_");
            if(!language.isEmpty()) {
                sb.append(language);
                fileNames.add(ind, sb.toString() + PO_FILE_SUFFIX);
            }
            sb.append("_");
            if(!country.isEmpty()) {
                sb.append(country);
                fileNames.add(ind, sb.toString() + PO_FILE_SUFFIX);
            }
            if(!variant.isEmpty() && !fileNames.isEmpty()) {
                sb.append("_").append(variant);
                fileNames.add(ind, sb.toString() + PO_FILE_SUFFIX);
            }
        }
        return fileNames;
    }

    private CachedPOData getCachedPOData(String fileName, boolean doCheck) throws IOException {

        CachedPOData cachedData = cache.get(fileName);
        if(cachedData == null || doCheck) {
            Resource res = resourceLoader.getResource(fileName);
            if(res.exists()) {
                if(cachedData == null || res.lastModified() > cachedData.lastModified) {
                    try (InputStream in = res.getInputStream()) {
                        POReader reader = new POReader();
                        POData messages = reader.read(in, defaultEncoding);
                        cachedData = new CachedPOData(messages, res.lastModified()); 
                        cache.put(fileName, cachedData);
                    }
                }
            } else {
                if(cachedData == null || cachedData.lastModified > 0) {
                    cachedData = new CachedPOData(null, 0);
                    cache.put(fileName, cachedData);
                }
            }
        }
        return cachedData;
    }

    private CachedLocaleData getCachedLocaleData(Locale locale) {

        CachedLocaleData localeData = localeCache.get(locale);
        if(localeData == null) {
            List<String> fileNames = getFileNames(locale);
            localeData = new CachedLocaleData();
            localeData.fileNames = fileNames;
            localeCache.put(locale, localeData);
        }
        return localeData;
    }


    private class CachedPOData {

        final POData data;
        final long lastModified;

        CachedPOData(POData data, long lastModified) {
            this.data = data;
            this.lastModified = lastModified;
        }
    }


    private class CachedLocaleData {

        long lastCheck;
        List<String> fileNames;
    }

}
