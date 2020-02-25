package jces1209.vu.page

interface Commenting {

    fun available(): Boolean
    fun openEditor()
    fun typeIn(comment: String)
    fun mentionAnyone()
    fun saveComment()
    fun waitForTheNewComment()
}
