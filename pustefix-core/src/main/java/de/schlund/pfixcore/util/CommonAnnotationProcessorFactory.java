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
