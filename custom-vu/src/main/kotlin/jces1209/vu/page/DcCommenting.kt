package jces1209.vu.page

import jces1209.vu.wait
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable
import org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated

class DcCommenting(
    private val driver: WebDriver
) : Commenting {

    override fun openEditor() {
        driver
            .wait(elementToBeClickable(By.id("footer-comment-button")))
            .click()
        InlineCommentForm(driver).waitForButton()
    }

    override fun typeIn(comment: String) {
        InlineCommentForm(driver).enterCommentText(comment)
    }

    override fun saveComment() {
        InlineCommentForm(driver).submit()
    }

    override fun waitForTheNewComment() {
        driver.wait(visibilityOfElementLocated(By.cssSelector(".activity-comment.focused")))
    }
}
