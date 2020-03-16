package jces1209

import com.atlassian.performance.tools.jiraactions.api.scenario.Scenario
import com.atlassian.performance.tools.virtualusers.api.TemporalRate
import com.atlassian.performance.tools.virtualusers.api.VirtualUserLoad
import com.atlassian.performance.tools.virtualusers.api.browsers.Browser
import com.atlassian.performance.tools.virtualusers.api.config.VirtualUserBehavior
import jces1209.vu.EagerChromeBrowser
import java.time.Duration

class SlowAndMeaningful private constructor(
    private val browser: Class<out Browser>
) : BenchmarkQuality {

    constructor() : this(
        EagerChromeBrowser::class.java
    )

    override fun provide(): VirtualUsersSource = AwsVus()

    override fun behave(scenario: Class<out Scenario>): VirtualUserBehavior = VirtualUserBehavior.Builder(scenario)
        .browser(browser)
        .load(
            VirtualUserLoad.Builder()
                .virtualUsers(72)
                .flat(Duration.ofMinutes(20))
                .maxOverallLoad(TemporalRate(15.0, Duration.ofSeconds(1)))
                .build()
        )
        .skipSetup(true)
        .seed(12345L)
        .build()
}
