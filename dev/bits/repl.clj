(ns bits.repl
  (:require
    [cider.nrepl :as cider]
    [clojure.tools.nrepl.server :as nrepl.server]
    [clojure.tools.namespace.repl :as namespace.repl]
    [bits.core :as bits]))


(set! *warn-on-reflection* true)


(bits/require [bits.tonsky.coll :as coll :just [find zip]]
              [bits.tonsky.coll :just [index-of]]
              [bits.tonsky.string :as string :just [left-pad]])


(defn -main [& args]
  (nrepl.server/start-server
    :port 8888
    :handler cider/cider-nrepl-handler)
  (println "Started nREPL server at port 8888")
  (println "(coll/zip (range 0 6) (range 5 -1 -1)) =>" (coll/zip (range 0 6) (range 5 -1 -1)))
  (println "(bits.tonsky.coll/find pos? (range -6 6 4)) =>" (bits.tonsky.coll/find pos? (range -6 6 4)))
  (println "(coll/index-of pos? (range -6 6)) =>" (coll/index-of pos? (range -6 6)))

  (println "(string/left-pad \"abc\" 10) =>" (string/left-pad "abc" 10))
  (println "(bits.tonsky.string/left-pad \"abc\" 10 \\.) =>" (bits.tonsky.string/left-pad "abc" 10 \.))
  )


(comment
  (clojure.tools.namespace.repl/refresh)
)
