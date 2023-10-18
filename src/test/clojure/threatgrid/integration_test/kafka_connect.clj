(ns threatgrid.integration-test.kafka-connect
  (:require
   [hato.client :as http]
   [jsonista.core :as json]))

(defn start-connector [name index topic]
  (http/post "http://localhost:8083/connectors"
             {:body (json/write-value-as-string
                     {:name name
                      :config {"connector.class"  "threatgrid.kafka.connect.ElasticsearchSourceConnector"
                               "es.compatibility" true
                               "es.host"          "elasticsearch.local"
                               "es.password"      "elastic"
                               "es.port"          9200
                               "es.scheme"        "http"
                               "es.user"          "elastic"
                               "index"            index
                               "key.converter"    "org.apache.kafka.connect.storage.StringConverter"
                               "query"            "{\"match_all\": {}}"
                               "sort"             "[{\"@timestamp\": {\"order\": \"asc\"}}, \"id\"]"
                               "topic"            topic
                               "value.converter"  "org.apache.kafka.connect.storage.StringConverter"}})
              :content-type :json}))

(defn stop-connector [name]
  (http/delete (format "http://localhost:8083/connectors/%s/" name)))

(defn running? [x]
  (= "RUNNING" (get-in x ["connector" "state"])))

(defn check-status [name]
  (try
    (-> (http/get (format "http://localhost:8083/connectors/%s/status" name))
        (:body)
        (json/read-value)
        (running?))
    (catch Exception _)))
