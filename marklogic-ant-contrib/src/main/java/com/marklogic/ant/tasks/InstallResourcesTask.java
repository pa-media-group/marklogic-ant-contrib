package com.marklogic.ant.tasks;

import com.marklogic.ant.annotation.AntTask;
import org.apache.tools.ant.BuildException;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
@AntTask("install-resources")
public class InstallResourcesTask extends AbstractInstallTask {
    @Override
    public void execute() throws BuildException {
        super.execute();
        installContent();
    }
}
