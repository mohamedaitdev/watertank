package com.watertank.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.watertank.app.data.model.BottleStatus
import com.watertank.app.data.model.ChlorineBottle
import com.watertank.app.data.model.Tank
import com.watertank.app.data.model.TankReading
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BottleStatusConverter {
    @TypeConverter fun toName(s: BottleStatus?): String? = s?.name
    @TypeConverter fun fromName(s: String?): BottleStatus? = s?.let { runCatching { BottleStatus.valueOf(it) }.getOrNull() }
}

@Database(
    entities = [Tank::class, TankReading::class, ChlorineBottle::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(BottleStatusConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun tankDao(): TankDao
    abstract fun readingDao(): ReadingDao
    abstract fun chlorineDao(): ChlorineDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "watertank.db"
            ).addCallback(object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(Dispatchers.IO).launch { seed(context) }
                }
            }).build().also { INSTANCE = it }
        }

        private suspend fun seed(context: Context) {
            val db = get(context)
            if (db.tankDao().getFirst() == null) {
                db.tankDao().insert(Tank(name = "Main Reservoir"))
            }
            if (db.chlorineDao().count() == 0) {
                listOf(
                    ChlorineBottle(label = "Bottle A", status = BottleStatus.WORKING),
                    ChlorineBottle(label = "Bottle B", status = BottleStatus.STANDBY),
                    ChlorineBottle(label = "Bottle C", status = BottleStatus.EMPTY),
                    ChlorineBottle(label = "Bottle D", status = BottleStatus.NOT_WORKING)
                ).forEach { db.chlorineDao().insert(it) }
            }
        }
    }
}
