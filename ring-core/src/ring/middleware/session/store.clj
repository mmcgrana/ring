(ns ring.middleware.session.store
  "Common session store objects and functions.")

(defprotocol SessionStore
  (read-session [store key]
   "Read a session map from the store. If the key is not found returns an
   empty map")
  (write-session [store key data]
   "Write a session map to the store. Returns the (possibly changed) key under
   which the data was stored. For new sessions a nil key could be passed.")
  (delete-session [store key]
   "Delete a session map from the store. Returns nil if the operation was
   successful"))
