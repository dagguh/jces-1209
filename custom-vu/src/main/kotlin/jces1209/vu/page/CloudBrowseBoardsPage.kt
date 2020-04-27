package jces1209.vu.page

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import java.time.Duration

class CloudBrowseBoardsPage(
    private val driver: WebDriver
) {
    private val tableLocator = By.cssSelector(
        "[data-test-id='global-pages.directories.directory-base.content.table.container']"
    )

    private val falliblePage = FalliblePage.Builder(
        expectedContent = ExpectedConditions.visibilityOfElementLocated(tableLocator),
        webDriver = driver
    )
        .cloudErrors()
        .timeout(Duration.ofSeconds(25))
        .build()

    fun waitForBoards(): CloudBoardList {
        falliblePage.waitForPageToLoad()
        val tableElement = driver.findElement(tableLocator)
        return CloudBoardList(tableElement, driver)
    }
}
