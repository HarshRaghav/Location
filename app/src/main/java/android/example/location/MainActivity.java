package android.example.location;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private LocationViewModel locationViewModel;
    private LocationAdapter locationAdapter;
    Intent mServiceIntent;
    String state="Running";
    private YourService mYourService;
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
    @Override
    protected void onDestroy() {
        state="Killed";
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, Restarter.class);
        this.sendBroadcast(broadcastIntent);
        super.onDestroy();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("Service status", "Running");
                return true;
            }
        }
        Log.i ("Service status", "Not running");
        return false;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mYourService = new YourService();
        mServiceIntent = new Intent(this, mYourService.getClass());
        if (!isMyServiceRunning(mYourService.getClass())) {
            startService(mServiceIntent);
        }
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
        RecyclerView rv =(RecyclerView) findViewById(R.id.recylerview);
        locationAdapter=new LocationAdapter(new LocationAdapter.LocationDiff());
        rv.setAdapter(locationAdapter);
        rv.setLayoutManager(new LinearLayoutManager(this));
        locationViewModel= new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(LocationViewModel.class);
        locationViewModel.AllLocations.observe(this, new Observer<List<Locate>>() {
            @Override
            public void onChanged(List<Locate> locates) {
                locationAdapter.update(locates);
            }
        });
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
        state="Running";
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        state="Background";
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onListenLocation(SendLocationActivity event){
        if(event != null){
            long time=System.currentTimeMillis();
            Date currentTime = Calendar.getInstance().getTime();
            String data=new StringBuilder().append(event.getLocation().getLatitude()).append(" ").append(event.getLocation().getLongitude()).append(" ").append(currentTime).append(" ").append(state).toString();

            locationViewModel.insert(new Locate(data));
            Log.v("Harsh","onListen");
            Toast.makeText(backgroundService,data,Toast.LENGTH_SHORT).show();
        }
    }
}