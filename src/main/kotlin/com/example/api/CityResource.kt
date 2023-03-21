package com.example.api

import com.example.model.City
import com.example.service.CityService
import javax.enterprise.context.ApplicationScoped
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType.APPLICATION_JSON
import javax.ws.rs.core.Response

@ApplicationScoped
@Path("/city")
class CityResource(private val cityService: CityService) {

    @GET
    @Path("/{id}")
    @Produces(APPLICATION_JSON)
    fun getCity(@PathParam("id") id: String): Response {
        val entity = cityService.get(id)
        return Response.ok(entity).build()
    }

    @GET
    @Path("/suggest/{query}")
    @Produces(APPLICATION_JSON)
    fun suggestCities(@PathParam("query") query: String): Response {
        val entities = cityService.suggest(query)
        return Response.ok(entities).build()
    }

    @GET
    @Path("/search/{name}")
    @Produces(APPLICATION_JSON)
    fun getCities(@PathParam("name") name: String): Response {
        val entities = cityService.searchByName(name)
        return Response.ok(entities).build()
    }

    @POST
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    fun createCity(city: City): Response {
        cityService.index(city)
        return Response.noContent().build()
    }
}
