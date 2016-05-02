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

import org.pustefixframework.generated.CoreStatusCodes;

import de.schlund.pfixcore.generator.IWrapperParamPreCheck;
import de.schlund.pfixcore.generator.SimpleCheck;
import de.schlund.pfixxml.RequestParam;
import de.schlund.util.statuscodes.StatusCode;
import de.schlund.util.statuscodes.StatusCodeHelper;

/**
 * Regular expression check using Java Patterns (or Perl5 regex for backwards compatibility).
 * 
 * Patterns of the form "/.../" (Perl5-style) are checked using Pattern.matcher().find() 
 * Other patterns will be evaluated using Pattern.matcher().matches()
 *
 */
public class RegexpCheck extends SimpleCheck implements IWrapperParamPreCheck {
    
    private String regexp;
    private StatusCode scode;
    
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
        Pattern pattern = null;
        if(regexp.startsWith("/")) {
            pattern = Pattern.compile(perl5ToJavaRegex(regexp));
        } else {
            pattern = Pattern.compile(regexp);
        }
        for (int i = 0; i < value.length; i++) {
            if(regexp.startsWith("/")) {
                if (!pattern.matcher(value[i].getValue()).find()) {
                    addSCode(scode);
                    break;
                }
            } else {
                if (!pattern.matcher(value[i].getValue()).matches()) {
                    addSCode(scode);
                    break;
                }
            }
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
    
}
