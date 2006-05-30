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

