package com.marklogic.ant.types;

import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.Session;
import org.apache.commons.lang.StringUtils;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
public class XCCSessionFactory extends AbstractConnectionFactory {

    public XCCSessionFactory(Connection connection) {
        super(connection);
    }

    public Session getXccSession() {
        return ContentSourceFactory.newContentSource(
                connection.getHost(), connection.getPort(),
                connection.getUsername(), connection.getPassword()).newSession();
    }

    public Session getXccSession(final String database) {
        checkNotNull(database);
        checkArgument(!StringUtils.isBlank(database));

        return ContentSourceFactory.newContentSource(
                connection.getHost(), connection.getPort(),
                connection.getUsername(), connection.getPassword(), database).newSession();
    }

}

