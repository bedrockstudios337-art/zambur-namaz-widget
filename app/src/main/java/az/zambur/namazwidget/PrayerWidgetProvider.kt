package az.zambur.namazwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.DateComponents
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.Prayer
import com.batoulapps.adhan.PrayerTimes
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class PrayerWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { updateWidget(context, manager, it) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_TIME_CHANGED ||
            intent.action == Intent.ACTION_TIMEZONE_CHANGED ||
            intent.action == Intent.ACTION_DATE_CHANGED) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, PrayerWidgetProvider::class.java))
            onUpdate(context, manager, ids)
        }
    }

    companion object {
        private val viewIds = listOf(R.id.fajr, R.id.sunrise, R.id.dhuhr, R.id.asr, R.id.maghrib, R.id.isha)

        fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_prayer)
            try {
                val now = Date()
                val calendar = Calendar.getInstance()
                val coordinates = Coordinates(40.4093, 49.8671)
                val params = CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
                params.madhab = Madhab.SHAFI
                val today = PrayerTimes(coordinates, DateComponents.from(calendar), params)
                val todayTimes = listOf(today.fajr, today.sunrise, today.dhuhr, today.asr, today.maghrib, today.isha)
                val formatter = SimpleDateFormat("HH:mm", Locale("az", "AZ")).apply { timeZone = TimeZone.getDefault() }
                viewIds.forEachIndexed { i, id -> views.setTextViewText(id, formatter.format(todayTimes[i])) }

                val nextPrayer = listOf(
                    "Sübh" to today.fajr, "Zöhr" to today.dhuhr, "Əsr" to today.asr,
                    "Məğrib" to today.maghrib, "İşa" to today.isha
                ).firstOrNull { it.second.after(now) } ?: run {
                    val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                    val tomorrowTimes = PrayerTimes(coordinates, DateComponents.from(tomorrow), params)
                    "Sübh" to tomorrowTimes.fajr
                }

                val remaining = (nextPrayer.second.time - now.time).coerceAtLeast(0L)
                val h = remaining / 3_600_000
                val m = (remaining % 3_600_000) / 60_000
                views.setTextViewText(R.id.next_prayer, "Növbəti namaz: ${nextPrayer.first}")
                views.setTextViewText(R.id.countdown, "Qalan vaxt: %02d saat %02d dəq.".format(h, m))
                views.setTextViewText(R.id.date, SimpleDateFormat("dd MMMM yyyy", Locale("az", "AZ")).format(now))
            } catch (e: Exception) {
                views.setTextViewText(R.id.next_prayer, "Namaz vaxtları hesablanmadı")
                views.setTextViewText(R.id.countdown, "Zəhmət olmasa, yenidən yoxlayın")
            }
            manager.updateAppWidget(widgetId, views)
        }
    }
}
