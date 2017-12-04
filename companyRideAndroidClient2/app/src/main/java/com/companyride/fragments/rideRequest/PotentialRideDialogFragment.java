package com.companyride.fragments.rideRequest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import com.companyride.R;

/**
 * Created by Valeriy on 7/03/2015.
 */
public class PotentialRideDialogFragment extends DialogFragment {
    public static final String DATE_TIME = "DATE_TIME";
    public static final String REQ_ID = "REQ_ID";
    public static final int RESULT_CODE = 1;

    private String dateTimeStr;
    private String reqId;

    private Fragment mParentListener;

    public static final PotentialRideDialogFragment newInstance(Context context, String date, String time, String reqId)
    {
        PotentialRideDialogFragment fragment = new PotentialRideDialogFragment();
        Bundle bundle = new Bundle(2);
        bundle.putSerializable(DATE_TIME,
                context.getString(R.string.date) + ": " + date + "\n" + context.getString(R.string.time) + ": " + time
        );
        bundle.putSerializable(REQ_ID, reqId);
        fragment.setArguments(bundle);
        return fragment ;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mParentListener = getTargetFragment();
        Bundle args = getArguments();
        this.dateTimeStr    = (String) args.get(DATE_TIME);
        this.reqId          = (String) args.get(REQ_ID);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(getString(R.string.chgRideRequestTitle));
        builder.setMessage(getString(R.string.chgRideRequestText) + "\n" + dateTimeStr);
        builder.setPositiveButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PotentialRideDialogFragment.this.getDialog().cancel();
            }
        });
        builder.setNegativeButton(R.string.apply, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = getActivity().getIntent();
                intent.putExtra(REQ_ID, reqId);
                mParentListener.onActivityResult(getTargetRequestCode(), RESULT_CODE, intent);
                dismiss();
            }
        });
        return builder.create();
    }

}