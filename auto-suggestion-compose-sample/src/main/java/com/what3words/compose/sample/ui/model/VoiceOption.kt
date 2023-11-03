package com.what3words.compose.sample.ui.model

import com.what3words.components.models.VoiceScreenType

sealed class VoiceOption(val label: String, val type: VoiceScreenType?) {
    object DisableVoice : VoiceOption(label = "disable voice", type = null)
    object VoiceWithInline :
        VoiceOption(label = "enable voice with inline animation", type = VoiceScreenType.Inline)

    object VoiceWithPopUp : VoiceOption(
        label = "enable voice with popup animation",
        type = VoiceScreenType.AnimatedPopup
    )

    object VoiceWithFullScreen :
        VoiceOption(label = "enable voice with fullscreen", type = VoiceScreenType.Fullscreen)

    companion object {
        fun values(): List<VoiceOption> {
            return listOf(DisableVoice, VoiceWithInline, VoiceWithPopUp, VoiceWithFullScreen)
        }
    }
}


