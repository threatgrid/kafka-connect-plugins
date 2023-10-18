(ns threatgrid.integration-test.kafka
  (:require
   [jsonista.core :as json])
  (:import
   [java.util Optional Properties]
   [org.apache.kafka.clients.admin Admin AdminClient NewTopic]
   [org.apache.kafka.clients.consumer ConsumerRecord KafkaConsumer]
   [org.apache.kafka.common
    TopicCollection$TopicNameCollection]
   [org.apache.kafka.common.header Headers]
   [org.apache.kafka.common.serialization
    Deserializer
    Serde
    Serdes$StringSerde
    Serializer]))

(set! *warn-on-reflection* true)

(defn admin-client [properties]
  (let [props (doto (Properties.)
                (.putAll properties))]
    (AdminClient/create props)))

(defn create-topic [^Admin admin ^String topic]
  (-> admin
      (.createTopics [(NewTopic. topic (Optional/empty) (Optional/empty))])
      (.all)
      (.get)))

(defn delete-topic [^Admin admin ^String topic]
  (-> admin
      (.deleteTopics (TopicCollection$TopicNameCollection/ofTopicNames [topic]))
      (.all)
      (.get)))

(defn serde ^Serde [clj->bytes bytes->clj]
  (reify Serde
    (serializer [_]
      (reify Serializer
        (serialize [_this _topic data]
          (clj->bytes data))
        (serialize [_this _topic _headers data]
          (clj->bytes data))))
    (deserializer [_]
      (reify Deserializer
        (deserialize [_this _topic data]
          (bytes->clj data))
        (deserialize [_this ^String _topic ^Headers _headers ^bytes data]
          (bytes->clj data))))))

(defn json-serde
  "JSON serializer/deserializer configurable through jsonista/object-mapper."
  (^Serde [] (json-serde (json/object-mapper {:encode-key-fn true :decode-key-fn true})))
  (^Serde [object-mapper]
   (serde
    (fn [obj]
      (json/write-value-as-bytes obj object-mapper))
    (fn [^bytes bs]
      (json/read-value bs object-mapper)))))

(defn consumer [properties]
  (let [^Serde key-serde (Serdes$StringSerde.)
        value-serde (json-serde)
        ^Deserializer key-deserializer (.deserializer key-serde)
        ^Deserializer value-deserializer (.deserializer value-serde)]
    (KafkaConsumer. (doto (Properties.) (.putAll properties))
                    key-deserializer value-deserializer)))

(defn cr->map [^ConsumerRecord cr]
  (let [value (.value cr)]
    value))
