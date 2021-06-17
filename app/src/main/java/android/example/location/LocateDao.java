package android.example.location;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LocateDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Locate locate);

    @Query("SELECT * FROM location_table ORDER BY id ASC")
    LiveData<List<Locate>> getAllLocations();

}
