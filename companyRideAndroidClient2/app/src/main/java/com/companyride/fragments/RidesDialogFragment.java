package com.companyride.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.companyride.R;
import com.companyride.activities.RideRequestMainScreen;
import com.companyride.fragments.calendar.CalendarFragment;
import com.companyride.fragments.calendar.CalendarLoader;
import com.companyride.parameters.Params;
import com.companyride.utils.UtilityFunctions;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Valeriy on 5/22/2015.
 */
public class RidesDialogFragment extends DialogFragment {
    public static  final String RIDES = "rides";
    public static final String SELECTED_DATE = "selected_date";

    private  ArrayList<CalendarLoader.Ride> pairs = null;
    private  Calendar selectedDate = null;
    private  ListView.OnItemClickListener rideEventOnItemClickListener = null;
    private RideEventsListAdapter mRidesListAdapter;

    public static final RidesDialogFragment newInstance(ArrayList<CalendarLoader.Ride> rides,
                                                        Calendar selectedDate,
                                                        ListView.OnItemClickListener rideEventOnClickListener)
    {
        RidesDialogFragment fragment = new RidesDialogFragment();
        fragment.rideEventOnItemClickListener = rideEventOnClickListener;
        Bundle bundle = new Bundle(2);
        bundle.putSerializable(RIDES, rides);
        bundle.putSerializable(SELECTED_DATE, selectedDate);
        fragment.setArguments(bundle);
        return fragment ;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        this.pairs = (ArrayList<CalendarLoader.Ride>) args.get(RIDES);
        this.selectedDate = (Calendar) args.get(SELECTED_DATE);
        args.remove(RIDES);
        args.remove(SELECTED_DATE);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        if (this.pairs != null && this.pairs.size() > 0 ) {
            final ListView listView = new ListView(builder.getContext());
            mRidesListAdapter = new RideEventsListAdapter(builder.getContext(), this.pairs);
            listView.setAdapter(mRidesListAdapter);
            listView.setOnItemClickListener(this.rideEventOnItemClickListener);
            builder.setView(listView);
        }
        else{
            TextView title = new TextView(getActivity());
            title.setText("No events exist yet!");
            title.setPadding(10, 10, 10, 10);
            title.setGravity(Gravity.CENTER);
            builder.setView(title);
        }
        builder.setTitle(getString(R.string.events) + " - " + Params.shortDate.format(this.selectedDate.getTime()));
//                Integer.toString(this.selectedDate.get(Calendar.DAY_OF_MONTH)) + "/" +
//                String.format("%02d", this.selectedDate.get(Calendar.MONTH) + 1));
        builder.setPositiveButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.propose_ride, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getActivity().getApplicationContext(), RideRequestMainScreen.class);
                intent.putExtra(Params.extraRideType, Params.extraRideTypeDriver);
                intent.putExtra(Params.extraDate, UtilityFunctions.dateToString(
                        RidesDialogFragment.this.selectedDate.getTime(),
                        Params.formDate)
                );
                intent.putExtra(Params.extraCallingActivity, CalendarFragment.class.toString());
                builder.getContext().startActivity(intent);
            }
        });
        builder.setNeutralButton(R.string.request_hitch, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getActivity().getApplicationContext(), RideRequestMainScreen.class);
                intent.putExtra(Params.extraRideType, Params.extraRideTypeHitcher);
                intent.putExtra(Params.extraDate, UtilityFunctions.dateToString(
                        RidesDialogFragment.this.selectedDate.getTime(),
                        Params.formDate)
                );
                intent.putExtra(Params.extraCallingActivity, CalendarFragment.class.toString());
                builder.getContext().startActivity(intent);
            }
        });
        return builder.create();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (this.getDialog() != null && this.getDialog().isShowing()) {
            this.getDialog().dismiss();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

//    @Override
//    public void onStop() {
//        super.onStop();
//        if (this.getDialog().isShowing()) {
//            this.getDialog().dismiss();
//        }
//    }


    public static class ViewHolderItem{
        public ImageView icon;
        public TextView eventText;
        public String itemId;

        public ViewHolderItem(ImageView icon, TextView eventText, String itemId){
            this.icon = icon;
            this.eventText = eventText;
            this.itemId = itemId;
        }
    }

    private class RideEventsListAdapter extends ArrayAdapter<CalendarLoader.Ride> {
        private final Context context;
        private final ArrayList<CalendarLoader.Ride> values;

        public RideEventsListAdapter(Context context, ArrayList<CalendarLoader.Ride> values){
            super(context, R.layout.ride_event, values);
            this.context = context;
            this.values  = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CalendarLoader.Ride ride = values.get(position);
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.ride_event, parent, false);
            }

            ViewHolderItem holder = new ViewHolderItem(
                        (ImageView) convertView.findViewById(R.id.event_icon),
                        (TextView) convertView.findViewById(R.id.event_info),
                        ride.getRideId()
                    );
//            ViewHolderItem.icon = (ImageView) convertView.findViewById(R.id.event_icon);
//            ViewHolderItem.eventText = (TextView) convertView.findViewById(R.id.event_info);
//            ViewHolderItem.itemId = ride.getRideId();

            if (!ride.isRideRequest())
                if (ride.isDriver())
                    holder.icon.setImageResource(R.drawable.ic_dialog_matched_ride_driver);
                else
                    holder.icon.setImageResource(R.drawable.ic_dialog_matched_ride_hitcher);
            else {
                if (ride.isDriver())
                    holder.icon.setImageResource(R.drawable.ic_dialog_in_process_driver);
                else
                    holder.icon.setImageResource(R.drawable.ic_dialog_in_process_hitcher);
            }
            holder.icon.setPadding(5,5,5,5);
            holder.eventText.setText(ride.getRideTimeFromStr() + " - " + ride.getRideTimeToStr());
            convertView.setPadding(5,5,5,5);
            convertView.setTag(holder);
            return convertView;
        }
    }
}