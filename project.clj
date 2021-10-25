(defproject org.clojars.abhinav/sisyphus "0.1.1"
  :description "Sisyphus is a task scheduler that runs clojure functions periodically."
  :url "https://github.com/AbhinavOmprakash/sisyphus"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [clojure.java-time "0.3.3"]]
  :plugins [[lein-auto "0.1.3"]]
  :repl-options {:init-ns sisyphus.core})
