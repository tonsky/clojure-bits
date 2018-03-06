(ns bits.core
  (:refer-clojure :exclude [require])
  (:require
    [clojure.edn :as edn]
    [clojure.string :as str]
    [clojure.tools.reader :as reader]
    [clojure.tools.reader.reader-types :as reader-types]))


(defmacro cond+ [& clauses]
  (when clauses
    (let [[c1 c2 & cs] clauses]
      (cond
        (< (count clauses) 2) (throw (IllegalArgumentException. "cond requires an even number of forms"))
        (= c1 :let)          `(let ~c2 (cond+ ~@cs))
        (= c1 :do)           `(do ~c2 (cond+ ~@cs))
        (= c1 :when-some)    `(if-some ~c2 ~(first cs) (cond+ ~@(next cs)))
        :else                `(if ~c1 ~c2 (cond+ ~@cs))))))


(defn read-clojure
  ([s]
    (binding [reader/*read-eval* false
              reader/*alias-map* {}]
      (reader/read
        { :read-cond :preserve }
        (reader-types/indexing-push-back-reader s))))
  ([s feature]
    (binding [reader/*read-eval* false
              reader/*alias-map* {}]
      (reader/read
        { :read-cond :allow, :features #{feature} }
        (reader-types/indexing-push-back-reader s)))))


(defn underline
  ([s]
    (apply str s "\n" (repeat (count s) "^")))
  ([s pos]
    (if (neg? pos)
      (recur s (+ (count s) pos))
      (str s "\n" (str/join (repeat pos " ")) "^"))))


; (defn name docstring meta2? [arglist*] body)
; (defn name docstring meta2? ([arglist*] body)+ meta3?)

(defn parse-defn-form [form]
  (cond+
    (not (list? form))
    {:code (underline (pr-str form)) :message "Expected list"}

    :let [[defn name docstring & fdecl] form]

    (< (count form) 1)
    {:code (underline (pr-str form) -1) :message "Expected “defn”"}

    (not= defn 'defn)
    {:code (underline (pr-str defn)) :message "Expected “defn”"}

    (< (count form) 2)
    {:code (underline (pr-str form) -1) :message "Expected symbol after “defn”"}
    
    (not (symbol? name))
    {:code (underline (pr-str name)) :message "Expected symbol"}

    (not (simple-symbol? name))
    {:code (underline (pr-str name)) :message "Expected non-namespaced symbol"}

    :let [m1 (meta name)]

    (< (count form) 3)
    {:code (underline (pr-str form) -1) :message "Expected docstring, mandatory"}

    (not (string? docstring))
    {:code (underline (str name " ") -1) :message "Expected docstring, mandatory"}

    (str/blank? docstring)
    {:code (underline (pr-str docstring)) :message "I see what you did here. No, docstring is still mandatory"}

    :let [[m2 fdecl] (if (map? (first fdecl))
                       [(first fdecl) (next fdecl)]
                       [nil fdecl])]
    
    (empty? fdecl)
    {:code (underline (pr-str form) -1) :message "Expected arglist vector"}

    (and (not (vector? (first fdecl)))
         (not (list? (first fdecl))))
    {:code (underline (pr-str (first fdecl))) :message "Expected arglist vector"}

    :let [[bodies m3] (cond
                        (vector? (first fdecl)) [[fdecl] nil]
                        (map? (last fdecl))     [(butlast fdecl) (last fdecl)]
                        :else                   [fdecl nil])]

    :when-some [not-list (first (remove list? bodies))]
    {:code (underline (pr-str not-list)) :message "Expected list"}

    :when-some [empty (first (filter empty? bodies))]
    {:code (underline (pr-str empty)) :message "Expected arglist"}

    :when-some [no-arglist (first (remove #(vector? (first %)) bodies))]
    {:code (underline (pr-str (first no-arglist))) :message "Expected arglist vector"}

    :else
    { :name      name
      :meta      (merge m1 m2 m3)
      :docstring docstring
      :bodies    bodies
      :arglists  (map first bodies) }))


(defmacro one-of? [x & opts]
  (let [xsym (gensym)]
   `(let [~xsym ~x]
      (or ~@(map (fn [o] `(= ~xsym ~o)) opts)))))


(defn- escape-char [ch]
  (cond
    (<= (int \a) (int ch) (int \z))   ch
    (<= (int \A) (int ch) (int \Z))   ch
    (<= (int \0) (int ch) (int \9))   ch
    (one-of? ch \_ \- \. \+ \! \= \/) ch
    (<= 128 (int ch))                 ch
    :else (str "(" (str/upper-case (Integer/toString (int ch) 16)) ")")))


(defn fqn->path [fqn]
  (str/join (map escape-char fqn)))


(defn path->fqn [path]
  (str/replace path #"\(([0-9A-F]+)\)"
    (fn [[_ hex]]
      (str (char (Integer/parseInt hex 16))))))


(defn fetch [fqn]
  (slurp (str "/Users/prokopov/Dropbox/ws/clojure-bits-server/bits/" (fqn->path fqn) ".cljc"))) ;; FIXME server url


(defn- require-bit [nssym ns sym]
  (when-not (.startsWith (name ns) "bits.")
    (throw (Exception. "Only the bits.* namespaces can be required")))
  (let [fn (parse-defn-form (read-clojure (fetch (str ns "/" sym)) :clj)) ;; TODO detect env
        {:keys [meta docstring bodies arglists]} fn]
   `(intern ~nssym
            (with-meta '~sym (assoc ~meta :arglists '~arglists :doc ~docstring))
            (fn ~sym ~@bodies))))


(defmacro require1 [spec]
  (let [[from & {as :as [& syms] :just}] spec
        nssym (gensym "ns")]
    (concat 
      `(let [~nssym (create-ns '~from)])
      (map #(require-bit nssym from %) syms)
      (when (some? as)
        [`(alias '~as '~from)]))))
  

(defmacro require
  "(require [`ns` :as `ns-alias` :just [`sym`+]]+)"
  [& specs]
  (cons `do
    (for [spec specs]
      `(require1 ~spec))))


(comment
  (require [bits.tonsky.coll :just [find zip]])
  (require [bits.tonsky.string :just [left-pad]])
  (clojure.pprint/pprint (macroexpand '(require [bits.tonsky.string :just [left-pad]])))
)