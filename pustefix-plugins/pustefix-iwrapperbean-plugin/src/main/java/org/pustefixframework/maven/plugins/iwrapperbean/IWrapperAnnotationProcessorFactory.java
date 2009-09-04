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
package org.pustefixframework.maven.plugins.iwrapperbean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.apt.AnnotationProcessors;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

public class IWrapperAnnotationProcessorFactory implements AnnotationProcessorFactory{

    List<String> annoTypes;
    
    public IWrapperAnnotationProcessorFactory() {
        annoTypes=new ArrayList<String>();
        annoTypes.add("de.schlund.pfixcore.generator.annotation.IWrapper");
        annoTypes.add("de.schlund.pfixcore.generator.annotation.Caster");
        annoTypes.add("de.schlund.pfixcore.generator.annotation.Param");
        annoTypes.add("de.schlund.pfixcore.generator.annotation.PostCheck");
        annoTypes.add("de.schlund.pfixcore.generator.annotation.PreCheck");
        annoTypes.add("de.schlund.pfixcore.generator.annotation.Property");
        annoTypes.add("de.schlund.pfixcore.generator.annotation.Transient");
    }
    
    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> decls, AnnotationProcessorEnvironment env) {
        AnnotationProcessor result;
        if(decls.isEmpty()) result=AnnotationProcessors.NO_OP;
        else result=new IWrapperAnnotationProcessor(env);
        return result;
    }
    
    public Collection<String> supportedOptions() {
        return Collections.emptyList();
    }
    
    public Collection<String> supportedAnnotationTypes() {
        return annoTypes;
    }
    
}
