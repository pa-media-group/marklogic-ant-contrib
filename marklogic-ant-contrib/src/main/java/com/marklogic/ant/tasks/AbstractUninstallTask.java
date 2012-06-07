package com.marklogic.ant.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.resources.FileResource;

import com.marklogic.ant.types.ResourceFileSet;
import com.marklogic.xcc.AdhocQuery;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;

/**
 * @author Gavin Haydon <gavin.haydon@pressassociation.com>
 */
public abstract class AbstractUninstallTask extends AbstractDeploymentTask {

	protected static final String ACTION_UNINSTALL_ALL = "uninstall-all";
	protected static final String ACTION_UNINSTALL_CONTENT = "uninstall-content";
	protected static final String ACTION_UNINSTALL_CPF = "uninstall-cpf";
	protected static final String ACTION_UNINSTALL_DATABASES = "uninstall-databases";
	protected static final String ACTION_UNINSTALL_INDICES = "uninstall-indices";
	protected static final String ACTION_UNINSTALL_FIELDS = "uninstall-fields";
	protected static final String ACTION_UNINSTALL_TRIGGERS = "uninstall-triggers";
	protected static final String ACTION_UNINSTALL_SERVERS = "uninstall-servers";
	protected static final String ACTION_UNINSTALL_TASKS = "uninstall-tasks";

	protected void uninstallAll() throws BuildException {
		executeAction(ACTION_UNINSTALL_ALL);
	}

	protected void uninstallContent() throws BuildException {
		if (getEnvironment().getResources() != null) {
			uninstallResources(getEnvironment().getResources());
		}
	}

	protected void uninstallCPF() throws BuildException {
		executeAction(ACTION_UNINSTALL_CPF);
	}

	protected void uninstallDatabases() throws BuildException {
		executeAction(ACTION_UNINSTALL_DATABASES);
	}

	protected void uninstallIndices() throws BuildException {
		executeAction(ACTION_UNINSTALL_INDICES);
	}

	protected void uninstallFields() throws BuildException {
		executeAction(ACTION_UNINSTALL_FIELDS);
	}

	protected void uninstallTriggers() throws BuildException {
		executeAction(ACTION_UNINSTALL_TRIGGERS);
	}

	protected void uninstallTasks() throws BuildException {
		executeAction(ACTION_UNINSTALL_TASKS);
	}

	protected void uninstallServers() throws BuildException {
		executeAction(ACTION_UNINSTALL_SERVERS);
	}

	private void uninstallResources(List<ResourceFileSet> resources) {
		List<String> uris = new ArrayList<String>();

		try {
            String applicationName = getEnvironment().getApplicationName();
			for (ResourceFileSet resource : resources) {
				final String targetDatabase =
                        applicationName != null ? applicationName + "-" + resource.getDatabase() : resource.getDatabase();
				log(" -- ".concat(targetDatabase).concat(" -- "));

				/*
				 * Get connection to database for uploading content
				 */
				Session session = getSession(targetDatabase);

				Iterator it = resource.iterator();
				while (it.hasNext()) {
					FileResource fileResource = (FileResource) it.next();
					String f = fileResource.getName();
					File destinationFile = new File(
							resource.getOutputDirectory(), f);

					String destinationPath = destinationFile.getPath().replace(
							File.separatorChar, '/');
					log(String.format("Submitting %s for removal.",
							destinationPath));
					uris.add(destinationPath);
				}

				StringBuilder b = new StringBuilder("for $uri in (");
				Iterator<String> iter = uris.iterator();
				while (iter.hasNext()) {
					b.append("'").append((String) iter.next()).append("'");
					if (iter.hasNext()) {
						b.append(",");
					}
				}
				b.append(") return try { xdmp:document-delete($uri) } catch ($e) { () }");

				AdhocQuery q = session.newAdhocQuery(b.toString());
				try {
					session.submitRequest(q);
				} catch (RequestException e) {
					log("Failed to remove content file: "
							+ e.getLocalizedMessage());
				}
			}

		} finally {
			for (Map.Entry<String, Session> e : sessions.entrySet()) {
				e.getValue().close();
			}
			sessions = new HashMap<String, Session>();
		}
	}
}
