package jces1209.vu.page

interface Commenting {

    fun openEditor()
    fun typeIn(comment: String)
    fun saveComment()
    fun waitForTheNewComment()
    fun mention()
}
