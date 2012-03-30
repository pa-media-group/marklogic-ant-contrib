package com.marklogic.ant.tasks;

import com.marklogic.ant.annotation.AntTask;
import org.apache.tools.ant.BuildException;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
@AntTask("uninstallBootstrap")
public class UninstallBootstrapTask extends AbstractBootstrapTask {

    @Override
    protected String getBootstrapExecuteQuery() throws BuildException {
        StringBuilder sb = new StringBuilder();

        sb.append(XQUERY_PROLOG);
        sb.append(ML_ADMIN_MODULE_IMPORT);
        sb.append("try { admin:save-configuration(                                         \n");
        sb.append("        admin:appserver-delete( admin:get-configuration()               \n");
        sb.append("                              , xdmp:server('" + xdbcName + "-WebDAV') )\n");
        sb.append("      ) } catch ($e) { () }                                             \n");
        sb.append(";\n");

        sb.append(XQUERY_PROLOG);
        sb.append(ML_ADMIN_MODULE_IMPORT);
        sb.append("try { admin:save-configuration(                                         \n");
        sb.append("        admin:appserver-delete( admin:get-configuration()               \n");
        sb.append("                              , xdmp:server('" + xdbcName + "') )       \n");
        sb.append("      ) } catch ($e) { () }                                             \n");
        sb.append(";\n");

        sb.append(XQUERY_PROLOG);
        sb.append(ML_ADMIN_MODULE_IMPORT);
        sb.append("try { admin:save-configuration(                                               \n");
        sb.append("        admin:database-delete( admin:get-configuration()                      \n");
        sb.append("                             , xdmp:database('" + xdbcModulesDatabase + "') ) \n");
        sb.append("      ) } catch ($e) { () }                                                   \n");
        sb.append(";\n");

        sb.append(XQUERY_PROLOG);
        sb.append(ML_ADMIN_MODULE_IMPORT);
        sb.append("try { admin:save-configuration(                                                      \n");
        sb.append("        admin:forest-delete( admin:get-configuration()                               \n");
        sb.append("                           , xdmp:forest('" + xdbcModulesDatabase + "'), fn:true() ) \n");
        sb.append("      ) } catch ($e) { () }                                                          \n");
        sb.append(";\n");

        sb.append("'Bootstrap Uninstall - OK'");

//        System.out.println(" *** uninstall-bootstrap.xqy *** ");
//        System.out.println(sb.toString());
//        System.out.println(" *** uninstall-bootstrap.xqy *** ");

        return sb.toString();
    }
}
