package com.marklogic.ant.types;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
public class AbstractConnectionFactory {

    protected Connection connection;

    public AbstractConnectionFactory(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

}
