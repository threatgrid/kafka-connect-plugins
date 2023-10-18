# kafka-connect-elasticsearch-source

Kafka Connect Elasticsearch Source: fetch data from elastic-search and sends it to kafka. The connector fetches only new data using ability to scroll search result using `search_after` token returned with response documents. This connector is sutable only for append-only indices.

## Requirements:

- Elasticsearch 7.x and up
- Java >= 8
- Maven

## Output data serialization format:

The connector encode output as JSON string.

## Installation:

Compile the project with:

```bash
mvn clean package
```

Copy the jar with dependencies from the target folder into connect classpath (e.g `/usr/share/java/kafka-connect-elasticsearch`) or set `plugin.path` parameter appropriately.

## Example

Using kafka connect in distributed way, a sample config file to fetch `example-index` index and to produce output topic `example-target-topic`:

```js
{
    "name": "elastic-source",
    "config": {
        "connector.class": "threatgrid.kafka.connect.ElasticsearchSourceConnector",
        "es.host": "localhost",
        "es.port": "9200",
        "es.user": "elastic",
        "es.password": "elastic",
        "index": "example-index",
        "topic.prefix": "example-target-topic",
        // Query might be used to output only interesting documents
        "query": "{\"match_all\": {}}",
        // It is important for sort field to contain enough fields to keep documents order
        "sort": "[{\"@timestamp\": {\"order\": \"asc\"}}, \"id\"]"
    }
}
```

To start the connector with curl:

```bash
curl -X POST -H "Content-Type: application/json" --data @config.json http://localhost:8083/connectors | jq
```

To check the status:

```bash
curl localhost:8083/connectors/elastic-source/status | jq
```

To stop the connector:

```bash
curl -X DELETE localhost:8083/connectors/elastic-source | jq
```

## Documentation

### Elasticsearch Configuration

`es.host`
ElasticSearch host. Optionally it is possible to specify many hosts using `;` as separator (`host1;host2;host3`)

* Type: string
* Importance: high
* Dependents: `index.prefix`

`es.port`
ElasticSearch port

* Type: string
* Importance: high
* Dependents: `index.prefix`

`es.scheme`
ElasticSearch scheme (http/https)

* Type: string
* Importance: medium
* Default: `http`

`es.user`
Elasticsearch username

* Type: string
* Default: null
* Importance: high

`es.password`
Elasticsearch password

* Type: password
* Default: null
* Importance: high

`es.tls.truststore.location`
Elastic ssl truststore location

* Type: string
* Importance: medium

`es.tls.truststore.password`
Elastic ssl truststore password

* Type: string
* Default: ""
* Importance: medium

`es.tls.keystore.location`
Elasticsearch keystore location

* Type: string
* Importance: medium

`es.tls.keystore.password`
Elasticsearch keystore password

* Type: string
* Default: ""
* Importance: medium

`es.compatibility`
Enable minor version API compatibility.

* Type: bool
* Default: false
* Importants: low

`connection.attempts`
Maximum number of attempts to retrieve a valid Elasticsearch connection.

* Type: int
* Default: 3
* Importance: low

`connection.backoff.ms`
Backoff time in milliseconds between connection attempts.

* Type: long
* Default: 10000
* Importance: low

`index`
Elasticsearch index name to fetch data from.

* Type: string
* Default: ""
* Importance: medium

`topic`
Kafka topic to publish data

* Type: string
* Default: null
* Importance: medium

`query`
JSON-encoded string to be used as a `"query"` field in search requests.

* Type: string
* Default: null
* Importance: high

`sort`
JSON-encoded string to be used as a `"sort"` field in search requests. It is important to add enough fields into the sorting criteria to allow search_after scroll for new documents.

* Type: string
* Default: null
* Importance: high

`key.field`
(Optional) Field name to extract string value from the document to be used as a record key.

* Type: string
* Default: null
* Importance: low

### Connector Configuration

`poll.interval.ms`
Frequency in ms to poll for new data in each index.

* Type: int
* Default: 5000
* Importance: high

`batch.max.rows`
Maximum number of documents to include in a single batch when polling for new data.

* Type: int
* Default: 10000
* Importance: low
