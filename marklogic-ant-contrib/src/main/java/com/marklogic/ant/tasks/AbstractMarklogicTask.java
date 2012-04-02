package com.marklogic.ant.tasks;

import com.marklogic.ant.types.Connection;
import com.marklogic.ant.types.ConnectionImpl;
import com.marklogic.ant.types.XCCSessionFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
public abstract class AbstractMarklogicTask extends Task {

    private XCCSessionFactory sessionFactory;

    protected String database;

    private Connection connection;

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setConnectionRef(String connectionRef) {
        Object o = getProject().getReference(connectionRef);
        if(o == null) {
            throw new BuildException("Reference does not exist.");
        }
        if(o instanceof ConnectionImpl) {
            this.connection = (Connection) o;
        } else {
            throw new BuildException("Reference is not to a connection object.");
        }
    }

    public XCCSessionFactory getXccSessionFactory() {
        if(sessionFactory == null) {
            this.sessionFactory = new XCCSessionFactory(connection);
        }
        return sessionFactory;
    }

    public Connection getConnection() {
        return connection;
    }
}
