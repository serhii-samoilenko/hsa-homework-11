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
              "filter": {
                "trigrams_filter": {
                  "type": "ngram",
                  "min_gram": 3,
                  "max_gram": 3
                }
              },
              "analyzer": {
                "trigrams": {
                  "type": "custom",
                  "tokenizer": "standard",
                  "filter": [
                    "lowercase",
                    "trigrams_filter"
                  ]
                }
              }
            }
          },
          "mappings": {
            "properties": {
              "name": {
                "type": "text",
                "analyzer": "trigrams",
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
        "New York City", "Tokyo", "Paris", "London", "Sydney", "Rio de Janeiro", "Los Angeles", "Berlin", "Istanbul",
        "Singapore", "Toronto", "Shanghai", "Amsterdam", "Hong Kong", "Barcelona", "Copenhagen", "Manchester",
        "Philadelphia", "Wellington", "Kathmandu", "Birmingham", "Melbourne", "Minneapolis",
    )
    Thread.sleep(2000)
    r.h2("Testing with various queries")
    r.h3("Simple query")
    val queryTemplate =
        """
        {
          "query": {
            "match": {
              "name": {
                "query": "{{value}}",
                "minimum_should_match": "90%"
              }
            }
          }
        }
        """.trimIndent()
    r.json(queryTemplate)
    val executor = agent.prepareQuery(queryTemplate)
    val results = mutableListOf<Pair<String, String>>()
    executor.execute("singapore").also { results.add(it) }
    executor.execute("singapor").also { results.add(it) }
    executor.execute("singapo").also { results.add(it) }
    executor.execute("singap").also { results.add(it) }
    executor.execute("singa").also { results.add(it) }
    executor.execute("sing").also { results.add(it) }
    executor.execute("sin").also { results.add(it) }
    r.table("Query", "Result", results)
    r.writeToFile()
}

@ApplicationScoped
class Demo(private val helper: Helper) {
    fun init() {
        runDemo(helper)
    }
}
