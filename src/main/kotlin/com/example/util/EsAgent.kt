package com.example.util

import com.example.service.EsService
import com.fasterxml.jackson.annotation.JsonProperty

class EsAgent(
    private val esService: EsService,
    private val indexName: String,
    private val report: Report,
) {
    fun deleteIndex() {
        esService.tryDeleteIndex(indexName)
    }

    fun createIndex(mappings: String) {
        report.json(mappings)
        esService.createIndex(indexName, mappings)
    }

    fun insertData(vararg values: String) {
        values.asList().chunked(7).forEach { chunk ->
            report.text(chunk.joinToString(", ") { "`$it`" })
        }
        values.forEach {
            esService.index(indexName, Data(it))
        }
    }

    fun prepareQuery(template: String) = QueryExecutor(template)

    inner class QueryExecutor(private val queryTemplate: String) {
        fun execute(value: String): Pair<String, String> {
            val query = queryTemplate.replace("{{value}}", value)
            var result = esService.query(indexName, query, Data::class).joinToString(", ") { it.name }
            if (result.isBlank()) {
                result = "No results"
            }
            return value to result
        }
    }

    data class Data(
        @JsonProperty("name")
        val name: String,
    )
}
