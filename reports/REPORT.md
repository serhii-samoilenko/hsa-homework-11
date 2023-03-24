# Elasticsearch fuzzy autocomplete demo

This demo shows how to use Elasticsearch to implement a fuzzy autocomplete feature.
In order to perform a fuzzy autocomplete search with fuzziness = 3, we'll use the n-gram tokenizer 
with a minimum and maximin n-gram length of 3

## Preparing the solution

### Creating index with mappings

```json
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
```

### Inserting various city names as data:

`Io`, `Rio`, `Rome`, `Paris`, `London`, `Toronto`, `New York City`

`Tokyo`, `Rio de Janeiro`, `Los Angeles`, `Berlin`, `Istanbul`, `Singapore`, `Shanghai`

`Amsterdam`, `Hong Kong`, `Barcelona`, `Copenhagen`, `Manchester`, `Philadelphia`, `Wellington`

`Kathmandu`, `Birmingham`, `Melbourne`, `Minneapolis`

### The query used to perform the fuzzy autocomplete search:

```json
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
```

The query is composed of two parts:
1. A query which uses tri-grams to perform a fuzzy match with the name field. 
   It uses a minimum_should_match of 60%, to provide Fuzziness (Distance) of 3.
2. A suggest query with a completion suggester. The completion suggester is used to suggest the most relevant
   results based on the input value. The fuzziness parameter is set to 2, which means that the suggester will
   return results with a maximum of 2 edits.
   It's used to match results too short to br picked by the n-gram query.

#### 2-letter city names search:

| Query | Result       |
|-------|--------------|
| Io    | Io           |
| I     | Io, Istanbul |
| Iol   | Io, Istanbul |
| ab    | No results   |

#### 3-letter city names search:

| Query | Result                    |
|-------|---------------------------|
| Rio   | Rio, Rio de Janeiro, Rome |
| io    | Io                        |
| ri    | Rio, Rio de Janeiro       |
| rid   | Rio, Rio de Janeiro, Rome |
| rat   | Rio, Rio de Janeiro, Rome |
| riot  | Rio, Rio de Janeiro, Rome |
| zio   | No results                |
| abc   | Amsterdam                 |

#### 4-letter city names search:

| Query | Result                    |
|-------|---------------------------|
| Rome  | Rome                      |
| rom   | Rio, Rio de Janeiro, Rome |
| ro    | Rome                      |
| r     | Rio, Rio de Janeiro, Rome |
| rume  | Rome                      |
| rum   | Rio, Rio de Janeiro, Rome |
| ramen | Rome                      |
| roqw  | Rome                      |
| rqwe  | Rome                      |
| gone  | No results                |
| abcd  | No results                |

#### 5-letter city names search:

| Query | Result              |
|-------|---------------------|
| Paris | Paris               |
| pari  | Paris, Philadelphia |
| par   | Paris, Philadelphia |
| pa    | Paris               |
| p     | Paris, Philadelphia |
| poris | Paris, Singapore    |
| poriz | Singapore, Paris    |
| pabcs | Paris               |
| pgone | No results          |
| abcde | No results          |

#### 6-letter city names search:

| Query  | Result            |
|--------|-------------------|
| London | London            |
| landon | London            |
| lando  | London, Kathmandu |
| bandon | London            |
| bando  | London, Kathmandu |
| loabc  | No results        |
| logone | No results        |
| abcdef | No results        |

#### 7-letter city names search:

| Query   | Result     |
|---------|------------|
| Toronto | Toronto    |
| taranta | No results |
| tabcnto | No results |
| togone  | Toronto    |
| abcdefg | No results |

#### 8-letter city names search:

| Query    | Result     |
|----------|------------|
| Shanghai | Shanghai   |
| shonghoi | Shanghai   |
| shonghoy | No results |
| abcdefgh | No results |

### Web UI to test the solution manually

The demo application also provides a web UI to test the solution manually.
The UI is available at [http://localhost:8080](http://localhost:8080)

