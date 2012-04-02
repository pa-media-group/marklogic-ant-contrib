package com.marklogic.ant.types;

import com.marklogic.ant.annotation.AntType;
import org.apache.tools.ant.types.DataType;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
@AntType("connection")
public class ConnectionImpl extends DataType implements Connection {
    /**
     * The host MarkLogic Server is running on.
     */
    private String host = "localhost";

    /**
     * The host MarkLogic Server is running on.
     */
    private String username = "admin";

    /**
     * The host MarkLogic Server is running on.
     */
    private String password = "admin";

    /**
     * The port used for the connection.
     */
    private int port = 8998;

    public ConnectionImpl() {
    }

    public ConnectionImpl(Connection connection) {
        this.host = connection.getHost();
        this.username = connection.getUsername();
        this.password = connection.getPassword();
        this.port = connection.getPort();
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
