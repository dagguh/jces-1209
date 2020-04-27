package jces1209.vu.page

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.ExpectedConditions.and
import org.openqa.selenium.support.ui.ExpectedConditions.or
import org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated
import java.time.Duration

class CloudIssueNavigator(
    driver: WebDriver
) {
    private val page = FalliblePage.Builder(
            driver,
            or(
                and(
                    or(
                        presenceOfElementLocated(By.cssSelector("ol.issue-list")),
                        presenceOfElementLocated(By.id("issuetable")),
                        presenceOfElementLocated(By.id("issue-content"))
                    ),
                    or(
                        presenceOfElementLocated(By.id("jira-issue-header")),
                        presenceOfElementLocated(By.id("key-val"))
                    ),
                    or(
                        presenceOfElementLocated(By.id("new-issue-body-container")),
                        presenceOfElementLocated(By.className("issue-body-content"))
                    )
                ),
                presenceOfElementLocated(By.className("no-results-hint")) // TODO is it too optimistic like in SearchServerFilter.waitForIssueNavigator ?
            )
        )
        .cloudErrors()
        .timeout(Duration.ofSeconds(30))
        .build()

    fun waitForNavigator() {
        page.waitForPageToLoad()
    }
}
