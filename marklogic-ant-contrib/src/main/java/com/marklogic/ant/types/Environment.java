package com.marklogic.ant.types;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.marklogic.AntHelper;
import com.marklogic.ant.annotation.AntType;
import com.marklogic.predicates.XPathMatchingPredicate;
import nu.xom.*;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.types.DataType;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
@AntType("environment")
public class Environment extends DataType {

    /**
     * Namespace for the install configuration block
     */
    public static final String INSTALL_NS = "http://www.marklogic.com/ps/install/config.xqy";

    /**
     * The name of the environment.
     */
    private String name;

    /**
     * The name of the application being installed.
     */
    private String applicationName;

    /**
     * The title of the application.
     */
    private String title;

    /**
     * The Filesystem Root for the application.
     */
    private String filesystemRoot = "/";

    /**
     * Set of pipeline configuration XML to be deployed.
     */
    private List<ResourceFileSet> pipelineResources = new ArrayList<ResourceFileSet>();

    /**
     * Set of resources to be deployed.
     */
    private List<ResourceFileSet> resources = new ArrayList<ResourceFileSet>();

    /**
     * XML Resource describing server installation
     */
    private File installationDescriptor;

    //    /**
//     * Set of database configurations.
//     */
    private List<Element> databases;

    private Document installationDescriptorDocument;

    private List<Element> servers;

    private String configurationXml;

//    /**
//     * Module Invoker configuration.
//     */
//    @MojoParameter
//    protected PlexusConfiguration moduleInvokes;

    public Document getInstallationDescriptorDocument() {
        if (installationDescriptorDocument == null) {
            try {
                /* Replace Properties */
                String descriptor = FileUtils.readFileToString(installationDescriptor, "UTF-8");
                descriptor = PropertyHelper.getPropertyHelper(getProject()).replaceProperties(descriptor);

                /* Create XOM */
                Builder parser = new Builder();
                installationDescriptorDocument = parser.build(
                        new BufferedReader(new StringReader(descriptor)));
            } catch (ValidityException e) {
                throw Throwables.propagate(e);
            } catch (ParsingException e) {
                throw Throwables.propagate(e);
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }

            checkNotNull(installationDescriptorDocument);
        }
        return installationDescriptorDocument;
    }

    /**
     * Iterate over node list returning only those nodes instances that are Elements
     *
     * @param nodes
     * @return
     */
    private static Iterator<Element> elementIterator(final Nodes nodes) {
        return new AbstractIterator<Element>() {
            private int index = 0;

            @Override
            protected Element computeNext() {
                while (index < nodes.size()) {
                    Node currentNode = nodes.get(
                            checkElementIndex(index++, nodes.size()));
                    if (currentNode instanceof Element) {
                        return (Element) currentNode;
                    }
                }
                return endOfData();
            }
        };
    }

    public List<Element> getDatabases() {
        if (databases == null) {
            Nodes nodes = getInstallationDescriptorDocument().query("//databases/database");
            databases = ImmutableList.copyOf(elementIterator(nodes));
        }
        return databases;
    }

    public List<Element> getServers() {
        if (servers == null) {
            Nodes nodes = getInstallationDescriptorDocument().query("//servers/server");
            servers = ImmutableList.copyOf(elementIterator(nodes));
        }
        return servers;
    }

    public String getInstallationDescriptorPath() {
        return installationDescriptor.getPath();
    }

    public void setInstallationDescriptor(File installationDescriptor) {
        checkNotNull(installationDescriptor);
        this.installationDescriptor = installationDescriptor;
    }

    /**
     * @return the name of the environment.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the set of resources to be deployed
     */
    public List<ResourceFileSet> getResources() {
        return ImmutableList.copyOf(resources);
    }

//    /**
//     * @return the set of modules to be invoked and the servers to invoke them against.
//     */
//    public PlexusConfiguration getModuleInvokes() {
//        return moduleInvokes;
//    }

    public void setApplicationName(String applicationName) {
        checkNotNull(applicationName);
        this.applicationName = applicationName;
    }

    /**
     * @return the application name
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * @return the application title
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        checkNotNull(title);
        this.title = title;
    }

    /**
     * @return the filesystem root property
     */
    public String getFilesystemRoot() {
        return filesystemRoot;
    }

    public void setFilesystemRoot(String filesystemRoot) {
        checkNotNull(filesystemRoot);
        this.filesystemRoot = filesystemRoot;
    }

    /**
     * @return the set of pipeline configuration xml to be deployed
     */
    public List<ResourceFileSet> getPipelineResources() {
        return ImmutableList.copyOf(pipelineResources);
    }

    public void addPipelineResources(ResourceFileSet pipelineResource) {
        checkNotNull(pipelineResource);
        this.pipelineResources.add(pipelineResource);
    }

    public void addResources(ResourceFileSet resource) {
        checkNotNull(resource);
        this.resources.add(resource);
    }

    public String getConfigurationXml() {
        if (configurationXml == null) {
            System.out.println("Using configuration : ".concat(installationDescriptor.getPath()));

            Element documentRoot = new Element("install", INSTALL_NS);

            Element namedElement = new Element(name, INSTALL_NS);
            documentRoot.appendChild(namedElement);

            Element applicationElement = new Element("application", INSTALL_NS);
            namedElement.appendChild(applicationElement);
            if (applicationName !=  null) {
                applicationElement.addAttribute(new Attribute("name", applicationName));
            }
            applicationElement.addAttribute(new Attribute("title", title));
            applicationElement.addAttribute(new Attribute("filesystem-root", filesystemRoot));

            for (Element database : getDatabases()) {
                database = (Element) database.copy();
                try {
                    updateNamespace(database);
                } catch (IOException e) {
                    throw Throwables.propagate(e);
                }
                namedElement.appendChild(database);
            }

            Element serversElement = new Element("servers", INSTALL_NS);
            namedElement.appendChild(serversElement);
            for (Element server : getServers()) {
                server = (Element) server.copy();
                try {
                    updateNamespace(server);
                } catch (IOException e) {
                    throw Throwables.propagate(e);
                }
                serversElement.appendChild(server);
            }

            Document doc = new Document(documentRoot);

            if (AntHelper.getAntHelper(getProject()).isVerbose()) {
                try {
                    prettyPrint(doc, System.out);
                } catch (IOException e) {
                    throw Throwables.propagate(e);
                }
            }
            configurationXml = doc.toXML();
        }
        return configurationXml;
    }

    public Element findServer(final String name) throws NoSuchElementException {
        return Iterators.find(getServers().iterator(),
                XPathMatchingPredicate.match(String.format(".[@name = '%s']", name)));
    }

    public Optional<Element> tryFindServer(final String name) {
        return Iterators.tryFind(getServers().iterator(),
                XPathMatchingPredicate.match(String.format(".[@name = '%s']", name)));
    }

    /**
     * Iterate over node list returning only those nodes instances that are Elements
     *
     * @param node Parent node
     * @return Child node iterator
     */
    private static Iterator<Node> nodeIterator(final Node node) {
        return new AbstractIterator<Node>() {
            private int index = 0;

            private final int limit = node.getChildCount();

            @Override
            protected Node computeNext() {
                while (index < limit) {
                    return node.getChild(checkElementIndex(index++, limit));
                }
                return endOfData();
            }
        };
    }

    private static void updateNamespace(Node node) throws IOException {
        if (node instanceof Element) {
            ((Element) node).setNamespaceURI(INSTALL_NS);
        }

        Iterator<Node> iter = nodeIterator(node);
        while (iter.hasNext()) {
            updateNamespace(iter.next());
        }
    }

    private static void prettyPrint(Document document, OutputStream out)
            throws IOException {
        Serializer serializer = new Serializer(out, "UTF-8");
        serializer.setIndent(2);
        serializer.setMaxLength(72);
        serializer.write(document);
        serializer.flush();
    }
}
