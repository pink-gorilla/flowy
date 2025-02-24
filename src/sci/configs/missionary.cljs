(ns sci.configs.missionary
  (:require
   [missionary.core :as m]
   [sci.core :as sci]))

; macros
; !
; ?
; ?<
; ?>
; amb
; amb=
; ap
; cp

(def mns (sci/create-ns 'missionary.core nil))


(def missionary-namespace
  {;'absolve (sci/copy-var m/absolve mns)
   ;'attempt (sci/copy-var m/attempt mns)
   ; blk
   ;'buffer (sci/copy-var m/buffer mns)
   ;'compel (sci/copy-var m/compel mns)
   ; cpu
   ;'dfv (sci/copy-var m/dfv mns)
   ;'join (sci/copy-var m/join mns)
   'latest (sci/copy-var m/latest mns)
   ;'mbx (sci/copy-var m/mbx mns)
   ;'memo (sci/copy-var m/memo mns)
   'none (sci/copy-var m/none mns)
   'observe (sci/copy-var m/observe mns)
   ;'race (sci/copy-var m/race mns)
   'reduce (sci/copy-var m/reduce mns)
   ;'reductions (sci/copy-var m/reductions mns)
   ;'relieve (sci/copy-var m/relieve mns)
   ;'sample(sci/copy-var m/sample mns)
   'seed (sci/copy-var m/seed mns)
   ;'sem (sci/copy-var m/sem mns)
   ;'signal (sci/copy-var m/signal mns)
   'sleep (sci/copy-var m/sleep mns)
   'timeout (sci/copy-var m/timeout mns)})