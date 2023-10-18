package threatgrid.kafka.connect;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import threatgrid.kafka.connect.elasticsearch.ElasticsearchConnection;
import threatgrid.kafka.connect.elasticsearch.ElasticsearchConnectionBuilder;

public class ElasticsearchSourceConnectorTask extends SourceTask {

    private final Logger log = LoggerFactory.getLogger(ElasticsearchSourceConnectorTask.class);

    private ElasticsearchSourceConnectorConfig config;
    private ElasticsearchConnection elasticConnection;

    private final AtomicBoolean stopping = new AtomicBoolean(false);
    private String topic;
    private String searchAfterJson;
    private String queryJson;
    private String sortJson;
    private String keyField;
    private String index;
    private int pollingMs;
    private int batchMaxRows;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String version() {
        return PropertiesUtil.getConnectorVersion();
    }

    @Override
    public void start(Map<String, String> props) {

        log.info("starting elastic source task");

        config = new ElasticsearchSourceConnectorConfig(props);
        topic = config.getString(ElasticsearchSourceConnectorConfig.TOPIC_CONF);
        index = config.getString(ElasticsearchSourceConnectorConfig.INDEX_NAME_CONF);
        searchAfterJson = getLastOffset(index);
        sortJson = config.getString(ElasticsearchSourceConnectorConfig.SORT_CONF);
        keyField = config.getString(ElasticsearchSourceConnectorConfig.KEY_FIELD_CONF);
        queryJson = config.getString(ElasticsearchSourceConnectorConfig.QUERY_CONF);
        pollingMs = config.getInt(ElasticsearchSourceConnectorConfig.POLL_INTERVAL_MS_CONF);
        batchMaxRows = config.getInt(ElasticsearchSourceConnectorConfig.BATCH_MAX_ROWS_CONF);

        String esScheme = config.getString(ElasticsearchSourceConnectorConfig.ES_SCHEME_CONF);
        String esHost = config.getString(ElasticsearchSourceConnectorConfig.ES_HOST_CONF);

        int esPort = config.getInt(ElasticsearchSourceConnectorConfig.ES_PORT_CONF);

        String esUser = config.getString(ElasticsearchSourceConnectorConfig.ES_USER_CONF);
        String esPwd = config.getString(ElasticsearchSourceConnectorConfig.ES_PWD_CONF);

        int maxConnectionAttempts = config.getInt(ElasticsearchSourceConnectorConfig.CONNECTION_ATTEMPTS_CONF);
        long connectionRetryBackoff = config.getLong(ElasticsearchSourceConnectorConfig.CONNECTION_BACKOFF_CONF);

        ElasticsearchConnectionBuilder connectionBuilder = new ElasticsearchConnectionBuilder(esHost, esPort)
            .withProtocol(esScheme)
            .withMaxAttempts(maxConnectionAttempts)
            .withBackoff(connectionRetryBackoff);

        Boolean compatibility = config.getBoolean(ElasticsearchSourceConnectorConfig.ES_COMPAT_CONF);

        if (compatibility != null) {
            connectionBuilder.withCompatibility(compatibility);
        }

        String truststore = config.getString(ElasticsearchSourceConnectorConfig.ES_TRUSTSTORE_CONF);
        String truststorePass = config.getString(ElasticsearchSourceConnectorConfig.ES_TRUSTSTORE_PWD_CONF);
        String keystore = config.getString(ElasticsearchSourceConnectorConfig.ES_KEYSTORE_CONF);
        String keystorePass = config.getString(ElasticsearchSourceConnectorConfig.ES_KEYSTORE_PWD_CONF);

        if (truststore != null) {
            connectionBuilder.withTrustStore(truststore, truststorePass);
        }

        if (keystore != null) {
            connectionBuilder.withKeyStore(keystore, keystorePass);
        }

        if (esUser == null || esUser.isEmpty()) {
            elasticConnection = connectionBuilder.build();
        } else {
            elasticConnection = connectionBuilder.withUser(esUser)
                .withPassword(esPwd)
                .build();
        }
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        List<SourceRecord> results = new ArrayList<>();
        try {
            if (!stopping.get()) {
                log.info("fetching from {}", index);
                Reader query = new StringReader("{\"query\":" + queryJson + "}");
                Reader sort = new StringReader("{\"sort\":" + sortJson + "}");
                log.info("SearchAfter: {}", searchAfterJson);
                Reader searchAfter = new StringReader(searchAfterJson != null ? "{\"search_after\":" + searchAfterJson + "}" : "{}");
                SearchResponse<ObjectNode> response = elasticConnection.getClient()
                    .search(s -> s.index(index).withJson(query).withJson(sort).withJson(searchAfter).size(batchMaxRows), ObjectNode.class);

                for (Hit<ObjectNode> hit: response.hits().hits()) {
                    // translate to offset
                    searchAfterJson = hit.sort()
                        .stream()
                        .map(n ->
                             {
                                 if (n.isString()) {
                                     return "\"" + n._toJsonString() + "\"";
                                 } else {
                                     return n._toJsonString();
                                 }
                             })
                        .collect(Collectors.joining(",", "[", "]"));

                    // create SourceRecord and append to results
                    ObjectNode elasticDocument = hit.source();

                    Map<String, String> sourcePartition = Collections.singletonMap("index", index);
                    Map<String, String> sourceOffset = Collections.singletonMap("search_after", searchAfterJson);
                    String key = keyField != null ? elasticDocument.get(keyField).textValue() : null;
                    String value = mapper.writeValueAsString(elasticDocument);

                    SourceRecord sourceRecord = new SourceRecord(sourcePartition, sourceOffset, topic, key != null ? Schema.STRING_SCHEMA : null, key, Schema.STRING_SCHEMA, value);
                    results.add(sourceRecord);
                }

                log.info("index {} total messages in batch: {} ", index, results.size());
            }

            if (results.isEmpty()) {
                log.info("no data found, sleeping for {} ms", pollingMs);
                Thread.sleep(pollingMs);
            }
        } catch (Exception e) {
            log.error("error", e);
        }

        return results;
    }

    // will be called by connect with a different thread than poll thread
    @Override
    public void stop() {

        log.info("stopping elastic source task");

        stopping.set(true);
        if (elasticConnection != null) {
            elasticConnection.closeQuietly();
        }
    }

    private String getLastOffset(String index) {
        Map<String, Object> offset = context.offsetStorageReader().offset(Collections.singletonMap("index", index));
        if (offset != null) {
            return (String) offset.get("search_after");
        } else {
            return null;
        }
    }

}
