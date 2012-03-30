package com.marklogic;

import com.google.common.base.Supplier;
import org.apache.tools.ant.Project;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
public class AntHelper {

    private static Map<Project, AntHelper> helpers = new HashMap<Project, AntHelper>();

    public synchronized static AntHelper getAntHelper(Project project) {
        AntHelper helper = helpers.get(project);
        if(helper == null) {
            helper = new AntHelper(project);
            helpers.put(project, helper);
        }
        return helper;
    }

    private Project project;

    private Supplier<Boolean> verbose = new Supplier<Boolean>() {
        public Boolean get() {
            return "true".equalsIgnoreCase(project.getProperty("verbose"));
        }
    };

    private AntHelper(Project project) {
        this.project = project;
    }

    public boolean isVerbose() {
        return verbose.get();
    }
}
