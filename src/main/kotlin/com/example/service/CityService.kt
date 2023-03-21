package com.example.service

import com.example.model.City
import com.example.model.SearchResponse
import io.vertx.core.json.JsonObject
import org.apache.http.util.EntityUtils
import org.elasticsearch.client.Request
import org.elasticsearch.client.Response
import org.elasticsearch.client.RestClient
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class CityService(
    private val restClient: RestClient,
) {

    fun index(city: City) {
        val request = Request("PUT", "/cities/_doc/${city.id}")
        request.setJsonEntity(JsonObject.mapFrom(city).toString())
        restClient.performRequest(request)
    }

    operator fun get(id: String): City {
        val request = Request("GET", "/cities/_doc/$id")
        val response: Response = restClient.performRequest(request)
        val responseBody: String = EntityUtils.toString(response.entity)
        val json = JsonObject(responseBody)
        return json.getJsonObject("_source").mapTo(City::class.java)
    }

    fun suggest(query: String): List<String> {
        return searchByName(query).map { it.name }.distinct()
    }

    fun searchByName(name: String): List<City> {
        val term = "name"
        val request = Request("GET", "/cities/_search")
        val query = """
        {
            "query": {
                "match": {
                    "$term": "$name"
                }
            }
        }
        """.trimIndent()
        request.setJsonEntity(query)
        val response: Response = restClient.performRequest(request)
        val responseBody: String = EntityUtils.toString(response.entity)
        val searchResponse = JsonObject(responseBody).mapTo(SearchResponse::class.java)
        return searchResponse.hits.hits
            .map { it._source }
            .map { City(it.id, it.name) }
    }
}
