(ns elastic.setup
  (:require
   [babashka.http-client :as http]
   [cheshire.core :as json]
   [progrock.core :as pr])
  (:import
   java.time.format.DateTimeFormatter
   java.time.LocalDateTime))

(def formatter (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm:ss"))

(defn now []
  (let [date (LocalDateTime/now)]
    (.format date formatter)))

(defn random-str [len]
  (apply str (take len (repeatedly #(char (+ (rand 26) 65))))))

(defn create-index []
  (http/put "http://localhost:9200/test"
            {:headers {"Content-Type" "application/json"}
             :basic-auth ["elastic" "elastic"]
             :body (json/generate-string
                    {:settings {:number_of_shards 1}
                     :mappings {:properties {:id {:type "keyword"}
                                             "@timestamp" {:type "date"
                                                           :format "yyyy-MM-dd HH:mm:ss"}
                                             :field1 {:type "text"}
                                             :field2 {:type "text"}
                                             :field3 {:type "text"}}}})}))

(defn generate-test-data [progress-bar count]
  (loop [[i & is] (range count)
         progress-bar progress-bar]
    (when i
      (let [id (random-uuid)
            field1 (random-str 10)
            field2 (random-str 100)
            field3 (random-str 1000)]
        (pr/print progress-bar)
        (http/put (format "http://localhost:9200/test/_doc/%s" i)
                  {:headers {"Content-Type" "application/json"}
                   :basic-auth ["elastic" "elastic"]
                   :body (json/generate-string
                          {:id id
                           "@timestamp" (now)
                           :field1 field1
                           :field2 field2
                           :field3 field3})})
        (recur is (pr/tick progress-bar 1))))))

(defn -main [& _args]
  (create-index)
  (let [count 50000
        bar (pr/progress-bar count)]
    (generate-test-data bar count)))
