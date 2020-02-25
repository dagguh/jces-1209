package jces1209.vu

import com.atlassian.performance.tools.jiraactions.api.SeededRandom
import com.atlassian.performance.tools.jiraactions.api.WebJira
import com.atlassian.performance.tools.jiraactions.api.action.Action
import com.atlassian.performance.tools.jiraactions.api.action.ProjectSummaryAction
import com.atlassian.performance.tools.jiraactions.api.measure.ActionMeter
import com.atlassian.performance.tools.jiraactions.api.memories.UserMemory
import com.atlassian.performance.tools.jiraactions.api.memories.adaptive.AdaptiveIssueKeyMemory
import com.atlassian.performance.tools.jiraactions.api.memories.adaptive.AdaptiveJqlMemory
import com.atlassian.performance.tools.jiraactions.api.memories.adaptive.AdaptiveProjectMemory
import com.atlassian.performance.tools.jiraactions.api.scenario.Scenario
import com.atlassian.performance.tools.jiraactions.api.w3c.JavascriptW3cPerformanceTimeline
import jces1209.vu.action.BrowseCloudBoards
import jces1209.vu.action.BrowseCloudProjects
import jces1209.vu.action.CreateAnIssue
import jces1209.vu.action.LogInWithAtlassianId
import jces1209.vu.action.SearchCloudJql
import jces1209.vu.action.ViewCloudBoard
import jces1209.vu.action.WorkAnIssue
import jces1209.vu.page.CloudIssuePage
import jces1209.vu.page.boards.BoardPage
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import java.util.Collections

class JiraCloudScenario : Scenario {

    override fun getLogInAction(
        jira: WebJira,
        meter: ActionMeter,
        userMemory: UserMemory
    ): Action {
        val user = userMemory
            .recall()
            ?: throw Exception("I cannot recall which user I am")
        return LogInWithAtlassianId(user, jira, meter)
    }

    override fun getActions(
        jira: WebJira,
        seededRandom: SeededRandom,
        meter: ActionMeter
    ): List<Action> {
        val waterfall = JavascriptW3cPerformanceTimeline(jira.driver as JavascriptExecutor)
        val waterfallMeter = meter.withW3cPerformanceTimeline(waterfall)
        val jqlMemory = AdaptiveJqlMemory(seededRandom)
            .also { it.remember(listOf("order by created DESC")) } // work around https://ecosystem.atlassian.net/browse/JPERF-573
        val issueKeyMemory = AdaptiveIssueKeyMemory(seededRandom)
        val projectMemory = AdaptiveProjectMemory(seededRandom)
        val issuePage = CloudIssuePage(jira.driver)
        val createIssue = CreateAnIssue(
            jira = jira,
            meter = meter,
            projectMemory = projectMemory,
            createIssueButton = By.id("createGlobalItem")
        )
        val searchWithJql = SearchCloudJql(
            jira = jira,
            meter = meter,
            jqlMemory = jqlMemory,
            issueKeyMemory = issueKeyMemory
        )
        val browseProjects = BrowseCloudProjects(
            jira = jira,
            meter = meter,
            projectMemory = projectMemory
        )
        val workAnIssue = WorkAnIssue(
            issuePage = issuePage,
            jira = jira,
            meter = waterfallMeter,
            issueKeyMemory = issueKeyMemory,
            random = seededRandom,
            commentProbability = 10.30f,
            mentionProbability = 10.50f
        )
        val projectSummary = ProjectSummaryAction(
            jira = jira,
            meter = meter,
            projectMemory = projectMemory
        )
        val boardPages = SeededMemory<BoardPage>(seededRandom)
        val browseBoards = BrowseCloudBoards(
            jira = jira,
            meter = meter,
            boardsMemory = boardPages
        )
        val viewBoard = ViewCloudBoard(
            jira = jira,
            meter = meter,
            boardMemory = boardPages,
            issueKeyMemory = issueKeyMemory
        )
        val exploreData = listOf(browseProjects, searchWithJql, browseBoards, workAnIssue)
        val spreadOut = mapOf(
            createIssue to 0,
            searchWithJql to 20,
            workAnIssue to 55,
            projectSummary to 5,
            browseProjects to 5,
            browseBoards to 5,
            viewBoard to 30
        )
            .map { (action, proportion) -> Collections.nCopies(proportion, action) }
            .flatten()
            .shuffled(seededRandom.random)
        return exploreData + spreadOut
    }
}
