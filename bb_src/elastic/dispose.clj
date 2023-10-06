(ns elastic.dispose
  (:require
   [babashka.http-client :as http]))

(defn delete-index []
  (http/delete "http://localhost:9200/test"
               {:basic-auth ["elastic" "elastic"]}))

(defn -main [& _args]
  (delete-index))
