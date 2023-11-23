package com.what3words.samples.multiple.test.utils

import com.what3words.javawrapper.response.Coordinates

//internal fun Coordinates.generateUniqueId(): Long {
//    if (lat < -90 || lat > 90) {
//        throw IllegalArgumentException("Invalid latitude value: must be between -90 and 90")
//    }
//    if (lng < -180 || lng > 180) {
//        throw IllegalArgumentException("Invalid longitude value: must be between -180 and 180")
//    }
//    val latBits = (lat * 1e6).toLong() shl 32
//    val lngBits = (lng * 1e6).toLong() and 0xffffffff
//    return (latBits or lngBits)
//}