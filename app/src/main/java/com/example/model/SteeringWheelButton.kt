package com.example.model

/**
 * Tasti fisici del volante Fiat/Alfa/Lancia intercettati dal Blue&Me (iHidReport 0x4D).
 */
enum class SteeringWheelButton(val label: String, val rawCode: Int) {
    ESC_BACK("ESC / Indietro", 0x01),
    ARROW_UP("Freccia Su", 0x02),
    ARROW_DOWN("Freccia Giù", 0x03),
    ARROW_LEFT("Freccia Sinistra", 0x04),
    ARROW_RIGHT("Freccia Destra", 0x06),
    OK_ENTER("OK / Invio", 0x05),
    PHONE("Telefono / Rubrica", 0x18),
    MENU_SOURCE("Menu / Source", 0x1A),
    VOICE_PTT("Comando Vocale", 0x1D),
    UNKNOWN("Sconosciuto", 0x00);

    companion object {
        fun fromRawCode(code: Int): SteeringWheelButton {
            return entries.firstOrNull { it.rawCode == code } ?: UNKNOWN
        }
    }
}
