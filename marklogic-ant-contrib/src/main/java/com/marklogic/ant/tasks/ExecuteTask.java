package com.marklogic.ant.tasks;

import com.marklogic.AntHelper;
import com.marklogic.ant.types.Execution;
import com.marklogic.ant.annotation.AntTask;
import org.apache.http.HttpResponse;
import org.apache.tools.ant.BuildException;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
@AntTask("execute-xquery")
public class ExecuteTask extends AbstractBootstrapTask {

    private AntHelper helper = AntHelper.getAntHelper(getProject());

    /**
     * Sequence of XQuery scripts to be executed
     */
    private List<Execution> executions = new ArrayList<Execution>();

    protected String getBootstrapExecuteQuery() throws BuildException {
        throw new UnsupportedOperationException("Not Implemented.");
    }

    public HttpResponse executeBootstrapQuery(String query) throws BuildException {
        if (helper.isVerbose()) {
            System.out.println(query);
        }
        return super.executeBootstrapQuery(query);
    }

    public void execute() throws BuildException {
        for (Execution execution : executions) {
            executeBootstrapQuery(execution.getExecutionXQuery());
        }
    }

    public void addExecution(Execution execution) {
        checkNotNull(execution);
        this.executions.add(execution);
    }
}
