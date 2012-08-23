package com.marklogic.ant.tasks;

import com.marklogic.ant.annotation.AntTask;
import org.apache.tools.ant.BuildException;

/**
 * @author Gavin Haydon <gavin.haydon@pressassociation.com>
 */
@AntTask("update-servers")
public class UpdateServersTask extends AbstractInstallTask {
    @Override
    public void execute() throws BuildException {
        super.execute();
        updateServers();
    }
}
