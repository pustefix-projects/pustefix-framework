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
import java.io.FileOutputStream;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;


/**
 * Generates list with display names of all pages.
 *
 * @goal patch
 * @phase process-resources
 *
 * @requiresDependencyResolution compile
 */
public class LibraryPatchMojo extends AbstractMojo {

    /**
     * @parameter default-value="${project.build.directory}/classes"
     */
    private File outputDirectory;

    /** @parameter default-value="${project}" */
    private MavenProject mavenProject;

    public void execute() throws MojoExecutionException {
        
        if (!"war".equals(mavenProject.getPackaging())) {
            getLog().info("Skip plugin in project with '" + mavenProject.getPackaging() + "' packaging.");
            return;
        }
        
        
        if(!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }
       
        getProjectRuntimeClassLoader();
       
    }
    
    private void getProjectRuntimeClassLoader() throws MojoExecutionException {
        
        try {
            ClassPool classPool = ClassPool.getDefault();
            List<String> elements = mavenProject.getCompileClasspathElements();
            for (String element : elements) {
                classPool.insertClassPath(element);
            }
                            
            CtClass cl = classPool.get("com.icl.saxon.expr.FunctionProxy");
            if(!cl.isFrozen()) {
                CtMethod[] methods = cl.getDeclaredMethods();
                for (int i = 0; i < methods.length; i++) {
                    if (methods[i].getName().equals("call")) {
                        methods[i].instrument(new ExprEditor() {
                            public void edit(final MethodCall m) throws CannotCompileException {
                                if ("java.lang.reflect.Method".equals(m.getClassName()) && "invoke".equals(m.getMethodName())) {
                                    m.replace("{$_ = de.schlund.pfixxml.util.ExtensionFunctionUtils.invokeFunction($0, $$);}");
                                }
                            }
                        });
                    }
                }
            }
            byte[] bytes = cl.toBytecode();
            File outFile = new File(outputDirectory, "com/icl/saxon/expr/FunctionProxy.class");
            outFile.getParentFile().mkdirs();
            FileOutputStream outStream = new FileOutputStream(outFile);
            outStream.write(bytes);
            outStream.close();
            
        } catch (Exception x) {
            throw new MojoExecutionException("Can't create project runtime classloader", x);
        }
    }

}