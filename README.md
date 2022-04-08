# sisyphus

"... one must imagine sisyphus happy" - Albert Camus

Sisyphus was the O.G Scheduler, he doggedly rolled a rock up the mountain (only to have it roll back down) 
and he repeated that, everyday. 
He wasn't with limitations, he could do only one task.
That was until he discovered clojure, drunk on the power of clojure, **sisyphus can schedule
multiple tasks and will do them happily for you**, since anything is more fun than rolling a rock up the mountain,
that's how one ~~must imagine~~ knows sisyphus is happy.

[![Clojars Project](https://img.shields.io/clojars/v/org.clojars.abhinav/sisyphus.svg)](https://clojars.org/org.clojars.abhinav/sisyphus)
## Usage

There are three functions that you will use the most. 
- `add-task!`
- `run-tasks!`
- `stop-tasks!`

### add-task!
Tasks are 0-arity clojure functions. i.e. they should take no arguments.

add-task's args
`[task schedule]`
`[name task schedule]`

schedule is a vector containing the gap at which tasks will be scheduled.
```clojure
(ns user.core 
  (:require [sisyphus.core :as sisy]))

(defn my-task1 [] 
  (println "this is a task"))

(add-task! "task1" my-task1 [:every 1 :day 5 :hours 3 :minutes 1 :second 
                             :starting-at 10 30])
=> Task task1 added successfully
; -----------snip--------------

;; optionally you can omit the task name, and the name of the function will be used.
(add-task! my-task1 [:every 1 :hour])
=> Task user.core/my-task1 added successfully
;-----------------snip-----------------------
```

Note: you can dynamically add tasks while sisyphus is running.
#### schedule vector

The first element to the vector is `:every` 
followed by an integer and a keyword denoting a time duration like `:minute`. This is the bare minimum.
The valid durations that can be passed are 

```clojure
:days :day
:hours :hour
:minutes :minute
:seconds :second
```

Optionally you can specify a time that you want the task to run for the first time, using `:starting-at`
like 
`[:starting-at 10 30]` a 24 hour format is used. 

## run-tasks!
This will start sisyphus and run the tasks in another thread, so you can keep doing your work.
So you can dynamically add or remove tasks 
## stop-tasks!
This will gracefulyl stop sisyphus. All tasks currently running will finish running. 

## logging 

Logs can be printed or written to a `sisyphus-log.edn` file.
`(print-log!)` will print to console
for e.g
```clojure
(print-log!)
SUCCESS writefileSleep 2021-10-24T21:09:48 2021-10-24T21:09:59  
SUCCESS writefile2seconds 2021-10-24T21:09:48 2021-10-24T21:09:48  
SUCCESS writefile2seconds 2021-10-24T21:09:54 2021-10-24T21:09:54  
```

`(write-log!)` will write to the edn file.
