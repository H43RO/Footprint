package com.haerokim.project_footprint.DataClass

import io.realm.RealmObject

open class VisitedPlace: RealmObject() {
    var beaconUUID: String? = null
    var naverPlaceID: String? = null
}