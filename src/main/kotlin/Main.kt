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

    fun MutableList<Word>.filter(): Int = filter {
        (it.correctAnswersCount ?: 0) >= 3
    }.size

    while (true) {
        println("Меню: 1 – Учить слова, 2 – Статистика, 0 – Выход")
        println("Введите номер меню:")
        when (readln().toIntOrNull()) {
            1 -> println("Учить слова")
            2 -> println(
                "Выучено ${dictionary.filter()} из ${dictionary.size} слов | " +
                        "${dictionary.filter() * 100 / dictionary.size}%"
            )
            0 -> break
            else -> println("Такой команды нет")
        }
    }
}
