package com.haerokim.project_footprint.DataClass

import io.realm.RealmObject

/**  방문했던 장소 이름 캐싱을 위한 Realm Model Object  **/

open class VisitedPlace: RealmObject() {
    var beaconUUID: String? = null
    var naverPlaceID: String? = null
    var placeTitle: String? = null
}