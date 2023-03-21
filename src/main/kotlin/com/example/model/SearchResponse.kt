package com.example.model

data class SearchResponse(
    val took: Int,
    val timed_out: Boolean,
    val _shards: Shards,
    val hits: Hits,
)

data class Shards(
    val total: Int,
    val successful: Int,
    val skipped: Int,
    val failed: Int,
)

data class Hits(
    val total: Total,
    val max_score: Double,
    val hits: List<Hit>,
)

data class Total(
    val value: Int,
    val relation: String,
)

data class Hit(
    val _index: String,
    val _type: String,
    val _id: String,
    val _score: Double,
    val _source: Source,
)

data class Source(
    val id: String,
    val name: String,
)
