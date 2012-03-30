package com.marklogic.ant.apt;

import com.google.common.collect.ImmutableList;
import com.marklogic.ant.annotation.AntTask;
import com.marklogic.ant.annotation.AntType;
import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.apt.AnnotationProcessors;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
public class AntAnnotationProcessorFactory implements AnnotationProcessorFactory {
    public Collection<String> supportedOptions() {
        return Collections.emptyList();
    }

    public Collection<String> supportedAnnotationTypes() {
        return ImmutableList.of(AntTask.class.getCanonicalName(), AntType.class.getCanonicalName());
    }

    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> annotationTypeDeclarations, AnnotationProcessorEnvironment annotationProcessorEnvironment) {
        AnnotationProcessor result;
        if (annotationTypeDeclarations.isEmpty()) {
            result = AnnotationProcessors.NO_OP;
        } else {
            result = new AntAnnotationProcessor(annotationProcessorEnvironment);
        }
        return result;
    }
}
