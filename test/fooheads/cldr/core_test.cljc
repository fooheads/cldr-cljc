(ns fooheads.cldr.core-test
  (:require
    [clojure.test :refer [deftest is]]
    [examples.cldr-reduced]
    [fooheads.cldr.core :as cldr]))


(deftest get-in-test
  (cldr/set-db! examples.cldr-reduced/data)

  (is (= "ledna"
         (cldr/get-in
           ["cs"]
           ["main" :locale "dates" "calendars" "gregorian" "months" "format" "wide" "1"])))

  (is (= "leden"
         (cldr/get-in
           ["cs"]
           ["main" :locale "dates" "calendars" "gregorian" "months" "stand-alone" "wide" "1"])))

  (is (= "sön"
         (cldr/get-in
           ["sv-SE" "sv"]
           ["main" :locale "dates" "calendars" "gregorian" "days" "format" "abbreviated" "sun"]))))

