package org.pustefixframework.xslt;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.icl.saxon.om.NamePool;

/**
 * Saxon NamePool implementation with minimized synchronization.
 * It extends Saxon's default NamePool and overrides all synchronized
 * public methods and caches the results in ConcurrentMaps.
 */
public class CachedNamePool extends NamePool {

    ConcurrentMap<CacheKey, Integer> namespaceCodes = new ConcurrentHashMap<>();
    ConcurrentMap<String, Short> uriCodes = new ConcurrentHashMap<>();
    ConcurrentMap<CacheKey, Integer> nameCodes = new ConcurrentHashMap<>();
    ConcurrentMap<Integer, Integer> nameToNamespaceCodes = new ConcurrentHashMap<>();
    
    @Override
    public int allocateNamespaceCode(String prefix, String uri) {

        CacheKey key = new CacheKey(prefix, uri);
        Integer code = namespaceCodes.get(key);
        if(code == null) {
            code = super.allocateNamespaceCode(prefix, uri);
            namespaceCodes.putIfAbsent(key, code);
        }
        return code;
    }

    @Override
    public short allocateCodeForURI(String uri) {

        Short code = uriCodes.get(uri);
        if(code == null) {
            code = super.allocateCodeForURI(uri);
            uriCodes.putIfAbsent(uri, code);
        }
        return code;
    }

    @Override
    public int allocate(String prefix, String uri, String localName) {

        short uriCode = allocateCodeForURI(uri);
        return allocate(prefix, uriCode, localName);
    }

    @Override
    public int allocate(String prefix, short uriCode, String localName) {

        CacheKey key = new CacheKey(prefix, uriCode, localName);
        Integer code = nameCodes.get(key);
        if(code == null) {
            code = super.allocate(prefix, uriCode, localName);
            nameCodes.putIfAbsent(key, code);
        }
        return code;
    }

    @Override
    public int allocateNamespaceCode(int namecode) {

        Integer code = nameToNamespaceCodes.get(namecode);
        if(code == null) {
            code = super.allocateNamespaceCode(namecode);
            nameToNamespaceCodes.putIfAbsent(namecode, code);
        }
        return code;
    }


    class CacheKey {
        
        private final int hashCode;
        private final Object[] elements;
        
        public CacheKey(Object... elements) {
            this.elements = elements;
            this.hashCode = Arrays.deepHashCode(elements);
        }
        
        @Override
        public int hashCode() {
            return hashCode;
        }
        
        @Override
        public boolean equals(Object obj) {
            return this == obj || ( obj instanceof CacheKey &&
                    Arrays.deepEquals(elements, ((CacheKey)obj).elements));
        }
        
    }
    
}