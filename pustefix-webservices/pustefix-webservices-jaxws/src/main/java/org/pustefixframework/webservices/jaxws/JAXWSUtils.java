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
