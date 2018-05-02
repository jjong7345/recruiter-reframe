(defproject recruit-app "1.3.7-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.9.0-alpha16"]
                 [org.clojure/clojurescript "1.9.854"]
                 [com.taoensso/timbre "4.10.0"]
                 [org.clojure/data.codec "0.1.0"]
                 [reagent "0.6.0"]
                 [re-frame "0.9.2"]
                 [re-frisk "0.4.5"]
                 [org.clojure/core.async "0.3.442"]
                 [re-com "2.1.0"]
                 [secretary "1.2.3"]
                 [garden "1.3.2"]
                 [cljsjs/quill "1.2.5-4"]
                 [ns-tracker "0.3.0"]
                 [compojure "1.5.0"]
                 [yogthos/config "0.8"]
                 [ring "1.4.0"]
                 [ring/ring-json "0.4.0"]
                 [cljs-ajax "0.5.9"]
                 [clj-http "2.0.0"]
                 [org.clojure/spec.alpha "0.1.108"]
                 [org.clojure/test.check "0.9.0"]
                 [buddy/buddy-core "1.4.0"]
                 [buddy/buddy-sign "1.4.0"]
                 [buddy/buddy-hashers "1.2.0"]
                 [com.cognitect/transit-cljs "0.8.239"]
                 [day8.re-frame/http-fx "0.1.3"]
                 [day8.re-frame/async-flow-fx "0.0.7"]
                 [domina "1.0.3"]
                 [hickory "0.7.1"]
                 [stylefy "1.2.0"]
                 [re-pressed "0.2.0"]
                 [com.andrewmcveigh/cljs-time "0.5.0"]
                 [com.cognitect/transit-cljs "0.8.239"]
                 [cljsjs/cropper "0.8.1-0"]
                 [figwheel-sidecar "0.5.0"]
                 [com.cemerick/url "0.1.1"]
                 [pandect "0.6.0"]
                 [yogthos/config "0.9"]
                 [ladders-domains "0.2.18"]
                 [hal "0.3.2"]
                 [org.purefn/irulan "0.3.1"]
                 [com.theladders/gardner "0.5.2"]
                 [com.theladders/ace-ventura "0.3.6"]
                 [com.theladders/hitch "0.2.0"]
                 [rid3 "0.2.0"]
                 [clojure.java-time "0.3.1"]]

  :repositories
  [["snapshots"
    {:url      "http://repo.aws.theladders.com:8081/nexus/content/repositories/snapshots/"
     :username "deployment"
     :password "test123"}]
   ["releases"
    {:url           "http://repo.aws.theladders.com:8081/nexus/content/repositories/releases/"
     :username      "deployment"
     :password      "test123"
     :sign-releases false}]]

  :plugins [[lein-cljsbuild "1.1.4"]
            [lein-garden "0.2.8"]
            [lein-less "1.7.5"]
            [cljs-simple-cache-buster "0.2.1"]]

  :cljs-simple-cache-buster {:cljsbuild-id  ["min" "dev"]
                             :template-file "resources/template/index.html"
                             :output-to     "resources/public/index.html"}

  :min-lein-version "2.5.3"

  :source-paths ["src/clj" "script" "src/cljc"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"
                                    "test/js"
                                    "resources/public/css"]

  :figwheel {:css-dirs     ["resources/public/css"]
             :ring-handler recruit-app.handler/dev-handler}

  :garden {:builds [{:id           "screen"
                     :source-paths ["src/clj"]
                     :stylesheet   recruit-app.css/screen
                     :compiler     {:output-to     "resources/public/css/screen.css"
                                    :pretty-print? true}}]}

  :less {:source-paths ["less"]
         :target-path  "resources/public/css"}

  :profiles
  {:prod {:resource-paths ["config/prod"]}
   :staging {:resource-paths ["config/staging"]}
   :qa   {:resource-paths ["config/qa"]}
   :dev  {:resource-paths ["config/dev"]
          :dependencies   [[binaryage/devtools "0.8.2"]]
          :plugins        [[lein-figwheel "0.5.10-SNAPSHOT"]
                           [lein-doo "0.1.7"]
                           [lein-re-frisk "0.4.5"]]}}

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs" "src/cljc"]
     :figwheel     {:on-jsload "recruit-app.core/mount-root"}
     :compiler     {:main                 recruit-app.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true
                    :preloads             [devtools.preload]
                    :external-config      {:devtools/config {:features-to-install :all}}}}

    {:id           "min"
     :source-paths ["src/cljs" "src/cljc"]
     :jar          true
     :compiler     {:main                   recruit-app.core
                    :output-to              "resources/public/js/compiled/app.js"
                    :optimizations          :advanced
                    :closure-defines        {goog.DEBUG false}
                    :closure-output-charset "US-ASCII"
                    :pretty-print           false
                    :externs                ["externs/cxApi.js"]}}

    {:id           "test"
     :source-paths ["src/cljs" "src/cljc" "test/cljs"]
     :compiler     {:main          "recruit-app.runner"
                    :output-to     "resources/public/js/compiled/test.js"
                    :output-dir    "resources/public/js/compiled/test/out"
                    :optimizations :none}}]}

  :main recruit-app.server

  :aot [recruit-app.server]

  :uberjar-name "recruit-app.jar"

  :prep-tasks [["cljsbuild" "once" "min"] ["garden" "once"] ["less" "once"] "compile"])
