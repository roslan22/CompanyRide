package com.companyride.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import com.companyride.R;
import com.companyride.activities.InitialScreen;
import com.companyride.activities.MainScreen;
import com.companyride.fragments.calendar.CalendarLoader;

import java.util.ArrayList;

/**
 * Created by Valeriy on 5/22/2015.
 */
public class ExitConfirmationDialog extends DialogFragment {
    private static final String SHOW_LOGOUT = "SHOW_LOGOUT";
    Boolean showLogout;

    public static ExitConfirmationDialog getInstance(Boolean showLogout){
        ExitConfirmationDialog fragment = new ExitConfirmationDialog();
        Bundle bundle = new Bundle(1);
        bundle.putBoolean(SHOW_LOGOUT, showLogout);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        this.showLogout = (Boolean) args.get(SHOW_LOGOUT);
        args.remove(SHOW_LOGOUT);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.exit_confirmation));
        builder.setNegativeButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.exit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getActivity().getApplicationContext(), getActivity().getClass());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(getString(R.string.exit), true);
                startActivity(intent);
            }
        });
        if(showLogout) {
            builder.setNeutralButton(R.string.logout, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(getActivity().getApplicationContext(), getActivity().getClass());
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(getString(R.string.logout), true);
                    startActivity(intent);
                }
            });
        }
        return builder.create();
    }
}