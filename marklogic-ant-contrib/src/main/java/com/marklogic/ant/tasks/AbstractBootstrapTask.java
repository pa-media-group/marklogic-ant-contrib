package com.marklogic.ant.tasks;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.tools.ant.BuildException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
public abstract class AbstractBootstrapTask extends AbstractMarklogicTask {

    protected static final String XQUERY_PROLOG = "xquery version '1.0-ml';\n";

    protected static final String ML_ADMIN_MODULE_IMPORT = "import module namespace admin = 'http://marklogic.com/xdmp/admin' at '/MarkLogic/admin.xqy';\n";

    /**
     * The port used to bootstrap MarkLogic Server.
     */
    protected int bootstrapPort = 8000;

    /**
     * The MarkLogic Installer XDBC server name.
     */
    protected String xdbcName = "MarkLogic-Installer-XDBC";

    /**
     * The modules database used to bootstrap MarkLogic Server.
     */
    protected String xdbcModulesDatabase;

    /**
     * The MarkLogic Installer XDBC module root setting.
     */
    protected String xdbcModuleRoot = "/";

    public void setXdbcModulesDatabase(String xdbcModulesDatabase) {
        this.xdbcModulesDatabase = xdbcModulesDatabase;
    }

    public void setBootstrapPort(int bootstrapPort) {
        this.bootstrapPort = bootstrapPort;
    }

    public void setXdbcName(String xdbcName) {
        this.xdbcName = xdbcName;
    }

    public void setXdbcModuleRoot(String xdbcModuleRoot) {
        this.xdbcModuleRoot = xdbcModuleRoot;
    }

    @Override
    public void execute() throws BuildException {
        System.out.println("Executing " + getTaskName());
        executeBootstrapQuery(getBootstrapExecuteQuery());
    }

    public HttpResponse executeBootstrapQuery(final String query) throws BuildException {
        /*
         * Build Query Parameters
         */
        List<NameValuePair> queryParameters = new ArrayList<NameValuePair>();
        queryParameters.add(new BasicNameValuePair("queryInput", query));

        URI uri;
        try {
            uri = URIUtils.createURI("http", this.host, bootstrapPort, "/use-cases/eval2.xqy",
                    URLEncodedUtils.format(queryParameters, "UTF-8"), null);
        } catch (URISyntaxException e) {
            throw new BuildException("Invalid URI", e);
        }

        HttpPost httpPost = new HttpPost(uri);

        HttpResponse response;
        try {
            response = getHttpClient().execute(httpPost);
        } catch (Exception e) {
            throw new BuildException("Error executing post", e);
        }

        if (response.getStatusLine().getStatusCode() != 200) {
            throw new BuildException("Execute of bootstrap query failed - " + response.getStatusLine());
        }

        return response;
    }

    protected abstract String getBootstrapExecuteQuery() throws BuildException;

    protected HttpClient getHttpClient() {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.getCredentialsProvider().setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(username, password));
        httpClient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");

        return httpClient;
    }
}
