package com.marklogic.ant.tasks;

import com.marklogic.ant.types.Environment;
import com.marklogic.ant.annotation.AntTask;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
@AntTask("testTask")
public class TestTask extends Task {
    
    private Environment env;
    
    @Override
    public void execute() throws BuildException {
        System.out.println(env.getServers());
        System.out.println(env.getDatabases());
    }

    public void addConfiguredEnvironment(Environment env) {
        this.env = env;
    }
}
