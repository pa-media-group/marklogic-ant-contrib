package com.marklogic.ant.tasks;

import com.marklogic.ant.annotation.AntTask;
import org.apache.tools.ant.BuildException;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
@AntTask("install")
public class InstallTask extends AbstractInstallTask {
    @Override
    public void execute() throws BuildException {
        super.execute();
        installDatabases();
        installIndices();
        installFields();
        installTriggers();
        installTasks();
        installServers();
        installCPF();
        installContent();
//        invokeModules();
        restartServers();
    }
}
