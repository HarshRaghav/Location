package android.example.location;

import android.location.Location;

public class SendLocationActivity {
    private Location location;
    public SendLocationActivity(Location location) {
        this.location=location;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}