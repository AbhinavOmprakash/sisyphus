(ns sisyphus.core)

(defn- get-before
  "Returns the element in coll before key-set.
   Returns 0 if key not in coll."
  [key-set coll]
  (if (some key-set coll)
    (reduce (fn [prev curr]
              (if (key-set curr)
                (reduced prev)
                curr))
            coll)
    nil))

(defn- time->seconds [days hours minutes seconds]
  (+
   (* days 24 60 60)
   (* hours 60 60)
   (* minutes 60)
   seconds))

