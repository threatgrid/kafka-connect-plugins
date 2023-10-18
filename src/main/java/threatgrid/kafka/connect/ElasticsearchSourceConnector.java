package threatgrid.kafka.connect;

import static threatgrid.kafka.connect.ElasticsearchSourceConnectorConfig.CONFIG_DEF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticsearchSourceConnector extends SourceConnector {

    private final Logger log = LoggerFactory.getLogger(ElasticsearchSourceConnector.class);

    private Map<String, String> configProperties;

    @Override
    public String version() {
        return PropertiesUtil.getConnectorVersion();
    }

    @Override
    public ConfigDef config() {
        return CONFIG_DEF;
    }

    @Override
    public Class<? extends Task> taskClass() {
        return ElasticsearchSourceConnectorTask.class;
    }

    @Override
    public void start(Map<String, String> props) {
        log.info("starting elastic source");
        try {
            configProperties = props;
            new ElasticsearchSourceConnectorConfig(props);
        } catch (ConfigException e) {
            throw new ConnectException("Couldn't start ElasticsearchSourceConnector due to configuration "
                                       + "error", e);
        }
    }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        List<Map<String, String>> taskConfigs = new ArrayList<>(1);
        Map<String, String> taskProps = new HashMap<>(configProperties);
        taskConfigs.add(taskProps);
        return taskConfigs;
    }

    @Override
    public void stop() {
        log.info("stopping elastic source");
    }

}
