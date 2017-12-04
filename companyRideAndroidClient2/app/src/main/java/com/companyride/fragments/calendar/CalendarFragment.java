package com.companyride.fragments.calendar;

import java.util.Calendar;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.util.Log;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.companyride.R;
import com.companyride.interfaces.SelectViewInterface;
import com.companyride.utils.UtilityFunctions;

public class CalendarFragment extends Fragment {
    private ViewFlipper container;
    private Boolean flipped = false;

    public Calendar selectedDate;
    public CalendarAdapter adapter;

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;
    private AppCompatActivity parentActivity;
    private View rootView;
    private android.support.v7.app.ActionBar actionBar;
    private Spinner mMonthsSpin;
    private Spinner mYearsSpin;
    private static boolean loaded =false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup groupContainer,
                             Bundle savedInstanceState) {
        parentActivity = (AppCompatActivity) getActivity();
//        Toast.makeText(parentActivity, Thread.currentThread().getStackTrace()[2].getMethodName(), Toast.LENGTH_SHORT).show();
        super.onCreate(savedInstanceState);
        rootView = inflater.inflate(R.layout.page_calendar, groupContainer, false);
        selectedDate = Calendar.getInstance();
        initDate();
        adapter = new CalendarAdapter(parentActivity, selectedDate);

        // Gesture detection
        gestureDetector = new GestureDetector(parentActivity, new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };

        // init days' names (Sun, Mon, ...)
        initDays();

        // Get flipper to switch between months
        container = (ViewFlipper)  rootView.findViewById(R.id.container);

        // Get the frame layout and fill it in the current one
        //LayoutInflater inflater = (LayoutInflater) parentActivity.getSystemService(LAYOUT_INFLATER_SERVICE);
        FrameLayout calendar = (FrameLayout) inflater.inflate(R.layout.calendar_grid, null);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        calendar.setLayoutParams(params);
        container.addView(calendar);

        // Define the onclick listener for calendar item click
        OnItemClickListener onItemClickListener = new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                TextView date = (TextView) v.findViewById(R.id.date);
                if (date != null && !date.getText().equals("")) {
                    Calendar selectedDate = (Calendar) CalendarFragment.this.selectedDate.clone();
                    selectedDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date.getText().toString()));
                    adapter.setSelectedDate(selectedDate);
                    adapter.notifyDataSetChanged();
                }
            }
        };

//        else{
//            // Restore state members from saved instance
//            Toast.makeText(parentActivity, "Restoring state!!!!!!!!!!!!1", Toast.LENGTH_SHORT).show();
//        }

        // Setup first gridview
        GridView gridview = (GridView)  rootView.findViewById(R.id.gridView);
        gridview.setAdapter(adapter);
        gridview.setOnTouchListener(gestureListener);
        gridview.setOnItemClickListener(onItemClickListener);

        // Setup second gridview
        gridview = (GridView)  rootView.findViewById(R.id.gridView2);
        gridview.setAdapter(adapter);
        gridview.setOnTouchListener(gestureListener);
        gridview.setOnItemClickListener(onItemClickListener);

        // customize the action bar
        setHasOptionsMenu(true);
        actionBar = parentActivity.getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        //actionBar.setNavigationMode(android.support.v7.app.ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.action_bar_custom);
        mMonthsSpin = (Spinner) parentActivity.findViewById(R.id.action_bar_spinner_month);
        mYearsSpin = (Spinner) parentActivity.findViewById(R.id.action_bar_spinner_year);

        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(parentActivity, R.array.month_names, R.layout.month_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final CharSequence[] years = UtilityFunctions.getCloseYears();
        ArrayAdapter<CharSequence> yearsAdapter = new ArrayAdapter<CharSequence>(parentActivity, R.layout.month_item, years);
        yearsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //actionBar.setListNavigationCallbacks(adapter, this);
        mMonthsSpin.setAdapter(monthAdapter);
        final int currMonth = UtilityFunctions.getCurrMonth();
        mMonthsSpin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                loaded = false;
                return false;
            }
        });
        mMonthsSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(loaded) return;
                selectedDate.set(Calendar.MONTH, i);
                refreshCalendar();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mYearsSpin.setAdapter(yearsAdapter);
        mYearsSpin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                loaded = false;
                return false;
            }
        });
        mYearsSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(loaded) return;
                selectedDate.set(Calendar.YEAR, Integer.valueOf(years[i].toString()));
//                selectedDate.set(Calendar.YEAR, i);
                refreshCalendar();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        loaded = false;
        refreshCalendar();
    }

    private void goToNextMonth() {
        if(selectedDate.get(Calendar.MONTH)== selectedDate.getActualMaximum(Calendar.MONTH)) {
            selectedDate.set((selectedDate.get(Calendar.YEAR)+1), selectedDate.getActualMinimum(Calendar.MONTH),1);
        } else {
            selectedDate.set(Calendar.MONTH, selectedDate.get(Calendar.MONTH)+1);
        }
        refreshCalendar();
        container.setInAnimation(AnimationUtils.loadAnimation(rootView.getContext(), R.anim.slide_right_in));
        container.setOutAnimation(AnimationUtils.loadAnimation(rootView.getContext(), R.anim.slide_left_out));
        manageTransition();
    }

    private void goToPreviousMonth() {
        if(selectedDate.get(Calendar.MONTH) == selectedDate.getActualMinimum(Calendar.MONTH)) {
            selectedDate.set((selectedDate.get(Calendar.YEAR)-1), selectedDate.getActualMaximum(Calendar.MONTH),1);
        } else {
            selectedDate.set(Calendar.MONTH, selectedDate.get(Calendar.MONTH)-1);
        }
        refreshCalendar();
        container.setInAnimation(AnimationUtils.loadAnimation(parentActivity,R.anim.slide_left_in));
        container.setOutAnimation(AnimationUtils.loadAnimation(parentActivity,R.anim.slide_right_out));
        manageTransition();
    }

    private void manageTransition(){
        if( flipped ){
            container.showPrevious();
            flipped = false;
        }
        else {
            container.showNext();
            flipped = true;
        }
    }

    private void initDays() {
        String[] daysNames = getResources().getStringArray(R.array.week_days);
        LinearLayout daysLayout = (LinearLayout) rootView.findViewById(R.id.days_of_week);
        int itemNum = daysLayout.getChildCount();
        for(int i=0; i<itemNum; i++){
            try {
                TextView dayText = (TextView) daysLayout.getChildAt(i);
                dayText.setText(daysNames[i]);
            }
            catch(Exception ex){
                Log.d("Tag", ex.getMessage());
            }
        }
    }

    private int getYearIndex(int year){
        ArrayAdapter<String> yearsAdapter = (ArrayAdapter<String>) mYearsSpin.getAdapter();
        for(int i=0; i < yearsAdapter.getCount(); i++){
            if (yearsAdapter.getItem(i).equals(Integer.toString(year)))
                return i;
        }
        return -1;
    }

    private void setDateOnActionBarFromSelectedDate(){
        mMonthsSpin = (Spinner) parentActivity.findViewById(R.id.action_bar_spinner_month);
        mYearsSpin = (Spinner) parentActivity.findViewById(R.id.action_bar_spinner_year);
        mYearsSpin.post(new Runnable() {
            @Override
            public void run() {
                mYearsSpin.setSelection(getYearIndex(selectedDate.get(Calendar.YEAR)));
            }
        });
        mMonthsSpin.post(new Runnable() {
            @Override
            public void run() {
                mMonthsSpin.setSelection(selectedDate.get(Calendar.MONTH));
            }
        });
    }

    public void refreshCalendar()
    {
        if (loaded)
            return;
        loaded = true;
        adapter.refreshData();  // refresh month days
        adapter.notifyDataSetChanged();
        setDateOnActionBarFromSelectedDate();
    }


    public void initDate() {
        Time today = new Time(Time.getCurrentTimezone());
        today.setToNow();
        selectedDate.set(today.year, today.month, today.monthDay);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu items for use in the action bar
//        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.calendar_menu, menu);
//        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                parentActivity.onBackPressed();
                break;
            case R.id.goto_user_profile:
                ((SelectViewInterface)parentActivity).selectView(getString(R.string.user_profile));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            loaded = false;
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    goToNextMonth();
                    // left to right swipe
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    goToPreviousMonth();
                }
            } catch (Exception ex) {
                Log.d("Tag", "Failed to slide calendar: " + ex.getMessage());
            }
            return false;
        }
    }

}
