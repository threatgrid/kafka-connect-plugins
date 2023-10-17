(ns threatgrid.integration-test.core-test
  (:require
   [clojure.test :refer [deftest is]]
   [threatgrid.integration-test.elastic :as elastic]
   [threatgrid.integration-test.kafka :as kafka]
   [threatgrid.integration-test.kafka-connect :as kafka-connect])
  (:import
   java.time.Duration
   java.time.format.DateTimeFormatter
   java.time.LocalDateTime))

(def formatter (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm:ss"))

(defn now []
  (let [date (LocalDateTime/now)]
    (.format date formatter)))

(defmacro integration-test [{:keys [kafka documents]} f]
  (let [topic (str (random-uuid))
        index (str (random-uuid))
        connector (str (random-uuid))]
    `(do
       (println "Testing:")
       (println (format "  Topic: %s" ~topic))
       (println (format "  ES index: %s" ~index))
       (println (format "  Connector: %s" ~connector))
       (let [ka# (kafka/admin-client ~kafka)
             kc# (kafka/consumer (merge ~kafka
                                        {"group.id" (str (random-uuid))
                                         "auto.offset.reset" "earliest"}))]
         (try
           (kafka/create-topic ka# ~topic)
           (elastic/create-index ~index ~documents)
           (kafka-connect/start-connector ~connector ~index ~topic)
           (.subscribe kc# (re-pattern ~topic))
           (Thread/sleep 5000)
           (~f kc#)
           (catch Exception e#
             (println e#))
           (finally
             (kafka-connect/stop-connector ~connector)
             (elastic/delete-index ~index)
             (kafka/delete-topic ka# ~topic)))))))

(deftest all-in-test
  (let [documents (for [id (repeatedly 10 random-uuid)
                        :let [doc {:id (str id)
                                   (keyword "@timestamp") (now)
                                   :field1 "a"
                                   :field2 "b"
                                   :field3 "c"}]]
                    doc)]
    (integration-test {:kafka {"bootstrap.servers" "localhost:11091"}
                       :documents documents}
                      (fn [consumer]
                        (let [recs (seq (.poll consumer (Duration/ofSeconds 10)))]
                          (is (= 10 (count recs)))
                          (is (= (into #{} documents)
                                 (into #{} (map kafka/cr->map) recs))))))))
