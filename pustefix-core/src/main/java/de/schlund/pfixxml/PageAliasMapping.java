package de.schlund.pfixxml;

import java.util.Map;
import java.util.Set;

/**
 * Mapping of logical (unique internal) page names to page alias names, e.g. for internationalization.
 * The mapping is done using so-called page selectors, a map of key/value pairs, e.g. language or 
 * country specifiers (like lang=en and country=US).
 *
 * Examples:
 *
 * home => Start  (lang=de)
 * home => home   (lang=en)
 * home => depart (lang=fr)
 *
 *
 * @author mleidig@schlund.de
 *
 */
public interface PageAliasMapping {

    /**
     * Get page alias name for logical page.
     *
     * @param page - Logical page name
     * @param pageSelectors - Page selector key/value pairs
     * @return - Returns page alias name or passed page name if no alias found
     */
    public String getAlias(String page, Map<String, String> pageSelectors);

    /**
     * Get logical page from alias page name.
     *
     * @param alias - Page alias name
     * @param pageSelectors - Page selector key/value pairs
     * @return - Returns logical page or passed alias if no logical page found 
     */
    public String getPage(String alias, Map<String, String> pageSelectors);

    /**
     *
     *
     * @param pageSelectors - Page selector key/value pairs
     * @return - Returns all page alias names matching the given page selectors
     */
    //public Set<String> getAliases(Map<String, String> pageSelectors);

}
