import java.io.File

const val ANSWERS_NUMBER = 4
const val CORRECT_ANSWERS_COUNTER = 3

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

    fun saveDictionary(dictionary: MutableList<Word>) {
        dictionary.forEach {
            wordsFile.appendText(it.original + "|" + it.translate + "|" + it.correctAnswersCount + "\n")
        }
    }

    while (true) {
        println("Меню: 1 – Учить слова, 2 – Статистика, 0 – Выход")
        println("Введите номер меню:")
        when (readln().toIntOrNull()) {
            1 -> {
                while (true) {
                    val unLearnedWordsList: List<Word> = dictionary.filter {
                        it.correctAnswersCount < CORRECT_ANSWERS_COUNTER
                    }

                    if (unLearnedWordsList.isEmpty()) {
                        println("Вы выучили все слова")
                        break
                    }
                    var shuffledWords = unLearnedWordsList.shuffled().take(ANSWERS_NUMBER)
                    if (shuffledWords.size < ANSWERS_NUMBER) {
                        shuffledWords += dictionary.filterLearnedWords().shuffled()
                            .take(ANSWERS_NUMBER - shuffledWords.size)
                    }

                    val learningWord = shuffledWords.random()
                    val correctWordNumber = shuffledWords.indexOf(learningWord) + 1
                    println(learningWord.original)

                    shuffledWords.forEachIndexed { index, word ->
                        print("${index + 1} - ${word.translate}, ")
                    }
                    println("0 - Меню")

                    when (readln().toIntOrNull()) {
                        0 -> break
                        correctWordNumber -> {
                            println("Правильно!")
                            learningWord.correctAnswersCount++
                            saveDictionary(dictionary)
                        }

                        else -> println("Неправильно - слово ${shuffledWords[correctWordNumber - 1].translate}")
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
        it.correctAnswersCount >= CORRECT_ANSWERS_COUNTER
    }
}
