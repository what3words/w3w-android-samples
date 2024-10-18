package com.what3words.samples.multiple.util

import com.what3words.core.types.domain.W3WSuggestion
import com.what3words.core.types.geometry.km
import com.what3words.javawrapper.response.Suggestion
import com.what3words.javawrapper.response.SuggestionWithCoordinates

/**
 * Mapper from [W3WSuggestion] to [SuggestionWithCoordinates]. Throws an [IllegalArgumentException] if
 * [W3WSuggestion] does not have coordinates
 */
@Throws(IllegalArgumentException::class)
fun W3WSuggestion.toSuggestionWithCoordinates(): SuggestionWithCoordinates {
    val w3wAddress = this.w3wAddress
    val center = w3wAddress.center
        ?: throw IllegalArgumentException("W3WSuggestion does not have coordinates")
    val square = w3wAddress.square
        ?: throw IllegalArgumentException("W3WSuggestion does not have coordinates")

    return SuggestionWithCoordinates(
        Suggestion(
            w3wAddress.words,
            w3wAddress.nearestPlace,
            w3wAddress.country.twoLetterCode,
            this.distanceToFocus?.km()?.toInt() ?: 0,
            this.rank,
            w3wAddress.language.w3wCode
        ),
        center.lat,
        center.lng,
        square.northeast.lat,
        square.northeast.lng,
        square.southwest.lat,
        square.southwest.lng,
    )
}

