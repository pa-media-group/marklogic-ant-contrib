package com.marklogic.ant.tasks;

import com.marklogic.ant.types.Environment;
import com.marklogic.xcc.AdhocQuery;
import com.marklogic.xcc.Request;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;
import com.marklogic.xcc.types.ValueType;
import nu.xom.Element;
import org.apache.tools.ant.BuildException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
public class AbstractDeploymentTask extends AbstractMarklogicTask {

    protected static final String ACTION_RESTART = "restart";

    /**
     * Namespace for the install configuration block
     */
    public static final String INSTALL_NS = "http://www.marklogic.com/ps/install/config.xqy";

    /**
     * The default encoding to use for the generated Ant build.
     */
    public static final String UTF_8 = "UTF-8";

    private Environment environment;

    /**
     * The installation module path.  This path is relative to the
     * xdbcModuleRoot.
     * <p/>
     * For example if the xdbcModuleRoot is set to /modules/,
     * and the installation module is deployed at /modules/install/install.xqy,
     * set the installModule to install/install.xqy.
     * <p/>
     * The default value is just "install.xqy" since we assume that the xdbc server
     * will point directly to the installation xquery.
     */
    protected String installModule = "install.xqy";

    protected Map<String, Session> sessions = new HashMap<String, Session>();

    @Override
    public void execute() throws BuildException {
        checkNotNull(environment);
        super.execute();
    }

    protected Session getSession(final String database) {
        Session s = sessions.get(database);
        if (s == null) {
            s = getXccSession(database);
            sessions.put(database, s);
        }
        return s;
    }

    protected void executeAction(final String action) throws BuildException {
        System.out.println("Executing ".concat(action));
        try {
            ResultSequence rs = executeInstallAction(action, installModule);
            System.out.println(rs.asString());
        } catch (RequestException e) {
            throw new BuildException("XCC request error: " + e.getLocalizedMessage());
        }
    }

    protected ResultSequence executeInstallAction(String action, String module) throws RequestException {
        Session session = this.getXccSession();
        Request request = session.newModuleInvoke(module);
        request.setNewStringVariable("action", action);
        request.setNewStringVariable("environ", environment.getName());
        request.setNewVariable("delete-data", ValueType.XS_BOOLEAN, false);
        request.setNewStringVariable("configuration-string", environment.getConfigurationXml());
        return session.submitRequest(request);
    }

    /**
     * Returns the string representation of the specified file
     *
     * @param file The file to be loaded
     * @return
     * @throws IOException
     */
    protected String getFileAsString(final File file) throws IOException {
        StringBuilder buffer = new StringBuilder((int) file.length());
        BufferedReader reader = new BufferedReader(new FileReader(file));
        char[] buf = new char[1024];
        int numRead;
        while ((numRead = reader.read(buf)) != -1) {
            buffer.append(buf, 0, numRead);
        }
        reader.close();
        return buffer.toString();
    }

    /**
     * Restart marklogic HTTP, XDBC servers.
     *
     * @throws BuildException
     */
    protected void restartServers() throws BuildException {
        executeAction(ACTION_RESTART);

        /**
         * Ensure server is ready
         */
        int count = 10;
        boolean success = false;
        RequestException lastException = null;
        while (!success && count-- > 0) {
            /* Try and get session */
            Session session = getXccSession();
            if (session != null) {
                /* Attempt simple xquery */
                AdhocQuery q = session.newAdhocQuery("xquery version \"1.0-ml\";\n1");
                try {
                    session.submitRequest(q);
                    success = true;
                } catch (RequestException e) {
                    lastException = e;
                } finally {
                    session.close();
                }
            }

            if (!success) {
                try {
                    System.out.println("Waiting for server to be ready.");
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
        }

        if (!success) {
            throw new BuildException("Job timed out waiting for servers on host to restart.", lastException);
        }
    }

    /**
     * Get the server configuration with name attribute of value parameter
     *
     * @param value
     * @return
     */
    protected Element getServer(String value) {
        checkNotNull(value);
        for (Element cfg : environment.getServers()) {
            if (value.equals(cfg.getAttribute("name").getValue())) {
                return cfg;
            }
        }
        throw new BuildException("Unknown server configuration: " + value);
    }

    public void addConfiguredEnvironment(Environment environment) {
        checkNotNull(environment);
        if (environment.isReference()) {
            this.environment = (Environment) environment.getRefid().getReferencedObject();
        } else {
            this.environment = environment;
        }
    }

    public Environment getEnvironment() {
        return environment;
    }
}