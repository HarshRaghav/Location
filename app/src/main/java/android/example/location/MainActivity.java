package android.example.location;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements onClicked, SharedPreferences.OnSharedPreferenceChangeListener{
    private LocationViewModel locationViewModel;
    private LocationAdapter locationAdapter;
    private FusedLocationProviderClient fusedLocationProviderClient;
    Button requestLocation;
    MyBackgroundService backgroundService=null;
    boolean mBound=false;
    private final ServiceConnection mServiceConnection= new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyBackgroundService.LocalBinder binder=(MyBackgroundService.LocalBinder) service;
            backgroundService=binder.getService();
            mBound=true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            backgroundService=null;
            mBound=false;

        }
    };
    private final int MY_Permission_Code=1234;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView rv =(RecyclerView) findViewById(R.id.recyclerView);
        locationAdapter=new LocationAdapter(new LocationAdapter.LocationDiff(),this);
        rv.setAdapter(locationAdapter);
        rv.setLayoutManager(new LinearLayoutManager(this));
        locationViewModel= new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(LocationViewModel.class);
        locationViewModel.AllLocations.observe(this, new Observer<List<Locate>>() {
            @Override
            public void onChanged(List<Locate> locates) {
                locationAdapter.update(locates);
            }
        });
        Dexter.withActivity(this).withPermissions(Arrays.asList(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION)).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                requestLocation=(Button)findViewById(R.id.updatebutton);
                requestLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        backgroundService.requestLocationUpdates();
                    }
                });
                setButtonState(Common.requestingLocationUpdateUpdate(MainActivity.this));
                bindService(new Intent(MainActivity.this,MyBackgroundService.class),mServiceConnection, Context.BIND_AUTO_CREATE);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

            }
        }).check();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(Common.Key)){
            setButtonState(sharedPreferences.getBoolean(Common.Key,false));
        }
    }

    private void setButtonState(boolean aBoolean) {
        if(aBoolean){
            requestLocation.setEnabled(false);
        }
        else{
            requestLocation.setEnabled(true);
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        if(mBound){
            unbindService(mServiceConnection);
            mBound=false;
        }
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onListenLocation(SendLocationActivity event){
        if(event != null){
            String data=new StringBuilder().append(event.getLocation().getLatitude()).append(" ").append(event.getLocation().getLongitude()).append(" ").append(event.getLocation().getTime()).toString();
            locationViewModel.insert(new Locate(data));
        }
    }
}