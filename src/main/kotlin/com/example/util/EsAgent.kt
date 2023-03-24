package com.example.util

import com.example.model.City
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
            esService.index(indexName, City(it))
        }
    }

    fun prepareQuery(template: String) = QueryExecutor(template)

    inner class QueryExecutor(private val queryTemplate: String) {
        fun execute(value: String): Pair<String, String> {
            val query = queryTemplate.replace("{{value}}", value)
            var result = esService.suggestQuery(indexName, query, City::class) { it.name }.joinToString(", ")
            if (result.isBlank()) {
                result = "No results"
            }
            return value to result
        }
    }
}
