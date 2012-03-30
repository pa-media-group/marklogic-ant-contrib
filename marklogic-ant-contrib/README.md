Marklogic Tasks for Ant
=======================

These tasks provide the functionality of the marklogic-maven-plugin for Ant.

Sample Build Script
-------------------
`
<?xml version="1.0"?>
<project xmlns:ml="antlib:com.marklogic.ant" name="sample-marklogic" default="install">
    <taskdef resource="com/marklogic/ant/antlib.xml" uri="antlib:com.marklogic.ant"
             classpath="lib/marklogic-ant-tasks-1.0-SNAPSHOT-jar-with-dependencies.jar"/>

    <property name="ml.src.dir" value="${base.dir}"/>

    <property name="verbose" value="false"/>

    <property name="xdbcModulesDatabase" value="InstallerModules"/>

    <property name="functional-sample.port" value="19001"/>
    <property name="unit-sample.port" value="19000"/>
    <property name="marklogic.group" value="Default"/>

    <property name="marklogic.application.name" value="sample-application"/>
    <property name="marklogic.host" value="localhost"/>
    <property name="marklogic.port" value="8997"/>
    <property name="marklogic.username" value="admin"/>
    <property name="marklogic.password" value="admin"/>

    <property name="marklogic.bootstrap.persist" value="true"/>

    <condition property="marklogic.bootstrap.exists">
        <ml:canConnectToBootstrap xdbcmodulesdatabase="${xdbcModulesDatabase}" password="${marklogic.password}"/>
    </condition>

    <ml:environment id="sample"
                    name="sample" title="Sample" applicationname="${marklogic.application.name}"
                    installationDescriptor="environments/default.xml">
        <ml:resources dir="${ml.src.dir}/src/main/xquery" database="modules" format="text">
            <ml:permission role="cp-executor" capability="execute"/>
            <include name="**/*.xqy"/>
        </ml:resources>
        <ml:resources dir="${ml.src.dir}/fixtures" database="content" format="xml">
            <ml:permission role="cp-reader" capability="read"/>
            <ml:permission role="cp-writer" capability="insert"/>
            <ml:permission role="cp-writer" capability="update"/>
        </ml:resources>
    </ml:environment>

    <!-- Only install bootstrap if it does not already exist -->
    <target name="install-bootstrap" unless="${marklogic.bootstrap.exists}">
        <ml:installBootstrap xdbcmodulesdatabase="${xdbcModulesDatabase}" password="${marklogic.password}"/>
    </target>

    <target name="install-project">
        <ml:install password="${marklogic.password}" database="Documents">
            <ml:environment refid="sample"/>
        </ml:install>
    </target>

    <target name="uninstall-project" depends="install-bootstrap">
        <ml:uninstall database="Documents" password="${marklogic.password}">
            <ml:environment refid="sample"/>
        </ml:uninstall>
    </target>

    <!-- Only uninstall if bootstrap is not configured to persist -->
    <target name="uninstall-bootstrap-conditional" unless="${marklogic.bootstrap.persist}">
        <ant target="uninstall-bootstrap"/>
    </target>

    <target name="uninstall-bootstrap">
        <ml:uninstallBootstrap xdbcmodulesdatabase="${xdbcModulesDatabase}" password="${marklogic.password}"/>
    </target>

    <target name="restart-project">
        <ml:restart-servers password="${marklogic.password}">
            <ml:environment refid="sample"/>
        </ml:restart-servers>
    </target>

    <target name="install-resources">
        <ml:install-resources password="${marklogic.password}">
            <ml:environment refid="sample"/>
        </ml:install-resources>
    </target>

    <!-- Core tasks -->
    <target name="install" depends="install-bootstrap,install-project,uninstall-bootstrap-conditional"/>
    <target name="uninstall" depends="install-bootstrap,uninstall-project,uninstall-bootstrap-conditional"/>
    <target name="restart" depends="install-bootstrap,restart-project,uninstall-bootstrap-conditional"/>

    <!-- Sample management tasks -->
    <target name="install-security">
        <ml:execute-xquery password="${marklogic.password}">
            <ml:execution xquery="${ml.src.dir}/schema/install-security.xqy" database="Security"/>
        </ml:execute-xquery>
    </target>

    <target name="set-api-permissions">
        <ml:execute-xquery password="${marklogic.password}">
            <ml:execution xquery="${ml.src.dir}/schema/set-api-permissions.xqy" database="${marklogic.application.name}-modules"/>
        </ml:execute-xquery>
    </target>

    <target name="set-content-permissions">
        <ml:execute-xquery password="${marklogic.password}">
            <ml:execution xquery="${ml.src.dir}/schema/set-content-permissions.xqy" database="${marklogic.application.name}-content"/>
        </ml:execute-xquery>
    </target>
</project>
`
