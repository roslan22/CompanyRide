package com.companyride.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import com.companyride.parameters.Params;
import com.companyride.utils.UtilityFunctions;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener
{
    TextView textField;

    public void setTextField( TextView textField)
    {
        this.textField = textField;
    }

//    public Dialog newInstance(int hour, int minute){
//
//    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        Date currTime = UtilityFunctions.stringToDate(textField.getText().toString(), Params.formatTime);
        if (currTime != null) c.setTime(currTime);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        Log.d("TAG", "Inside create dialog!");
        return new TimePickerDialog(getActivity(),this, hour,minute, true);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute)
    {
        populateSetHour(hourOfDay,minute);
    }

    public void populateSetHour(int hourOfDay, int minute)
    {
        //textField.setText(hourOfDay + ":" + minute);
        textField.setText(String.format("%02d:%02d", hourOfDay, minute));
        textField.invalidate();
    }


}
