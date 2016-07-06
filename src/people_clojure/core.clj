(ns people-clojure.core
  (:require [clojure.string :as str]
            [compojure.core :as c]
            [ring.adapter.jetty :as j]
            [hiccup.core :as h])
  (:gen-class))

(defn read-people []
  (let [people (slurp "people.csv")
        people (str/split-lines people)
        people (map (fn [line] (str/split line #","))
                 people)
        header (first people)
        people (rest people)
        people (map (fn [line] (zipmap header line))
                 people)]               
    people))

(defn people-html [people]
  [:html
   [:body
    [:ol
     (map (fn [person]
            [:li (str (get person "first_name") " " (get person "last_name"))])
        people)]]])

(defn filter-by-country [people country]
  (filter (fn [person]
            (= country (get person "country")))
    people))
     
(c/defroutes app
  (c/GET "/:country{.*}" [country]
    (let [people (read-people)
          people (if (= 0 (count country))
                   people
                   (filter-by-country people country))]
      (h/html (people-html people)))))

(defonce server (atom nil))

(defn -main []
  (if @server
    (.stop @server))
  (reset! server (j/run-jetty app {:port 3000 :join? false})))
  