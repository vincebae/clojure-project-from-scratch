# Tic-Tac-Toe by ClojureScript / Shadow-cljs / Re-frame

Example project of shadow-cljs / re-frame application.

## References

- [Clojure](https://clojure.org/)
- [ClojureScript](https://clojurescript.org/index)
- [shadow-cljs](https://github.com/thheller/shadow-cljs)
- [Re-frame](https://github.com/day8/re-frame)

## Instructions

First, install npm packages by
```
$ npm install
```

### Dev mode

The app can run in dev mode by

```
$ npm run dev 
```

- Dev dashboard can be accessed from `http://localhost:9630`
- Dev server can be accessed from `http://localhost:8000`
- nREPL server will be started with `.nrepl-port` generated

The nREPL server can be accessed by any nREPL client,
but it will start as a regluar clojure session.
In order to properly interact with the application,
cljs session needs to be started.
It can be started and exited with these forms:
```
;; start cljs session
(shadow.cljs.devtools.api/repl :client)

;; exit cljs session
:cljs/quit
```

`user.cljs` file in the top level contains useful forms for repl driven development.

