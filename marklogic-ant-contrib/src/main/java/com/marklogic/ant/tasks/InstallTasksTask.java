package com.marklogic.ant.tasks;

import com.marklogic.ant.annotation.AntTask;
import org.apache.tools.ant.BuildException;

/**
 * @author Gavin Haydon <gavin.haydon@pressassociation.com>
 */
@AntTask("install-tasks")
public class InstallTasksTask extends AbstractInstallTask {
    @Override
    public void execute() throws BuildException {
        super.execute();
        installTasks();
    }
}
