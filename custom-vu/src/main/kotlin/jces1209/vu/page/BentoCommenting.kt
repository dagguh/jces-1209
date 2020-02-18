package jces1209.vu.page

import jces1209.vu.wait
import org.openqa.selenium.By
import org.openqa.selenium.Keys
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable
import org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElementLocated
import org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfAllElementsLocatedBy

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

    override fun mentionAnyone() {
        typeIn("@")
        val usersToSkip = 1
        val nameAttribute = "data-mention-name"
        val userName = driver
            .wait(visibilityOfAllElementsLocatedBy(By.cssSelector("[$nameAttribute]")))
            .drop(usersToSkip)
            .first()
            .getAttribute(nameAttribute)
        Actions(driver)
            .sendKeys(Keys.ARROW_DOWN.repeat(usersToSkip))
            .sendKeys(Keys.ENTER)
            .perform()
        val commentSection = By.cssSelector("[data-test-id='issue.activity.comment']")
        driver.wait(textToBePresentInElementLocated(commentSection, "@$userName"))
    }

    override fun saveComment() {
        findSaveButton().click()
    }

    override fun waitForTheNewComment() {
        waitForPlaceholder()
    }
}
