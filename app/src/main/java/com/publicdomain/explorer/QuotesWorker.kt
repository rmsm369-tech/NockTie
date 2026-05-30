package com.nyxtesla.talk2u

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlin.random.Random

class QuotesWorker(private val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val quotes = listOf(
            "The unexamined life is not worth living. - Socrates",
            "Stay hungry, stay foolish. - Steve Jobs",
            "We are what we repeatedly do. Excellence, then, is not an act, but a habit. - Aristotle",
            "It does not matter how slowly you go as long as you do not stop. - Confucius",
            "The only way to do great work is to love what you do. - Steve Jobs",
            "In the middle of difficulty lies opportunity. - Albert Einstein",
            "The future belongs to those who believe in the beauty of their dreams. - Eleanor Roosevelt",
            "Do not go where the path may lead, go instead where there is no path and leave a trail. - Ralph Waldo Emerson",
            "Success is not final, failure is not fatal: It is the courage to continue that counts. - Winston Churchill",
            "The only limit to our realization of tomorrow is our doubts of today. - Franklin D. Roosevelt",
            "Act as if what you do makes a difference. It does. - William James",
            "Believe you can and you're halfway there. - Theodore Roosevelt",
            "The secret of getting ahead is getting started. - Mark Twain",
            "It always seems impossible until it's done. - Nelson Mandela",
            "Strive not to be a success, but rather to be of value. - Albert Einstein",
            "The best time to plant a tree was 20 years ago. The second best time is now. - Chinese Proverb",
            "Your vibe attracts your tribe. - Unknown",
            "YOLO, but make it meaningful. - Original",
            "You are your only limit. - Original",
            "Glow up, inside and out. - Original",
            "Don't just exist, live. - Original",
            "Be the main character of your story. - Original",
            "Today is a good day to have a good day. - Original",
            "Dream big, hustle harder. - Original",
            "Be a voice, not an echo. - Original",
            "Make your own magic. - Original",
            "Do it with passion or not at all. - Original",
            "Hustle until your haters ask if you're hiring. - Original",
            "Focus on good energy. - Original",
            "Chase the vision, not the money. - Original",
            "Create the things you wish existed. - Original",
            "Don't be afraid to give up the good to go for the great. - John D. Rockefeller",
            "Life is what happens when you're busy making other plans. - John Lennon",
            "Get busy living or get busy dying. - Stephen King",
            "You only live once, but if you do it right, once is enough. - Mae West",
            "Away, away, from men and towns, To the wild wood and the downs, To the silent wilderness, Where the soul need not repress Its music lest it should not find An echo in another's mind. - P.B. Shelley",
            "Many of life's failures are people who did not realize how close they were to success when they gave up. - Thomas Edison",
            "Life is either a daring adventure or nothing at all. - Helen Keller",
            "If you want to lift yourself up, lift up someone else. - Booker T. Washington",
            "I never dreamed about success. I worked for it. - Estée Lauder",
            "The purpose of our lives is to be happy. - Dalai Lama",
            "If you look at what you have in life, you'll always have more. - Oprah Winfrey",
            "Don't let yesterday take up too much of today. - Will Rogers",
            "Life is not a problem to be solved, but a reality to be experienced. - Soren Kierkegaard",
            "Inspiration does exist, but it must find you working. - Pablo Picasso",
            "To be yourself in a world that is constantly trying to make you something else is the greatest accomplishment. - Ralph Waldo Emerson",
            "Imagination is more important than knowledge. - Albert Einstein",
            "Try not to become a man of success. Rather become a man of value. - Albert Einstein",
            "The mind is everything. What you think you become. - Buddha",
            "Everything you can imagine is real. - Pablo Picasso",
            "Happiness is not something ready-made. It comes from your own actions. - Dalai Lama"
        )

        val quote = quotes.random()

        // Save the chosen quote so the app can display it in the Notes/Quotes section
        try {
            val prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
            prefs.edit().putString("daily_quote", quote).apply()
        } catch (_: Exception) {}

        // Ensure notification channel exists
        val channelId = "daily_quotes"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val channel = NotificationChannel(channelId, "Daily Quotes", NotificationManager.IMPORTANCE_DEFAULT)
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.createNotificationChannel(channel)
            } catch (_: Exception) {}
        }

        // Post the notification
        try {
            val notificationId = Random.nextInt(1000)
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Daily Quote")
                .setContentText(quote)
                .setStyle(NotificationCompat.BigTextStyle().bigText(quote))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (_: Exception) {}

        return Result.success()
    }
}
