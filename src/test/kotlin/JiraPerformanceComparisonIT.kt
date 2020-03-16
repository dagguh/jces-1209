import com.atlassian.performance.tools.concurrency.api.AbruptExecutorService
import com.atlassian.performance.tools.concurrency.api.submitWithLogContext
import com.atlassian.performance.tools.jiraactions.api.scenario.Scenario
import com.atlassian.performance.tools.report.api.FullReport
import com.atlassian.performance.tools.report.api.FullTimeline
import com.atlassian.performance.tools.report.api.WaterfallHighlightReport
import com.atlassian.performance.tools.report.api.result.EdibleResult
import com.atlassian.performance.tools.report.api.result.RawCohortResult
import com.atlassian.performance.tools.virtualusers.api.VirtualUserOptions
import com.atlassian.performance.tools.virtualusers.api.config.VirtualUserBehavior
import com.atlassian.performance.tools.virtualusers.api.config.VirtualUserTarget
import com.atlassian.performance.tools.workspace.api.RootWorkspace
import com.atlassian.performance.tools.workspace.api.TestWorkspace
import jces1209.BenchmarkQuality
import jces1209.SlowAndMeaningful
import jces1209.log.LogConfigurationFactory
import jces1209.vu.JiraCloudScenario
import jces1209.vu.JiraDcScenario
import org.apache.logging.log4j.core.config.ConfigurationFactory
import org.junit.Test
import java.io.File
import java.net.URI
import java.nio.file.Paths
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors.newCachedThreadPool

class JiraPerformanceComparisonIT {

    private val workspace = RootWorkspace(Paths.get("build")).currentTask
    private val quality: BenchmarkQuality = SlowAndMeaningful.Builder().build()

    init {
        ConfigurationFactory.setConfigurationFactory(LogConfigurationFactory(workspace))
    }

    @Test
    fun shouldComparePerformance() {
        val results: List<EdibleResult> = AbruptExecutorService(newCachedThreadPool()).use { pool ->
            listOf(
                benchmark("a.properties", JiraDcScenario::class.java, quality, pool),
                benchmark("b.properties", JiraCloudScenario::class.java, quality, pool)
                // feel free to add more, e.g. benchmark("c.properties", ...
            )
                .map { it.get() }
                .map { it.prepareForJudgement(FullTimeline()) }
        }
        FullReport().dump(results, workspace.isolateTest("Compare"))
        dumpMegaSlowWaterfalls(results)
    }

    private fun benchmark(
        secretsName: String,
        scenario: Class<out Scenario>,
        quality: BenchmarkQuality,
        pool: ExecutorService
    ): CompletableFuture<RawCohortResult> {
        val secretsFile = File("cohort-secrets/").resolve(secretsName)
        val properties = CohortProperties.load(secretsFile)
        return pool.submitWithLogContext(properties.cohort) {
            benchmark(properties, scenario, quality)
        }
    }

    private fun benchmark(
        properties: CohortProperties,
        scenario: Class<out Scenario>,
        quality: BenchmarkQuality
    ): RawCohortResult {
        val options = loadOptions(properties, scenario)
        val cohort = properties.cohort
        val resultsTarget = workspace.directory.resolve("vu-results").resolve(cohort)
        val provisioned = quality
            .provide()
            .obtainVus(resultsTarget, workspace.directory)
        val virtualUsers = provisioned.virtualUsers
        return try {
            virtualUsers.applyLoad(options)
            virtualUsers.gatherResults()
            RawCohortResult.Factory().fullResult(cohort, resultsTarget)
        } catch (e: Exception) {
            virtualUsers.gatherResults()
            RawCohortResult.Factory().failedResult(cohort, resultsTarget, e)
        } finally {
            provisioned.resource.release().get()
        }
    }

    private fun loadOptions(
        properties: CohortProperties,
        scenario: Class<out Scenario>
    ): VirtualUserOptions {
        val target = VirtualUserTarget(
            webApplication = properties.jira,
            userName = properties.userName,
            password = properties.userPassword
        )
        val behavior = quality.behave(scenario)
            .let { VirtualUserBehavior.Builder(it) }
            .avoidLeakingPersonalData(properties.jira)
            .build()
        return VirtualUserOptions(target, behavior)
    }

    private fun VirtualUserBehavior.Builder.avoidLeakingPersonalData(
        uri: URI
    ) = apply {
        if (uri.host.endsWith("atlassian.net")) {
            diagnosticsLimit(0)
        }
    }

    private fun dumpMegaSlowWaterfalls(
        results: List<EdibleResult>
    ) {
        results.forEach { result ->
            val megaSlow = result.actionMetrics.filter { it.duration > Duration.ofMinutes(1) }
            WaterfallHighlightReport().report(
                metrics = megaSlow,
                workspace = workspace
                    .isolateTest("Mega slow")
                    .directory
                    .resolve(result.cohort)
                    .let { TestWorkspace(it) }
            )
        }
    }
}
