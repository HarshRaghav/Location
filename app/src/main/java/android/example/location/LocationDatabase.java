package android.example.location;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Locate.class}, version =1, exportSchema = false)
public abstract class LocationDatabase extends RoomDatabase{
    public abstract LocateDao locateDao();
    private static volatile LocationDatabase INSTANCE;
    private static final int FIXED_THREADS = 4;


    static final ExecutorService databaseWriter = Executors.newFixedThreadPool(FIXED_THREADS);

    static LocationDatabase getInstance(final Context context){
        if (INSTANCE==null){
            synchronized (LocationDatabase.class){
                if(INSTANCE==null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),LocationDatabase.class , "location_database").addCallback(sRoomDatabaseCallback).build();
                }
            }
        }
        return INSTANCE;
    }
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback(){
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriter.execute(() -> {
                LocateDao dao = INSTANCE.locateDao();
                Locate word = new Locate("14.55 14.55 12:12am open");
                dao.insert(word);
            });
        }
    };
}
