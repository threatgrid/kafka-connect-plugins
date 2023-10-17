(ns threatgrid.integration-test.elastic
  (:require
   [hato.client :as http]
   [jsonista.core :as json]))

(defn create-index [name documents]
  (http/put (format "http://elastic:elastic@localhost:9200/%s" name)
            {:body (json/write-value-as-string
                    {:settings {:number_of_shards 1}
                     :mappings {:properties {:id {:type "keyword"}
                                             "@timestamp" {:type "date"
                                                           :format "yyyy-MM-dd HH:mm:ss"}
                                             :field1 {:type "text"}
                                             :field2 {:type "text"}
                                             :field3 {:type "text"}}}})
             :content-type :json})
  (doseq [doc documents]
    (http/post (format "http://elastic:elastic@localhost:9200/%s/_doc/" name)
               {:body (json/write-value-as-string
                       doc)
                :content-type :json})))

(defn delete-index [name]
  (http/delete (format "http://elastic:elastic@localhost:9200/%s" name)))
