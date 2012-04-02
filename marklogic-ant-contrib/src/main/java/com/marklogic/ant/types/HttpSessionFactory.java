package com.marklogic.ant.types;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
public class HttpSessionFactory extends AbstractConnectionFactory {

    private static final String HTTP_SCHEME = "http";
    private static final String UTF_8 = "UTF-8";

    public HttpSessionFactory(Connection connection) {
        super(connection);
    }

    public URI getURI(final String path, List<NameValuePair> parameters) throws URISyntaxException {
        return URIUtils.createURI(HTTP_SCHEME, connection.getHost(), connection.getPort(), path,
                URLEncodedUtils.format(parameters, UTF_8), null);
    }
}
