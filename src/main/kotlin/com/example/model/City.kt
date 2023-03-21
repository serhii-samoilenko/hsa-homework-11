package com.example.model

data class City(
    val id: String = randomString(4),
    val name: String,
) {
    companion object {
        private val alphabet: List<Char> = ('A'..'Z') + ('0'..'9')
        fun randomString(length: Int) = List(length) { alphabet.random() }.joinToString("")
    }
}
