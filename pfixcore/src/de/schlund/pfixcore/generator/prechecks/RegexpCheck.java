/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package de.schlund.pfixcore.generator.prechecks;
import org.apache.oro.text.PatternCacheLRU;
import org.apache.oro.text.perl.Perl5Util;
import org.pustefixframework.CoreStatusCodes;

import de.schlund.pfixcore.generator.IWrapperParamPreCheck;
import de.schlund.pfixcore.generator.SimpleCheck;
import de.schlund.pfixxml.RequestParam;
import de.schlund.util.statuscodes.StatusCode;
import de.schlund.util.statuscodes.StatusCodeHelper;

/**
 * RegexpCheck.java
 *
 *
 * Created: Fri Aug 17 10:38:12 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class RegexpCheck  extends SimpleCheck implements IWrapperParamPreCheck {
    private final static Perl5Util p5 = new Perl5Util(new PatternCacheLRU(20));
    private        String     regexp = null;
    private        StatusCode scode;
    
    public RegexpCheck() {
        scode = CoreStatusCodes.PRECHECK_REGEXP_NO_MATCH;
    }

    public void put_regexp(String regexp) {
        this.regexp = regexp;
    }

    public void put_scode_nomatch(String fqscode) {
        scode = StatusCodeHelper.getStatusCodeByName(fqscode);
    }
    
    public void check(RequestParam[] value) {
        reset();
        for (int i = 0; i < value.length; i++) {
            if (!p5.match(regexp, value[i].getValue())) {
                addSCode(scode);
                break;
            }
        }
    }
    
}// RegexpCheck
