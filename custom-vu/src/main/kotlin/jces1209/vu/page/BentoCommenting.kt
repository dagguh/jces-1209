package jces1209.vu.page

import jces1209.vu.wait
import org.openqa.selenium.By
import org.openqa.selenium.Keys
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable

class BentoCommenting(
    private val driver: WebDriver
) : Commenting {

    override fun openEditor() {
        clickThePlaceholder()
        waitForEditor()
    }

    private fun clickThePlaceholder() {
        waitForPlaceholder().click()
    }

    private fun waitForPlaceholder(): WebElement = driver.wait(
        elementToBeClickable(By.cssSelector("[placeholder='Add a comment…']"))
    )

    private fun waitForEditor() = findSaveButton()

    private fun findSaveButton() = driver.wait(elementToBeClickable(By.xpath("//*[contains(text(),'Save')]")))

    override fun typeIn(comment: String) {
        driver.wait(elementToBeClickable(By.xpath("//*[contains(text(),'Add a comment…')]")))
        Actions(driver)
            .sendKeys(comment)
            .perform()
    }

    override fun saveComment() {
        findSaveButton().click()
        val lastComment = driver.findElement(By.cssSelector("[data-test-id='issue.activity.comments-list'] :last-child"))
        lastComment
            .findElements(By.xpath("*[contains(text(),'Saving...')]"))
            .firstOrNull()
            ?.let { lastCommentBeingSaved ->
                driver.wait(ExpectedConditions.invisibilityOf(lastCommentBeingSaved))
            }
    }

    override fun waitForTheNewComment() {
        waitForPlaceholder()
    }

    override fun mention() {
        startMentioning()
        waitForPopup()
        selectUserToMention()
    }

    private fun startMentioning() {
        Actions(driver)
            .sendKeys(" @")
            .perform()
    }

    private fun waitForPopup() {
        driver.wait(
            ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-mention-name]"))
        )
    }

    private fun selectUserToMention() {
        Actions(driver)
            .sendKeys(Keys.ARROW_DOWN, Keys.ARROW_DOWN, Keys.ENTER)
            .perform()
    }
}
