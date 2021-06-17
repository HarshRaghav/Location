package android.example.location;

import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;

public class Common {
        public static final String Key="LocationUpdateEnable";

        public static String getLocationData(Location location){
            return location == null ? "Unknown Location" : new StringBuilder().append(location.getLatitude()).append("/").append(location.getLongitude())
                    .append("/").append(location.getTime()).toString();
        }

        public static void setRequestUpdate(Context context, boolean b) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(Key,b);
        }

        public static boolean requestingLocationUpdateUpdate(Context context) {
            return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Key,false);
        }

        //Date
    }