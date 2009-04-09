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
 *
 */

package de.schlund.pfixcore.generator.casters;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.pustefixframework.generated.CoreStatusCodes;

import de.schlund.pfixcore.generator.IWrapperParamCaster;
import de.schlund.pfixcore.generator.IWrapperParamUncaster;
import de.schlund.pfixcore.generator.SimpleCheck;
import de.schlund.pfixxml.RequestParam;
import de.schlund.util.statuscodes.StatusCode;
import de.schlund.util.statuscodes.StatusCodeHelper;

/**
 * ToDate.java
 *
 *
 * Created: Thu Aug 16 15:34:25 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class ToDate extends SimpleCheck implements IWrapperParamCaster, IWrapperParamUncaster {
    private Date[]           value  = null;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
    private SimpleDateFormat paramFormat = new SimpleDateFormat("yyyy/MM/dd");
    private StatusCode       scode;
    
    public ToDate() {
        scode = CoreStatusCodes.CASTER_ERR_TO_DATE;
    }
    
    public void setScodeCastError(String fqscode) {
        scode = StatusCodeHelper.getStatusCodeByName(fqscode);
    }

    public void setFormat(String fmtstr) {
        format = new SimpleDateFormat(fmtstr);
    }

    public void setParamFormat(String fmtstr) {
    	paramFormat = new SimpleDateFormat(fmtstr);
    }
    
    public Object[] getValue() {
        return value;
    }

    public void castValue(RequestParam[] param) {
        reset();
        format.setLenient(false);
        Date      val;
        ArrayList<Date> dates = new ArrayList<Date>();
        for (int i = 0; i < param.length; i++) {
            try {
                val = format.parse(param[i].getValue());
                dates.add(val);
            } catch (ParseException e) {
                val = null;
                addSCode(scode);
                break;
            }
        }
        if (!errorHappened()) {
            value = dates.toArray(new Date[] {});
        }
    }

    public String[] uncastValue(Object[] objArray) {
        List<String> uncastedValues = new ArrayList<String>();
        for (Object obj : objArray) {
            if (obj instanceof Date) {
                Date date = (Date)obj;
                uncastedValues.add(paramFormat.format(date));
            }
        }
        return uncastedValues.toArray(new String[] {});
    }

}// ToDate
