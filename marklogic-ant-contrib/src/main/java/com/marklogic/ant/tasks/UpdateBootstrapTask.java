package com.marklogic.ant.tasks;

import com.marklogic.AntHelper;
import com.marklogic.ant.annotation.AntTask;
import com.marklogic.install.xquery.XQueryDocumentBuilder;
import com.marklogic.install.xquery.XQueryModule;
import com.marklogic.install.xquery.XQueryModuleAdmin;
import com.marklogic.install.xquery.XQueryModuleXDMP;
import com.marklogic.xcc.AdhocQuery;
import com.marklogic.xcc.Content;
import com.marklogic.xcc.ContentFactory;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;
import org.apache.tools.ant.BuildException;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
@AntTask("updateBootstrap")
public class UpdateBootstrapTask extends AbstractBootstrapTask {


    @Override
    public void execute() throws BuildException {
        Session session = getXccSessionFactory().getXccSession();
        System.out.println("Bootstrap session is to " + session.getConnectionUri().toASCIIString());

        AdhocQuery q = session.newAdhocQuery("xquery version \"1.0-ml\";\n1");
        boolean success = false;
        try {
            session.submitRequest(q);
            success = true;
        } catch (RequestException e) {
        } finally {
            session.close();
        }

        if (success) {
            System.out.println("Updating bootstrap.");
            updateModules();
        } else {
            throw new BuildException("Cannot connect to bootstrap server to update it.");
        }
    }

    @Override
    protected String getBootstrapExecuteQuery() throws BuildException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
