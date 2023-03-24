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
        with a minimum and maximin n-gram length of 3 and will use the query string query with the minimum_should_match
        parameter set to 60%. This will cover typo cases for long words.
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
            "index": {
              "max_ngram_diff": 2,
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
        "Io",
        "Rio",
        "Rome",
        "Paris",
        "London",
        "Toronto",
        "Shanghai",
        "Manchester",
        "Minneapolis",
        "Philadelphia",
        "New York City", "Tokyo", "Rio de Janeiro", "Los Angeles", "Berlin", "Istanbul", "Singapore", "Amsterdam",
        "Hong Kong", "Barcelona", "Copenhagen", "Wellington", "Kathmandu", "Birmingham", "Melbourne", "Sydney", "Dublin",
        "Brisbane", "Perth", "Adelaide", "Auckland", "Cape Town", "Johannesburg", "Cairo", "Beijing", "Seoul",
        "Mexico City", "Santiago", "Buenos Aires", "Sao Paulo", "Lima", "Bogota", "Caracas", "Baku", "Tehran",
        "Florence", "Venice", "Bologna", "Turin", "Palermo", "Genoa", "Bari", "Catania", "Verona", "Padua",
        "Parma", "Brescia", "Modena", "Reggio Calabria", "Reggio Emilia", "Messina", "Livorno", "Ravenna", "Ferrara",
        "Trieste", "Perugia", "Taranto", "Cagliari", "Sassari", "Siena", "Forli", "Foggia", "Rimini", "Monza",
        "Bergamo", "Ancona", "Pescara", "Lecce", "Salerno", "Trento", "Piacenza", "Pisa", "Arezzo", "Pesaro",
        "Novara", "Vicenza", "Asti", "La Spezia", "Varese", "Catanzaro", "Como", "Savona", "Lucca", "Pordenone",
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
                "minimum_should_match": "7<30% 10<60%"
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
    query(listOf("London", "landon", "landan", "lando", "bandon", "bando", "loabcd", "logone")).also { results.addAll(it) }
    r.table("Query", "Result", results).also { results.clear() }

    r.h4("7-letter city names search:")
    query(listOf("Toronto", "Tironto", "Tironti", "taranta", "tabcnto", "togone", "abcdefg")).also { results.addAll(it) }
    r.table("Query", "Result", results).also { results.clear() }

    r.h4("8-letter city names search:")
    query(listOf("Shanghai", "shanghoi", "shonghoi", "shonghoy", "abcdefgh")).also { results.addAll(it) }
    r.table("Query", "Result", results).also { results.clear() }

    r.h4("9-letter city names search:")
    query(listOf("Manchester", "Monchester", "Monchuster", "Minchistir", "Mihchistor")).also { results.addAll(it) }
    r.table("Query", "Result", results).also { results.clear() }

    r.h4("10-letter city names search:")
    query(listOf("Minneapolis", "Minnwapolis", "Middeapolis", "Munneupolus", "Mynnyypolys")).also { results.addAll(it) }
    r.table("Query", "Result", results).also { results.clear() }

    r.h4("11-letter city names search:")
    query(listOf("Philadelphia", "Phyladelphia", "Pholodelphia", "Pholodelphio", "Pholodolphio")).also { results.addAll(it) }
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
