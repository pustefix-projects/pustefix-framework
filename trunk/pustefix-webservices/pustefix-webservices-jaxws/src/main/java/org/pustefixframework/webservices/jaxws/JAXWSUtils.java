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
package org.pustefixframework.webservices.jaxws;

public class JAXWSUtils {

    /**
     * Returns the default target namespace of a class as defined in the JAX-WS
     * specification. The namespace is derived from the package of the class
     * using {@link #getTargetNamespace(Package)}.
     * 
     * @param clazz the Java class
     * @return the default target namespace
     */
    public static String getTargetNamespace(Class<?> clazz) {
        if (clazz == null) throw new IllegalArgumentException("Class argument must not be null");
        return getTargetNamespace(clazz.getPackage());
    }

    /**
     * Returns the default target namespace of a package as defined in the
     * JAX-WS specification.
     * 
     * @param pkg the Java package
     * @return the default target namespace
     */
    public static String getTargetNamespace(Package pkg) {
        if (pkg == null) throw new IllegalArgumentException("Class has no package information");
        StringBuilder sb = new StringBuilder();
        sb.append("http://");
        String name = pkg.getName();
        String[] pkgs = name.split("\\.");
        for (int i = pkgs.length - 1; i > -1; i--) {
            sb.append(pkgs[i]);
            if (i > 0) sb.append(".");
        }
        sb.append("/");
        return sb.toString();
    }
    
}
