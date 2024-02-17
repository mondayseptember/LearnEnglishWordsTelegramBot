import java.io.File

data class Statistics(
    val learnedWords: Int,
    val wordCount: Int,
    val percentageOfLearnedWords: Int,
)

data class Questions(
    val variants: List<Word>,
    val correctAnswer: Word,
)

class LearnWordsTrainer(
    private val wordsFile: File = File("words.txt")
) {
    private var question: Questions? = null
    private val dictionary = loadDictionary()

    fun getStatistics(): Statistics {
        val learnedWords = dictionary.filterLearnedWords().size
        val wordsNumber = dictionary.size
        val percentageOfLearnedWords = learnedWords * 100 / wordsNumber
        return Statistics(learnedWords, wordsNumber, percentageOfLearnedWords)
    }

    fun getNextQuestion(): Questions? {
        val unLearnedWordsList: List<Word> = dictionary.filter {
            it.correctAnswersCount < CORRECT_ANSWERS_COUNTER
        }
        if (unLearnedWordsList.isEmpty()) return null
        var shuffledWords = unLearnedWordsList.shuffled().take(ANSWERS_NUMBER)
        val learningWord = shuffledWords.random()
        if (shuffledWords.size < ANSWERS_NUMBER) {
            shuffledWords += dictionary.filterLearnedWords().shuffled()
                .take(ANSWERS_NUMBER - shuffledWords.size)
        }
        question = Questions(
            variants = shuffledWords,
            correctAnswer = learningWord,
        )
        return question
    }

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        return question?.let {
            val correctWordNumber = it.variants.indexOf(it.correctAnswer)
            if (userAnswerIndex == correctWordNumber) {
                it.correctAnswer.correctAnswersCount++
                saveDictionary(dictionary)
                true
            } else {
                false
            }
        } ?: false
    }

    private fun loadDictionary(): List<Word> {
        val dictionary = mutableListOf<Word>()
        for (i in wordsFile.readLines()) {
            val split = i.split("|")
            val word =
                Word(original = split[0], translate = split[1], correctAnswersCount = split[2].toIntOrNull() ?: 0)
            dictionary.add(word)
        }
        return dictionary
    }

    private fun saveDictionary(dictionary: List<Word>) {
        wordsFile.writeText("")
        dictionary.forEach {
            wordsFile.appendText("${it.original}|${it.translate}|${it.correctAnswersCount}\n")
        }
    }
}
