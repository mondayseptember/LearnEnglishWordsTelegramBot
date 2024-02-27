fun main(args: Array<String>) {
    val telegramBot = TelegramBotService()
    val botToken = args[0]
    var updateId = 0
    val text = "Hello"

    while (true) {
        Thread.sleep(2000)
        val updates: String = telegramBot.getUpdates(botToken, updateId)
        println(updates)

        val updateIdRegex = "\"update_id\":(\\d+)".toRegex()
        val matchResult = updateIdRegex.find(updates)
        val groups = matchResult?.groups
        val updateIdString = groups?.get(1)?.value
        if (updateIdString != null) {
            updateId = updateIdString.toInt() + 1
        }

        val chatIdRegex = "\"chat\":\\{\"id\":(\\d+)".toRegex()
        val matchChatIdResult = chatIdRegex.find(updates)
        val chatId = matchChatIdResult?.groups?.get(1)?.value

        val messageTextRegex = "\"text\":\"(.+?)\"".toRegex()
        val matchTextResult = messageTextRegex.find(updates)
        val userMessage = matchTextResult?.groups?.get(1)?.value

        if (userMessage == text) {
            telegramBot.sendMessage(botToken, chatId, text)
        }
    }
}
