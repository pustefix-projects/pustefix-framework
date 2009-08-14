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

package org.pustefixframework.resource.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import org.pustefixframework.resource.Resource;
import org.pustefixframework.resource.ResourceSelector;
import org.pustefixframework.resource.ThemedResource;

/**
 * Selector that checks for a parameter "preferredThemes" that contains 
 * a (descending) list of preferred themes and sorts the resources (that have
 * theme information) in the order of the theme preference.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ThemeResourceSelector implements ResourceSelector {

    public Resource[] selectResources(Resource[] resources, Map<String, ?> parameters) {
        if (parameters == null) {
            return resources;
        }
        Object parameter = parameters.get("preferredThemes");
        if (parameter == null && !(parameter instanceof String[])) {
            return resources;
        }
        String[] preferredThemes = (String[]) parameter;

        LinkedList<Resource> newResources = new LinkedList<Resource>();
        for (String theme : preferredThemes) {
            for (Resource resource : resources) {
                if (resource instanceof ThemedResource) {
                    ThemedResource themedResource = (ThemedResource) resource;
                    String resourceTheme = themedResource.getTheme();
                    if (theme.equals(resourceTheme)) {
                        newResources.add(resource);
                    }
                }
            }
        }
        HashSet<String> checkedThemes = new HashSet<String>(Arrays.asList(preferredThemes));
        for (Resource resource : resources) {
            if (resource instanceof ThemedResource) {
                ThemedResource themedResource = (ThemedResource) resource;
                if (!checkedThemes.contains(themedResource.getTheme())) {
                    newResources.add(resource);
                }
            } else {
                newResources.add(resource);
            }
        }
        return newResources.toArray(new Resource[newResources.size()]);
    }

}
