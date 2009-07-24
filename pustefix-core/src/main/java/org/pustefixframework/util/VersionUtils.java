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

import java.util.regex.Pattern;


/**
 * Utility methods for comparing version numbers and ranges.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public abstract class VersionUtils {
    
    private final static Pattern NUMERIC_PATTERN = Pattern.compile("^[0-9]+$");
    private final static Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[0-9]+[A-Za-z]*[0-9]*$");
    
    /**
     * Checks whether the supplied string represents a valid
     * version number or version range.
     * 
     * @param version version string
     * @return <code>true</code> if the string is valid version number
     *  or version range, <code>false</code> otherwise
     */
    public static boolean checkVersionOrRange(String version) {
        return checkVersion(version) || checkVersionRange(version);
    }

    /**
     * Checks whether the supplied string represents a valid
     * version number.
     * 
     * @param version version string
     * @return <code>true</code> if the string is valid version number,
     *  <code>false</code> otherwise
     */
    public static boolean checkVersion(String version) {
        version = version.trim();
        while (version.contains(".")) {
            String firstPart = version.substring(0, version.indexOf('.'));
            if (!NUMERIC_PATTERN.matcher(firstPart).matches()) {
                return false;
            }
            version = version.substring(version.indexOf('.') + 1);
        }
        if (version.length() == 0) {
            return false;
        }
        if (!ALPHANUMERIC_PATTERN.matcher(version).matches()) {
            return false;
        }
        return true;
    }

    /**
     * Checks whether the supplied string represents a valid
     * version range.
     * 
     * @param version version range string
     * @return <code>true</code> if the string is valid
     *  version range, <code>false</code> otherwise
     */
    public static boolean checkVersionRange(String version) {
        version = version.trim();
        if (version.equals("*")) {
            return true;
        }
        if (!version.startsWith("(") && !version.startsWith("[")) {
            return false;
        }
        if (!version.endsWith(")") && !version.endsWith("]")) {
            return false;
        }
        version = version.substring(1, version.length() - 1).trim();
        int commaPos = version.indexOf(',');
        if (commaPos == -1) {
            return false;
        }
        String minVersion = version.substring(0, commaPos).trim();
        String maxVersion = version.substring(commaPos + 1, version.length()).trim();
        if (!minVersion.equals("*") && !checkVersion(minVersion)) {
            return false;
        }
        if (!maxVersion.equals("*") && !checkVersion(maxVersion)) {
            return false;
        }
        return true;
    }

    /**
     * Compares two version strings. Returns a negative integer, zero, 
     * or a positive integer as the first version is less than, equal 
     * to, or greater than the second one.
     * 
     * @param v1 first version string
     * @param v2 second version string
     * @return a negative integer, zero, or a positive integer as the 
     *  first version is less than, equal to, or greater than the second 
     */
    public static int compareVersions(String v1, String v2) {
        v1 = v1.trim();
        v2 = v2.trim();
        if (!checkVersion(v1)) {
            throw new IllegalArgumentException("v1 is not a valid version number: " + v1);
        }
        if (!checkVersion(v2)) {
            throw new IllegalArgumentException("v2 is not a valid version number: " + v2);
        }
        while (v2.contains(".")) {
            if (!v1.contains(".")) {
                // v1 is less than v2
                return -1;
            }
            String partV1 = v1.substring(0, v1.indexOf('.'));
            String partV2 = v1.substring(0, v2.indexOf('.'));
            int numV1 = Integer.parseInt(partV1);
            int numV2 = Integer.parseInt(partV2);
            if (numV1 < numV2) {
                return -1;
            } else if (numV1 > numV2) {
                return 1;
            }
            // up to here, both versions are equal, so we have to continue
            v1 = v1.substring(partV1.length() + 1);
            v2 = v2.substring(partV2.length() + 1);
        }
        // the last part may be alphanumeric, so we need an extended
        // comparison algorithm
        String[] partsV1 = v1.split("[A-Za-z]+", -1);
        String[] partsV2 = v2.split("[A-Za-z]+", -1);
        int numV1 = Integer.parseInt(partsV1[0]);
        int numV2 = Integer.parseInt(partsV2[0]);
        if (numV1 > numV2) {
            return 1;
        } else if (numV1 < numV2) {
            return -1;
        }
        // if we are here, both numeric parts are equal
        if (partsV1.length == 1) {
            // v1 has no alphanumeric part.
            // For same numeric part, the version
            // number without an alphanumeric part
            // is considered higher.
            if (partsV2.length == 1) {
                return 0;
            } else {
                return 1;
            }
        }
        if (partsV2.length == 1) {
            // v2 has no alphanumeric part.
            // For same numeric part, the version
            // number without an alphanumeric part
            // is considered higher.
            // We already know, that v1 has an
            // alphanumeric part
            return -1;
        }
        // We got here, this means both version have
        // an alphanumeric part.
        String alphaV1 = v1.substring(partsV1[0].length(), v1.length() - partsV1[0].length());
        String alphaV2 = v1.substring(partsV2[0].length(), v2.length() - partsV2[0].length());
        int comparison = alphaV1.compareToIgnoreCase(alphaV2);
        if (comparison != 0) {
            return comparison;
        }
        // Alphanumeric part is equal, too, so we have
        // to compare the last numeric part.
        // A missing last numeric part is considered
        // to be equal to zero.
        if (partsV1[1].length() == 0) {
            numV1 = 0;
        } else {
            numV1 = Integer.parseInt(partsV1[1]);
        }
        if (partsV2[1].length() == 0) {
            numV2 = 0;
        } else {
            numV2 = Integer.parseInt(partsV2[1]);
        }
        if (numV1 > numV2) {
            return 1;
        } else if (numV1 < numV2) {
            return -1;
        } else {
            return 0;
        }
    }

    /**
     * Checks whether a given version is within a given range. The range
     * might either be a version number (which will cause a check for
     * equality), "*" to match any version or an interval.
     * 
     * @param version version number to check against the range
     * @param versionRange range or version number that the version should
     *  be checked against
     * @return <code>true</code> if version is within version range, 
     *  <code>false</code> otherwise
     */
    public static boolean isVersionInRange(String version, String versionRange) {
        version = version.trim();
        versionRange = versionRange.trim();
        if (!checkVersion(version)) {
            throw new IllegalArgumentException("version string is invalid: " + version);
        }
        if (checkVersion(versionRange)) {
            return (compareVersions(version, versionRange) == 0);
        } else if (checkVersionRange(versionRange)) {
            if (versionRange.equals("*")) {
                return true;
            }
            boolean minInclusive = false;
            boolean maxInclusive = false;
            if (versionRange.charAt(0) == '[') {
                minInclusive = true;
            }
            if (versionRange.charAt(versionRange.length()) == ']') {
                maxInclusive = true;
            }
            int commaPos = versionRange.indexOf(',');
            String vMin = versionRange.substring(0, commaPos).trim();
            String vMax = versionRange.substring(commaPos + 1).trim();
            if (!vMin.equals("*")) {
                int comparison = compareVersions(vMin, version);
                if (comparison > 0 || (comparison == 0 && !minInclusive)) {
                    return false;
                }
            }
            if (!vMax.equals("*")) {
                int comparison = compareVersions(vMax, version);
                if (comparison < 0 || (comparison == 0 && !maxInclusive)) {
                    return false;
                }
            }
            return true;
        } else {
            throw new IllegalArgumentException("version range string is invalid: " + versionRange);
        }
        
    }
}
