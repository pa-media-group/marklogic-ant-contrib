package com.marklogic.ant.tasks;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;

import com.google.common.collect.ImmutableList;
import com.marklogic.AntHelper;
import com.marklogic.ant.annotation.AntTask;
import com.marklogic.ant.types.Execution;
import com.marklogic.xcc.AdhocQuery;
import com.marklogic.xcc.ResultItem;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
@AntTask("execute-xquery")
public class ExecuteTask extends AbstractInstallTask {

	private AntHelper helper = AntHelper.getAntHelper(getProject());

	/**
	 * Sequence of XQuery scripts to be executed
	 */
	private List<Execution> executions = new ArrayList<Execution>();

	public void executeQuery(String query, String database) throws BuildException {

		/* Do query here */

		Session session = getXccSessionFactory().getXccSession(database);
		if (session == null || session.isClosed()) {
			throw new BuildException(
					"Unable to get XCC session to bootstrap server");
		}

		AdhocQuery adhocQuery = session.newAdhocQuery(query);
		try {
			ResultSequence results = session.submitRequest(adhocQuery);
				for (ResultItem i : ImmutableList.copyOf(results.iterator())) {
					System.out.println(i.asString());
				}
		} catch (RequestException e) {
			throw new BuildException(e);
		} finally {
			session.close();
		}
	}

	public void execute() throws BuildException {
		System.out.println("Executing queries via connection to bootstrap server");
		for (Execution execution : executions) {
			String database = getDatabase();
			if (execution.getDatabase() != null && !StringUtils.isBlank(execution.getDatabase())) {
				database = execution.getDatabase();
			}
			if (database == null || StringUtils.isBlank(database)) {
				throw new BuildException("No database is set at the task or execute level!");
			}
			System.out.println("Executing following XQuery against " + database + ":\n" + execution.getXquery());
			try {
				executeQuery(execution.getExecutionXQuery(), database);
			} catch (BuildException e) {
				System.err.println("Failed to execute " + execution.getXquery());
				throw(e);
			}
		}
	}

	public void addExecution(Execution execution) {
		checkNotNull(execution);
		this.executions.add(execution);
	}
}
