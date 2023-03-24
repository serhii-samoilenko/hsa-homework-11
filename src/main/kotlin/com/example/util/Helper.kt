package com.example.util

import com.example.service.EsService
import io.quarkus.runtime.Startup
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
@Startup
class Helper(val esService: EsService)
