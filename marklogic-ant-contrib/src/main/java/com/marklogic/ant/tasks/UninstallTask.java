package com.marklogic.ant.tasks;

import com.marklogic.ant.annotation.AntTask;
import org.apache.tools.ant.BuildException;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
@AntTask("uninstall")
public class UninstallTask extends AbstractDeploymentTask {

    private static final String ACTION_UNINSTALL_ALL = "uninstall-all";

    @Override
    public void execute() throws BuildException {
        super.execute();

        executeAction(ACTION_UNINSTALL_ALL);
    }
}
