(ns fooheads.cldr.generate
  (:require
    [clojure.data.json :as json]
    [clojure.java.io :as io]
    [clojure.pprint :refer [pprint]]
    [clojure.string :as str]))


(def example-config
  {:json-path "../cldr-json"

   :paths [["availableLocales" "modern"]
           ["defaultContent"]
           ["main" :locale "dates" "calendars" :calendar]
           ["main" :locale "dates" "fields"]]

   :smap {:locale ["cs" "en-US" "en-GB" "nb" "no" "sv"]
          :calendar ["gregorian"]}

   :output :code
   :output-ns 'example.cldr})


(defn- combinations
  "seq of all combinations with one element from each coll"
  [& colls]
  (let [head (first colls)
        tail (rest colls)]
    (if (seq tail)
      (for [x head
            c (apply combinations tail)]
        (cons x c))
      (map list head))))


(defn- expanded-paths [paths smap]
  (reduce
    (fn [reduced-data path]
      (let [ks (filter keyword? path)
            combos (apply combinations (map #(get smap %) ks))]
        (if (seq combos)
          (reduce
            (fn [reduced-data combo]
              (conj reduced-data (replace combo path)))

            reduced-data
            (if (seq combos)
              (map (fn [vs] (zipmap ks vs)) combos)
              path))
          (conj reduced-data path))))
    []
    paths))


(defn- json-files [path]
  (let [re #".*\.json$"]
    (->>
      path
      (io/file)
      (file-seq)
      (map io/as-relative-path)
      (remove #(str/includes? % "bower.json"))
      (remove #(str/includes? % "package.json"))
      (filter #(re-matches re %)))))


(defn- json-data [path]
  (->>
    path
    (json-files)
    (mapv (comp json/read-str slurp))))


(defn- deep-merge
  "Insipred by https://clojuredocs.org/clojure.core/merge-with"
  [& maps]
  (apply
    (fn m [& inner-maps]
      (if (every? map? inner-maps)
        (apply merge-with m inner-maps)
        (if (apply = inner-maps)
          (first inner-maps)
          (throw (ex-info (str "Can't merge these items:" inner-maps)
                          {:maps maps :inner-maps inner-maps})))))

    maps))


(defn- load-cldr-json [json-data]
  (reduce
    (fn [sum m]
      (deep-merge
        sum
        (dissoc m "version" "segments" "annotations" "annotationsDerived" "rbnf")))
    {}
    json-data))


(defn- load-cldr-data [json-path]
  (load-cldr-json (json-data json-path)))


(defn- reduce-cldr
  "Filters out data from cldr given some paths and a substitution map.
    A path with keywords, like

      [\"main\" :locale \"dates\" \"calendars\" :calendar]

    will be
    expanded to all combinations of :locale and :calendar given in the
    substitution map.

    Returns a tree containing only the selected data."

  [cldr-data paths smap]
  (reduce
    (fn [m path]
      (assoc-in m path (get-in cldr-data path)))
    {}
    (expanded-paths paths smap)))


(defn generate
  "Outputs a (reduced) CLDR definition given a config. If called with a nil args,
  outputs an example config."
  ([]
   (println ";;\n;; Example config\n;;\n")
   (pprint example-config))

  ([config]
   (let [cldr-data (load-cldr-data (:json-path config))
         reduced-cldr-data (reduce-cldr
                             cldr-data (:paths config) (:smap config))]


     (binding [*print-level* nil
               *print-length* nil]
       (println (str "(ns " (:output-ns config) ")\n"))
       (clojure.pprint/pprint
         (list 'def 'data reduced-cldr-data))))))


(comment
  (generate)

  (generate
    {:json-path "../../../Downloads/cldr/modern",
     :paths
     [["main" :locale "dates" "calendars" "gregorian" :fields]]
     :smap
     {:locale ["cs" "en" "en-GB" "sv"],
      :fields ["months" "days"]}
     ;:output :code,
     :output-ns 'example.cldr}))

