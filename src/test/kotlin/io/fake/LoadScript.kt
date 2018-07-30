package io.fake

import org.apache.jmeter.config.Arguments
import org.apache.jmeter.config.gui.ArgumentsPanel
import org.apache.jmeter.control.LoopController
import org.apache.jmeter.control.gui.LoopControlPanel
import org.apache.jmeter.control.gui.TestPlanGui
import org.apache.jmeter.engine.StandardJMeterEngine
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy
import org.apache.jmeter.reporters.ResultCollector
import org.apache.jmeter.reporters.Summariser
import org.apache.jmeter.save.SaveService
import org.apache.jmeter.testelement.TestElement
import org.apache.jmeter.testelement.TestPlan
import org.apache.jmeter.threads.ThreadGroup
import org.apache.jmeter.threads.gui.ThreadGroupGui
import org.apache.jmeter.util.JMeterUtils
import org.apache.jorphan.collections.HashTree
import java.io.File
import java.io.FileOutputStream

fun main(args: Array<String>) {
    val jmeterHome = File("/")
    val slash = "/"

    val jmeterProperties = File("bin/jmeter.properties")

    val jmeter = StandardJMeterEngine()

    //JMeter initialization (properties, log levels, locale, etc)
    JMeterUtils.setJMeterHome(".")
    JMeterUtils.loadJMeterProperties(jmeterProperties.path)
    JMeterUtils.initLogging()// you can comment this line out to see extra log messages of i.e. DEBUG level
    JMeterUtils.initLocale()

    // JMeter Test Plan, basically JOrphan HashTree
    val testPlanTree = HashTree()


    // open blazemeter.com
    val blazemetercomSampler = HTTPSamplerProxy().apply {
        domain = "blazemeter.com"
        port = 80
        path = "/"
        method = "GET"
        name = "Open blazemeter.com"
    }

    // Loop Controller
    val loopController = LoopController().apply {
        loops = 1
        setFirst(true)
        initialize()
    }

    // Thread Group
    val threadGroup = ThreadGroup().apply {
        name = "Example Thread Group"
        numThreads = 1
        rampUp = 1
        setSamplerController(loopController)
    }

    // Test Plan
    val testPlan = TestPlan("Create JMeter Script From Java Code")

    // Construct Test Plan from previously initialized elements
    testPlanTree.add(testPlan)
    val threadGroupHashTree = testPlanTree.add(testPlan, threadGroup)
    threadGroupHashTree.add(blazemetercomSampler)

    // save generated test plan to JMeter's .jmx file format
    SaveService.saveTree(testPlanTree, FileOutputStream("out/example.jmx"))

    //add Summarizer output to get test progress in stdout like:
    // summary =      2 in   1.3s =    1.5/s Avg:   631 Min:   290 Max:   973 Err:     0 (0.00%)
    var summer: Summariser? = null
    val summariserName = JMeterUtils.getPropDefault("summariser.name", "summary")
    if (summariserName.length > 0) {
        summer = Summariser(summariserName)
    }


    // Store execution results into a .jtl file
    val logger = ResultCollector(summer)
    logger.filename = "out/example.jtl"
    testPlanTree.add(testPlanTree.array[0], logger)

    // Run Test Plan
    jmeter.configure(testPlanTree)
    jmeter.run()

    System.out.println("Test completed. See " + jmeterHome + slash + "example.jtl file for results")
    System.out.println("JMeter .jmx script is available at " + jmeterHome + slash + "example.jmx")
    System.exit(0)


}