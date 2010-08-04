(defproject cd-wsapi "0.1.0-SNAPSHOT"
  :description "ClojureDocs External API"
  :dependencies [[org.clojure/clojure "1.2.0-RC1"]
                 [org.clojure/clojure-contrib "1.2.0-RC1"]
		 [mysql/mysql-connector-java "5.1.12"]
		 [aleph "0.1.0-SNAPSHOT"]
		 [net.cgrand/moustache "1.0.0-SNAPSHOT"]
		 [org.danlarkin/clojure-json "1.1"]]
  :dev-dependencies [[leiningen/lein-swank "1.2.0-SNAPSHOT"]
		     [leiningen-init-script "1.2.0-SNAPSHOT"]]
  :lis-opts {:properties {:config-clj.env "prod"}
	     :redirect-output-to "/var/log/cd-wsapi.log"}
  :main main)