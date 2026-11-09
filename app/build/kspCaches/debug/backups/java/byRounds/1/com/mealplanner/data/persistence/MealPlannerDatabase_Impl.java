package com.mealplanner.data.persistence;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MealPlannerDatabase_Impl extends MealPlannerDatabase {
  private volatile MealPlannerDao _mealPlannerDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `weekly_plan_state` (`planId` TEXT NOT NULL, `weekStartContext` TEXT, `lockState` TEXT NOT NULL, `activeLockSnapshotId` TEXT, `staleWarning` TEXT, `slotsJson` TEXT NOT NULL, `lockedRecipeIdsJson` TEXT NOT NULL, `updatedAtEpochMs` INTEGER NOT NULL, PRIMARY KEY(`planId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `lock_snapshot` (`snapshotId` TEXT NOT NULL, `planId` TEXT NOT NULL, `createdAtEpochMs` INTEGER NOT NULL, `selectionsJson` TEXT NOT NULL, `planContextJson` TEXT NOT NULL, `sourceCatalogVersion` TEXT, PRIMARY KEY(`snapshotId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `shopping_list` (`shoppingListId` TEXT NOT NULL, `planId` TEXT NOT NULL, `snapshotId` TEXT, `itemsJson` TEXT NOT NULL, `statusMetadataJson` TEXT, `updatedAtEpochMs` INTEGER NOT NULL, PRIMARY KEY(`shoppingListId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `app_preferences` (`key` TEXT NOT NULL, `valueJson` TEXT NOT NULL, `updatedAtEpochMs` INTEGER NOT NULL, PRIMARY KEY(`key`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '837687f737ce2c925f94edd485e98883')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `weekly_plan_state`");
        db.execSQL("DROP TABLE IF EXISTS `lock_snapshot`");
        db.execSQL("DROP TABLE IF EXISTS `shopping_list`");
        db.execSQL("DROP TABLE IF EXISTS `app_preferences`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsWeeklyPlanState = new HashMap<String, TableInfo.Column>(8);
        _columnsWeeklyPlanState.put("planId", new TableInfo.Column("planId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWeeklyPlanState.put("weekStartContext", new TableInfo.Column("weekStartContext", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWeeklyPlanState.put("lockState", new TableInfo.Column("lockState", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWeeklyPlanState.put("activeLockSnapshotId", new TableInfo.Column("activeLockSnapshotId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWeeklyPlanState.put("staleWarning", new TableInfo.Column("staleWarning", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWeeklyPlanState.put("slotsJson", new TableInfo.Column("slotsJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWeeklyPlanState.put("lockedRecipeIdsJson", new TableInfo.Column("lockedRecipeIdsJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWeeklyPlanState.put("updatedAtEpochMs", new TableInfo.Column("updatedAtEpochMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysWeeklyPlanState = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesWeeklyPlanState = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoWeeklyPlanState = new TableInfo("weekly_plan_state", _columnsWeeklyPlanState, _foreignKeysWeeklyPlanState, _indicesWeeklyPlanState);
        final TableInfo _existingWeeklyPlanState = TableInfo.read(db, "weekly_plan_state");
        if (!_infoWeeklyPlanState.equals(_existingWeeklyPlanState)) {
          return new RoomOpenHelper.ValidationResult(false, "weekly_plan_state(com.mealplanner.data.persistence.WeeklyPlanEntity).\n"
                  + " Expected:\n" + _infoWeeklyPlanState + "\n"
                  + " Found:\n" + _existingWeeklyPlanState);
        }
        final HashMap<String, TableInfo.Column> _columnsLockSnapshot = new HashMap<String, TableInfo.Column>(6);
        _columnsLockSnapshot.put("snapshotId", new TableInfo.Column("snapshotId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLockSnapshot.put("planId", new TableInfo.Column("planId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLockSnapshot.put("createdAtEpochMs", new TableInfo.Column("createdAtEpochMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLockSnapshot.put("selectionsJson", new TableInfo.Column("selectionsJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLockSnapshot.put("planContextJson", new TableInfo.Column("planContextJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLockSnapshot.put("sourceCatalogVersion", new TableInfo.Column("sourceCatalogVersion", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysLockSnapshot = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesLockSnapshot = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoLockSnapshot = new TableInfo("lock_snapshot", _columnsLockSnapshot, _foreignKeysLockSnapshot, _indicesLockSnapshot);
        final TableInfo _existingLockSnapshot = TableInfo.read(db, "lock_snapshot");
        if (!_infoLockSnapshot.equals(_existingLockSnapshot)) {
          return new RoomOpenHelper.ValidationResult(false, "lock_snapshot(com.mealplanner.data.persistence.LockSnapshotEntity).\n"
                  + " Expected:\n" + _infoLockSnapshot + "\n"
                  + " Found:\n" + _existingLockSnapshot);
        }
        final HashMap<String, TableInfo.Column> _columnsShoppingList = new HashMap<String, TableInfo.Column>(6);
        _columnsShoppingList.put("shoppingListId", new TableInfo.Column("shoppingListId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsShoppingList.put("planId", new TableInfo.Column("planId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsShoppingList.put("snapshotId", new TableInfo.Column("snapshotId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsShoppingList.put("itemsJson", new TableInfo.Column("itemsJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsShoppingList.put("statusMetadataJson", new TableInfo.Column("statusMetadataJson", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsShoppingList.put("updatedAtEpochMs", new TableInfo.Column("updatedAtEpochMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysShoppingList = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesShoppingList = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoShoppingList = new TableInfo("shopping_list", _columnsShoppingList, _foreignKeysShoppingList, _indicesShoppingList);
        final TableInfo _existingShoppingList = TableInfo.read(db, "shopping_list");
        if (!_infoShoppingList.equals(_existingShoppingList)) {
          return new RoomOpenHelper.ValidationResult(false, "shopping_list(com.mealplanner.data.persistence.ShoppingListEntity).\n"
                  + " Expected:\n" + _infoShoppingList + "\n"
                  + " Found:\n" + _existingShoppingList);
        }
        final HashMap<String, TableInfo.Column> _columnsAppPreferences = new HashMap<String, TableInfo.Column>(3);
        _columnsAppPreferences.put("key", new TableInfo.Column("key", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppPreferences.put("valueJson", new TableInfo.Column("valueJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppPreferences.put("updatedAtEpochMs", new TableInfo.Column("updatedAtEpochMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAppPreferences = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAppPreferences = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAppPreferences = new TableInfo("app_preferences", _columnsAppPreferences, _foreignKeysAppPreferences, _indicesAppPreferences);
        final TableInfo _existingAppPreferences = TableInfo.read(db, "app_preferences");
        if (!_infoAppPreferences.equals(_existingAppPreferences)) {
          return new RoomOpenHelper.ValidationResult(false, "app_preferences(com.mealplanner.data.persistence.PreferenceEntity).\n"
                  + " Expected:\n" + _infoAppPreferences + "\n"
                  + " Found:\n" + _existingAppPreferences);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "837687f737ce2c925f94edd485e98883", "e6e30e41ce6230c344680181822b36c4");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "weekly_plan_state","lock_snapshot","shopping_list","app_preferences");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `weekly_plan_state`");
      _db.execSQL("DELETE FROM `lock_snapshot`");
      _db.execSQL("DELETE FROM `shopping_list`");
      _db.execSQL("DELETE FROM `app_preferences`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(MealPlannerDao.class, MealPlannerDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public MealPlannerDao dao() {
    if (_mealPlannerDao != null) {
      return _mealPlannerDao;
    } else {
      synchronized(this) {
        if(_mealPlannerDao == null) {
          _mealPlannerDao = new MealPlannerDao_Impl(this);
        }
        return _mealPlannerDao;
      }
    }
  }
}
