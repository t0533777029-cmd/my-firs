package com.proporit.app.ui.util

/** Russian plural forms for "день/дня/дней", reused by Home and Settings screens. */
fun dayWord(n: Int): String {
    val mod100 = n % 100
    val mod10 = n % 10
    return when {
        mod100 in 11..14 -> "дней"
        mod10 == 1 -> "день"
        mod10 in 2..4 -> "дня"
        else -> "дней"
    }
}

fun dayLabel(n: Int): String = "$n ${dayWord(n)}"

fun hourWord(n: Int): String {
    val mod100 = n % 100
    val mod10 = n % 10
    return when {
        mod100 in 11..14 -> "часов"
        mod10 == 1 -> "час"
        mod10 in 2..4 -> "часа"
        else -> "часов"
    }
}

fun hourLabel(n: Int): String = "$n ${hourWord(n)}"
