package jces1209.vu.page

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.ExpectedConditions.or
import org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated
import com.atlassian.performance.tools.jiraactions.api.page.wait
import org.openqa.selenium.support.ui.ExpectedCondition
import org.openqa.selenium.support.ui.ExpectedConditions
import java.time.Duration

class FalliblePage private constructor(
    private val webDriver: WebDriver,
    private val expectedContent: ExpectedCondition<*>,
    private val potentialErrors: List<By>,
    private val loadTimeout: Duration
) {

    fun waitForPageToLoad() {
        webDriver.wait(
            condition = or(expectedContent, potentialErrors.anyElementVisible()),
            timeout = loadTimeout
        )
        assertNoErrors()
    }

    private fun assertNoErrors() {
        potentialErrors.forEach { locator ->
            webDriver.findElements(locator).firstOrNull()?.let { element ->
                throw FallibleException(element.text)
            }
        }
    }

    class FallibleException(elementText: String) : Exception("Error page detected by \"$elementText\"")

    class Builder(
        private val webDriver: WebDriver,
        private val expectedContent: ExpectedCondition<*>
    ) {
        constructor(
             webDriver: WebDriver,
             expectedContent: List<By>
        ): this (
            webDriver,
            expectedContent.anyElementVisible()
        )

        private var loadTimeout: Duration = Duration.ofSeconds(10)
        private var potentialErrors: List<By> = emptyList()

        fun timeout(timeout: Duration) = apply { this.loadTimeout = timeout }
        fun potentialErrors(potentialErrors: List<By>) = apply { this.potentialErrors = potentialErrors }

        fun cloudErrors() = potentialErrors(
            listOf(
                By.xpath("//*[contains(text(), \"We couldn't connect to that issue\")]"),
                By.xpath("//*[contains(text(), \"429 - Too many requests\")]"),
                By.xpath("//*[contains(text(), \"Something's gone wrong\")]"),
                By.cssSelector(".aui-message.error")
            )
        )

        fun serverErrors() = potentialErrors(
            listOf(
                By.cssSelector("section div.aui-message.error"),
                By.id("errorPageContainer"),
                By.cssSelector("div.form-body div.error")
            )
        )

        fun build(): FalliblePage = FalliblePage(
            webDriver = webDriver,
            expectedContent = expectedContent,
            potentialErrors = potentialErrors,
            loadTimeout = loadTimeout
        )
    }

    private companion object {
        private fun List<By>.anyElementVisible(): ExpectedCondition<Boolean> {
            return or(*this.map { visibilityOfElementLocated(it) }.toTypedArray())
        }
    }
}
