(ns main
  (:use [cd-wsapi.core]
	[aleph core http])
  (:gen-class))

(defn -main [& args]
  (start-http-server app-wrapper {:port 8080})
  (loop []
      (Thread/sleep 1000)
    (recur)))