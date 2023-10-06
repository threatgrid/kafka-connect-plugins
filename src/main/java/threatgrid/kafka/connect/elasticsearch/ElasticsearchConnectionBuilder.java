package threatgrid.kafka.connect.elasticsearch;

public class ElasticsearchConnectionBuilder {
    final String hosts;
    final int port;

    String protocol = "http";
    int maxConnectionAttempts = 3;
    long connectionRetryBackoff = 1_000;
    String user;
    String pwd;

    String trustStorePath;
    String trustStorePassword;
    String keyStorePath;
    String keyStorePassword;

    public ElasticsearchConnectionBuilder(String hosts, int port) {
        this.hosts = hosts;
        this.port = port;
    }

    public ElasticsearchConnectionBuilder withProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public ElasticsearchConnectionBuilder withUser(String user) {
        this.user = user;
        return this;
    }

    public ElasticsearchConnectionBuilder withPassword(String password) {
        this.pwd = password;
        return this;
    }

    public ElasticsearchConnectionBuilder withMaxAttempts(int maxConnectionAttempts) {
        this.maxConnectionAttempts = maxConnectionAttempts;
        return this;
    }

    public ElasticsearchConnectionBuilder withBackoff(long connectionRetryBackoff) {
        this.connectionRetryBackoff = connectionRetryBackoff;
        return this;
    }

    public ElasticsearchConnectionBuilder withTrustStore(String path, String password) {
        this.trustStorePath = path;
        this.trustStorePassword = password;
        return this;
    }

    public ElasticsearchConnectionBuilder withKeyStore(String path, String password) {
        this.keyStorePath = path;
        this.keyStorePassword = password;
        return this;
    }

    public ElasticsearchConnection build() {
        return new ElasticsearchConnection(this);
    }

}
