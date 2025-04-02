(ns flowy.log
  (:require
   [missionary.core :as m]
   [babashka.fs :refer [create-dirs]])
  (:import
   [java.io StringWriter]))

(def root-dir ".flowy")

(defn create-logger [browser-id]
  (create-dirs root-dir)
  (str root-dir "/" browser-id ".txt"))

(defn log [this & texts]
  (spit this (apply str "\n" texts) :append true))