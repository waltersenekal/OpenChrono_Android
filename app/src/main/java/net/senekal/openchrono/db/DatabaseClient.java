package net.senekal.openchrono.db;

import android.content.Context;
import androidx.room.Room;

public class DatabaseClient {
    private static AppDatabase appDatabase;


    public static AppDatabase getInstance(Context context) {
        if (appDatabase == null) {
            // Build a standard Room database without encryption
            appDatabase = Room.databaseBuilder(context, AppDatabase.class, "unencrypted_database")
                    .build();
        }
        return appDatabase;
    }
}