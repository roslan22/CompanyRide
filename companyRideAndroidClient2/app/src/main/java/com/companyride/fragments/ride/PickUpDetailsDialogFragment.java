package com.companyride.fragments.ride;

import android.app.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.companyride.R;
import com.companyride.geoLocation.GoogleApiConnection;
import com.companyride.geoLocation.PlaceAutocompleteAdapter;
import com.companyride.utils.UtilityFunctions;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Valeriy on 7/03/2015.
 */
public class PickUpDetailsDialogFragment extends DialogFragment {
    private static final String LOG_TAG = " PickUp dialog";
    public static final String TIME = "time";
    public static final String PICKUP = "pickup";
    public static final String DROP = "drop";
    public static final String POSITION = "position";
    public static final int RESULT_CODE = 1;
    private static SimpleDateFormat timeForm = new SimpleDateFormat("HH:mm");


    private Fragment mParentListener;
    private String time;
    private String pickup;
    private String drop;
    private int position;

    public String getTime() {
        return time;
    }
    public String getPickup() {
        return pickup;
    }
    public String getDrop() {
        return drop;
    }



    public static final PickUpDetailsDialogFragment newInstance(int position, String time, String pickup, String drop)
    {
        PickUpDetailsDialogFragment fragment = new PickUpDetailsDialogFragment();
        Bundle bundle = new Bundle(2);
        bundle.putSerializable(TIME, time);
        bundle.putSerializable(PICKUP, pickup);
        bundle.putSerializable(DROP, drop);
        bundle.putSerializable(POSITION, position);
        fragment.setArguments(bundle);
        return fragment ;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mParentListener = getTargetFragment();
        Bundle args = getArguments();
        this.time   = (String) args.get(TIME);
        this.pickup = (String) args.get(PICKUP);
        this.drop   = (String) args.get(DROP);
        this.position = (int) args.get(POSITION);
        Date dateTime = null;
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.set(Calendar.AM_PM, 1);
        try {
            dateTime = timeForm.parse(time);
            calendar.setTime(dateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View myView = inflater.inflate(R.layout.pickup_details_fragment, null);
        final TimePicker picker = (TimePicker) myView.findViewById(R.id.pickupTimePicker);
        picker.setIs24HourView(true);
        final AutoCompleteTextView pickupText = (AutoCompleteTextView) myView.findViewById(R.id.pickUpLocation);
        final AutoCompleteTextView dropText = (AutoCompleteTextView) myView.findViewById(R.id.dropLocation);
        picker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        picker.setCurrentMinute(calendar.get(Calendar.MINUTE));
        picker.setOnTimeChangedListener(mOnTimeChangedListener);
        pickupText.setText(pickup);
        dropText.setText(drop);

        pickupText.setOnItemClickListener(mAutocompleteClickListener);
        pickupText.setAdapter(((DriverRideFragment)mParentListener).mAdapter);
        dropText.setOnItemClickListener(mAutocompleteClickListener);
        dropText.setAdapter(((DriverRideFragment)mParentListener).mAdapter);

        builder.setView(myView);
        builder.setTitle(getString(R.string.edit_pickup_details));
        builder.setNegativeButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PickUpDetailsDialogFragment.this.getDialog().cancel();
            }
        });
        builder.setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PickUpDetailsDialogFragment pickupDialog = PickUpDetailsDialogFragment.this;
                pickupDialog.time = String.format("%s:%s",picker.getCurrentHour(), picker.getCurrentMinute());
                pickupDialog.pickup = pickupText.getText().toString();
                pickupDialog.drop = dropText.getText().toString();
                Intent intent = getActivity().getIntent();
                intent.putExtra(TIME, time);
                intent.putExtra(PICKUP, pickup);
                intent.putExtra(DROP, drop);
                intent.putExtra(POSITION, position);
                mParentListener.onActivityResult(getTargetRequestCode(), RESULT_CODE, intent);
                dismiss();
            }
        });
        return builder.create();
    }

    private TimePicker.OnTimeChangedListener mOnTimeChangedListener = new TimePicker.OnTimeChangedListener() {
        @Override
        public void onTimeChanged(TimePicker timePicker, int hour, int minute) {
            timePicker.setCurrentHour(hour);
            timePicker.setCurrentMinute(minute);
        }
    };

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            final AutocompletePrediction item = ((DriverRideFragment)mParentListener).mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);

            Log.i(LOG_TAG, "Autocomplete item selected: " + primaryText);
        }
    };
}