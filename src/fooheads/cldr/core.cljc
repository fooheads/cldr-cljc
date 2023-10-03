(ns fooheads.cldr.core
  (:refer-clojure :rename {format core-format
                           get-in core-get-in})
  (:require
    [clojure.pprint]))


(defonce ^:private cldr-db (atom nil))

(defn set-db! [data] (reset! cldr-db data))
(defn db [] @cldr-db)


(defn get-in
  "Gets the value, trying each of the accept-locales until there is a match.
  Returns nil if no value found for any locale."
  [accept-locales path]
  (reduce
    (fn [_ locale]
      (let [path (replace {:locale locale} path)]
        (when-let [v (core-get-in (db) path)]
          (reduced v))))
    nil
    accept-locales))

