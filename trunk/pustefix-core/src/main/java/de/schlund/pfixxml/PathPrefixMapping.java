package de.schlund.pfixxml;

import java.util.Map;

/**
 * Mapping page selectors, a map of key/value pairs, e.g. language or country specifiers (like 
 * lang=en and country=US), to path prefixes, e.g. for internationalization.
 *
 * Examples:
 *
 * (lang=en) => en
 * (lang=fr) => fr
 *
 *
 * @author mleidig@schlund.de
 *
 */
public interface PathPrefixMapping {

    /**
     * Get path prefix based on page selectors. 
     * 
     * @param pageSelectors - Page selector key/value pairs
     * @return - Returns path prefix or empty String if no prefix found
     */
    public String getPrefix(Map<String, String> pageSelectors);
    
    public String unmap(String pagePath, Map<String, String> pageSelectors);
    
}
