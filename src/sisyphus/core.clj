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
