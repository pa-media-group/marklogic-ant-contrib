package com.marklogic.ant.tasks;

import com.google.common.base.Optional;
import com.marklogic.ant.annotation.AntTask;
import com.marklogic.ant.types.ConnectionImpl;
import com.marklogic.ant.types.XCCSessionFactory;
import com.marklogic.xcc.ResultItem;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;
import nu.xom.Element;
import org.apache.tools.ant.BuildException;

import java.util.Iterator;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
@AntTask("invoke-module")
public class InvokeModuleTask extends AbstractInstallTask {

    /**
     * Name of module to be invoked
     */
    private Optional<String> module = Optional.absent();

    /**
     * Server on which to execute
     */
    private Optional<String> serverName = Optional.absent();

    public void setServerName(String server) {
        this.serverName = Optional.of(server);
    }

    public void setModule(String module) {
        this.module = Optional.of(module);
    }

    @Override
    public void execute() throws BuildException {
        if (!module.isPresent()) {
            throw new BuildException("Attribute 'module' is not present");
        }

        if (!serverName.isPresent()) {
            throw new BuildException("Attribute 'serverName' is not present");
        }

        Optional<Element> serverElement = getEnvironment().tryFindServer(serverName.get());

        if (!serverElement.isPresent()) {
            throw new BuildException(String.format("Could not find serverName named '%s'", serverName.get()));
        }

        /*
         * Get necessary properties from serverName to create XCC session
         */
        final String database = getEnvironment().getApplicationName()
                .concat("-")
                .concat(serverElement.get().getAttribute("database").getValue());
        final int port = Integer.parseInt(serverElement.get().getAttribute("port").getValue());

        ConnectionImpl connection = new ConnectionImpl(getConnection());
        connection.setPort(port);
        XCCSessionFactory factory = new XCCSessionFactory(connection);

        Session session = factory.getXccSession(database);
        getProject().log("Connecting to " + session.getConnectionUri().toString());
        getProject().log("Invoking module " + module.get());

        try {
            ResultSequence results = session.submitRequest(session.newModuleInvoke(module.get()));
            Iterator<ResultItem> iter = results.iterator();
            while (iter.hasNext()) {
                getProject().log(iter.next().asString());
            }
        } catch (RequestException e) {
            throw new BuildException(e.getLocalizedMessage(), e);
        }
    }
}
