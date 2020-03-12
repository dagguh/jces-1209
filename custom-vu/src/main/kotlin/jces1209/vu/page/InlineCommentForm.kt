package jces1209.vu.page

import com.atlassian.performance.tools.jiraactions.api.page.IssuePage
import com.atlassian.performance.tools.jiraactions.api.page.JiraErrors
import com.atlassian.performance.tools.jiraactions.api.page.RichTextEditorTextArea
import com.atlassian.performance.tools.jiraactions.api.page.wait
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable
import org.openqa.selenium.support.ui.ExpectedConditions.or
import org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated
import java.time.Duration

class InlineCommentForm(
    private val driver: WebDriver
) {

    private val submitLocator = By.id("issue-comment-add-submit")

    fun waitForButton(): InlineCommentForm {
        val jiraErrors = JiraErrors(driver)
        driver.wait(
            timeout = Duration.ofSeconds(6),
            condition = or(
                presenceOfElementLocated(submitLocator),
                jiraErrors.anyCommonError()
            )
        )
        jiraErrors.assertNoErrors()
        driver.tolerateDirtyFormsOnCurrentPage()
        return this
    }

    fun submit(): IssuePage {
        driver
            .wait(
                timeout = Duration.ofSeconds(3),
                condition = elementToBeClickable(submitLocator)
            )
            .click()
        return IssuePage(driver)
    }

    fun enterCommentText(
        comment: String
    ): InlineCommentForm {
        RichTextEditorTextArea(driver, driver.findElement(By.id("comment")))
            .overwriteIfPresent(comment)
        return this
    }
}

/**
 * Makes sure the next navigation will not pop a dirty form alert even if a filled form was not submitted.
 * Use it just before filling in a form, because your action might fail at any moment.
 */
private fun WebDriver.tolerateDirtyFormsOnCurrentPage() {
    (this as JavascriptExecutor).executeScript("window.onbeforeunload = null")
}
