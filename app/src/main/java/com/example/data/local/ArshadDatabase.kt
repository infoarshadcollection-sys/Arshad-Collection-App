package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [
    ProductEntity::class,
    CategoryEntity::class,
    CartItemEntity::class,
    WishlistItemEntity::class,
    OrderEntity::class,
    ReviewEntity::class,
    CouponEntity::class,
    BannerEntity::class,
    NotificationEntity::class,
    AppSettingsEntity::class,
    UserEntity::class
  ],
  version = 2,
  exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ArshadDatabase : RoomDatabase() {
  abstract fun productDao(): ProductDao
  abstract fun categoryDao(): CategoryDao
  abstract fun cartDao(): CartDao
  abstract fun wishlistDao(): WishlistDao
  abstract fun orderDao(): OrderDao
  abstract fun reviewDao(): ReviewDao
  abstract fun couponDao(): CouponDao
  abstract fun bannerDao(): BannerDao
  abstract fun notificationDao(): NotificationDao
  abstract fun settingsDao(): SettingsDao
  abstract fun userDao(): UserDao

  companion object {
    @Volatile
    private var INSTANCE: ArshadDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): ArshadDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          ArshadDatabase::class.java,
          "arshad_collection_db"
        )
          .fallbackToDestructiveMigration()
          .addCallback(object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
              super.onCreate(db)
              scope.launch {
                INSTANCE?.let { database ->
                  DatabaseInitializer.seedDatabase(database)
                }
              }
            }
          })
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
