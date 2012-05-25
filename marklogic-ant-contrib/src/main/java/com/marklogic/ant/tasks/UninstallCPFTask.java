package com.marklogic.ant.tasks;

import org.apache.tools.ant.BuildException;

import com.marklogic.ant.annotation.AntTask;

/**
 * @author Gavin Haydon <gavin.haydon@pressassociation.com>
 */
@AntTask("uninstall-CPF")
public class UninstallCPFTask extends AbstractUninstallTask {

	@Override
	public void execute() throws BuildException {
		super.execute();
		uninstallCPF();
	}
}
