package com.biometrico.personal.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.biometrico.personal.data.model.ConfiguracionHorario
import com.biometrico.personal.data.model.RegistroAsistencia
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [RegistroAsistencia::class, ConfiguracionHorario::class],
    version = 2,
    exportSchema = false
)
abstract class BiometricoDatabase : RoomDatabase() {

    abstract fun registroDao(): RegistroDao
    abstract fun configuracionDao(): ConfiguracionDao

    companion object {
        @Volatile
        private var INSTANCE: BiometricoDatabase? = null

        fun getDatabase(context: Context): BiometricoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BiometricoDatabase::class.java,
                    "biometrico_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                database.configuracionDao().guardarConfiguracion(
                                    ConfiguracionHorario()
                                )
                            }
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
