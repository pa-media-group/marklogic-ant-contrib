package com.marklogic.ant.conditions;

import com.marklogic.ant.annotation.AntType;
import com.marklogic.ant.tasks.AbstractBootstrapTask;
import com.marklogic.xcc.AdhocQuery;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.condition.Condition;

import java.util.logging.Level;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
@AntType("canConnectToBootstrap")
public class BootstrapCondition extends AbstractBootstrapTask implements Condition {

    private boolean canConnectToBootstrapServer() {
        boolean success = false;
        /* Try and get session */
        Session session = getXccSession();
        if (session != null) {
            session.getLogger().setLevel(Level.OFF);
            /* Attempt simple xquery */
            AdhocQuery q = session.newAdhocQuery(getBootstrapExecuteQuery());
            try {
                session.submitRequest(q);
                success = true;
            } catch (RequestException ignored) {
            } finally {
                session.close();
            }
        }
        return success;
    }

    public boolean eval() throws BuildException {
        return canConnectToBootstrapServer();
    }

    @Override
    protected String getBootstrapExecuteQuery() throws BuildException {
        return "xquery version \"1.0-ml\";\n1";
    }

}