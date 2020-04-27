package jces1209.vu.action

import com.atlassian.performance.tools.jiraactions.api.SEARCH_WITH_JQL
import com.atlassian.performance.tools.jiraactions.api.WebJira
import com.atlassian.performance.tools.jiraactions.api.action.Action
import com.atlassian.performance.tools.jiraactions.api.measure.ActionMeter
import com.atlassian.performance.tools.jiraactions.api.memories.Memory
import jces1209.vu.page.CloudIssueNavigator
import java.net.URI

class SearchCloudFilter(
    private val jira: WebJira,
    private val meter: ActionMeter,
    private val filters: Memory<URI>
) : Action {

    private val brokenFilters = mutableSetOf<URI>()

    override fun run() {
        val filter = filters.recall()!!
        meter.measure(
            key = SEARCH_WITH_JQL,
            action = {
                jira.navigateTo(filter.toString())
                CloudIssueNavigator(jira.driver).waitForNavigator()
            }
        )
    }
}
