package com.marklogic.ant.tasks;

import com.marklogic.ant.annotation.AntTask;
import org.apache.tools.ant.BuildException;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
@AntTask("restart-servers")
public class RestartServersTask extends AbstractDeploymentTask {
    @Override
    public void execute() throws BuildException {
        super.execute();
        restartServers();
    }
}
