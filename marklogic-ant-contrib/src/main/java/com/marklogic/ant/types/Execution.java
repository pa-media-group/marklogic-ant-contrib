package com.marklogic.ant.types;

import com.marklogic.AntHelper;
import com.marklogic.ant.annotation.AntType;
import com.marklogic.xquery.XQueryDocumentBuilder;
import com.marklogic.xquery.XQueryModule;
import com.marklogic.xquery.XQueryModuleXDMP;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.DataType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Defines the context of an XQuery Execution
 *
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
@AntType("execution")
public class Execution extends DataType {
    /**
     * XQuery to be invoked
     */
    private File xquery;

    /**
     * Database to invoke XQuery against
     */
    private String database;

    /**
     * Properties to be used for invocation.
     */
    private Map<String, String> properties;

    public File getXquery() {
        return xquery;
    }

    public void setXquery(File xquery) {
        this.xquery = xquery;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getExecutionXQuery() throws BuildException {
    	return this.getExecutionXQuery(false);
    }

    public String getExecutionXQuery(boolean useEval) throws BuildException {
        checkNotNull(xquery);
        try {
        	if (useEval == false) {
                return getFileAsString(xquery);
        	}
        	else if (StringUtils.isBlank(database) && (properties == null || properties.isEmpty())) {
                return getFileAsString(xquery);
            } else {
            	checkNotNull(database);
                XQueryDocumentBuilder builder = new XQueryDocumentBuilder();
                String values = "";
                if (properties != null && !properties.isEmpty()) {
                    List<String> vars = new ArrayList<String>(properties.size());
                    for (Object key : properties.keySet()) {
                        vars.add(XQueryModule.createQName(((String) key).replace('.', '_')));
                        vars.add(XQueryModule.quote(properties.get(key)));
                    }
                    values = StringUtils.join(vars.toArray(), ",");
                }
                StringBuilder options = new StringBuilder();
                options.append("<options xmlns='xdmp:eval'>");
                if (StringUtils.isNotBlank(database)) {
                    options.append("<database>{xdmp:database('" + database + "')}</database>");
                }
                options.append("<isolation>different-transaction</isolation>");
                options.append("</options>");

                builder.append(XQueryModuleXDMP.eval(getFileAsString(xquery),
                        "(" + values + ")", options.toString()));

                if (AntHelper.getAntHelper(getProject()).isVerbose()) {
                    System.out.println(builder.toString());
                }
                return builder.toString();
            }
        } catch (IOException e) {
            throw new BuildException(e.getMessage(), e);
        }
    }

    /**
     * Returns the string representation of the specified file
     *
     * @param file The file to be loaded
     * @return
     * @throws java.io.IOException
     */
    private String getFileAsString(final File file) throws IOException {
        StringBuilder builder = new StringBuilder((int) file.length());
        BufferedReader reader = new BufferedReader(new FileReader(file));
        char[] buf = new char[1024];
        int numRead;
        while ((numRead = reader.read(buf)) != -1) {
            builder.append(buf, 0, numRead);
        }
        reader.close();
        return builder.toString();
    }

    private Map<String, String> collectExecutionProps() {
        Map<String, String> collected = new HashMap<String, String>(properties);
        Set<String> names = System.getProperties().stringPropertyNames();
        for (String name : names) {
            if (name.startsWith("marklogic.executionProperties.")) {
                collected.put(name.substring(30), System.getProperty(name));
            }
        }
        return collected;
    }

}