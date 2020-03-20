package jces1209.vu.page

import jces1209.vu.wait
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.ExpectedConditions.or
import org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated

class CloudIssuePage(
    private val driver: WebDriver
) : AbstractIssuePage {
    private val bentoSummary = By.cssSelector("[data-test-id='issue.views.issue-base.foundation.summary.heading']")
    private val classicSummary = By.id("key-val")

    override fun waitForSummary(): AbstractIssuePage {
        driver.wait(
            or(
                visibilityOfElementLocated(bentoSummary),
                visibilityOfElementLocated(classicSummary)
            )
        )
        return this
    }

    override fun comment(): Commenting {
        return if (isCommentingClassic()) {
            ClassicCloudCommenting(driver)
        } else {
            BentoCommenting(driver)
        }
    }

    private fun isCommentingClassic(): Boolean = driver
        .findElements(By.id("footer-comment-button"))
        .isNotEmpty()
}
