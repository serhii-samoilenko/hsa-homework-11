package com.example

import com.example.util.Helper
import com.example.util.Report
import javax.enterprise.context.ApplicationScoped

/**
 * TODO
 */
fun runDemo(helper: Helper) = with(helper) {
    val r = Report("REPORT.md")
    r.h1("Elasticsearch fuzzy autocomplete demo")
    r.writeToFile()
}

@ApplicationScoped
class Initializer(private val helper: Helper) {
    fun init() {
        runDemo(helper)
    }
}
