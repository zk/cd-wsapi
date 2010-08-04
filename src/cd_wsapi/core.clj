(ns cd-wsapi.core
  (:use [aleph core http]
	[net.cgrand.moustache]
	[clojure.contrib.sql]
	[org.danlarkin.json]))

(def *server-port* 8080)

;;JSON Encoders
(add-encoder 
 java.util.Date
 (fn [#^java.util.Date date #^java.io.Writer writer
      #^String pad #^String current-indent
      #^String start-token-indent #^Integer indent-size]
   (.append writer (str start-token-indent \" date \"))))

;; Database

(def db {:classname "com.mysql.jdbc.Driver"
	 :subprotocol "mysql"
	 :subname "//localhost:3306/clojuredocs?user=cd_wsapi&password=cd_wsapi"
	 :create true
	 :username "cd_wsapi"
	 :password "cd_wsapi"})

(defn default [request]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body "null"})

(defn examples [ns name]
  (fn [r]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (encode-to-str 
	    (with-connection db
	      (transaction
	       (when-let [id (with-query-results 
			       rs 
			       ["select id from functions where ns = ? and name = ?" ns name] 
			       (:id (first (doall rs))))]
		 (when-let [examples (with-query-results 
				       rs 
				       ["select * from examples where function_id = ?" id] 
				       (doall rs))]
		   examples)))))}))

(defn app-handler [channel request]
  (enqueue-and-close 
   channel
   ((app
     ["stuff"] examples
     ["examples" ns name] (examples ns name)
     [&] default) 
    request)))

(defn app-wrapper [channel request]
  (app-handler channel request))

(comment (def server (start-http-server app-wrapper {:port *server-port*}))

	 (defn restart-server []
	   (server)
	   (def server (start-http-server app-wrapper {:port *server-port*})))

	 (restart-server))

