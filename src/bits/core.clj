(ns bits.core
  (:refer-clojure :exclude [require])
  (:require
    [clojure.edn :as edn]
    [clojure.string :as str]))


(defn- parse-fn [form]
  (assert (= (first form) 'fn))
  (let [form    (next form)
        name    (when (symbol? (first form)) (first form))
        form    (if (some? name) (next form) form)
        arities (if (vector? (first form)) [form] form)]
    { :arities  arities
      :arglists (map first arities)
      :name     name }))


(defn- require1 [nssym ns sym]
  (when-not (.startsWith (name ns) "bits.")
    (throw (Exception. "Only the bits.* namespaces can be required")))
  (let [file (str "../clojure-bits-server/"
                  (-> (str ns "/" sym)
                      (str/replace "." "/")
                      (str/replace "-" "_")
                      (str ".edn")))
        {_meta :meta
         _body :body} (edn/read-string (slurp file))
        {_arglists :arglists} (parse-fn _body)]
   `(intern ~nssym
            (with-meta '~sym (assoc ~_meta :arglists '~_arglists))
            ; (with-meta ~_body ~(meta _body)))))
            ~_body)))


(defmacro require
  "(require [`sym`+] :from `ns` :as `ns-alias`)"
  [syms & {:keys [from as]}]
  (let [nssym (gensym "ns")]
    (concat 
      `(let [~nssym (create-ns '~from)])
      (map #(require1 nssym from %) syms)
      (when (some? as)
        [`(alias '~as '~from)]))))


(comment
  (macroexpand '(req [find zip] :from bits.tonsky.coll :as coll))
)