package com.example.service

import com.example.model.City
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class CityService(
    private val esService: EsService,
) {
    private val index = "cities"

    fun index(city: City) {
        esService.index(index, city.id, city)
    }

    fun get(id: String): City {
        return esService.get(index, id, City::class)
    }

    fun suggest(query: String): List<String> {
        return searchByName(query).map { it.name }.distinct()
    }

    fun searchByName(name: String): List<City> {
        return esService.search(index, "name", name, City::class)
    }
}
