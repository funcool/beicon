(ns build
  (:refer-clojure :exclude [compile])
  (:require
   [clojure.tools.build.api :as b]
   [cljs.build.api :as api]))

(def lib 'funcool/beicon2)
(def version (format "2.0-%s" (b/git-count-revs nil)))
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def jar-file (format "target/%s-%s.jar" (name lib) version))

(defn clean [_]
  (b/delete {:path "target"}))

(defn jar [_]
  (b/write-pom
   {:class-dir class-dir
    :lib lib
    :version version
    :basis basis
    :src-dirs ["src"]})

  (b/copy-dir
   {:src-dirs ["src" "resources"]
    :target-dir class-dir})

  (b/jar
   {:class-dir class-dir
    :jar-file jar-file}))

(defn install [_]
  (b/install {:basis basis
              :lib lib
              :version version
              :jar-file jar-file
              :class-dir class-dir}))

(defn clojars [_]
  (b/process
   {:command-args ["mvn"
                   "deploy:deploy-file"
                   (str "-Dfile=" jar-file)
                   "-DpomFile=target/classes/META-INF/maven/funcool/beicon/pom.xml"
                   "-DrepositoryId=clojars"
                   "-Durl=https://clojars.org/repo/"]}))
