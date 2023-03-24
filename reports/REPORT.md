# Elasticsearch fuzzy autocomplete demo

This demo shows how to use Elasticsearch to implement a fuzzy autocomplete feature.
In order to perform a fuzzy autocomplete search with fuzziness = 3, we'll use the n-gram tokenizer 
with a minimum and maximin n-gram length of 3 and will use the query string query with the minimum_should_match
parameter set to 60%. This will cover typo cases for long words.

## Preparing the solution

### Creating index with mappings

```json
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
```

### Inserting various city names as data:

`Io`, `Rio`, `Rome`, `Paris`, `London`, `Toronto`, `Shanghai`

`Manchester`, `Minneapolis`, `Philadelphia`, `New York City`, `Tokyo`, `Rio de Janeiro`, `Los Angeles`

`Berlin`, `Istanbul`, `Singapore`, `Amsterdam`, `Hong Kong`, `Barcelona`, `Copenhagen`

`Wellington`, `Kathmandu`, `Birmingham`, `Melbourne`, `Sydney`, `Dublin`, `Brisbane`

`Perth`, `Adelaide`, `Auckland`, `Cape Town`, `Johannesburg`, `Cairo`, `Beijing`

`Seoul`, `Mexico City`, `Santiago`, `Buenos Aires`, `Sao Paulo`, `Lima`, `Bogota`

`Caracas`, `Baku`, `Tehran`, `Florence`, `Venice`, `Bologna`, `Turin`

`Palermo`, `Genoa`, `Bari`, `Catania`, `Verona`, `Padua`, `Parma`

`Brescia`, `Modena`, `Reggio Calabria`, `Reggio Emilia`, `Messina`, `Livorno`, `Ravenna`

`Ferrara`, `Trieste`, `Perugia`, `Taranto`, `Cagliari`, `Sassari`, `Siena`

`Forli`, `Foggia`, `Rimini`, `Monza`, `Bergamo`, `Ancona`, `Pescara`

`Lecce`, `Salerno`, `Trento`, `Piacenza`, `Pisa`, `Arezzo`, `Pesaro`

`Novara`, `Vicenza`, `Asti`, `La Spezia`, `Varese`, `Catanzaro`, `Como`

`Savona`, `Lucca`, `Pordenone`

### The query used to perform the fuzzy autocomplete search:

```json
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

| Query | Result                                                                     |
|-------|----------------------------------------------------------------------------|
| Rio   | Rio, Rio de Janeiro, Ravenna, Reggio Calabria, Reggio Emilia, Rimini, Rome |
| io    | Io                                                                         |
| ri    | Rimini, Rio, Rio de Janeiro                                                |
| rid   | Ravenna, Reggio Calabria, Reggio Emilia, Rimini, Rio, Rio de Janeiro, Rome |
| rat   | Ravenna, Reggio Calabria, Reggio Emilia, Rimini, Rio, Rio de Janeiro, Rome |
| riot  | Rimini, Rio, Rio de Janeiro, Rome                                          |
| zio   | No results                                                                 |
| abc   | Adelaide, Amsterdam, Ancona, Arezzo, Asti, Auckland                        |

#### 4-letter city names search:

| Query | Result                                                                     |
|-------|----------------------------------------------------------------------------|
| Rome  | Rome, Ravenna, Reggio Calabria, Reggio Emilia, Rimini                      |
| rom   | Ravenna, Reggio Calabria, Reggio Emilia, Rimini, Rio, Rio de Janeiro, Rome |
| ro    | Rome                                                                       |
| r     | Ravenna, Reggio Calabria, Reggio Emilia, Rimini, Rio, Rio de Janeiro, Rome |
| rume  | Ravenna, Reggio Calabria, Reggio Emilia, Rimini, Rome                      |
| rum   | Ravenna, Reggio Calabria, Reggio Emilia, Rimini, Rio, Rio de Janeiro, Rome |
| ramen | Ravenna, Rimini, Rome                                                      |
| roqw  | Rome                                                                       |
| rqwe  | Ravenna, Reggio Calabria, Reggio Emilia, Rome                              |
| gone  | Genoa                                                                      |
| abcd  | Adelaide, Ancona, Auckland                                                 |

#### 5-letter city names search:

| Query | Result                                                                                |
|-------|---------------------------------------------------------------------------------------|
| Paris | Paris, Parma, Pisa                                                                    |
| pari  | Padua, Palermo, Paris, Parma, Perth, Perugia, Philadelphia, Piacenza, Pisa, Pordenone |
| par   | Padua, Palermo, Paris, Parma, Perth, Perugia, Pesaro, Pescara, Philadelphia, Piacenza |
| pa    | Padua, Palermo, Paris, Parma                                                          |
| p     | Padua, Palermo, Paris, Parma, Perth, Perugia, Pesaro, Pescara, Philadelphia, Piacenza |
| poris | Pordenone, Paris, Pisa                                                                |
| poriz | Pordenone, Paris                                                                      |
| pabcs | Paris                                                                                 |
| pgone | No results                                                                            |
| abcde | Adelaide                                                                              |

#### 6-letter city names search:

| Query  | Result     |
|--------|------------|
| London | London     |
| landon | London     |
| landan | London     |
| lando  | London     |
| bandon | No results |
| bando  | No results |
| loabcd | No results |
| logone | No results |

#### 7-letter city names search:

| Query   | Result                   |
|---------|--------------------------|
| Toronto | Toronto, Taranto, Trento |
| Tironto | Taranto, Toronto, Trento |
| Tironti | Toronto                  |
| taranta | Taranto                  |
| tabcnto | Taranto                  |
| togone  | Toronto                  |
| abcdefg | No results               |

#### 8-letter city names search:

| Query    | Result     |
|----------|------------|
| Shanghai | Shanghai   |
| shanghoi | Shanghai   |
| shonghoi | Shanghai   |
| shonghoy | No results |
| abcdefgh | No results |

#### 9-letter city names search:

| Query      | Result                         |
|------------|--------------------------------|
| Manchester | Manchester, Trieste, Amsterdam |
| Monchester | Manchester, Trieste, Amsterdam |
| Monchuster | Manchester, Amsterdam          |
| Minchistir | No results                     |
| Mihchistor | No results                     |

#### 10-letter city names search:

| Query       | Result      |
|-------------|-------------|
| Minneapolis | Minneapolis |
| Minnwapolis | Minneapolis |
| Middeapolis | Minneapolis |
| Munneupolus | Minneapolis |
| Mynnyypolys | No results  |

#### 11-letter city names search:

| Query        | Result       |
|--------------|--------------|
| Philadelphia | Philadelphia |
| Phyladelphia | Philadelphia |
| Pholodelphia | Philadelphia |
| Pholodelphio | Philadelphia |
| Pholodolphio | No results   |

### Web UI to test the solution manually

The demo application also provides a web UI to test the solution manually.
The UI is available at [http://localhost:8080](http://localhost:8080)

