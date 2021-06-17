package android.example.location;

import android.app.Application;
import android.location.Location;

import androidx.lifecycle.LiveData;

import java.util.List;

public class LocationRepository {
    private LocateDao locateDao;
    private LiveData<List<Locate>> AllLocations;

    LocationRepository(Application application) {
        LocationDatabase locationDatabase =LocationDatabase.getInstance(application);
        locateDao=locationDatabase.locateDao();
        AllLocations = locateDao.getAllLocations();
    }

    void insert(Locate locate){
        LocationDatabase.databaseWriter.execute(() -> {
            locateDao.insert(locate);
        });
    }

    LiveData<List<Locate>> getAllLocations(){
        return AllLocations;
    }
}
