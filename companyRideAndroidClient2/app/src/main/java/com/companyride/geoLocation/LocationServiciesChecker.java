package com.companyride.geoLocation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;

import com.companyride.R;

/**
 * Created by Ruslan on 02-Oct-15.
 */
public class LocationServiciesChecker {
    boolean gps_enabled = false;
    boolean network_enabled = false;
    LocationManager lm;
    Context mContext;

    public LocationServiciesChecker(Context i_context) {
        lm = (LocationManager)i_context.getSystemService(Context.LOCATION_SERVICE);
        mContext = i_context;
    }

    public boolean checkLocationServicies() {
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            //TODO add exception
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
            dialog.setMessage(mContext.getResources().getString(R.string.gps_network_not_enabled));
            dialog.setPositiveButton(mContext.getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    mContext.startActivity(myIntent);
                    //get gps
                }
            });
            dialog.setNegativeButton(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub

                }
            });
            dialog.show();
            return false;
        }
        else
            return true;
    }
}
