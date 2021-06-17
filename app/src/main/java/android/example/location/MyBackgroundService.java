package android.example.location;

import android.Manifest;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.loader.app.LoaderManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.greenrobot.eventbus.EventBus;

public class MyBackgroundService extends Service {

    private static final String CHANNEL_ID = "my_channel";
    private final IBinder mBinder = new LocalBinder();
    // update interval
    private static final long UPDATE_INTERVAL = 10000;
    private static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2;
    private static final int NOTI_ID = 1234;
    private boolean mChangeConfig = false;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private Handler handler;
    private Location location;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean dataFromIntent=intent.getBooleanExtra("ifback",false);
        if (dataFromIntent){
            removeLocationUpdate();
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(null);
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangeConfig=true;
    }

    private void removeLocationUpdate() {

        try{
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            Common.setRequestUpdate(this,false);
            stopSelf();
        }
        catch(SecurityException e){
            Common.setRequestUpdate(this,true);
        }
    }

    @Override
    public void onCreate() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };

        createLocationRequest();
        getLastLocation();

        HandlerThread handlerThread=new HandlerThread("hands");
        handlerThread.start();
        handler=new Handler(handlerThread.getLooper());
    }

    private void getLastLocation() {
        try {
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        location=task.getResult();
                    }
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void createLocationRequest() {

        locationRequest=new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void onNewLocation(Location lastLocation) {
        location=lastLocation;
        EventBus.getDefault().postSticky(new SendLocationActivity(location));

        if(serviceIsRunningInForeground(this)){
            // background running
            Intent intent=new Intent(this,MyBackgroundService.class);
            String text=Common.getLocationData(location);
            intent.putExtra("ifback",true);
            PendingIntent pendingIntent=PendingIntent.getService(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent activityIntent=PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),0);

        }


    }

    //Notification code

    private boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)){
            if(getClass().getName().equals(serviceInfo.service.getClassName())){
                if(serviceInfo.foreground){
                    return true;
                }

            }
        }
        return false;

    }

    public MyBackgroundService(){

    }

    public void requestLocationUpdates() {
        Common.setRequestUpdate(this,true);
        startService(new Intent(getApplicationContext(),MyBackgroundService.class));
        try{
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
        }
        catch(SecurityException e){

        }
    }

    public class LocalBinder extends Binder {
        MyBackgroundService getService(){
            return MyBackgroundService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        mChangeConfig=false;
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {

        mChangeConfig=false;
        super.onRebind(intent);
    }
}