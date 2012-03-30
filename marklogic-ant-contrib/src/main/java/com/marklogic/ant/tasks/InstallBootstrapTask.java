package com.marklogic.ant.tasks;

import com.marklogic.ant.annotation.AntTask;
import com.marklogic.xcc.Content;
import com.marklogic.xcc.ContentFactory;
import com.marklogic.xcc.Session;
import com.marklogic.xquery.XQueryDocumentBuilder;
import com.marklogic.xquery.XQueryModule;
import com.marklogic.xquery.XQueryModuleAdmin;
import com.marklogic.xquery.XQueryModuleXDMP;
import org.apache.tools.ant.BuildException;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
@AntTask("installBootstrap")
public class InstallBootstrapTask extends AbstractBootstrapTask {

    private final String[] libraryPaths = {
            "/install.xqy"
            , "/lib/lib-app-server.xqy"
            , "/lib/lib-cpf.xqy"
            , "/lib/lib-database-add.xqy"
            , "/lib/lib-database-set.xqy"
            , "/lib/lib-database.xqy"
            , "/lib/lib-field.xqy"
            , "/lib/lib-trigger.xqy"
            , "/lib/lib-task.xqy"
            , "/lib/lib-index.xqy"
            , "/lib/lib-install.xqy"
            , "/lib/lib-load.xqy"
    };

    private String createDatabase() {
        XQueryDocumentBuilder xq = new XQueryDocumentBuilder();
        xq.append(XQueryModuleAdmin.importModule());
        String config = xq.assign("config", XQueryModuleAdmin.getConfiguration());
        xq.assign(config, XQueryModuleAdmin.databaseCreate(config, xdbcModulesDatabase));
        xq.doReturn(XQueryModuleAdmin.saveConfiguration(config));
        return XQueryModuleXDMP.eval(xq.toString());
    }

    private String createForest() {
        XQueryDocumentBuilder xq = new XQueryDocumentBuilder();
        xq.append(XQueryModuleAdmin.importModule());
        String config = xq.assign("config", XQueryModuleAdmin.getConfiguration());
        xq.assign(config, XQueryModuleAdmin.forestCreate(config, xdbcModulesDatabase));
        xq.doReturn(XQueryModuleAdmin.saveConfiguration(config));
        return XQueryModuleXDMP.eval(xq.toString());
    }

    private String attachForestToDatabase() {
        XQueryDocumentBuilder xq = new XQueryDocumentBuilder();
        xq.append(XQueryModuleAdmin.importModule());
        String config = xq.assign("config", XQueryModuleAdmin.getConfiguration());
        xq.assign(config, XQueryModuleAdmin.attachForest(config, XQueryModuleXDMP.database(xdbcModulesDatabase),
                XQueryModuleXDMP.forest(xdbcModulesDatabase)));
        xq.doReturn(XQueryModuleAdmin.saveConfiguration(config));
        return XQueryModuleXDMP.eval(xq.toString());
    }

    private String createWebDAVServer() {
        XQueryDocumentBuilder xq = new XQueryDocumentBuilder();
        xq.append(XQueryModuleAdmin.importModule());
        String config = xq.assign("config", XQueryModuleAdmin.getConfiguration());
        xq.assign(config, XQueryModuleAdmin.webdavServerCreate(config, xdbcName + "-WebDAV", xdbcModuleRoot,
                xdbcPort + 1, XQueryModuleXDMP.database(xdbcModulesDatabase)));
        xq.doReturn(XQueryModuleAdmin.saveConfiguration(config));
        return XQueryModuleXDMP.eval(xq.toString());
    }

    private String createXDBCServer() {
        XQueryDocumentBuilder xq = new XQueryDocumentBuilder();
        xq.append(XQueryModuleAdmin.importModule());
        String config = xq.assign("config", XQueryModuleAdmin.getConfiguration());
        xq.assign(config, XQueryModuleAdmin.xdbcServerCreate(config, xdbcName, xdbcModuleRoot, xdbcPort,
                XQueryModuleXDMP.database(xdbcModulesDatabase), XQueryModuleXDMP.database("Security")));
        xq.doReturn(XQueryModuleAdmin.saveConfiguration(config));
        return XQueryModuleXDMP.eval(xq.toString());
    }

    protected String getBootstrapExecuteQuery() {
        XQueryDocumentBuilder sb = new XQueryDocumentBuilder();

        if (!"file-system".equalsIgnoreCase(xdbcModulesDatabase)) {
            sb.assign("_", createDatabase());
            sb.assign("_", createForest());
            sb.assign("_", attachForestToDatabase());
        }
        sb.assign("_", createXDBCServer());

        sb.doReturn(XQueryModule.quote("Bootstrap Install - OK"));

//        /* Log xquery invocation */
//        System.out.println(" *** bootstrap.xqy *** ");
//        System.out.println(sb.toString());
//        System.out.println(" *** bootstrap.xqy *** ");

        return sb.toString();
    }

    @Override
    public void execute() throws BuildException {
//        System.out.println("bootstrap execute");

        super.execute();

        if (!"file-system".equalsIgnoreCase(xdbcModulesDatabase)) {
            this.database = xdbcModulesDatabase;

            Session session = getXccSession();

            ClassLoader loader = InstallBootstrapTask.class.getClassLoader();
            for (String path : libraryPaths) {
                System.out.println("Uploading " + path);
                try {
                    Content cs = ContentFactory.newContent(path, loader.getResource("xquery" + path), null);
                    session.insertContent(cs);
                } catch (Exception e) {
                    throw new BuildException("Failed to insert required library. " + e.getLocalizedMessage(), e);
                }
                session.commit();
            }
        } else {
            System.out.println("");
            System.out.println("***************************************************************");
            System.out.println("* Using filesystem modules location, ensure that install.xqy  *");
            System.out.println("* and associated libraries are placed into the specified root *");
            System.out.println("***************************************************************");
            System.out.println("");
        }
    }
}
