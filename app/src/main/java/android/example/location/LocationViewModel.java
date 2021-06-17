package android.example.location;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class LocationViewModel extends AndroidViewModel {
        public LiveData<List<Locate>> AllLocations;
        public LocationRepository locationRepository;

        public LocationViewModel(Application application){
            super(application);
            locationRepository =new LocationRepository(application);
            this.AllLocations=locationRepository.getAllLocations();
        }
        public void insert(Locate locate){
            locationRepository.insert(locate);
        }
    }