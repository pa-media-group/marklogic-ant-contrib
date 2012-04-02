Marklogic Tasks for Ant
=======================

These tasks provide the functionality of the marklogic-maven-plugin for Ant.

Sample Build Script
-------------------
`
<?xml version="1.0"?>
<project xmlns:ml="antlib:com.marklogic.ant" name="test-marklogic" default="usage">
    <taskdef resource="com/marklogic/ant/antlib.xml" uri="antlib:com.marklogic.ant"
             classpath="lib/marklogic-ant-contrib-1.0-SNAPSHOT-jar-with-dependencies.jar"/>

    <!-- Path to REST api -->
    <property name="ml.src.dir"
              value="${user.home}/Development/sample-rest-api"/>

    <property name="verbose" value="false"/>

    <!-- Environment server configuration -->
    <property name="functional-test.port" value="19001"/>
    <property name="unit-test.port" value="19000"/>
    <property name="marklogic.group" value="Default"/>

    <!-- Connection Configuration -->
    <property name="marklogic.host" value="localhost"/>
    <property name="marklogic.username" value="admin"/>
    <property name="marklogic.password" value="admin"/>

    <!-- Bootstrap Configuration -->
    <property name="marklogic.bootstrap.database" value="InstallerModules"/>
    <property name="marklogic.bootstrap.name" value="8997-Installer" />
    <property name="marklogic.bootstrap.persist" value="true"/>
    <property name="marklogic.bootstrap.port" value="8997"/>

    <!-- Connection Object -->
    <ml:connection id="defaultConnection"
                   host="${marklogic.host}" port="${marklogic.bootstrap.port}"
                   username="${marklogic.username}" password="${marklogic.password}"/>

    <!-- Bootstrap presence condition -->
    <condition property="marklogic.bootstrap.exists">
        <ml:canConnectToBootstrap database="${marklogic.bootstrap.database}" connectionref="defaultConnection"/>
    </condition>

    <!-- Environment Object -->
    <ml:environment id="test"
                    name="test" title="Test" applicationname="sample-api"
                    installationDescriptor="environments/test.xml">
        <ml:resources dir="${ml.src.dir}/src/main/xquery" database="modules" format="text">
            <ml:permission role="executor" capability="execute"/>
            <include name="**/*.xqy"/>
        </ml:resources>
        <ml:resources dir="${ml.src.dir}/fixtures" database="content" format="xml">
            <ml:permission role="reader" capability="read"/>
            <ml:permission role="writer" capability="insert"/>
            <ml:permission role="writer" capability="update"/>
        </ml:resources>
    </ml:environment>

    <!-- Only install bootstrap if it does not already exist -->
    <target name="install-bootstrap" unless="${marklogic.bootstrap.exists}">
        <ml:installBootstrap xdbcname="${marklogic.bootstrap.name}"
                             database="${marklogic.bootstrap.database}"
                             bootstrapport="8000"
                             connectionref="defaultConnection"/>
    </target>

    <!-- Private target -->
    <target name="-install-project">
        <ml:install database="Documents" environmentref="test" connectionref="defaultConnection"/>
    </target>

    <!-- Private target -->
    <target name="-uninstall-project" depends="install-bootstrap">
        <ml:uninstall database="Documents" environmentref="test" connectionref="defaultConnection"/>
    </target>

    <!-- Only uninstall if bootstrap is not configured to persist -->
    <target name="uninstall-bootstrap" unless="${marklogic.bootstrap.persist}">
        <ml:uninstallBootstrap xdbcname="${marklogic.bootstrap.name}"
                               database="${marklogic.bootstrap.database}"
                               bootstrapport="8000"
                               connectionref="defaultConnection"/>
    </target>

    <!-- Restart servers -->
    <target name="restart-servers">
        <ml:restart-servers environmentref="test" connectionref="defaultConnection"/>
    </target>

    <target name="install-resources">
        <ml:install-resources environmentref="test" connectionref="defaultConnection"/>
    </target>

    <!-- Core tasks -->
    <target name="install" depends="install-bootstrap,-install-project,uninstall-bootstrap"/>
    <target name="uninstall" depends="install-bootstrap,-uninstall-project,uninstall-bootstrap"/>
    <target name="restart" depends="install-bootstrap,restart-servers,uninstall-bootstrap"/>

    <target name="install-security">
        <ml:execute-xquery connectionref="defaultConnection">
            <ml:execution xquery="${ml.src.dir}/schema/install-security.xqy" database="Security"/>
        </ml:execute-xquery>
    </target>

    <target name="set-api-permissions">
        <ml:execute-xquery connectionref="defaultConnection">
            <ml:execution xquery="${ml.src.dir}/schema/set-api-permissions.xqy" database="sample-api-modules"/>
        </ml:execute-xquery>
    </target>

    <target name="set-content-permissions">
        <ml:execute-xquery connectionref="defaultConnection">
            <ml:execution xquery="${ml.src.dir}/schema/set-content-permissions.xqy" database="sample-api-content"/>
        </ml:execute-xquery>
    </target>

    <target name="insert-retention-rules">
        <ml:invoke-module servername="XCC"
                          module="/static/insert-retention-rules.xqy"
                          connectionref="defaultConnection"
                          environmentref="test"/>
    </target>

    <target name="usage">
        <echo>
            Usage: build target-name
            Available targets:

            # Bootstrap
            install-bootstrap - Install bootstrap server
            uninstall-bootstrap - Uninstall bootstrap server

            # Main
            install - Install api, servers and fixtures
            uninstall - Uninstall api, servers and fixtures
            restart - Restart servers

            # Management
            install-security - Install security roles and users
            set-api-permissions - Update installed API with security permissions
            set-content-permissions - Update installed content with security permissions
            invoke-module - Insert document retention rules

            Issues:

            * If bootstrap is not persistent then restarting servers during install can cause the
              uninstalling of the bootstrap to fail because the backdoor HTTP server has yet to resume.
        </echo>
    </target>

</project>
`
