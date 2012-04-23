package com.marklogic.ant.apt;

import com.marklogic.ant.annotation.AntTask;
import com.marklogic.ant.annotation.AntType;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
@SupportedAnnotationTypes({"com.marklogic.ant.annotation.AntType", "com.marklogic.ant.annotation.AntTask"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class AntAnnotationProcessorJSR269 extends AbstractProcessor {

    private AntConfigurationBuilder builder;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        builder = new AntConfigurationBuilder(processingEnvironment);
    }

    @Override
    public boolean process(Set<? extends TypeElement> typeElements, RoundEnvironment roundEnvironment) {
        for (Element e : roundEnvironment.getElementsAnnotatedWith(AntTask.class)) {
            builder.addTask(e);
        }

        for (Element e : roundEnvironment.getElementsAnnotatedWith(AntType.class)) {
            builder.addType(e);
        }

        if (roundEnvironment.processingOver()) {
            builder.build();
        }

        return true;
    }
}
