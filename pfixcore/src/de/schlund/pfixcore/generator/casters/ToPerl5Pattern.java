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

package de.schlund.pfixcore.generator.casters;

import java.util.ArrayList;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Pattern;
import org.pustefixframework.generated.CoreStatusCodes;

import de.schlund.pfixcore.generator.IWrapperParamCaster;
import de.schlund.pfixcore.generator.SimpleCheck;
import de.schlund.pfixxml.RequestParam;
import de.schlund.util.statuscodes.StatusCode;
import de.schlund.util.statuscodes.StatusCodeHelper;


/**
 * ToPerl5Pattern.java
 *
 *
 * Created: Sun Mar 10 15:51:47 2002
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class ToPerl5Pattern extends SimpleCheck implements IWrapperParamCaster {
    private        Perl5Pattern[] value    = null;
    private final static Perl5Compiler compiler = new Perl5Compiler();
    private        StatusCode     scode;
    
    public ToPerl5Pattern() {
        scode = CoreStatusCodes.CASTER_ERR_TO_P5PATTERN;
    }
    
    public void put_scode_casterror(String fqscode) {
        scode = StatusCodeHelper.getStatusCodeByName(fqscode);
    }

    // implementation of de.schlund.pfixcore.generator.IWrapperParamCaster interface
    
    /**
     *
     * @return <description>
     */
    public Object[] getValue() {
        return value;
    }

    /**
     *
     * @param param <description>
     */
    public void castValue(RequestParam[] param) {
        reset();
        Perl5Pattern val;
        ArrayList<Perl5Pattern> patterns = new ArrayList<Perl5Pattern>();
        for (int i = 0; i < param.length; i++) {
            try {
                val = tryCompile(param[i].getValue());
                patterns.add(val);
            } catch (MalformedPatternException e) {
                val = null;
                addSCode(scode);
                break;
            }
        }
        if (!errorHappened()) {
            value = (Perl5Pattern[]) patterns.toArray(new Perl5Pattern[] {});
        }
    }

    private synchronized Perl5Pattern tryCompile(String in) throws MalformedPatternException {
        return (Perl5Pattern) compiler.compile(in, Perl5Compiler.CASE_INSENSITIVE_MASK);
    }
    
}// ToPerl5Pattern
