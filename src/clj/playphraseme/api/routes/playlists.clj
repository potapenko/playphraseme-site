(ns playphraseme.api.routes.playlists
  (:require [playphraseme.api.middleware.cors :refer [cors-mw]]
            [playphraseme.api.queries.playlists :as playlists]
            [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]))

(s/defschema Playlist
  {:title                 s/Str
   :uuid                  s/Str
   (s/optional-key :info) s/Str
   :phrases               [{:text                  s/Str
                            (s/optional-key :info) s/Str}]})

(defn add-new-playlist [playlist-data]
  (let [exists      (playlists/find-one-playlist playlist-data)
        playlist-id (if exists
                      (:id exists)
                      (:id (playlists/insert-playlist! playlist-data)))]
    playlist-id))

(defn save-device-playplist [device-id playlist]
  (let [exists      (playlists/find-one-playlist {:device-id device-id
                                        :title     (:title playlist)})
        playlist-id (if exists
                      (do
                        (playlists/update-playlist! (merge exists (dissoc playlist :id)))
                        (:id exists))
                      (:id (playlists/insert-playlist! (assoc playlist :device-id device-id))))]
    playlist-id))

(defn get-device-playplists [device-id]
  (playlists/find-playlists {:device-id device-id}))

(def playlists-routes
  "Specify routes for Mobile Playlists"
  (context "/api/v1/playlists" []
    :tags ["Playlists"]

    (POST "/"            {:as request}
          :return        s/Str
          :middleware    [cors-mw]
          :body-params   [playlist :- Playlist]
          :summary       "Add "
          (ok (add-new-playlist playlist)))

    (GET "/:id"         [id :as request]
         :tags          ["Playlists"]
         :return        s/Any
         :middleware    [cors-mw]
         :summary       "Return playlist by id"
         (ok
          (playlists/get-playlist-by-id id)))
    (GET "/device/:id"  [id :as request]
         :tags          ["Playlists"]
         :return        s/Any
         :middleware    [cors-mw]
         :summary       "Return device playlists"
         (ok
          (get-device-playplists id)))
    (POST "/device/:id" [id :as request]
         :tags          ["Playlists"]
         :return        s/Any
         :body-params   [playlist :- s/Any]
         :middleware    [cors-mw]
         :summary       "Add device playlists"
         (ok
          (save-device-playplist id playlist)))))

(comment



  )
