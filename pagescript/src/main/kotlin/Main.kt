import kotlinx.browser.document

fun main() {
    document.apply {
        ondrop = {
            it.preventDefault()
        }
        ondragenter = {
            it.preventDefault()
        }
        ondragover = {
            it.preventDefault()
        }
        ondragleave = {
            it.preventDefault()
        }
    }
}
