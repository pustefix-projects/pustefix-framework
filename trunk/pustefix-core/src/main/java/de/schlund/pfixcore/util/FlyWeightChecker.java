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
package de.schlund.pfixcore.util;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Describe class FlyWeightChecker here.
 *
 *
 * Created: Mon May 29 22:54:29 2006
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class FlyWeightChecker {

    /**
     * Creates a new <code>FlyWeightChecker</code> instance.
     *
     */
     static public boolean check(Object obj) {
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field: fields) {
            int mod = field.getModifiers();
            if (!Modifier.isStatic(mod) && !Modifier.isFinal(mod)) {
                return false;
            }
        }
        return true;
    }
}

