(ns beicon.v2.core
  (:refer-clojure :exclude [comp]))

(defmacro push!
  [ob val]
  `(.next ~(with-meta ob {:tag 'js}) ~val))

(defmacro error!
  [ob val]
  `(.error ~(with-meta ob {:tag 'js}) ~val))

(defmacro end!
  [ob]
  `(.complete ~(with-meta ob {:tag 'js})))

(defmacro comp
  [& items]
  (let [source-s (gensym "source")
        binds    (map-indexed
                  (fn [index item]
                    (gensym (str "item" index)))
                  items)]

    `(fn [~source-s]
       (let [~@(->> (map vector binds items)
                    (mapcat identity))]

         ~(reduce (fn [acc bind]
                    `(beicon.v2.core/internal-call ~bind ~acc))
                  source-s
                  binds)))))
