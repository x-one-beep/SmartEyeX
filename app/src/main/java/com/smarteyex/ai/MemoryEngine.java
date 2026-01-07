package com.smarteyex.ai;

import android.content.Context;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Entity
public class LogEntry {
    @PrimaryKey(autoGenerate = true) public int id;
    public String content;
}

@Dao
public interface LogDao {
    @Insert void insert(LogEntry log);
    @Query("SELECT * FROM LogEntry") List<LogEntry> getAll();
}

@androidx.room.Database(entities = {LogEntry.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract LogDao logDao();
}

public class MemoryEngine {
    private AppDatabase db;

    public MemoryEngine(Context context) {
        try {
            db = Room.databaseBuilder(context, AppDatabase.class, "smarteyex.db").build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveLog(String content) {
        try {
            new Thread(() -> {
                try {
                    LogEntry log = new LogEntry();
                    log.content = content;
                    db.logDao().insert(log);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
