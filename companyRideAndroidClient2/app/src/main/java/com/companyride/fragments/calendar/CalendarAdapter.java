/*
* Copyright 2011 Lauri Nevala.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.companyride.fragments.calendar;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.companyride.R;
import com.companyride.activities.RideMainScreen;
import com.companyride.activities.RideRequestMainScreen;
import com.companyride.fragments.RidesDialogFragment;
import com.companyride.parameters.Params;
import com.companyride.utils.UtilityFunctions;

import java.util.*;

public class CalendarAdapter extends BaseAdapter {
    static final int NUM_TO_SHOW=2;
    static final int FIRST_DAY_OF_WEEK =0; // Sunday = 0, Monday = 1
    private static final String EVENTS_DIALOG = "EVENT_DIALOG";
    // references to our items
    public String[] days;

    private CalendarLoader calendarLoader;
    private Context mContext;

    private Calendar month;
    private Calendar selectedDate;
    private HashMap<String, ArrayList<CalendarLoader.Ride>> inprocess_items;
    private HashMap<String, ArrayList<CalendarLoader.Ride>> matched_items;

    public CalendarAdapter(Context c, Calendar monthCalendar) {
        calendarLoader = new CalendarLoader();
        month = monthCalendar;
        selectedDate = (Calendar)monthCalendar.clone();
        mContext = c;
        month.set(Calendar.DAY_OF_MONTH, 1);
        this.inprocess_items = new HashMap<String, ArrayList<CalendarLoader.Ride>>();
        this.matched_items = new HashMap<String, ArrayList<CalendarLoader.Ride>>();
        refreshDays();
    }

    private void prepareItems(HashMap<String, ArrayList<CalendarLoader.Ride>> items) {
        HashMap<String, ArrayList<CalendarLoader.Ride>> newItems = new HashMap<String, ArrayList<CalendarLoader.Ride>>();
        Iterator<String> iter = items.keySet().iterator();
        while(iter.hasNext()) {
            String dateItem = iter.next();
            // if no leading zero - add leading zero
            if (dateItem.length() == 1) {
                ArrayList<CalendarLoader.Ride> timeArray = items.get(dateItem);
                String newDateItem = "0" + dateItem;
                newItems.put(newDateItem, timeArray);
                iter.remove();
            }
        }
        // add back new items
        for (Map.Entry<String, ArrayList<CalendarLoader.Ride>> dateItem : newItems.entrySet()){
            items.put(dateItem.getKey(), dateItem.getValue());
        }
    }

    public void setSelectedDate(Calendar date){
        selectedDate = date;
        // now sort current date items and display on dialog
        displayDialogAllCurrentDayEvents(date);
    }

    private void displayDialogAllCurrentDayEvents(Calendar date) {
        ArrayList<CalendarLoader.Ride> sortedRides = getSortedEventsForDay(
                String.format("%02d",date.get(Calendar.DAY_OF_MONTH))
        );
//        if (sortedRides.size() == 0)
//            return;
        RidesDialogFragment eventsDialog =  RidesDialogFragment.newInstance(
                sortedRides,
                selectedDate,
                rideEventOnItemClickListener);
//        FragmentTransaction dialogTransaction = ((Activity) mContext).getFragmentManager().beginTransaction();
//        dialogTransaction.add(eventsDialog, EVENTS_DIALOG)
//                .addToBackStack(null)
//                .commit();

        eventsDialog.show(((Activity) mContext).getFragmentManager(), EVENTS_DIALOG);
    }

    public void setInProcessItems(HashMap<String, ArrayList<CalendarLoader.Ride>> items) {
        prepareItems(items);
        this.inprocess_items = items;
    }

    public void setMatchedItems(HashMap<String, ArrayList<CalendarLoader.Ride>> items) {
        prepareItems(items);
        this.matched_items = items;
    }


    public int getCount() {
        return days.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new view for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        TextView dayView;

        // if it's not recycled, initialize some attributes
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.calendar_item, null);
        }
        dayView = (TextView)v.findViewById(R.id.date);

        // disable empty days from the beginning
        if(days[position].equals("")) {
            dayView.setClickable(false);
            dayView.setFocusable(false);
            v.setBackgroundResource(0);
        }
        else {
//            // mark selected day as focused
            if(month.get(Calendar.YEAR)== selectedDate.get(Calendar.YEAR) && month.get(Calendar.MONTH)== selectedDate.get(Calendar.MONTH) && days[position].equals(""+selectedDate.get(Calendar.DAY_OF_MONTH))) {
                v.setBackgroundResource(R.color.accent);
            }
            // if not focused - set simple background
            else {
               v.setBackgroundResource(R.color.white);
            }
        }
        String date = days[position];
        // set day text (number)
        dayView.setText(date);
        // get ride events layout
        LinearLayout rideEventsLayout = (LinearLayout) v.findViewById(R.id.ride_events);
        // remove all views from the layout
        cleanRideEvents(rideEventsLayout);
        // if day is not null - add ride events if exist
        if(!date.equals("")) {
            // zfill prefix for 2
            if(date.length()==1) {
                date = "0"+date;
            }
            ArrayList<CalendarLoader.Ride> sortedRides = getSortedEventsForDay(date);
            if (sortedRides.size() == 0)
                rideEventsLayout.setVisibility(View.INVISIBLE);
            else{
                rideEventsLayout.setVisibility(View.VISIBLE);
                // sort items
                sortRideItems(sortedRides);
                // add ride events
                addRideItems(rideEventsLayout, sortedRides);
            }
        }
        setCellHeight((GridView)parent, v);
        setCellWidth((GridView)parent, v);
        return v;
    }

    private ArrayList<CalendarLoader.Ride> getSortedEventsForDay(String date) {
        ArrayList<CalendarLoader.Ride> sortedRides = new ArrayList<CalendarLoader.Ride>();
        // if inprocess_items defined and has the date - add ride request events from array
        if (inprocess_items != null && inprocess_items.containsKey(date)) {
            sortedRides.addAll(inprocess_items.get(date));
        }
        // if matched_items defined and has the date - add ride events from array
        if (matched_items != null && matched_items.containsKey(date)) {
            sortedRides.addAll(matched_items.get(date));
        }
        return sortedRides;
    }

    private void sortRideItems(ArrayList<CalendarLoader.Ride> sortedRides) {
        // Sort items by time
        Collections.sort(sortedRides, new Comparator<CalendarLoader.Ride>() {
            @Override
            public int compare(CalendarLoader.Ride lhs, CalendarLoader.Ride rhs) {
                Date lDate = lhs.getRideTimeFrom();
                Date rDate = rhs.getRideTimeFrom();
                return lDate.compareTo(rDate);
            }
        });
    }

    public ListView.OnItemClickListener rideEventOnItemClickListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // get the selected item tag
            RidesDialogFragment.ViewHolderItem vHolder = (RidesDialogFragment.ViewHolderItem) view.getTag();
            String rideId = vHolder.itemId;
            CalendarLoader.Ride ride = null;
            try {
                ride = getRideById(rideId);
            } catch (Exception e) {
                //todo: handle the exception
                e.printStackTrace();
            }
            Intent intent = null;
            // if it is matched ride - open matched ride activity
            if( !ride.isRideRequest()){
                intent = new Intent(view.getContext(), RideMainScreen.class);
                intent.putExtra(Params.extraDate,  UtilityFunctions.dateToString(
                        selectedDate.getTime(),
                        Params.formDate)
                );
            }
            // else- open the ride request activity
            else{
                intent = new Intent(view.getContext(), RideRequestMainScreen.class);
                intent.putExtra(Params.extraRideType,
                        ride.isDriver() ? Params.extraRideTypeDriver : Params.extraRideTypeHitcher);
                intent.putExtra(Params.extraDate,  UtilityFunctions.dateToString(
                                selectedDate.getTime(),
                                Params.formDateJS)
                );
            }
            if (intent != null){
                intent.putExtra(Params.extraCallingActivity,CalendarFragment.class.toString());
                intent.putExtra(Params.extraID, ride.getRideId());
                intent.putExtra(Params.extraDate, UtilityFunctions.dateToString(
                                selectedDate.getTime(),
                                Params.formDateJS));
                view.getContext().startActivity(intent);
            }
        }
    };

    private CalendarLoader.Ride getRideById(String rideId) throws Exception {
        CalendarLoader.Ride ride = calendarLoader.findRideInCache(rideId);
        if ( ride == null )
            throw new Exception("Could not find a ride by id: " + rideId);
        return ride;
    }

    public void refreshDays()
    {
        // clear items
        inprocess_items.clear();
        matched_items.clear();

        int lastDay = month.getActualMaximum(Calendar.DAY_OF_MONTH);
        int firstDay = (int)month.get(Calendar.DAY_OF_WEEK);

        // figure size of the array
        if(firstDay==1){
            days = new String[lastDay+(FIRST_DAY_OF_WEEK*6)];
        }
        else {
            days = new String[lastDay+firstDay-(FIRST_DAY_OF_WEEK+1)];
        }

        int j=FIRST_DAY_OF_WEEK;

        // populate empty days before first real day
        if(firstDay>1) {
            for(j=0;j<firstDay-FIRST_DAY_OF_WEEK;j++) {
                days[j] = "";
            }
        }
        else {
            for(j=0;j<FIRST_DAY_OF_WEEK*6;j++) {
                days[j] = "";
            }
            j=FIRST_DAY_OF_WEEK*6+1; // sunday => 1, monday => 7
        }

        // populate days
        int dayNumber = 1;
        for(int i=j-1;i<days.length;i++) {
            days[i] = ""+dayNumber;
            dayNumber++;
        }
    }

    private void setCellHeight(GridView parent, View view) {
        LinearLayout dayItem = (LinearLayout) view.findViewById(R.id.day_item);
        int maxHeight = parent.getHeight()/6 - ((GridView)parent).getVerticalSpacing();
        if ( maxHeight <= 0 )
            return;
        dayItem.setMinimumHeight(maxHeight);
        int currHeight = dayItem.getHeight();
        if (currHeight > maxHeight)
            dayItem.getLayoutParams().height = maxHeight;
    }

    private void setCellWidth(GridView parent, View view) {
        LinearLayout dayItem = (LinearLayout) view.findViewById(R.id.day_item);
        int maxWidth = parent.getWidth()/7 - ((GridView)parent).getHorizontalSpacing();
        if ( maxWidth <= 0 )
            return;
        dayItem.setMinimumWidth(maxWidth);
        int currWidth = dayItem.getWidth();
        if (currWidth > maxWidth)
            dayItem.getLayoutParams().width = maxWidth;
    }

    public void refreshData() {
        refreshDays();
        calendarLoader.resetCache();
        calendarLoader.loadRidesAndRequestsData(month); // execute async loading of data
        setInProcessItems(calendarLoader.inprocess_items);
        setMatchedItems(calendarLoader.matched_items);
    }

    private void cleanRideEvents(LinearLayout rideEventsLayout){
        LinearLayout eventsList = (LinearLayout) rideEventsLayout.findViewById(R.id.events_list);
        eventsList.removeAllViews();
    }

    private void addRideItems(LinearLayout rideEventsLayout, final ArrayList<CalendarLoader.Ride> rides) {
        int exceeded = 0;

        ArrayList<CalendarLoader.Ride> ridesToShow = new ArrayList<CalendarLoader.Ride>();

        // go over ride times in the sorted array
        for ( CalendarLoader.Ride ride : rides) {
            // if reached the max number of items to show
            // - count how many more
            if (ridesToShow.size() >= NUM_TO_SHOW) {
                exceeded++;
                continue;
            }
            ridesToShow.add(ride);
        }
        if (ridesToShow.size() == 0)
            return;

//        ListView listView = (ListView) rideEventsLayout.findViewById(R.id.ride_events_list);
//        listView.setAdapter(new RideEventsListAdapter(mContext, ridesToShow));
        //listView.setSelector(android.R.color.transparent);
        LinearLayout eventsList = (LinearLayout) rideEventsLayout.findViewById(R.id.events_list);
        for (CalendarLoader.Ride ride : ridesToShow){
            LinearLayout newEventItem = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.ride_event, null);
            newEventItem.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            ImageView icon = (ImageView) newEventItem.findViewById(R.id.event_icon);
            icon.setPadding(0,0,0,0);
            if (!ride.isRideRequest())
                    if (ride.isDriver())
                        icon.setImageResource(R.drawable.ic_matched_ride_driver);
                    else
                        icon.setImageResource(R.drawable.ic_matched_ride_hitcher);
                else {
                    if (ride.isDriver())
                        icon.setImageResource(R.drawable.ic_in_process_driver);
                    else
                        icon.setImageResource(R.drawable.ic_in_process_hitcher);
                }
            TextView eventTime = (TextView) newEventItem.findViewById(R.id.event_info);
            eventTime.setTextSize(TypedValue.COMPLEX_UNIT_SP,8);
            eventTime.setPadding(5,0,0,0);
            eventTime.setText(ride.getRideTimeFromStr());
            // add to the event list
            eventsList.addView(newEventItem);
        }

        // if number of items exceeded max to show number - add "show more" item
        if ( exceeded > 0) {
            TextView moreTxt = (TextView) rideEventsLayout.findViewById(R.id.more_txt);
            // add the string with count of exceeded items
            moreTxt.setText(mContext.getString(R.string.more_info) + " (+" + exceeded +")");
        }
    }

//    private class RideEventsListAdapter extends ArrayAdapter<CalendarLoader.Ride> {
//        private final Context context;
//        private final ArrayList<CalendarLoader.Ride> values;
//
//        public RideEventsListAdapter(Context context, ArrayList<CalendarLoader.Ride> values){
//            super(context, R.layout.ride_event, values);
//            this.context = context;
//            this.values  = values;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            CalendarLoader.Ride ride = values.get(position);
//            RidesDialogFragment.ViewHolderItem holder;
//            if (convertView == null) {
//                holder = new RidesDialogFragment.ViewHolderItem();
//                LayoutInflater inflater = (LayoutInflater) context
//                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                convertView = inflater.inflate(R.layout.ride_event, parent, false);
//                holder.icon = (ImageView) convertView.findViewById(R.id.event_icon);
//                holder.eventText = (TextView) convertView.findViewById(R.id.event_info);
//                holder.itemId = ride.getRideId();
//                convertView.setTag(holder);
//            }
//            else{
//                holder = (RidesDialogFragment.ViewHolderItem) convertView.getTag();
//            }
//
//            if (!ride.isRideRequest())
//                if (ride.isDriver())
//                    holder.icon.setImageResource(R.drawable.ic_matched_ride_driver);
//                else
//                    holder.icon.setImageResource(R.drawable.ic_matched_ride_hitcher);
//            else {
//                if (ride.isDriver())
//                    holder.icon.setImageResource(R.drawable.ic_in_process_driver);
//                else
//                    holder.icon.setImageResource(R.drawable.ic_in_process_hitcher);
//            }
//            holder.eventText.setText(ride.getRideTimeFromStr());
//            holder.eventText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
//            holder.icon.setPadding(0,0,10,5);
//            convertView.setDuplicateParentStateEnabled(true);
//            convertView.setClickable(false);
//            convertView.setFocusable(false);
//            convertView.setFocusableInTouchMode(false);
//            return convertView;
//        }
//
////        @Override
////        public boolean areAllItemsEnabled() {
////            return false;
////        }
////
////        @Override
////        public boolean isEnabled(int position) {
////            return false;
////        }
//    }
}