package com.fretpitch.domain.model

enum class GuitarString(val number: Int, val openNoteMidi: Int, val noteName: String) {
    STRING_1(1, 64, "E"),
    STRING_2(2, 59, "B"),
    STRING_3(3, 55, "G"),
    STRING_4(4, 50, "D"),
    STRING_5(5, 45, "A"),
    STRING_6(6, 40, "E");

    companion object {
        fun all(): List<GuitarString> = entries.toList()
        fun fromNumber(number: Int): GuitarString? = entries.find { it.number == number }
    }
}
