package com.example.service

import com.example.model.SearchResponse
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import org.apache.http.util.EntityUtils
import org.elasticsearch.client.Request
import org.elasticsearch.client.Response
import org.elasticsearch.client.RestClient
import javax.enterprise.context.ApplicationScoped
import kotlin.reflect.KClass

@ApplicationScoped
class EsService(
    private val restClient: RestClient,
) {
    fun createIndex(index: String, mapping: String) {
        val request = Request("PUT", "/$index")
        request.setJsonEntity(mapping)
        restClient.performRequest(request)
    }

    fun tryDeleteIndex(indexName: String) {
        val request = Request("DELETE", "/$indexName")
        try {
            restClient.performRequest(request)
        } catch (e: Exception) {
            println("Cannot delete index $indexName")
        }
    }

    fun index(index: String, id: String, entity: Any) {
        val request = Request("PUT", "/$index/_doc/$id")
        request.setJsonEntity(JsonObject.mapFrom(entity).toString())
        restClient.performRequest(request)
    }

    fun index(index: String, entity: Any) {
        val request = Request("POST", "/$index/_doc")
        request.setJsonEntity(JsonObject.mapFrom(entity).toString())
        restClient.performRequest(request)
    }

    fun <T : Any> get(index: String, id: String, klass: KClass<T>): T {
        val request = Request("GET", "/$index/_doc/$id")
        val response: Response = restClient.performRequest(request)
        val responseBody: String = EntityUtils.toString(response.entity)
        val json = JsonObject(responseBody)
        return json.getJsonObject("_source").mapTo(klass.java)
    }

    fun <T : Any> search(index: String, term: String, value: String, klass: KClass<T>): List<T> {
        val request = Request("GET", "/$index/_search")
        val query = """
        {
            "query": {
                "match": {
                    "$term": "$value"
                }
            }
        }
        """.trimIndent()
        request.setJsonEntity(query)
        val response: Response = restClient.performRequest(request)
        val responseBody: String = EntityUtils.toString(response.entity)
        val searchResponse = JsonObject(responseBody).mapTo(SearchResponse::class.java)
        val hits: JsonArray = JsonObject(responseBody)
            .getJsonObject("hits")
            .getJsonArray("hits")
        return hits.map {
            it as JsonObject
            it.getJsonObject("_source").mapTo(klass.java)
        }
    }

    fun <T : Any> suggestQuery(
        index: String,
        query: String,
        resultClass: KClass<T>,
        fieldExtractor: (T) -> String,
    ): List<String> {
        val request = Request("GET", "/$index/_search")
        request.setJsonEntity(query)
        val response: Response = restClient.performRequest(request)
        val responseBody: String = EntityUtils.toString(response.entity)
        println(responseBody)
        println()
        val found: List<String> = JsonObject(responseBody)
            .getJsonObject("hits")
            .getJsonArray("hits")
            .map {
                (it as JsonObject).getJsonObject("_source").mapTo(resultClass.java).let(fieldExtractor)
            }
        val suggestions = JsonObject(responseBody)
            .getJsonObject("suggest")
            .getJsonArray("suggest")
            .flatMap { (it as JsonObject).getJsonArray("options") }
            .map {
                (it as JsonObject).getJsonObject("_source").mapTo(resultClass.java).let(fieldExtractor)
            }
        return (found + suggestions).distinct()
    }
}
