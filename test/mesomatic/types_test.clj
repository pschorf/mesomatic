(ns mesomatic.types-test
  (:use [clojure.test])
  (:require [mesomatic.types :as types])
  (:import [mesomatic.types DockerImage
                            Image
                            MesosInfo]
           [org.apache.mesos Protos$ContainerInfo$MesosInfo
                             Protos$ContainerInfo$Type
                             Protos$Image
                             Protos$Image$Type
                             Protos$Image$Docker]))

(deftest test-mesos-info
  (testing "data->pb"
    (let [name "foo-image"
          mesos-info {:image {:type :image-type-docker
                              :docker {:name name}
                              :cached true}}
          proto (types/->pb :MesosInfo mesos-info)
          container-info-proto (types/->pb :ContainerInfo
                                           {:type :container-type-mesos
                                            :volumes []
                                            :hostname "host"
                                            :mesos mesos-info})]
      (is (= Protos$Image$Type/DOCKER (-> proto .getImage .getType)))
      (is (= name (-> proto .getImage .getDocker .getName)))
      (is (-> proto .getImage .getCached))
      (is (= Protos$ContainerInfo$Type/MESOS (.getType container-info-proto)))
      (is (= name (-> container-info-proto .getMesos .getImage .getDocker .getName)))))

  (testing "pb->data"
    (let [name "foo-image"
          docker-image (-> (Protos$Image$Docker/newBuilder)
                           (.setName name)
                           .build)
          image (-> (Protos$Image/newBuilder)
                    (.setType Protos$Image$Type/DOCKER)
                    (.setDocker docker-image)
                    (.setCached false)
                    .build)
          mesos-info (-> (Protos$ContainerInfo$MesosInfo/newBuilder)
                         (.setImage image)
                         (.build))
          record (types/pb->data mesos-info)]
      (is (= (MesosInfo. (Image. :image-type-docker (DockerImage. name) false))
             record)))))

