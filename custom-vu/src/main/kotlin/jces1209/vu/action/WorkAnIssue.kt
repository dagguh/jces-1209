package jces1209.vu.action

import com.atlassian.performance.tools.jiraactions.api.ADD_COMMENT
import com.atlassian.performance.tools.jiraactions.api.ADD_COMMENT_SUBMIT
import com.atlassian.performance.tools.jiraactions.api.SeededRandom
import com.atlassian.performance.tools.jiraactions.api.VIEW_ISSUE
import com.atlassian.performance.tools.jiraactions.api.WebJira
import com.atlassian.performance.tools.jiraactions.api.action.Action
import com.atlassian.performance.tools.jiraactions.api.measure.ActionMeter
import com.atlassian.performance.tools.jiraactions.api.memories.IssueKeyMemory
import jces1209.vu.page.AbstractIssuePage
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * Works for both Cloud and Data Center.
 */
class WorkAnIssue(
    private val issuePage: AbstractIssuePage,
    private val jira: WebJira,
    private val meter: ActionMeter,
    private val issueKeyMemory: IssueKeyMemory,
    private val random: SeededRandom,
    private val editProbability: Float,
    private val commentProbability: Float
) : Action {
    private val logger: Logger = LogManager.getLogger(this::class.java)

    override fun run() {
        val issueKey = issueKeyMemory.recall()
        if (issueKey == null) {
            logger.debug("I don't recall any issue keys. Maybe next time I will.")
            return
        }
        val loadedIssuePage = read(issueKey)
        if (random.random.nextFloat() < editProbability) {
            edit(loadedIssuePage)
        }
        if (random.random.nextFloat() < commentProbability) {
            comment(loadedIssuePage)
        }
    }

    private fun read(
        issueKey: String
    ): AbstractIssuePage = meter.measure(VIEW_ISSUE) {
        jira.goToIssue(issueKey)
        issuePage.waitForSummary()
    }

    private fun edit(issuePage: AbstractIssuePage) {
        logger.debug("I want to edit the $issuePage")
    }

    private fun comment(issuePage: AbstractIssuePage) {
        val commenting = issuePage.comment()
        meter.measure(ADD_COMMENT) {
            commenting.openEditor()
            commenting.typeIn("abc def")
            meter.measure(ADD_COMMENT_SUBMIT) {
                commenting.saveComment()
                commenting.waitForTheNewComment()
            }
        }
    }
}
