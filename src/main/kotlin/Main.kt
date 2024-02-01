import java.io.File

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

fun main() {
    val dictionary = mutableListOf<Word>()
    val wordsFile: File = File("words.txt")

    for (i in wordsFile.readLines()) {
        val split = i.split("|")
        val word = Word(original = split[0], translate = split[1], correctAnswersCount = split[2].toIntOrNull() ?: 0)
        dictionary.add(word)
    }

    while (true) {
        println("Меню: 1 – Учить слова, 2 – Статистика, 0 – Выход")
        println("Введите номер меню:")
        when (readln().toIntOrNull()) {
            1 -> {
                val answersNumber = 4
                while (true) {
                    val unLearnedWordsList: List<Word> = dictionary.filter {
                        it.correctAnswersCount < 3
                    }

                    if (unLearnedWordsList.isNotEmpty()) {
                        var shuffledWords = unLearnedWordsList.shuffled().take(answersNumber)
                        if (shuffledWords.size < answersNumber) {
                            shuffledWords += dictionary.filterLearnedWords().shuffled()
                                .take(answersNumber - shuffledWords.size)
                        }

                        for (i in shuffledWords.take(1)) {
                            println(i.original)
                        }
                        shuffledWords.forEach {
                            print("${shuffledWords.indexOf(it) + 1} - ${it.translate}, ")
                        }
                        println("0 - выход")

                        if (readln().toIntOrNull() == 0) break

                    } else {
                        println("Вы выучили все слова")
                        break
                    }
                }
            }

            2 -> {
                val learnedWords = dictionary.filterLearnedWords().size
                val wordCount = dictionary.size
                val percentageOfLearnedWords = learnedWords * 100 / wordCount
                println(
                    "Выучено $learnedWords из $wordCount слов | $percentageOfLearnedWords%"
                )
            }

            0 -> break
            else -> println("Такой команды нет")
        }
    }
}

fun MutableList<Word>.filterLearnedWords(): List<Word> {
    return filter {
        it.correctAnswersCount >= 3
    }
}
