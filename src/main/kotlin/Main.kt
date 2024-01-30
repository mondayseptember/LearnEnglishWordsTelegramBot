import java.io.File

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int? = 0,
)

fun main() {
    val dictionary = mutableListOf<Word>()
    val wordsFile: File = File("words.txt")

    for (i in wordsFile.readLines()) {
        val split = i.split("|")
        val word = Word(original = split[0], translate = split[1], correctAnswersCount = split[2].toIntOrNull() ?: 0)
        dictionary.add(word)
    }

    dictionary.forEach {
        println(it)
    }
}
