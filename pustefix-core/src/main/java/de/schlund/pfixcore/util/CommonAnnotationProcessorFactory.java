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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.apt.AnnotationProcessors;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

import de.schlund.pfixcore.generator.iwrpgen.IWrapperAnnotationProcessorFactory;

public class CommonAnnotationProcessorFactory implements AnnotationProcessorFactory {
  
    List<AnnotationProcessorFactory> factories;
    
    public CommonAnnotationProcessorFactory() {
        factories=new ArrayList<AnnotationProcessorFactory>();
        factories.add(new IWrapperAnnotationProcessorFactory());
    }
    
    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> decls, AnnotationProcessorEnvironment env) {
        AnnotationProcessor result=AnnotationProcessors.NO_OP;
        StringBuilder sb=new StringBuilder();
        for(AnnotationTypeDeclaration decl:decls) sb.append(decl.getQualifiedName()+" ");
        env.getMessager().printNotice("Requested annotations: "+sb.toString());
        if(!decls.isEmpty()) {
            for(AnnotationProcessorFactory factory:factories) {
                Collection<String> supTypes=factory.supportedAnnotationTypes();
                boolean matches=true;
                for(AnnotationTypeDeclaration decl:decls) {
                    if(!supTypes.contains(decl.getQualifiedName())) {
                        matches=false;
                        break;
                    }
                }
                if(matches) {
                    env.getMessager().printNotice("Matching factory: "+factory.getClass().getName());
                    result=factory.getProcessorFor(decls,env);
                    break;
                }
            }
        }
        if(result==AnnotationProcessors.NO_OP) env.getMessager().printNotice("No matching factory");
        return result;
    }
    
    public Collection<String> supportedOptions() {
        List<String> list=new ArrayList<String>();
        for(AnnotationProcessorFactory factory:factories) {
            list.addAll(factory.supportedOptions());
        }
        return list;
    }
    
    public Collection<String> supportedAnnotationTypes() {
        List<String> list=new ArrayList<String>();
        for(AnnotationProcessorFactory factory:factories) {
            list.addAll(factory.supportedAnnotationTypes());
        }
        return list;
    }
    
}
