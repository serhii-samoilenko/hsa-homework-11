package com.example

import com.example.util.EsAgent
import com.example.util.Helper
import com.example.util.Report
import javax.enterprise.context.ApplicationScoped

/**
 * Elasticsearch fuzzy autocomplete demo
 *
 * This demo shows how to use Elasticsearch to implement a fuzzy autocomplete feature.
 */
fun runDemo(helper: Helper) = with(helper) {
    val r = Report("REPORT.md")
    r.h1("Elasticsearch fuzzy autocomplete demo")
    r.text(
        """
        This demo shows how to use Elasticsearch to implement a fuzzy autocomplete feature.
        In order to perform a fuzzy autocomplete search with fuzziness = 3, we'll use the n-gram tokenizer 
        with a minimum and maximin n-gram length of 3
        """.trimIndent(),
    )
    val agent = EsAgent(esService, "demo", r)
    agent.deleteIndex()

    r.h2("Preparing the solution")
    r.h3("Creating index with mappings")
    agent.createIndex(
        """
        {
          "settings": {
            "analysis": {
              "analyzer": {
                "trigram_analyzer": {
                  "tokenizer": "trigram_tokenizer"
                }
              },
              "tokenizer": {
                "trigram_tokenizer": {
                  "type": "ngram",
                  "min_gram": 3,
                  "max_gram": 3,
                  "token_chars": [
                    "letter"
                  ]
                }
              }
            }
          },
          "mappings": {
            "properties": {
              "name": {
                "type": "text",
                "analyzer": "trigram_analyzer",
                "fields": {
                  "suggest": {
                    "type": "completion"
                  }
                }
              }
            }
          }
        }
        """.trimIndent(),
    )
    r.h3("Inserting various city names as data:")
    agent.insertData(
        "Io", "Rio", "Rome", "Paris", "London", "Toronto",
        "New York City", "Tokyo", "Rio de Janeiro", "Los Angeles", "Berlin", "Istanbul",
        "Singapore", "Shanghai", "Amsterdam", "Hong Kong", "Barcelona", "Copenhagen", "Manchester",
        "Philadelphia", "Wellington", "Kathmandu", "Birmingham", "Melbourne", "Minneapolis",
    )
    Thread.sleep(2000)
    r.h3("The query used to perform the fuzzy autocomplete search:")
    val queryTemplate =
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
    r.json(queryTemplate)
    r.text(
        """
        The query is composed of two parts:
        1. A query which uses tri-grams to perform a fuzzy match with the name field. 
           It uses a minimum_should_match of 60%, to provide Fuzziness (Distance) of 3.
        2. A suggest query with a completion suggester. The completion suggester is used to suggest the most relevant
           results based on the input value. The fuzziness parameter is set to 2, which means that the suggester will
           return results with a maximum of 2 edits.
           It's used to match results too short to br picked by the n-gram query.
        """.trimIndent(),
    )

    val executor = agent.prepareQuery(queryTemplate)
    val results = mutableListOf<Pair<String, String>>()
    val query = { queries: List<String> ->
        val res = mutableListOf<Pair<String, String>>()
        queries.forEach { query ->
            executor.execute(query).also { res.add(it) }
        }
        res
    }

    r.h4("2-letter city names search:")
    query(listOf("Io", "I", "Iol", "ab")).also { results.addAll(it) }
    r.table("Query", "Result", results).also { results.clear() }

    r.h4("3-letter city names search:")
    query(listOf("Rio", "io", "ri", "rid", "rat", "riot", "zio", "abc")).also { results.addAll(it) }
    r.table("Query", "Result", results).also { results.clear() }

    r.h4("4-letter city names search:")
    query(listOf("Rome", "rom", "ro", "r", "rume", "rum", "ramen", "roqw", "rqwe", "gone", "abcd")).also { results.addAll(it) }
    r.table("Query", "Result", results).also { results.clear() }

    r.h4("5-letter city names search:")
    query(listOf("Paris", "pari", "par", "pa", "p", "poris", "poriz", "pabcs", "pgone", "abcde")).also { results.addAll(it) }
    r.table("Query", "Result", results).also { results.clear() }

    r.h4("6-letter city names search:")
    query(listOf("London", "landon", "lando", "bandon", "bando", "loabc", "logone", "abcdef")).also { results.addAll(it) }
    r.table("Query", "Result", results).also { results.clear() }

    r.h4("7-letter city names search:")
    query(listOf("Toronto", "taranta", "tabcnto", "togone", "abcdefg")).also { results.addAll(it) }
    r.table("Query", "Result", results).also { results.clear() }

    r.h4("8-letter city names search:")
    query(listOf("Shanghai", "shonghoi", "shonghoy", "abcdefgh")).also { results.addAll(it) }
    r.table("Query", "Result", results).also { results.clear() }

    r.h3("Web UI to test the solution manually")
    r.text(
        """
        The demo application also provides a web UI to test the solution manually.
        The UI is available at [http://localhost:8080](http://localhost:8080)
        """.trimIndent(),
    )

    r.writeToFile()
}

@ApplicationScoped
class Demo(private val helper: Helper) {
    fun run() {
        runDemo(helper)
    }
}
