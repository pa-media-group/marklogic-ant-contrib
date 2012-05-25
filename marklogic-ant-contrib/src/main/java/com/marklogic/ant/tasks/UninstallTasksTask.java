package com.marklogic.ant.tasks;

import org.apache.tools.ant.BuildException;

import com.marklogic.ant.annotation.AntTask;

/**
 * @author Gavin Haydon <gavin.haydon@pressassociation.com>
 */
@AntTask("uninstall-tasks")
public class UninstallTasksTask extends AbstractUninstallTask {

	@Override
	public void execute() throws BuildException {
		super.execute();
		uninstallTasks();
	}
}
