package com.marklogic.ant.types;

import com.marklogic.ant.annotation.AntType;
import com.marklogic.xcc.ContentCapability;
import org.apache.tools.ant.types.DataType;

/**
 * @author Gavin Haydon <gavin.haydon@pressassociation.com>
 */
@AntType("permission")
public class Permission extends DataType {

    public enum ContentCapabilityAttribute {
        execute(ContentCapability.EXECUTE),
        insert(ContentCapability.INSERT),
        read(ContentCapability.READ),
        update(ContentCapability.UPDATE);

        private ContentCapability capability;

        private ContentCapabilityAttribute(ContentCapability capability) {
            this.capability = capability;
        }

        public ContentCapability getCapability() {
            return capability;
        }
    }

    /**
     * The role name for this permission.
     */
    private String role;

    /**
     * The capability for this permission.
     */
    private ContentCapabilityAttribute capability;

    public void setRole(String role) {
        this.role = role;
    }

    public void setCapability(ContentCapabilityAttribute capability) {
        this.capability = capability;
    }

    public ContentCapability getCapability() {
        return capability.getCapability();
    }

    public String getRole() {
        return role;
    }

}
