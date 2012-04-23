package com.marklogic.ant.apt;

import com.google.common.collect.Lists;
import com.marklogic.ant.annotation.AntTask;
import com.marklogic.ant.annotation.AntType;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
public class AntConfigurationBuilder {

    private List<javax.lang.model.element.Element> types = Lists.newLinkedList();
    private List<javax.lang.model.element.Element> tasks = Lists.newLinkedList();
    private ProcessingEnvironment env;

    public AntConfigurationBuilder(ProcessingEnvironment processingEnv) {
        env = processingEnv;
    }

    public void addTask(javax.lang.model.element.Element task) {
        tasks.add(task);
    }

    public void addType(javax.lang.model.element.Element type) {
        types.add(type);
    }

    public void build() {
        /* Create Root Document */
        Element documentRoot = new Element("antlib");
        documentRoot.addNamespaceDeclaration("current", "ant:current");

        for (javax.lang.model.element.Element typeElement : types) {
            processAntTypeAnnotation(typeElement, documentRoot);
        }

        for (javax.lang.model.element.Element taskElement : tasks) {
            processAntTaskAnnotation(taskElement, documentRoot);
        }

        generateAntlibResource(new Document(documentRoot));
    }

    private void processAntTaskAnnotation(javax.lang.model.element.Element declaration, final Element parent) {
        AntTask task = declaration.getAnnotation(AntTask.class);
        Element taskDefElement = new Element("taskdef");
        taskDefElement.addAttribute(new Attribute("name", task.value()));
        taskDefElement.addAttribute(new Attribute("classname", declaration.toString()));
        parent.appendChild(taskDefElement);
    }

    private void processAntTypeAnnotation(javax.lang.model.element.Element declaration, final Element parent) {
        AntType type = declaration.getAnnotation(AntType.class);
        Element taskDefElement = new Element("typedef");
        taskDefElement.addAttribute(new Attribute("name", type.value()));
        taskDefElement.addAttribute(new Attribute("classname", declaration.toString()));
        parent.appendChild(taskDefElement);
    }

    private void generateAntlibResource(Document document) {
        checkNotNull(document);

        try {
            Filer filer = env.getFiler();
            FileObject fileObject = filer.createResource(
                    StandardLocation.CLASS_OUTPUT, "com.marklogic.ant", "antlib.xml");
            OutputStreamWriter writer = new OutputStreamWriter(fileObject.openOutputStream(), "UTF-8");
            try {
                writer.write(document.toXML());
            } finally {
                writer.close();
            }
        } catch (FileNotFoundException e) {
            env.getMessager()
                    .printMessage(Diagnostic.Kind.ERROR, "Unable to create: com/marklogic/ant/antlib.xml - " +
                            e.getMessage());
        } catch (IOException e) {
            env.getMessager()
                    .printMessage(Diagnostic.Kind.ERROR, "Unable to create: com/marklogic/ant/antlib.xml - " +
                            e.getMessage());
        }
    }

}
