package com.marklogic.ant.types;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
public interface Connection {
    String getHost();

    String getUsername();

    String getPassword();

    int getPort();
}
