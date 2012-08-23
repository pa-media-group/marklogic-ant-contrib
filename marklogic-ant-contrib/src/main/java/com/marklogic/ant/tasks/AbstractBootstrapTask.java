package com.marklogic.ant.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.marklogic.xcc.Content;
import com.marklogic.xcc.ContentFactory;
import com.marklogic.xcc.Session;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.tools.ant.BuildException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import com.google.common.base.Optional;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
public abstract class AbstractBootstrapTask extends AbstractMarklogicTask {
    protected final String[] libraryPaths = {
            "/install.xqy"
            , "/lib/lib-app-server.xqy"
            , "/lib/lib-cpf.xqy"
            , "/lib/lib-database-add.xqy"
            , "/lib/lib-database-set.xqy"
            , "/lib/lib-database.xqy"
            , "/lib/lib-field.xqy"
            , "/lib/lib-trigger.xqy"
            , "/lib/lib-task.xqy"
            , "/lib/lib-index.xqy"
            , "/lib/lib-install.xqy"
            , "/lib/lib-load.xqy"
    };

	protected static final String XQUERY_PROLOG = "xquery version '1.0-ml';\n";

	protected static final String ML_ADMIN_MODULE_IMPORT = "import module namespace admin = 'http://marklogic.com/xdmp/admin' at '/MarkLogic/admin.xqy';\n";

	/**
	 * The port used to bootstrap MarkLogic Server.
	 */
	private Optional<Integer> bootstrapPort = Optional.of(8000);

	/**
	 * The config manager port used to query MarkLogic Server.
	 */
	private Optional<Integer> configPort = Optional.of(8002);

	/**
	 * The server used to gain an sid for bootstrap creation.
	 */
	private Optional<String> configServer = Optional.of("Admin");

	/**
	 * The MarkLogic group name.
	 */
	private Optional<String> group = Optional.of("Default");

	/**
	 * The MarkLogic Installer XDBC server name.
	 */
	protected String xdbcName = "MarkLogic-Installer-XDBC";

	/**
	 * The MarkLogic Installer XDBC module root setting.
	 */
	protected String xdbcModuleRoot = "/";
	
	public AbstractBootstrapTask() {
		/* Default the bootstrap database name */
		database = "InstallerModules";	
	}

	public void setBootstrapPort(int bootstrapPort) {
		this.bootstrapPort = Optional.of(bootstrapPort);
	}

	public void setConfigPort(int configPort) {
		this.bootstrapPort = Optional.of(configPort);
	}

	public void setConfigServer(String configServer) {
		this.configServer = Optional.of(configServer);
	}

	public void setGroup(String group) {
		this.group = Optional.of(group);
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

	public HttpResponse executeBootstrapQuery(final String query)
			throws BuildException {
		HttpResponse response;

		if (isMarkLogic5()) {
			System.out.println("Bootstrapping MarkLogic 5");
			response = executeML5BootstrapQuery(query);
		} else {
			System.out.println("Bootstrapping MarkLogic 4");
			response = executeML4BootstrapQuery(query);
		}

		if (response.getEntity() != null) {
			try {
				InputStream is = response.getEntity().getContent();
				System.out.println(IOUtils.toString(is));
			} catch (IOException e) {
				throw new BuildException("IO Error reading response", e);
			}
		}

		return response;
	}

	protected HttpResponse executeML4BootstrapQuery(String query)
			throws BuildException {
		HttpClient httpClient = this.getHttpClient();
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair("queryInput", query));

		URI uri;
		try {
			uri = URIUtils.createURI("http", getConnection().getHost(),
					bootstrapPort.get(), "/use-cases/eval2.xqy",
					URLEncodedUtils.format(qparams, "UTF-8"), null);
		} catch (URISyntaxException e1) {
			throw new BuildException("Invalid uri for bootstrap query", e1);
		}

		HttpPost httpPost = new HttpPost(uri);

		HttpResponse response;

		try {
			response = httpClient.execute(httpPost);
		} catch (Exception e) {
			throw new BuildException(
					"Error executing POST to create bootstrap server", e);
		}
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new BuildException("POST response failed: "
					+ response.getStatusLine());
		}

		return response;
	}

	protected HttpResponse executeML5BootstrapQuery(String query)
			throws BuildException {
		HttpClient httpClient = this.getHttpClient();
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair("group-id", group.get()));
		qparams.add(new BasicNameValuePair("format", "json"));

		URI uri;
		try {
			uri = URIUtils.createURI("http", getConnection().getHost(),
					configPort.get(),
					"/manage/v1/servers/" + configServer.get(),
					URLEncodedUtils.format(qparams, "UTF-8"), null);
		} catch (URISyntaxException e1) {
			throw new BuildException("Invalid uri for querying "
					+ configServer.get() + " for it's id", e1);
		}

		HttpGet httpGet = new HttpGet(uri);

		HttpResponse response;
		String sid = null;

		try {
			response = httpClient.execute(httpGet);
		} catch (Exception e) {
			throw new BuildException("Error executing GET " + uri, e);
		}
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new BuildException("Failed to GET " + uri + "\n"
					+ response.getStatusLine());
		}

		try {
			if (response.getEntity() != null) {
				InputStream is = response.getEntity().getContent();
				JSONObject json = new JSONObject(IOUtils.toString(is));
				sid = json.getJSONObject("server-default").getString("id");
			}
		} catch (Exception e) {
			throw new BuildException("Error parsing json response to get "
					+ configServer.get() + " server id", e);
		}

		if (sid == null) {
			throw new BuildException("Server id for " + configServer.get()
					+ " is null, aborting");
		}

		qparams.clear();
		qparams.add(new BasicNameValuePair("q", query));
		qparams.add(new BasicNameValuePair("resulttype", "xml"));
		qparams.add(new BasicNameValuePair("sid", sid));

		try {
			uri = URIUtils.createURI("http", getConnection().getHost(),
					bootstrapPort.get(), "/qconsole/endpoints/eval.xqy",
					URLEncodedUtils.format(qparams, "UTF-8"), null);
		} catch (URISyntaxException e1) {
			throw new BuildException("Invalid uri for bootstrap query", e1);
		}

		HttpPost httpPost = new HttpPost(uri);

		try {
			response = httpClient.execute(httpPost);
		} catch (Exception e) {
			throw new BuildException(
					"Error executing POST to create bootstrap server", e);
		}
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new BuildException("POST response failed: "
					+ response.getStatusLine());
		}

		Header[] headers = response.getHeaders("qconsole");
		if (headers != null && headers.length > 0) {
			try {
				JSONObject json = new JSONObject(headers[0].getValue());
				if (json.getString("type").equals("error")) {
					StringBuilder b = new StringBuilder(
							"Failed to execute query ...\n");
					if (response.getEntity() != null) {
						InputStream is = response.getEntity().getContent();
						JSONObject jsonError = new JSONObject(
								IOUtils.toString(is));
						b.append(XML.toString(jsonError));
					}
					throw new BuildException(b.toString());
				}
			} catch (JSONException e) {
				throw new BuildException("Unable to parse json error header", e);
			} catch (IOException ioe) {
				throw new BuildException("IOException parsing json error", ioe);
			}
		}

		return response;
	}

	protected abstract String getBootstrapExecuteQuery() throws BuildException;

	protected HttpClient getHttpClient() {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials(
				AuthScope.ANY,
				new UsernamePasswordCredentials(getConnection().getUsername(),
						getConnection().getPassword()));
		httpClient.getParams().setParameter(
				CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");

		return httpClient;
	}

	protected boolean isMarkLogic5() throws BuildException {
		HttpClient httpClient = this.getHttpClient();
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		URI uri;

		try {
			uri = URIUtils.createURI("http", getConnection().getHost(),
					bootstrapPort.get(), "/qconsole",
					URLEncodedUtils.format(qparams, "UTF-8"), null);
		} catch (URISyntaxException e1) {
			throw new BuildException("Invalid uri for qconsole probe", e1);
		}

		HttpGet httpGet = new HttpGet(uri);

		HttpResponse response;
		try {
			response = httpClient.execute(httpGet);
			if (response.getEntity() != null) {
				response.getEntity().getContent();
			}
		} catch (Exception e) {
			throw new BuildException("Error executing GET to proble qconsole",
					e);
		}

		System.out.println("Probe got " + response.getStatusLine());

		return (response.getStatusLine().getStatusCode() == 200);
	}

    protected void updateModules() {
        Session session = getXccSessionFactory().getXccSession();
        System.out.println("Bootstrap session is to " + session.getConnectionUri().toASCIIString());

        if (!"file-system".equalsIgnoreCase(database)) {
            session = getXccSessionFactory().getXccSession(database);

            ClassLoader loader = UpdateBootstrapTask.class.getClassLoader();
            for (String path : libraryPaths) {
                System.out.println("Uploading " + path);
                try {
                    Content cs = ContentFactory.newContent(path, loader.getResource("xquery" + path), null);
                    session.insertContent(cs);
                } catch (Exception e) {
                    throw new BuildException("Failed to insert required library. " + e.getLocalizedMessage(), e);
                }
                session.commit();
            }
        } else {
            System.out.println("");
            System.out.println("***************************************************************");
            System.out.println("* Using filesystem modules location, ensure that install.xqy  *");
            System.out.println("* and associated libraries are placed into the specified root *");
            System.out.println("***************************************************************");
            System.out.println("");
        }
    }
}
