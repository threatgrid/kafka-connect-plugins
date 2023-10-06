package threatgrid.kafka.connect.elasticsearch;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.Objects;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;

public class ElasticsearchConnection {
    public final static Logger logger = LoggerFactory.getLogger(ElasticsearchConnection.class);

    private ElasticsearchTransport transport;
    private ElasticsearchClient client;
    private final long connectionRetryBackoff;
    private final int maxConnectionAttempts;
    private final String hosts;
    private final String protocol;
    private final int port;
    private final SSLContext sslContext;
    private final CredentialsProvider credentialsProvider;

    ElasticsearchConnection(ElasticsearchConnectionBuilder builder) {
        hosts = builder.hosts;
        protocol = builder.protocol;
        port = builder.port;

        String user = builder.user;
        String pwd = builder.pwd;
        if (user != null) {
            credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, pwd));
        } else {
            credentialsProvider = null;
        }

        sslContext = builder.trustStorePath == null ? null :
            getSslContext(builder.trustStorePath,
                          builder.trustStorePassword,
                          builder.keyStorePath,
                          builder.keyStorePassword);

        createConnection();

        this.maxConnectionAttempts = builder.maxConnectionAttempts;
        this.connectionRetryBackoff = builder.connectionRetryBackoff;
    }

    private void createConnection() {
        HttpHost[] hostList = parseHosts(hosts, protocol, port);
        RestClient restClient = RestClient.builder(hostList)
            .setHttpClientConfigCallback(httpClientBuilder -> {
                    if (credentialsProvider != null) {
                        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                    if (sslContext != null) {
                        httpClientBuilder.setSSLContext(sslContext);
                    }
                    return httpClientBuilder;
                })
            .build();

        transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        client = new ElasticsearchClient(transport);
    }

    private SSLContext getSslContext(String trustStoreConf, String trustStorePass,
                                     String keyStoreConf, String keyStorePass) {

        Objects.requireNonNull(trustStoreConf, "truststore location is required");
        Objects.requireNonNull(trustStorePass, "truststore password is required");

        try {
            Path trustStorePath = Paths.get(trustStoreConf);
            KeyStore truststore = KeyStore.getInstance("pkcs12");
            try (InputStream is = Files.newInputStream(trustStorePath)) {
                truststore.load(is, trustStorePass.toCharArray());
            }
            SSLContextBuilder sslBuilder = SSLContexts.custom()
                .loadTrustMaterial(truststore, null);

            if (keyStoreConf != null) {
                Objects.requireNonNull(keyStorePass, "keystore password is required");
                Path keyStorePath = Paths.get(keyStoreConf);
                KeyStore keyStore = KeyStore.getInstance("pkcs12");
                try (InputStream is = Files.newInputStream(keyStorePath)) {
                    keyStore.load(is, keyStorePass.toCharArray());
                }
                sslBuilder.loadKeyMaterial(keyStore, keyStorePass.toCharArray());
            }

            return sslBuilder.build();
        } catch (Exception e) {
            throw new SslContextException(e);
        }
    }

    private HttpHost[] parseHosts(String hosts, String protocol, int port) {
        return Arrays.stream(hosts.split(";"))
            .map(host -> new HttpHost(host, port, protocol))
            .toArray(HttpHost[]::new);
    }

    public ElasticsearchClient getClient() {
        return client;
    }

    public long getConnectionRetryBackoff() {
        return connectionRetryBackoff;
    }

    public int getMaxConnectionAttempts() {
        return maxConnectionAttempts;
    }

    public void closeQuietly() {
        try {
            transport.close();
        } catch (IOException e) {
            logger.error("error in close", e);
        }
    }

}
