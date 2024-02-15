const val ANSWERS_NUMBER = 4
const val CORRECT_ANSWERS_COUNTER = 3

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

fun Questions.asConsoleString(): String {
    val variants = this.variants.mapIndexed { index, word: Word ->
        "${index + 1} - ${word.translate}"
    }.joinToString(separator = "\n")
    return this.correctAnswer.original + "\n" + variants + "\n0 - Меню"
}

fun main() {
    val trainer = LearnWordsTrainer()

    while (true) {
        println("Меню: 1 – Учить слова, 2 – Статистика, 0 – Выход")
        println("Введите номер меню:")
        when (readln().toIntOrNull()) {
            1 -> {
                while (true) {
                    val question = trainer.getNextQuestion()
                    if (question == null) {
                        println("Вы выучили все слова")
                        break
                    } else {
                        println(question.asConsoleString())

                        val userAnswerInput = readln().toIntOrNull()
                        if (userAnswerInput == 0) break

                        if (trainer.checkAnswer(userAnswerInput?.minus(1))) {
                            println("Правильно!\n")
                        } else {
                            println("Неправильно - слово ${question.correctAnswer.translate}\n")
                        }
                    }
                }
            }

            2 -> {
                val statistics = trainer.getStatistics()
                println(
                    "Выучено ${statistics.learnedWords} из ${statistics.wordCount} слов | " +
                            "${statistics.percentageOfLearnedWords}%"
                )
            }

            0 -> break
            else -> println("Такой команды нет")
        }
    }
}

fun List<Word>.filterLearnedWords(): List<Word> {
    return filter {
        it.correctAnswersCount >= CORRECT_ANSWERS_COUNTER
    }
}
