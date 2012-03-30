package com.marklogic.ant.types;

import com.google.common.base.Preconditions;
import com.marklogic.ant.annotation.AntType;
import com.marklogic.xcc.DocumentFormat;
import org.apache.tools.ant.types.FileSet;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
@AntType("resourceFileSet")
public class ResourceFileSet extends FileSet {

    public enum DocumentFormatAttribute {
        binary(DocumentFormat.BINARY),
        text(DocumentFormat.TEXT),
        xml(DocumentFormat.XML),
        none(DocumentFormat.NONE);

        private DocumentFormat format;

        private DocumentFormatAttribute(DocumentFormat format) {
            this.format = format;
        }

        public DocumentFormat getFormat() {
            return format;
        }
    }

    /**
     * The database to load the specified resources into.
     */
    private String database;

    /**
     * The collection to load the specified resources into.
     */
    private List<String> collections = new ArrayList<String>();

    /**
     * The permissions the specified resources should have.
     */
    private List<Permission> permissions = new ArrayList<Permission>();

    /**
     * The formatAttribute of the specified resources.
     */
    private DocumentFormatAttribute formatAttribute = DocumentFormatAttribute.text;

    private String outputDirectory = "/";

    public void setDatabase(String database) {
        this.database = database;
    }

    public void addCollections(String collection) {
        Preconditions.checkNotNull(collection);
        this.collections.add(collection);
    }

    public void addPermission(Permission permission) {
        checkNotNull(permission);
        permissions.add(permission);
    }

    public void setFormat(DocumentFormatAttribute format) {
        this.formatAttribute = format;
    }

    /**
     * @return Collections to load resources into.
     */
    public String[] getCollections() {
        return collections.toArray(new String[collections.size()]);
    }

    /**
     * @return Database to load resources into.
     */
    public String getDatabase() {
        return database;
    }

    /**
     * @return Format of the specified resources (binary, text, xml).
     */
    public DocumentFormat getFormat() {
        return formatAttribute.getFormat();
    }

    /**
     * @return Permissions that should be applied to the resources.
     */
    public Permission[] getPermissions() {
        return permissions.toArray(new Permission[permissions.size()]);
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        checkNotNull(outputDirectory);
        this.outputDirectory = outputDirectory;
    }

}
