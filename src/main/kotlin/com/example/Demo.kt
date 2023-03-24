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
                    "letter",
                    "digit"
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
                  },
                  "keyword": {
                    "type": "keyword"
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
        "Io", "Rio", "Rome", "London", "Sydney", "Toronto",
        "New York City", "Tokyo", "Paris", "Rio de Janeiro", "Los Angeles", "Berlin", "Istanbul",
        "Singapore", "Shanghai", "Amsterdam", "Hong Kong", "Barcelona", "Copenhagen", "Manchester",
        "Philadelphia", "Wellington", "Kathmandu", "Birmingham", "Melbourne", "Minneapolis",
    )
    Thread.sleep(2000)
    r.h3("The query used to perform the fuzzy autocomplete search:")
    val queryTemplate =
        """
        {
          "query": {
            "bool": {
              "should": [
                {
                  "match": {
                    "name": {
                      "query": "{{value}}",
                      "minimum_should_match": "60%"
                    }
                  }
                },
                {
                  "match": {
                    "name.keyword": {
                      "query": "{{value}}",
                    }
                  }
                }
              ]
            }
          },
          "suggest": {
            "text-suggest": {
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
    val executor = agent.prepareQuery(queryTemplate)
    val results = mutableListOf<Pair<String, String>>()

    r.h4("2-letter city names search:")
    executor.execute("Io").also { results.add(it) }
    executor.execute("I").also { results.add(it) }
    executor.execute("Iol").also { results.add(it) }
    r.table("Query", "Result", results).also { results.clear() }

    r.h4("3-letter city names search:")
    executor.execute("Rio").also { results.add(it) }
    executor.execute("io").also { results.add(it) }
    executor.execute("ri").also { results.add(it) }
    executor.execute("riot").also { results.add(it) }
    executor.execute("wio").also { results.add(it) }
    executor.execute("wao").also { results.add(it) }
    executor.execute("wat").also { results.add(it) }
    r.table("Query", "Result", results).also { results.clear() }

    r.h4("3-letter city names search:")
    executor.execute("Rome").also { results.add(it) }
    r.table("Query", "Result", results).also { results.clear() }

    r.writeToFile()
}

@ApplicationScoped
class Demo(private val helper: Helper) {
    fun init() {
        runDemo(helper)
    }
}
