package com.companyride.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.companyride.R;
import com.companyride.parameters.AppSharedData;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Created by Ruslan on 13-Jun-16.
 */
public class AboutFragment extends Fragment {
    private Activity parentActivity;
    private View rootView;
    private AppSharedData sharedData = AppSharedData.getInstance();
    private TextView product_eula;
    private LinearLayout product_eula_layout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        parentActivity = getActivity();
        rootView = inflater.inflate(R.layout.page_about, container, false);

        initVariables();
        setEulaLicenceToField();
        setHasOptionsMenu(true);

        return rootView;
    }

    private void initVariables()
    {
        product_eula_layout =  (LinearLayout)rootView.findViewById(R.id.product_eula_layout);
        product_eula = (TextView)rootView.findViewById(R.id.eulaEditText);
        product_eula.setMovementMethod(new ScrollingMovementMethod());
    }

    private void setEulaLicenceToField() {
        try {
            product_eula.setText(getAsset("eula.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String getAsset(String fileName) throws IOException
    {
        AssetManager am = rootView.getContext().getResources().getAssets();
        InputStream is = am.open(fileName, AssetManager.ACCESS_BUFFER);
        return new Scanner(is).useDelimiter("\\Z").next();
    }

}
