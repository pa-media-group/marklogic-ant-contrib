package com.marklogic.ant.tasks;

import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.Session;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.Task;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
public abstract class AbstractMarklogicTask extends Task {

    /**
     * The host MarkLogic Server is running on.
     */
    protected String host = "localhost";

    /**
     * The host MarkLogic Server is running on.
     */
    protected String username = "admin";

    /**
     * The host MarkLogic Server is running on.
     */
    protected String password = "admin";

    /**
     * The XDBC port used for install purposes.
     */
    protected int xdbcPort = 8998;

    /**
     * The database to be used for XDBC connections.
     */
    protected String database;

    public void setHost(String host) {
        this.host = host;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setXdbcPort(int xdbcPort) {
        this.xdbcPort = xdbcPort;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    protected Session getXccSession() {
        return getXccSession(database);
    }

    protected Session getXccSession(final String database) {
        ContentSource cs;
        if (StringUtils.isBlank(database)) {
            cs = ContentSourceFactory.newContentSource(host, xdbcPort, username, password);
        } else {
            cs = ContentSourceFactory.newContentSource(host, xdbcPort, username, password, database);
        }
        return cs.newSession();
    }

    protected Session getXccSession(final String database, final int port) {
        return ContentSourceFactory.newContentSource(host, port, username, password, database).newSession();
    }
}
