package com.example

import io.quarkus.runtime.Quarkus
import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain
import javax.inject.Inject

@QuarkusMain
class Main : QuarkusApplication {

    @Inject
    private lateinit var demo: Demo

    override fun run(vararg args: String): Int {
        println(" --- Initializing Elasticsearch with mappings and data --- ")
        demo.init()
        println(" --- Now you can use the Rest API ---")
        Quarkus.waitForExit()
        return 0
    }
}

object JavaMain {
    @JvmStatic
    fun main(vararg args: String) {
        Quarkus.run(Main::class.java, *args)
    }
}
