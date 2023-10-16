package threatgrid.kafka.connect;

import java.util.Map;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigDef.Width;

public class ElasticsearchSourceConnectorConfig extends AbstractConfig {

    public ElasticsearchSourceConnectorConfig(final Map<?, ?> originalProps) {
        super(CONFIG_DEF, originalProps);
    }

    public final static String ES_HOST_CONF = "es.host";
    private final static String ES_HOST_DOC = "ElasticSearch host. " +
        "Optionally it is possible to specify many hosts " +
        "using ; as separator (host1;host2;host3)";
    private final static String ES_HOST_DISPLAY = "Elastic host";

    public final static String ES_SCHEME_CONF = "es.scheme";
    private final static String ES_SCHEME_DOC = "Elasticsearch scheme (default: http)";
    private final static String ES_SCHEME_DISPLAY = "Elasticsearch scheme";
    private static final String ES_SCHEME_DEFAULT = "http";

    public final static String ES_PORT_CONF = "es.port";
    private final static String ES_PORT_DOC = "ElasticSearch port";
    private final static String ES_PORT_DISPLAY = "ElasticSearch port";
    private final static int ES_PORT_DEFAULT = 9200;

    public final static String ES_USER_CONF = "es.user";
    private final static String ES_USER_DOC = "Elasticsearch username";
    private final static String ES_USER_DISPLAY = "Elasticsearch username";

    public final static String ES_PWD_CONF = "es.password";
    private final static String ES_PWD_DOC = "Elasticsearch password";
    private final static String ES_PWD_DISPLAY = "Elasticsearch password";

    public final static String ES_KEYSTORE_CONF = "es.tls.keystore.location";
    private final static String ES_KEYSTORE_DOC = "Elasticsearch keystore location";

    public final static String ES_KEYSTORE_PWD_CONF = "es.tls.keystore.password";
    private final static String ES_KEYSTORE_PWD_DOC = "Elasticsearch keystore password";

    public final static String ES_TRUSTSTORE_CONF = "es.tls.truststore.location";
    private final static String ES_TRUSTSTORE_DOC = "Elasticsearch truststore location";

    public final static String ES_TRUSTSTORE_PWD_CONF = "es.tls.truststore.password";
    private final static String ES_TRUSTSTORE_PWD_DOC = "Elasticsearch truststore password";

    public final static String ES_COMPAT_CONF = "es.compatibility";
    private final static String ES_COMPAT_DOC = "Enable elasticsearch compatibility.";
    private final static String ES_COMPAT_DISPLAY = "Enable elasticsearch compatibility.";
    private final static Boolean ES_COMPAT_DEFAULT = false;

    public static final String CONNECTION_ATTEMPTS_CONF = "connection.attempts";
    private static final String CONNECTION_ATTEMPTS_DOC = "Maximum number of attempts to retrieve a valid Elasticsearch connection.";
    private static final String CONNECTION_ATTEMPTS_DISPLAY = "Elasticsearch connection attempts";
    private static final int CONNECTION_ATTEMPTS_DEFAULT = 3;

    public static final String CONNECTION_BACKOFF_CONF = "connection.backoff.ms";
    private static final String CONNECTION_BACKOFF_DOC = "Backoff time in milliseconds between connection attempts.";
    private static final String CONNECTION_BACKOFF_DISPLAY = "Elastic connection backoff in milliseconds";
    private static final Long CONNECTION_BACKOFF_DEFAULT = 10000L;

    public static final String POLL_INTERVAL_MS_CONF = "poll.interval.ms";
    private static final String POLL_INTERVAL_MS_DOC = "Frequency in ms to poll for new data in each index.";
    private static final int POLL_INTERVAL_MS_DEFAULT = 5000;
    private static final String POLL_INTERVAL_MS_DISPLAY = "Poll Interval (ms)";

    public static final String BATCH_MAX_ROWS_CONF = "batch.max.rows";
    private static final String BATCH_MAX_ROWS_DOC = "Maximum number of documents to include in a single batch when polling for new data.";
    private static final int BATCH_MAX_ROWS_DEFAULT = 10000;
    private static final String BATCH_MAX_ROWS_DISPLAY = "Max Documents Per Batch";

    public static final String QUERY_CONF = "query";
    private static final String QUERY_DOC = "JSON encoded query to filter documents. Must be in the form of '{\"query\": ...}'";
    private static final String QUERY_DISPLAY = "Query";

    public static final String SORT_CONF = "sort";
    private static final String SORT_DOC = "JSON encoded sorting criteria. Must be in the form of '{\"sort\": [...]}'. Sorting criteria will be used to enable smooth pagination via search_after.";
    private static final String SORT_DISPLAY = "Sort";

    public static final String INDEX_NAME_CONF = "index";
    private static final String INDEX_NAME_DOC = "Elasticsearch index name to fetch data from.";
    private static final String INDEX_NAME_DEFAULT = null;
    private static final String INDEX_NAME_DISPLAY = "Elasticsearch index";

    public static final String TOPIC_CONF = "topic";
    private static final String TOPIC_DOC = "Kafka topic to publish data";
    private static final String TOPIC_DISPLAY = "Kafka Topic";

    private static final String DATABASE_GROUP = "Elasticsearch";
    private static final String REQUEST_GROUP = "Request";
    private static final String CONNECTOR_GROUP = "Connector";

    public static final ConfigDef CONFIG_DEF = createConfigDef();

    private static ConfigDef createConfigDef() {
        ConfigDef configDef = new ConfigDef();
        addDatabaseOptions(configDef);
        addQueryOptions(configDef);
        addConnectorOptions(configDef);
        return configDef;
    }

    private static void addDatabaseOptions(ConfigDef config) {
        int orderInGroup = 0;
        config
            .define(ES_HOST_CONF,
                    Type.STRING,
                    Importance.HIGH,
                    ES_HOST_DOC,
                    DATABASE_GROUP,
                    ++orderInGroup,
                    Width.LONG,
                    ES_HOST_DISPLAY)
            .define(ES_SCHEME_CONF,
                    Type.STRING,
                    ES_SCHEME_DEFAULT,
                    Importance.HIGH,
                    ES_SCHEME_DOC,
                    DATABASE_GROUP,
                    ++orderInGroup,
                    Width.LONG,
                    ES_SCHEME_DISPLAY)
            .define(ES_PORT_CONF,
                    Type.INT,
                    ES_PORT_DEFAULT,
                    Importance.HIGH,
                    ES_PORT_DOC,
                    DATABASE_GROUP,
                    ++orderInGroup,
                    Width.LONG,
                    ES_PORT_DISPLAY)
            .define(ES_USER_CONF,
                    Type.STRING,
                    null,
                    Importance.HIGH,
                    ES_USER_DOC,
                    DATABASE_GROUP,
                    ++orderInGroup,
                    Width.LONG,
                    ES_USER_DISPLAY)
            .define(ES_PWD_CONF,
                    Type.STRING,
                    null,
                    Importance.HIGH,
                    ES_PWD_DOC,
                    DATABASE_GROUP,
                    ++orderInGroup,
                    Width.SHORT,
                    ES_PWD_DISPLAY)
            .define(ES_KEYSTORE_CONF,
                    Type.STRING,
                    null,
                    Importance.MEDIUM,
                    ES_KEYSTORE_DOC,
                    DATABASE_GROUP,
                    ++orderInGroup,
                    Width.SHORT,
                    ES_KEYSTORE_DOC)
            .define(ES_KEYSTORE_PWD_CONF,
                    Type.STRING,
                    "",
                    Importance.MEDIUM,
                    ES_KEYSTORE_PWD_DOC,
                    DATABASE_GROUP,
                    ++orderInGroup,
                    Width.SHORT,
                    ES_KEYSTORE_PWD_DOC)
            .define(ES_TRUSTSTORE_CONF,
                    Type.STRING,
                    null,
                    Importance.MEDIUM,
                    ES_TRUSTSTORE_DOC,
                    DATABASE_GROUP,
                    ++orderInGroup,
                    Width.SHORT,
                    ES_TRUSTSTORE_DOC)
            .define(ES_TRUSTSTORE_PWD_CONF,
                    Type.STRING,
                    "",
                    Importance.MEDIUM,
                    ES_TRUSTSTORE_PWD_DOC,
                    DATABASE_GROUP,
                    ++orderInGroup,
                    Width.SHORT,
                    ES_TRUSTSTORE_PWD_DOC)
            .define(ES_COMPAT_CONF,
                    Type.BOOLEAN,
                    ES_COMPAT_DEFAULT,
                    Importance.LOW,
                    ES_COMPAT_DOC,
                    DATABASE_GROUP,
                    ++orderInGroup,
                    Width.LONG,
                    ES_COMPAT_DISPLAY)
            .define(CONNECTION_ATTEMPTS_CONF,
                    Type.INT,
                    CONNECTION_ATTEMPTS_DEFAULT,
                    Importance.LOW,
                    CONNECTION_ATTEMPTS_DOC,
                    DATABASE_GROUP,
                    ++orderInGroup,
                    ConfigDef.Width.SHORT,
                    CONNECTION_ATTEMPTS_DISPLAY)
            .define(CONNECTION_BACKOFF_CONF,
                    Type.LONG,
                    CONNECTION_BACKOFF_DEFAULT,
                    Importance.LOW,
                    CONNECTION_BACKOFF_DOC,
                    DATABASE_GROUP,
                    ++orderInGroup,
                    Width.SHORT,
                    CONNECTION_BACKOFF_DISPLAY)
            .define(INDEX_NAME_CONF,
                    Type.STRING,
                    INDEX_NAME_DEFAULT,
                    Importance.HIGH,
                    INDEX_NAME_DOC,
                    DATABASE_GROUP,
                    ++orderInGroup,
                    Width.LONG,
                    INDEX_NAME_DISPLAY);
    }

    private static void addQueryOptions(ConfigDef config) {
        int orderInGroup = 0;
        config
            .define(QUERY_CONF,
                    Type.STRING,
                    ConfigDef.NO_DEFAULT_VALUE,
                    Importance.HIGH,
                    QUERY_DOC,
                    REQUEST_GROUP,
                    ++orderInGroup,
                    Width.MEDIUM,
                    QUERY_DISPLAY)
            .define(SORT_CONF,
                    Type.STRING,
                    ConfigDef.NO_DEFAULT_VALUE,
                    Importance.HIGH,
                    SORT_DOC,
                    REQUEST_GROUP,
                    ++orderInGroup,
                    Width.MEDIUM,
                    SORT_DISPLAY);
    }

    private static void addConnectorOptions(ConfigDef config) {
        int orderInGroup = 0;
        config
            .define(POLL_INTERVAL_MS_CONF,
                    Type.INT,
                    POLL_INTERVAL_MS_DEFAULT,
                    Importance.HIGH,
                    POLL_INTERVAL_MS_DOC,
                    CONNECTOR_GROUP,
                    ++orderInGroup,
                    Width.SHORT,
                    POLL_INTERVAL_MS_DISPLAY)
            .define(BATCH_MAX_ROWS_CONF,
                    Type.INT,
                    BATCH_MAX_ROWS_DEFAULT,
                    Importance.LOW,
                    BATCH_MAX_ROWS_DOC,
                    CONNECTOR_GROUP,
                    ++orderInGroup,
                    Width.SHORT,
                    BATCH_MAX_ROWS_DISPLAY)
            .define(TOPIC_CONF,
                    Type.STRING,
                    Importance.HIGH,
                    TOPIC_DOC,
                    CONNECTOR_GROUP,
                    ++orderInGroup,
                    Width.MEDIUM,
                    TOPIC_DISPLAY);
    }
}
