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

package de.schlund.pfixcore.generator.prechecks;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.oro.text.PatternCacheLRU;
import org.apache.oro.text.perl.Perl5Util;
import org.pustefixframework.generated.CoreStatusCodes;
import org.pustefixframework.util.LogUtils;

import de.schlund.pfixcore.generator.IWrapperParamPreCheck;
import de.schlund.pfixcore.generator.SimpleCheck;
import de.schlund.pfixxml.RequestParam;
import de.schlund.util.statuscodes.StatusCode;
import de.schlund.util.statuscodes.StatusCodeHelper;

/**
 * Regular expression check using Java Patterns (or Perl5 regex for backwards compatibility).
 * 
 * Patterns of the form "/.../" are checked using Perl5Util, future versions will use Pattern.matcher().find() 
 * Other patterns will be evaluated using Pattern.matcher().matches()
 *
 */
public class RegexpCheck  extends SimpleCheck implements IWrapperParamPreCheck {
    
    private Logger LOG = Logger.getLogger(RegexpCheck.class);
    
    private final static Perl5Util p5 = new Perl5Util(new PatternCacheLRU(20));
    private        String     regexp = null;
    private        StatusCode scode;
    
    public RegexpCheck() {
        scode = CoreStatusCodes.PRECHECK_REGEXP_NO_MATCH;
    }

    public void setRegexp(String regexp) {
        this.regexp = regexp;
    }

    public void setScodeNoMatch(String fqscode) {
        scode = StatusCodeHelper.getStatusCodeByName(fqscode);
    }
    
    public void check(RequestParam[] value) {
        reset();
        for (int i = 0; i < value.length; i++) {
            if(regexp.startsWith("/")) {
                boolean p5Match = p5.match(regexp, value[i].getValue());
                checkJavaRegexMatch(regexp, value[i].getValue(), p5Match);
                if (!p5Match) {
                    addSCode(scode);
                    break;
                }
            } else {
                if (!Pattern.matches(regexp, value[i].getValue())) {
                    addSCode(scode);
                    break;
                }
            }
        }
    }
    
    /**
     * Convert the perl5 regex to a Java pattern and log out
     * result when they don't match equally.
     * This code will be removed after having migrated from 
     * perl5 to Java patterns in Pustefix 0.19.
     */
    private void checkJavaRegexMatch(String regex, String value, boolean expected) {
        try {
            Pattern pattern = Pattern.compile(perl5ToJavaRegex(regex));
            boolean actual = pattern.matcher(value).find();
            if(actual != expected) {
                LOG.warn("INCOMPATIBLE_REGEX|" + regex + "|" + LogUtils.makeLogSafe(value) + "|" + expected + "|" + actual);
            }
        } catch(Exception x) {
            LOG.warn("REGEX_ERROR|" + regex + "|" + LogUtils.makeLogSafe(value) + "|" + expected + "|" + x.getMessage(), x);
        }
    }
    
    public static String perl5ToJavaRegex(String perl5Regex) {
        if(perl5Regex.startsWith("/")) {
            String patternStr = null;
            String flagStr = null;
            for(int i=1; i<perl5Regex.length(); i++) {
                if(perl5Regex.charAt(i) == '/' && perl5Regex.charAt(i-1) != '\\') {
                    patternStr = perl5Regex.substring(1, i);
                    flagStr = perl5Regex.substring(i + 1);
                }
            }
            if(patternStr != null) {
                if(flagStr != null) {
                    patternStr = "(?" + flagStr + ")" + patternStr;
                }
                return patternStr;
            }
        }
        return perl5Regex;
    }
    
}// RegexpCheck
