package com.mealplanner.data.persistence;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MealPlannerDao_Impl implements MealPlannerDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<WeeklyPlanEntity> __insertionAdapterOfWeeklyPlanEntity;

  private final EntityInsertionAdapter<LockSnapshotEntity> __insertionAdapterOfLockSnapshotEntity;

  private final EntityInsertionAdapter<ShoppingListEntity> __insertionAdapterOfShoppingListEntity;

  private final EntityInsertionAdapter<PreferenceEntity> __insertionAdapterOfPreferenceEntity;

  public MealPlannerDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfWeeklyPlanEntity = new EntityInsertionAdapter<WeeklyPlanEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `weekly_plan_state` (`planId`,`weekStartContext`,`lockState`,`activeLockSnapshotId`,`staleWarning`,`slotsJson`,`lockedRecipeIdsJson`,`updatedAtEpochMs`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WeeklyPlanEntity entity) {
        statement.bindString(1, entity.getPlanId());
        if (entity.getWeekStartContext() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getWeekStartContext());
        }
        statement.bindString(3, entity.getLockState());
        if (entity.getActiveLockSnapshotId() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getActiveLockSnapshotId());
        }
        if (entity.getStaleWarning() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getStaleWarning());
        }
        statement.bindString(6, entity.getSlotsJson());
        statement.bindString(7, entity.getLockedRecipeIdsJson());
        statement.bindLong(8, entity.getUpdatedAtEpochMs());
      }
    };
    this.__insertionAdapterOfLockSnapshotEntity = new EntityInsertionAdapter<LockSnapshotEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `lock_snapshot` (`snapshotId`,`planId`,`createdAtEpochMs`,`selectionsJson`,`planContextJson`,`sourceCatalogVersion`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LockSnapshotEntity entity) {
        statement.bindString(1, entity.getSnapshotId());
        statement.bindString(2, entity.getPlanId());
        statement.bindLong(3, entity.getCreatedAtEpochMs());
        statement.bindString(4, entity.getSelectionsJson());
        statement.bindString(5, entity.getPlanContextJson());
        if (entity.getSourceCatalogVersion() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getSourceCatalogVersion());
        }
      }
    };
    this.__insertionAdapterOfShoppingListEntity = new EntityInsertionAdapter<ShoppingListEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `shopping_list` (`shoppingListId`,`planId`,`snapshotId`,`itemsJson`,`statusMetadataJson`,`updatedAtEpochMs`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ShoppingListEntity entity) {
        statement.bindString(1, entity.getShoppingListId());
        statement.bindString(2, entity.getPlanId());
        if (entity.getSnapshotId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getSnapshotId());
        }
        statement.bindString(4, entity.getItemsJson());
        if (entity.getStatusMetadataJson() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getStatusMetadataJson());
        }
        statement.bindLong(6, entity.getUpdatedAtEpochMs());
      }
    };
    this.__insertionAdapterOfPreferenceEntity = new EntityInsertionAdapter<PreferenceEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `app_preferences` (`key`,`valueJson`,`updatedAtEpochMs`) VALUES (?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PreferenceEntity entity) {
        statement.bindString(1, entity.getKey());
        statement.bindString(2, entity.getValueJson());
        statement.bindLong(3, entity.getUpdatedAtEpochMs());
      }
    };
  }

  @Override
  public Object upsertPlan(final WeeklyPlanEntity entity,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfWeeklyPlanEntity.insert(entity);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertSnapshot(final LockSnapshotEntity entity,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfLockSnapshotEntity.insert(entity);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertShoppingList(final ShoppingListEntity entity,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfShoppingListEntity.insert(entity);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertPreference(final PreferenceEntity entity,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPreferenceEntity.insert(entity);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getPlan(final String planId,
      final Continuation<? super WeeklyPlanEntity> $completion) {
    final String _sql = "SELECT * FROM weekly_plan_state WHERE planId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, planId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<WeeklyPlanEntity>() {
      @Override
      @Nullable
      public WeeklyPlanEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPlanId = CursorUtil.getColumnIndexOrThrow(_cursor, "planId");
          final int _cursorIndexOfWeekStartContext = CursorUtil.getColumnIndexOrThrow(_cursor, "weekStartContext");
          final int _cursorIndexOfLockState = CursorUtil.getColumnIndexOrThrow(_cursor, "lockState");
          final int _cursorIndexOfActiveLockSnapshotId = CursorUtil.getColumnIndexOrThrow(_cursor, "activeLockSnapshotId");
          final int _cursorIndexOfStaleWarning = CursorUtil.getColumnIndexOrThrow(_cursor, "staleWarning");
          final int _cursorIndexOfSlotsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "slotsJson");
          final int _cursorIndexOfLockedRecipeIdsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "lockedRecipeIdsJson");
          final int _cursorIndexOfUpdatedAtEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAtEpochMs");
          final WeeklyPlanEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpPlanId;
            _tmpPlanId = _cursor.getString(_cursorIndexOfPlanId);
            final String _tmpWeekStartContext;
            if (_cursor.isNull(_cursorIndexOfWeekStartContext)) {
              _tmpWeekStartContext = null;
            } else {
              _tmpWeekStartContext = _cursor.getString(_cursorIndexOfWeekStartContext);
            }
            final String _tmpLockState;
            _tmpLockState = _cursor.getString(_cursorIndexOfLockState);
            final String _tmpActiveLockSnapshotId;
            if (_cursor.isNull(_cursorIndexOfActiveLockSnapshotId)) {
              _tmpActiveLockSnapshotId = null;
            } else {
              _tmpActiveLockSnapshotId = _cursor.getString(_cursorIndexOfActiveLockSnapshotId);
            }
            final String _tmpStaleWarning;
            if (_cursor.isNull(_cursorIndexOfStaleWarning)) {
              _tmpStaleWarning = null;
            } else {
              _tmpStaleWarning = _cursor.getString(_cursorIndexOfStaleWarning);
            }
            final String _tmpSlotsJson;
            _tmpSlotsJson = _cursor.getString(_cursorIndexOfSlotsJson);
            final String _tmpLockedRecipeIdsJson;
            _tmpLockedRecipeIdsJson = _cursor.getString(_cursorIndexOfLockedRecipeIdsJson);
            final long _tmpUpdatedAtEpochMs;
            _tmpUpdatedAtEpochMs = _cursor.getLong(_cursorIndexOfUpdatedAtEpochMs);
            _result = new WeeklyPlanEntity(_tmpPlanId,_tmpWeekStartContext,_tmpLockState,_tmpActiveLockSnapshotId,_tmpStaleWarning,_tmpSlotsJson,_tmpLockedRecipeIdsJson,_tmpUpdatedAtEpochMs);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getSnapshot(final String snapshotId,
      final Continuation<? super LockSnapshotEntity> $completion) {
    final String _sql = "SELECT * FROM lock_snapshot WHERE snapshotId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, snapshotId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<LockSnapshotEntity>() {
      @Override
      @Nullable
      public LockSnapshotEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSnapshotId = CursorUtil.getColumnIndexOrThrow(_cursor, "snapshotId");
          final int _cursorIndexOfPlanId = CursorUtil.getColumnIndexOrThrow(_cursor, "planId");
          final int _cursorIndexOfCreatedAtEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtEpochMs");
          final int _cursorIndexOfSelectionsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "selectionsJson");
          final int _cursorIndexOfPlanContextJson = CursorUtil.getColumnIndexOrThrow(_cursor, "planContextJson");
          final int _cursorIndexOfSourceCatalogVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceCatalogVersion");
          final LockSnapshotEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpSnapshotId;
            _tmpSnapshotId = _cursor.getString(_cursorIndexOfSnapshotId);
            final String _tmpPlanId;
            _tmpPlanId = _cursor.getString(_cursorIndexOfPlanId);
            final long _tmpCreatedAtEpochMs;
            _tmpCreatedAtEpochMs = _cursor.getLong(_cursorIndexOfCreatedAtEpochMs);
            final String _tmpSelectionsJson;
            _tmpSelectionsJson = _cursor.getString(_cursorIndexOfSelectionsJson);
            final String _tmpPlanContextJson;
            _tmpPlanContextJson = _cursor.getString(_cursorIndexOfPlanContextJson);
            final String _tmpSourceCatalogVersion;
            if (_cursor.isNull(_cursorIndexOfSourceCatalogVersion)) {
              _tmpSourceCatalogVersion = null;
            } else {
              _tmpSourceCatalogVersion = _cursor.getString(_cursorIndexOfSourceCatalogVersion);
            }
            _result = new LockSnapshotEntity(_tmpSnapshotId,_tmpPlanId,_tmpCreatedAtEpochMs,_tmpSelectionsJson,_tmpPlanContextJson,_tmpSourceCatalogVersion);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getLatestShoppingList(final String planId,
      final Continuation<? super ShoppingListEntity> $completion) {
    final String _sql = "SELECT * FROM shopping_list WHERE planId = ? ORDER BY updatedAtEpochMs DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, planId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ShoppingListEntity>() {
      @Override
      @Nullable
      public ShoppingListEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfShoppingListId = CursorUtil.getColumnIndexOrThrow(_cursor, "shoppingListId");
          final int _cursorIndexOfPlanId = CursorUtil.getColumnIndexOrThrow(_cursor, "planId");
          final int _cursorIndexOfSnapshotId = CursorUtil.getColumnIndexOrThrow(_cursor, "snapshotId");
          final int _cursorIndexOfItemsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "itemsJson");
          final int _cursorIndexOfStatusMetadataJson = CursorUtil.getColumnIndexOrThrow(_cursor, "statusMetadataJson");
          final int _cursorIndexOfUpdatedAtEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAtEpochMs");
          final ShoppingListEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpShoppingListId;
            _tmpShoppingListId = _cursor.getString(_cursorIndexOfShoppingListId);
            final String _tmpPlanId;
            _tmpPlanId = _cursor.getString(_cursorIndexOfPlanId);
            final String _tmpSnapshotId;
            if (_cursor.isNull(_cursorIndexOfSnapshotId)) {
              _tmpSnapshotId = null;
            } else {
              _tmpSnapshotId = _cursor.getString(_cursorIndexOfSnapshotId);
            }
            final String _tmpItemsJson;
            _tmpItemsJson = _cursor.getString(_cursorIndexOfItemsJson);
            final String _tmpStatusMetadataJson;
            if (_cursor.isNull(_cursorIndexOfStatusMetadataJson)) {
              _tmpStatusMetadataJson = null;
            } else {
              _tmpStatusMetadataJson = _cursor.getString(_cursorIndexOfStatusMetadataJson);
            }
            final long _tmpUpdatedAtEpochMs;
            _tmpUpdatedAtEpochMs = _cursor.getLong(_cursorIndexOfUpdatedAtEpochMs);
            _result = new ShoppingListEntity(_tmpShoppingListId,_tmpPlanId,_tmpSnapshotId,_tmpItemsJson,_tmpStatusMetadataJson,_tmpUpdatedAtEpochMs);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getPreference(final String key,
      final Continuation<? super PreferenceEntity> $completion) {
    final String _sql = "SELECT * FROM app_preferences WHERE key = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, key);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PreferenceEntity>() {
      @Override
      @Nullable
      public PreferenceEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfKey = CursorUtil.getColumnIndexOrThrow(_cursor, "key");
          final int _cursorIndexOfValueJson = CursorUtil.getColumnIndexOrThrow(_cursor, "valueJson");
          final int _cursorIndexOfUpdatedAtEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAtEpochMs");
          final PreferenceEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpKey;
            _tmpKey = _cursor.getString(_cursorIndexOfKey);
            final String _tmpValueJson;
            _tmpValueJson = _cursor.getString(_cursorIndexOfValueJson);
            final long _tmpUpdatedAtEpochMs;
            _tmpUpdatedAtEpochMs = _cursor.getLong(_cursorIndexOfUpdatedAtEpochMs);
            _result = new PreferenceEntity(_tmpKey,_tmpValueJson,_tmpUpdatedAtEpochMs);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
