# Clojure Project from Scratch

This repo has a simple "Hello, World" clojure project based on these tools
with a step-by-step guide.
- [Clojure CLI](https://clojure.org/guides/deps_and_cli)
- [tools.build](https://github.com/clojure/tools.build)
- [Babashka](https://babashka.org)
- [Cognitect Labs Test Runner](https://github.com/cognitect-labs/test-runner)
- [nREPL](https://nrepl.org)
- [Rebel](https://github.com/bhauman/rebel-readline)

## Hello, World!

#### Create a minimal `deps.edn`
```
{:paths ["src"]}
```

#### Create a minimal `src/my/app.clj`
```
(ns my.app
  (:gen-class))

(defn -main
  [& args]
  (println "Hello, World!"))
```

#### Execute with clojure CLI
```
$ clj -X my.app/-main
Hello, World!

$ clj -M -m my.app
Hello, World!
```

## Command line args support

#### Update `main.clj`
```
(ns my.app
  (:require [clojure.string :as s])
  (:gen-class))

(defn greet
  [xs]
  (if (empty? xs)
    "Hello, World!" 
    (str "Hello, " (s/join ", " xs) "!")))

(defn -main
  [& args]
  (println (greet args)))
```

#### Execute with clojure CLI
```
$ clj -M -m my.app
Hello, World!

$ clj -M -m my.app Alice Bob
Hello, Alice, Bob!
```

## Babashka as a task runner

#### Create `bb.edn` by simply sym-linking `deps.edn`
`$ ln -s deps.edn bb.edn`

#### Add `:deps` and `:tasks` to deps.edn
```
{:paths ["src"]

 :deps
 {org.clojure/clojure {:mvn/version "1.9.0"}}
 
 :tasks 
 {:init (def MAIN-NS "my.app")

  run-clj
  {:doc "Run main"
   :task (apply clojure "-M -m" MAIN-NS *command-line-args*)}}}
```

#### Execute with Babashka
```
$ bb run-clj
Hello, World!

$ bb run-clj Alice Bob
Hello, Alice, Bob!
```

#### Execute from task directly

Since babashka task is simply a clojure form, main function can be directly called.
Execution in this way has much faster start-up time than using clojure CLI.
However, it works only with native clojure codes,
and if there are any java libraries used, execution will fail.

```
{
 :tasks
 {:init (def MAIN-NS "my.app")

  run-bb
  {:doc "Run main function directly"
   :task (do
           (require [(symbol MAIN-NS)])
           (apply
            (ns-resolve (find-ns (symbol MAIN-NS)) (symbol "-main"))
            *command-line-args*))}}
  ...
}
```

Simple comparison between `run-clj` and `run-bb` execution time:
```
$ time bb run-clj Alice Bob
Hello, Alice, Bob!
        0:00.81 real,   1.46 user,      0.14 sys,       0 amem, 105960 mmem

$ time bb run-bb Alice Bob    
Hello, Alice, Bob!
        0:00.01 real,   0.00 user,      0.00 sys,       0 amem, 41652 mmem
```

## Integration with tools.build

We can build jar and uberjar using tools.build.
Additionally, we can run any clojure codes through it.

#### Add `build.clj`
```
(ns build
  (:require [clojure.tools.build.api :as b]))

(def lib 'my/app) ;; NOTE: there is /, not . between my and app
(def version "0.0.1")
(def class-dir "target/classes")
(def uber-file (format "target/%s-%s-standalone.jar" (name lib) version))

;; delay to defer side effects (artifact downloads)
(def basis (delay (b/create-basis {:project "deps.edn"})))

(defn clean [_]
  (b/delete {:path "target"}))

(defn jar [_]
  (b/write-pom {:class-dir class-dir
                :lib lib
                :version version
                :basis @basis
                :src-dirs ["src"]})
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/jar {:class-dir class-dir
          :jar-file jar-file}))

(defn uber [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/compile-clj {:basis @basis
                  :ns-compile '[my.app]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis @basis
           :main 'my.app}))
```

#### Add aliasses to `deps.edn`
```
{:paths ["src"]

 ;; ...
 
 :aliases
 {:build {:deps {io.github.clojure/tools.build {:mvn/version "0.10.9"}}
          :ns-default build}}}
```

#### Build and execute uberjar

`build.clj` file is accessed through `:build` alias, and it can be executed using clojure CLI, e.g.

```
$ clj -T:build clean

$ clj -T:build jar

$ clj -T:build uber

$ java -jar target/app-0.0.1-standalone.jar
Hello, World!

$ java -jar target/app-0.0.1-standalone.jar Alice Bob
Hello, Alice, Bob!
```

#### Add `run-uber` to `build.clj`

Additionally we can even run the uber-jar throught `build.clj` by adding these to `build.clj`.

```
(ns build
  (:require [clojure.tools.build.api :as b]
            [babashka.process :refer [shell]]))

;; ...

(defn run-uber [opts]
  (apply shell "java" "-jar" uber-file (:args opts)))
```

Command line args can be passed like this when directly using `clj`
```
$ clj -T:build run-uber :args '["Alice" "Bob"]'
```

#### Add babashka tasks to `deps.edn`

The commands can be further simplied through babashka tasks:

```
{
 :tasks 
 {
  clean
  {:doc "Clean"
   :task (clojure "-T:build clean")}

  jar
  {:doc "Build jar"
   :task (clojure "-T:build jar")}

  uber
  {:doc "Build uberjar"
   :override-builtin true
   :task (clojure "-T:build uber")}
  
  run-uber
  {:doc "Run uberjar"
   :task (let [args *command-line-args*
               opts (if (empty? args) [] [":args" (str args)])]
           (apply clojure "-T:build run-uber" opts))}}

 :aliases
 {:build
  {:deps {io.github.clojure/tools.build {:mvn/version "0.10.9"}
          babashka/process {:mvn/version "0.6.23"}}
   :ns-default build}}}
```

#### Execute with Babashka
```
$ bb clean
$ bb jar
$ bb uber
$ bb run-uber
Hello, World!

$ bb run-uber Alice Bob
Hello, Alice, Bob!
```

## Tests

#### Add `test/my/app_test.clj`
```
(ns my.app-test
  (:require [clojure.test :refer [deftest testing is]]
            [my.app :refer [greet]))

(deftest greet-tests
  (testing "No args"
    (is (= (greet []) "Hello, World!"))
    (is (= (greet nil) "Hello, World!")))
  (testing "With args"
    (is (= (greet ["Alice" "Bob"]) "Hello, Alice, Bob!"))))
```

#### Update `deps.edn`
```
{:deps
 {org.clojure/core.async {:mvn/version "1.3.610"}}
 
 :tasks 
 {test
  {:doc "Run tests"
   :task (apply clojure "-M:test" *command-line-args*)}}

 :aliases
 {:test
  {:extra-paths ["test"]
   :extra-deps {io.github.cognitect-labs/test-runner {:git/tag "v0.5.1"
                                                      :git/sha "dfb30dd"}}
   :main-opts ["-m" "cognitect.test-runner"]
   :exec-fn cognitect.test-runner.api/test}}}
```

#### Run all tests
```
$ bb test
```

#### Run a single namespace
```
$ bb test -n my.app-test
```

#### Run a single test case
```
$ bb test -v my.app-test/greet-test
```

## REPL / nREPL

#### Rebel REPL

Update `deps.edn` by adding these
```
{
 :task
 {repl
  {:doc "Start Rebel REPL"
   :override-builtin true
   :task (clojure "-T:rebel")}}

 :aliases
 {:rebel
  {:extra-deps {com.bhauman/rebel-readline-nrepl {:mvn/version "0.1.5"}}
   :exec-fn rebel-readline.tool/repl
   :exec-args {}
   :main-opts ["-m" "rebel-readline.main"]}}}
```

Run REPL by
```
$ bb repl
```

#### nREPL Server

Update `deps.edn` by adding these
```
{
 :tasks
 {nrepl-server
  {:doc "Start nREPL server"
   :override-builtin true
   :task (clojure "-M:nrepl -m nrepl.cmdline")}}

 :aliases
 {:nREPL
  {:extra-deps {nrepl/nrepl {:mvn/version "1.3.1"}}}}
```

Start nREPL server by
```
$ bb nrepl-server
```

#### nREPL client using Rebel REPL

Update `deps.edn` by adding these
```
{
 :tasks
 {nrepl
  {:doc "Start nREPL client"
   :task (clojure "-T:nrebel :port" (slurp ".nrepl-port"))}}

 :aliases 
 {:nrebel 
  {:extra-deps {com.bhauman/rebel-readline-nrepl {:mvn/version "0.1.5"}}
   :exec-fn rebel-readline.nrepl/connect
   :exec-args {:background-print false}
   :main-opts ["-m" "rebel-readline.nrepl.main"]}}}
```

Start nREPL client by
```
$ bb nrepl
```

## Dev mode

Games can run in dev mode for easier REPL based development.
This dev mode is inspired by 
- [moon](https://github.com/damn/moon)
- [Clojure Workflow Reloaded](https://www.cognitect.com/blog/2013/06/04/clojure-workflow-reloaded)

Start dev mode using
```
$ bb dev
```

Workflow will be slightly different depending on the app type
- Short running apps like CLI tools
- Long running apps like server or UI app

In both cases, [nREPL server](https://github.com/nrepl/nrepl) will be started with `.nrepl-port` file created.
Any nREPL client can be used to interact with running app.
`bb nrepl` can also be used to start a nREPL client

### Short running apps
- Once the nREPL server is started, the main thread will be block on a channel indefinitely.
- The app can be controlled by `init!`, `start!`, `go`, `stop!`, and `refresh!`,
  similarily described in "Clojure Workflow Reloaded"

### Long running apps
- Dev loop can be useful for this case.
- To enable dev lopp, comment `(<!! dev-chan)` and uncomment `(start-dev-loop!)`
  in the `-main` function in `dev.clj`.
- After the nREPL server is started, the dev loop will call `go!`.
- The app data will be stored to `dev/system` atom defined in `dev/dev.clj` file.
- When the app is finished, dev loop will automatically tries to reload all the changes and restart the app. 
- This can be triggered from REPL by evaluating `(dev/stop!)`
- When there is any exception, app will print out the error and wait to be restarted
- Once changes are made, app can be restarted from REPL by evaluating `(dev/restart!)`

`user.clj` in the top level directory contains various useful forms for this process.


