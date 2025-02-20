(ns flowy.config)

(defn config-flowy [_module-name config _exts _default-config]
  (select-keys config [:ports :mode]))