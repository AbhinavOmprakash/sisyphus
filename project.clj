(defproject org.clojars.abhinav/sisyphus "0.1.1"
  :description "Sisyphus is a task scheduler that runs clojure functions periodically."
  :url "https://github.com/AbhinavOmprakash/sisyphus"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [clojure.java-time "0.3.3"]]
  :plugins [[lein-auto "0.1.3"]]
  :repl-options {:init-ns sisyphus.core})
