from telegram import Update
from telegram.ext import Application, CommandHandler, ContextTypes, JobQueue
import asyncio


async def start(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    await update.message.reply_text("Привет! Я буду читать текст из файла и отправлять его каждые 20 секунд.")


def read_text_from_file(file_path: str) -> str:
    try:
        with open(file_path, "r", encoding="utf-8") as file:
            return file.read()
    except FileNotFoundError:
        return "Файл не найден. Проверьте путь к файлу."


async def send_periodic_messages(context: ContextTypes.DEFAULT_TYPE) -> None:
    chat_id = context.job.chat_id
    text = read_text_from_file("text.txt")  # Укажите путь к вашему файлу
    await context.bot.send_message(chat_id=chat_id, text=text)


async def start_periodic(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    chat_id = update.effective_chat.id
    context.job_queue.run_repeating(send_periodic_messages, interval=20, first=0, chat_id=chat_id, name=str(chat_id))
    await update.message.reply_text("Периодические сообщения из файла запущены!")


async def stop_periodic(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    chat_id = update.effective_chat.id

    # Остановить задачу
    job = context.job_queue.get_jobs_by_name(str(chat_id))
    if job:
        job[0].schedule_removal()  # Остановить задачу
        await update.message.reply_text("Периодические сообщения остановлены.")
    else:
        await update.message.reply_text("Периодические сообщения уже не работают.")


def main():
    TOKEN = "тут токен"

    application = Application.builder().token(TOKEN).build()

    if application.job_queue is None:
        application.job_queue = JobQueue()
        application.job_queue.set_application(application)
        application.job_queue.start()

    application.add_handler(CommandHandler("start", start_periodic))
    application.add_handler(CommandHandler("stop", stop_periodic))

    application.run_polling()


if __name__ == "__main__":
    main()
