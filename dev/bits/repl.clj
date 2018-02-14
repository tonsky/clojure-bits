(ns bits.repl
  (:require
    [cider.nrepl :as cider]
    [clojure.tools.nrepl.server :as nrepl.server]
    [clojure.tools.namespace.repl :as namespace.repl]
    [bits.core :as bits]))


(set! *warn-on-reflection* true)


(bits/require [find zip] :from bits.tonsky.coll :as coll)
(bits/require [index-of] :from bits.tonsky.coll)
(bits/require [left-pad] :from bits.tonsky.string :as string)
#_(bits/require [left-pad] :from src.tonsky.string :as string)

; (bits/require [bits.tonsky.string :as string :only [left-pad]])
; (bits/require [bits.tonsky.coll :as coll :only [find zip index-of])

(defn -main [& args]
  (nrepl.server/start-server
    :port 8888
    :handler cider/cider-nrepl-handler)
  (println "Started nREPL server at port 8888")
  (println "(coll/zip (range 0 6) (range 5 -1 -1)) =>" (coll/zip (range 0 6) (range 5 -1 -1)))
  (println "(coll/find pos? (range -6 6 4)) =>" (coll/find pos? (range -6 6 4)))
  (println "(coll/index-of pos? (range -6 6)) =>" (coll/index-of pos? (range -6 6)))

  (println "(string/left-pad \"abc\" 10) =>" (string/left-pad "abc" 10))
  (println "(string/left-pad \"abc\" 10 \\.) =>" (string/left-pad "abc" 10 \.)))


(comment
  (clojure.tools.namespace.repl/refresh)

  (meta #'string/left-pad)
  (meta #'left-pad)
  (string/left-pad "abc" 10 \space)
  (left-pad "abc" 10)

  (macroexpand '(bits/require [left-pad] :from bits.tonsky.string))
  
  ((clojure.core/with-meta
      (fn left-pad
        ([s len] (left-pad s len \space))
        ([s len ch]
          (let [c (count s)]
            (if (>= c len)
              s
              (let [sb (StringBuilder. len)]
                (dotimes [_ (- len c)] (.append sb ch))
                (.append sb s)
                (str sb))))))
        nil) "abc" 10 \.)
  ; (require '[bits.tonsky.coll :as coll])
  (alias 'coll 'bits.tonsky.coll)
  (coll/find pos? (range -50 50 4))
  (defn ^String somefn "Docs"
    (^String [^long a] (str a))
    ([a [b c]] (str a b c))
    ([a b & rest] (apply str a b rest)))

  (let [ns (create-ns 'bits.test2)]
    (intern ns 'a 1))

  (req bits.tonsky.coll find)
  (binding [*print-meta* true]
    (prn (macroexpand '(req bits.tonsky.coll find))))
  (bits.tonsky.coll/find pos? [-1 0 1 2 3 4])
  (binding [*print-meta* true]
    (prn (meta #'bits.tonsky.coll/find))
    (prn (meta bits.tonsky.coll/find)))
  (type (:tag (meta #'bits.tonsky.coll/find)))
  (type (:rettag (meta bits.tonsky.coll/find)))

  (req bits.tonsky.coll zip)
  (bits.tonsky.coll/zip (range 0 10) (range 0 5))
  (meta #'bits.tonsky.coll/zip)
  
  (binding [*print-meta* true]
    (prn (meta #'somefn)))
  (meta (first (:arglists (meta #'somefn))))

  (binding [*print-meta* true]
    (prn (macroexpand '(defn afn "doc" ^String [x y] (str x y)))))
  (binding [*print-meta* true]
    (prn (macroexpand '(fn afn [x y] (str x y)))))

  
  ; (set! *warn-on-reflection* true)
  ; (defn a [] 1)
  ; (loop [x 0] (if (= x 1) x (recur (a))))
)
