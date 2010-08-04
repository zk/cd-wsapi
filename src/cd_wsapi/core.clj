(ns cd-wsapi.core
  (:use [aleph core http]
	[net.cgrand.moustache]
	[clojure.contrib.sql]
	[org.danlarkin.json]))

(def *server-port* 8080)

;;JSON Encoders
(add-encoder java.util.Date
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
     :body (encode-to-str (with-connection db
			    (transaction
			     (when-let [id (with-query-results rs ["select id from functions where ns = ? and name = ?" ns name] (:id (first (doall rs))))]
			       (when-let [examples (with-query-results rs ["select * from examples where function_id = ?" id] (doall rs))]
				 (if examples
				   examples
				   []))))))
     }))

(let [ns "clojure.core"
      name "mapa"]
  (encode-to-str (with-connection db
		   (transaction
		    (when-let [id (with-query-results rs ["select id from functions where ns = ? and name = ?" ns name] (:id (first (doall rs))))]
		      (when-let [examples (with-query-results rs ["select * from examples where function_id = ?" id] (doall rs))]
			examples))))))

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

(def server (start-http-server app-wrapper {:port *server-port*}))

(defn restart-server []
  (server)
  (def server (start-http-server app-wrapper {:port *server-port*})))

(restart-server)



(def req {:remote-addr "127.0.0.1", :scheme :http, :query-string "asdf=qwer", :uri "/foo", :keep-alive? true, :server-port "8080", :server-name "localhost", :headers {"host" "localhost:8080", "connection" "keep-alive", "user-agent" "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_3; en-US) AppleWebKit/533.4 (KHTML, like Gecko) Chrome/5.0.375.125 Safari/533.4", "cache-control" "max-age=0", "accept" "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5", "accept-encoding" "gzip,deflate,sdch", "accept-language" "en-US,en;q=0.8", "accept-charset" "ISO-8859-1,utf-8;q=0.7,*;q=0.3"}, :request-method :get})

#_ ((app ["foo"] (partial app-handler nil)) req)

