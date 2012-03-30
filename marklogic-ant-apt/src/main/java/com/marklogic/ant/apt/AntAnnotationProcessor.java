package com.marklogic.ant.apt;

import com.marklogic.ant.annotation.AntTask;
import com.marklogic.ant.annotation.AntType;
import com.sun.mirror.apt.*;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.Declaration;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
public class AntAnnotationProcessor implements AnnotationProcessor {

    private AnnotationProcessorEnvironment environment;

    private AnnotationTypeDeclaration antTaskDeclaration;
    private AnnotationTypeDeclaration antTypeDeclaration;

    public AntAnnotationProcessor(AnnotationProcessorEnvironment environment) {
        this.environment = environment;
        this.antTaskDeclaration = (AnnotationTypeDeclaration)
                environment.getTypeDeclaration(AntTask.class.getCanonicalName());
        this.antTypeDeclaration = (AnnotationTypeDeclaration)
                environment.getTypeDeclaration(AntType.class.getCanonicalName());
    }

    public void process() {
        /* Create Root Document */
        Element documentRoot = new Element("antlib");
        documentRoot.addNamespaceDeclaration("current", "ant:current");

        Collection<Declaration> declarations = environment.getDeclarationsAnnotatedWith(antTypeDeclaration);
        for (Declaration declaration : declarations) {
            if( declaration.getAnnotation(AntType.class) != null) {
                processAntTypeAnnotation(declaration, documentRoot);
            }
        }

        declarations = environment.getDeclarationsAnnotatedWith(antTaskDeclaration);
        for (Declaration declaration : declarations) {
            if( declaration.getAnnotation(AntTask.class) != null) {
                processAntTaskAnnotation(declaration, documentRoot);
            }
            if( declaration.getAnnotation(AntType.class) != null) {
                processAntTypeAnnotation(declaration, documentRoot);
            }
        }

        generateAntlibResource(new Document(documentRoot));
    }

    private void generateAntlibResource(Document document) {
        checkNotNull(document);

        try {
            PrintWriter generatedSourceFile = environment.getFiler()
                    .createTextFile(Filer.Location.SOURCE_TREE, "com.marklogic.ant", new File("antlib.xml"), "UTF-8");
            generatedSourceFile.write(document.toXML());
        } catch (FileNotFoundException e) {
            environment.getMessager().printError("Unable to create: " + "com/marklogic/ant/antlib.xml");
        } catch (IOException e) {
            environment.getMessager().printError("Unable to create: " + "com/marklogic/ant/antlib.xml");
        }
    }

    private void processAntTaskAnnotation(Declaration declaration, final Element parent) {
        AntTask task = declaration.getAnnotation(AntTask.class);
        Element taskDefElement = new Element("taskdef");
        taskDefElement.addAttribute(new Attribute("name", task.value()));
        taskDefElement.addAttribute(new Attribute("classname", declaration.toString()));
        parent.appendChild(taskDefElement);
    }

    private void processAntTypeAnnotation(Declaration declaration, final Element parent) {
        AntType type = declaration.getAnnotation(AntType.class);
        Element taskDefElement = new Element("typedef");
        taskDefElement.addAttribute(new Attribute("name", type.value()));
        taskDefElement.addAttribute(new Attribute("classname", declaration.toString()));
        parent.appendChild(taskDefElement);
    }
}
