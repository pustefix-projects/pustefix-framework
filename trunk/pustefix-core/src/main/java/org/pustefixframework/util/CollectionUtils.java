package org.pustefixframework.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Enumeration;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class CollectionUtils {

    public static Properties unmodifiableProperties(Properties properties) {
        return new UnmodifiableProperties(properties);
    }
    
    private static class UnmodifiableProperties extends Properties {

        private static final long serialVersionUID = 5804294267482619090L;
        
        private static final String ERROR_MSG = "You tried to modify read-only properties.";
        
        private Properties properties;
        
        public UnmodifiableProperties(Properties properties) {
            this.properties = properties;
        }
        
        @Override
        public synchronized Object setProperty(String key, String value) {
            throw new UnsupportedOperationException(ERROR_MSG);
        }
        
        @Override
        public synchronized void clear() {
            throw new UnsupportedOperationException(ERROR_MSG);
        }
        
        @Override
        public synchronized Object put(Object key, Object value) {
            throw new UnsupportedOperationException(ERROR_MSG);
        }
        
        @Override
        public synchronized void putAll(Map<? extends Object, ? extends Object> t) {
            throw new UnsupportedOperationException(ERROR_MSG);
        }
        
        @Override
        public synchronized Object remove(Object key) {
            throw new UnsupportedOperationException(ERROR_MSG);
        }  
        
        @Override
        public synchronized void load(InputStream inStream) throws IOException {
            throw new UnsupportedOperationException(ERROR_MSG);
        }
        
        @Override
        public synchronized void load(Reader reader) throws IOException {
            throw new UnsupportedOperationException(ERROR_MSG);
        }
        
        @Override
        public synchronized void loadFromXML(InputStream in) throws IOException, InvalidPropertiesFormatException {
            throw new UnsupportedOperationException(ERROR_MSG);
        }

        @SuppressWarnings("deprecation")
        @Override
        public synchronized void save(OutputStream out, String comments) {
            properties.save(out, comments);
        }

        @Override
        public void store(Writer writer, String comments) throws IOException {
            properties.store(writer, comments);
        }

        @Override
        public void store(OutputStream out, String comments) throws IOException {
            properties.store(out, comments);
        }

        @Override
        public synchronized void storeToXML(OutputStream os, String comment) throws IOException {
            properties.storeToXML(os, comment);
        }

        @Override
        public synchronized void storeToXML(OutputStream os, String comment, String encoding) throws IOException {
            properties.storeToXML(os, comment, encoding);
        }

        @Override
        public String getProperty(String key) {
            return properties.getProperty(key);
        }

        @Override
        public String getProperty(String key, String defaultValue) {
            return properties.getProperty(key, defaultValue);
        }

        @Override
        public Enumeration<?> propertyNames() {
            return properties.propertyNames();
        }

        @Override
        public Set<String> stringPropertyNames() {
            return properties.stringPropertyNames();
        }

        @Override
        public void list(PrintStream out) {
            properties.list(out);
        }

        @Override
        public void list(PrintWriter out) {
            properties.list(out);
        }
        
    }
    
}
