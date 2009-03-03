package de.schlund.pfixcore.generator.iwrpgen;

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
