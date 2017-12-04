package com.companyride;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import com.companyride.parameters.Params;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Valeriy on 10/27/2015.
 */
public class CompanyRideApp extends Application
{
    private Locale locale = null;

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        if (locale == null || !locale.getLanguage().equals(newConfig.locale.getLanguage()))
        {
            locale = newConfig.locale;
            Params.formDate = new SimpleDateFormat(Params.getLocaleCustomizedDateFormat());
            Params.shortDate= new SimpleDateFormat(Params.getLocaleCustomizedShortDateFormat());
//            getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
        }
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

//        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

//        Configuration config = getBaseContext().getResources().getConfiguration();

//        String lang = settings.getString(getString(R.string.pref_locale), "");
//        if (! "".equals(lang) && ! config.locale.getLanguage().equals(lang))
//        {
//        locale = new Locale(lang);
        locale = Locale.getDefault();
        if (!locale.getLanguage().equals("iw"))
            Locale.setDefault(new Locale("en_US"));
//        Locale.setDefault(locale);
//        config.locale = locale;
//        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
//        }
    }
}
