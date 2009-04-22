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
package org.pustefixframework.maven.plugins;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;

/**
 * Utility methods used by the mojo.
 * 
 * @author mleidig@schlund.de
 */
public class Reflection {
    public static Reflection create(MavenProject project) throws MojoExecutionException {
        URL[] cp;
        List<Artifact> artifacts;
        StringBuilder classpath;
        File file;

        classpath = new StringBuilder();
        try {
            artifacts = project.getCompileArtifacts();
            cp = new URL[artifacts.size() + 1];
            file = new File(project.getBuild().getOutputDirectory());
            cp[0] = file.toURI().toURL();
            classpath.append(file);
            for (int i = 1; i < cp.length; i++) {
                file = artifacts.get(i - 1).getFile();
                cp[i] = file.toURI().toURL();
                classpath.append(':').append(file.getAbsolutePath());
            }
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("invalid url", e); 
        }
        return new Reflection(new URLClassLoader(cp, Reflection.class.getClassLoader()), classpath.toString());
    }
    
    private final URLClassLoader loader;
    private final String classpath;
    
    public Reflection(URLClassLoader loader, String classpath) {
        this.loader = loader;
        this.classpath = classpath;
    }
    
    public String getClasspath() {
        return classpath;
    }
    
    public Class<?> clazz(String name) throws MojoExecutionException {
        try {
            return loader.loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException(name + ": class not found in classpath " + toString(loader.getURLs()));
        }
    }
    
    private static String toString(URL[] urls) {
        StringBuilder builder;

        builder = new StringBuilder();
        for (URL url : urls) {
            if (builder.length() > 0) {
                builder.append(':');
            }
            builder.append(url.toString());
        }
        return builder.toString();
    }
    

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

    /**
     * Checks if the webservice interface has been modified since the last
     * modification of a reference file.
     */
    public boolean checkInterfaceChange(String className, File buildDir, File refFile) throws MojoExecutionException {
        if(className == null) return false;
        Class<?> clazz = clazz(className);
        if (!clazz.isInterface()) throw new MojoExecutionException("Web service interface class '" + className + "' doesn't define an interface type.");
        // Check if interface or dependant interfaces changed
        boolean changed = checkTypeChange(clazz, buildDir, refFile);
        if (changed) return true;
        // Check if method parameter or return type classes changed
        Method[] meths = clazz.getMethods();
        for (int i = 0; i < meths.length; i++) {
            Class<?> ret = meths[i].getReturnType();
            changed = checkTypeChange(ret, buildDir, refFile);
            if (changed) return true;
            Class<?>[] pars = meths[i].getParameterTypes();
            for (int j = 0; j < pars.length; j++) {
                changed = checkTypeChange(pars[j], buildDir, refFile);
                if (changed) return true;
            }
        }
        return false;
    }

    /**
     * Checks if a class has been modified since the last modification of a
     * reference file.
     * @throws MojoExecutionException 
     */
    public boolean checkTypeChange(Class<?> clazz, File buildDir, File refFile) throws MojoExecutionException {
        if (!clazz.isPrimitive()) {
            ClassLoader cl = clazz.getClassLoader();
            if (cl == loader) {
                if (clazz.isArray()) return checkTypeChange(getArrayType(clazz), buildDir, refFile);
                String path = clazz.getName().replace('.', File.separatorChar) + ".class";
                File file = new File(buildDir, path);
                long lastMod = Long.MAX_VALUE;
                if (!file.exists()) {
                    URL url = cl.getResource(path);
                    if (url == null) throw new MojoExecutionException("Can't get URL for webservice class '" + clazz.getName() + "' from jar file.");
                    else {
                        try {
                            JarURLConnection con = (JarURLConnection) url.openConnection();
                            lastMod = con.getJarEntry().getTime();
                        } catch (IOException x) {
                            throw new MojoExecutionException("Can't get modification time for webservice class '" + clazz.getName() + "' from jar file.");
                        }
                    }
                } else {
                    lastMod = file.lastModified();
                }
                if (refFile.lastModified() < lastMod) return true;
                if (clazz.isInterface()) {
                    Class<?>[] itfs = clazz.getInterfaces();
                    for (int i = 0; i < itfs.length; i++) {
                        boolean changed = checkTypeChange(itfs[i], buildDir, refFile);
                        if (changed) return true;
                    }
                } else {
                    Class<?> sup = clazz.getSuperclass();
                    boolean changed = checkTypeChange(sup, buildDir, refFile);
                    if (changed) return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the root component type of an array (with arbitrary dimensions)
     */
    public static Class<?> getArrayType(Class<?> clazz) {
        if (clazz.isArray()) return getArrayType(clazz.getComponentType());
        else return clazz;
    }

    /**
     * Checks if the method's return or parameter types contain an interface
     * (which isn't supported by JAXB)
     */
    public static boolean hasInterfaceType(Method method) {
        if (method.getReturnType().isInterface()) return true;
        Class<?>[] types = method.getParameterTypes();
        for (Class<?> type : types) {
            if (isInterfaceType(type)) return true;
        }
        return false;
    }

    /**
     * Checks if class is an interface or an array of an interface type
     */
    private static boolean isInterfaceType(Class<?> clazz) {
        if(clazz.isArray()) return isInterfaceType(clazz.getComponentType());
        return clazz.isInterface();
    }
    
    /**
     * Creates string representation of Java type (in source code form)
     */
    public static String getTypeString(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) type;
            Type rawType = paramType.getRawType();
            String str = getTypeString(rawType);
            Type[] typeArgs = paramType.getActualTypeArguments();
            str += "<";
            for (int i = 0; i < typeArgs.length; i++) {
                if (i > 0) str += ",";
                str += getTypeString(typeArgs[i]);
            }
            str += ">";
            return str;
        } else if (type instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType) type;
            Type compType = arrayType.getGenericComponentType();
            return getTypeString(compType) + "[]";
        } else if (type instanceof WildcardType) {
            WildcardType wildType = (WildcardType) type;
            Type[] lowerTypes = wildType.getLowerBounds();
            String str = "";
            if (lowerTypes.length == 1) {
                str += "? super ";
                str += getTypeString(lowerTypes[0]);
            } else if (lowerTypes.length > 1) throw new RuntimeException("Multiple lower bounds aren't supported");
            Type[] upperTypes = wildType.getUpperBounds();
            if (upperTypes.length == 1) {
                if (upperTypes[0] == Object.class) str += "?";
                else {
                    if (lowerTypes.length > 0) throw new RuntimeException("Lower bounds with upper bounds aren't supported");
                    str += "? extends ";
                    str += getTypeString(upperTypes[0]);
                }
            } else if (upperTypes.length > 1) throw new RuntimeException("Multiple upper bounds aren't supported");
            return str;
        } else if (type instanceof TypeVariable) {
            throw new RuntimeException("Not supported");
        } else {
            Class<?> clazz = (Class<?>) type;
            return clazz.getCanonicalName();
        }
    }

    /**
     * Reads Document from a XML file.
     */
    public static Document loadDoc(File file) throws  MojoExecutionException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setValidating(false);
            DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            Document doc = docBuilder.parse(file);
            return doc;
        } catch (Exception x) {
            throw new MojoExecutionException("Can't load XML document from file " + file.getAbsolutePath(), x);
        }
    }

}
