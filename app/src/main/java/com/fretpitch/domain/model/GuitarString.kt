package com.fretpitch.domain.model

enum class GuitarString(val number: Int, val openNoteMidi: Int) {
    STRING_1(1, 64),
    STRING_2(2, 59),
    STRING_3(3, 55),
    STRING_4(4, 50),
    STRING_5(5, 45),
    STRING_6(6, 40);

    companion object {
        fun all(): List<GuitarString> = entries.toList()
        fun fromNumber(number: Int): GuitarString? = entries.find { it.number == number }
    }
}
