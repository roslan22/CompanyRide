package com.companyride.interfaces;

import android.location.Location;

/**
 * Created by Ruslan on 02-Oct-15.
 */
public interface CurrentLocationInterface {
    void onCurrentLocationReady();
    void onLocationChanged(Location location);
}
