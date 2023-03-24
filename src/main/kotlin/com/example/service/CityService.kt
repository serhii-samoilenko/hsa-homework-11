package com.example.service

import com.example.model.City
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class CityService(
    private val esService: EsService,
) {
    private val indexName = "demo"
    private val queryTemplate =
        """
        {
          "query": {
            "match": {
              "name": {
                "query": "{{value}}",
                "minimum_should_match": "60%"
              }
            }
          },
          "suggest": {
            "suggest": {
              "prefix": "{{value}}",
              "completion": {
                "field": "name.suggest",
                "fuzzy": {
                  "fuzziness": 2
                },
                "size": 10,
                "skip_duplicates": true
              }
            }
          }
        }
        """.trimIndent()

    fun index(city: City) {
        esService.index(indexName, city)
    }

    fun get(id: String): City {
        return esService.get(indexName, id, City::class)
    }

    fun suggest(prefix: String): List<String> {
        val query = queryTemplate.replace("{{value}}", prefix)
        return esService.suggestQuery(indexName, query, City::class) { it.name }
    }

    fun searchByName(name: String): List<City> {
        return esService.search(indexName, "name", name, City::class)
    }
}
