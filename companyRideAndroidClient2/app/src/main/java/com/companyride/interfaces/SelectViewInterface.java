package com.companyride.interfaces;

import android.os.Bundle;

/**
 * Created by Valeriy on 7/8/2015.
 */
public interface SelectViewInterface {
    void selectView(String viewName, Bundle args);
    void selectView(String viewName);
    void goBackView();
}
