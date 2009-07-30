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

package org.pustefixframework.util;

import org.junit.Test;

import junit.framework.Assert;


/**
 * Tests for {@link VersionUtils}  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class VersionUtilsTest {

    @Test
    public void testIsVersion() {
        testIsVersion("4.23.17-alpha5", true);
        testIsVersion("4.23.17-alpha-5", true);
        testIsVersion("4.23.17alpha5", true);
        testIsVersion("4.23.17", true);
        testIsVersion("4.17-alpha-5", true);
        testIsVersion("4.23.17-alp-ha-5", false);
    }
    
    private void testIsVersion(String version, boolean result) {
        Assert.assertTrue("isVersion(\"" + version + "\" did not return " + result, VersionUtils.isVersion(version) == result); 
    }

    @Test
    public void testIsVersionRange() {
        testIsVersionRange("*", true);
        testIsVersionRange("[4.28.19,7.34.3)", true);
        testIsVersionRange(">7.34.3", true);
        testIsVersionRange(">7.34.3)", false);
        testIsVersionRange("[>7.34.3)", false);
        testIsVersionRange("=5.18.2-beta", true);
    }

    private void testIsVersionRange(String versionRange, boolean result) {
        Assert.assertTrue("isVersionRange(\"" + versionRange + "\" did not return " + result, VersionUtils.isVersionRange(versionRange) == result); 
    }

    @Test
    public void testIsVersionInRange() {
        testIsVersionInRange("4.18.25", ">=4.18.25", true);
        testIsVersionInRange("4.18.25", ">4.18.25", false);
        testIsVersionInRange("4.18.25", "(*, 4.18.25]", true);
        testIsVersionInRange("4.18.25", "(*, 4.18.25)", false);
        testIsVersionInRange("4.18.25-beta2", ">=4.18.25", false);
        testIsVersionInRange("4.18.25-beta2", ">=4.18.25-alpha", true);
    }

    private void testIsVersionInRange(String version, String versionRange, boolean result) {
        Assert.assertTrue("isVersionInRange(\"" + version + "\", \"" + versionRange + "\" did not return " + result, VersionUtils.isVersionInRange(version, versionRange) == result); 
    }
}
