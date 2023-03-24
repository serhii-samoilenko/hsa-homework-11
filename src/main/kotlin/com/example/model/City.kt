package com.example.model

import com.fasterxml.jackson.annotation.JsonProperty

data class City(
    @JsonProperty("name")
    val name: String,
)
