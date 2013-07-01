package com.marklogic.ant.types;

import com.marklogic.ant.annotation.AntType;
import org.apache.tools.ant.types.DataType;

/**
 * @author Steven Robinson
 */
@AntType("collection")
public class Collection extends DataType {

    private String uri;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

}
