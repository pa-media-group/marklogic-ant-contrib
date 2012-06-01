package com.marklogic.ant.tasks;

import org.apache.tools.ant.BuildException;

import com.marklogic.AntHelper;
import com.marklogic.ant.annotation.AntTask;
import com.marklogic.install.xquery.XQueryDocumentBuilder;
import com.marklogic.install.xquery.XQueryModule;
import com.marklogic.install.xquery.XQueryModuleAdmin;
import com.marklogic.install.xquery.XQueryModuleXDMP;
import com.marklogic.xcc.AdhocQuery;
import com.marklogic.xcc.Content;
import com.marklogic.xcc.ContentFactory;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;

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
        xq.assign(config, XQueryModuleAdmin.databaseCreate(config, getDatabase()));
        xq.doReturn(XQueryModuleAdmin.saveConfiguration(config));
        return XQueryModuleXDMP.eval(xq.toString());
    }

    private String createForest() {
        XQueryDocumentBuilder xq = new XQueryDocumentBuilder();
        xq.append(XQueryModuleAdmin.importModule());
        String config = xq.assign("config", XQueryModuleAdmin.getConfiguration());
        xq.assign(config, XQueryModuleAdmin.forestCreate(config, database));
        xq.doReturn(XQueryModuleAdmin.saveConfiguration(config));
        return XQueryModuleXDMP.eval(xq.toString());
    }

    private String attachForestToDatabase() {
        XQueryDocumentBuilder xq = new XQueryDocumentBuilder();
        xq.append(XQueryModuleAdmin.importModule());
        String config = xq.assign("config", XQueryModuleAdmin.getConfiguration());
        xq.assign(config, XQueryModuleAdmin.attachForest(config, XQueryModuleXDMP.database(database),
                XQueryModuleXDMP.forest(database)));
        xq.doReturn(XQueryModuleAdmin.saveConfiguration(config));
        return XQueryModuleXDMP.eval(xq.toString());
    }

    private String createWebDAVServer() {
        XQueryDocumentBuilder xq = new XQueryDocumentBuilder();
        xq.append(XQueryModuleAdmin.importModule());
        String config = xq.assign("config", XQueryModuleAdmin.getConfiguration());
        xq.assign(config, XQueryModuleAdmin.webdavServerCreate(config, xdbcName + "-WebDAV", xdbcModuleRoot,
                getConnection().getPort() + 1, XQueryModuleXDMP.database(database)));
        xq.doReturn(XQueryModuleAdmin.saveConfiguration(config));
        return XQueryModuleXDMP.eval(xq.toString());
    }

    private String createXDBCServer() {
        XQueryDocumentBuilder xq = new XQueryDocumentBuilder();
        xq.append(XQueryModuleAdmin.importModule());
        String config = xq.assign("config", XQueryModuleAdmin.getConfiguration());
        xq.assign(config, XQueryModuleAdmin.xdbcServerCreate(
                config, xdbcName, xdbcModuleRoot, getConnection().getPort(),
                XQueryModuleXDMP.database(database), XQueryModuleXDMP.database("Security")));
        xq.doReturn(XQueryModuleAdmin.saveConfiguration(config));
        return XQueryModuleXDMP.eval(xq.toString());
    }

    protected String getBootstrapExecuteQuery() {
        XQueryDocumentBuilder sb = new XQueryDocumentBuilder();

        if (!"file-system".equalsIgnoreCase(database)) {
            sb.assign("_", createDatabase());
            sb.assign("_", createForest());
            sb.assign("_", attachForestToDatabase());
        }
        sb.assign("_", createXDBCServer());

        sb.doReturn(XQueryModule.quote("Bootstrap Install - OK"));

        if(AntHelper.getAntHelper(getProject()).isVerbose()) {
            /* Log xquery invocation */
            System.out.println(" *** bootstrap.xqy *** ");
            System.out.println(sb.toString());
            System.out.println(" *** bootstrap.xqy *** ");
        }

        return sb.toString();
    }

    @Override
    public void execute() throws BuildException {
    	
        Session session = getXccSessionFactory().getXccSession();    
        System.out.println("Bootstrap session is to " + session.getConnectionUri().toASCIIString());
        
		AdhocQuery q = session.newAdhocQuery("xquery version \"1.0-ml\";\n1");
		boolean success = false;
		try {
			session.submitRequest(q);
			success = true;
		} catch (RequestException e) {
		} finally {
			session.close();
		}
        
        if (success) {
        	System.out.println("Bootstrap already exists, skipping this goal");
        	return;
        }
        
        super.execute();

        if (!"file-system".equalsIgnoreCase(database)) {
            session = getXccSessionFactory().getXccSession(database);

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
