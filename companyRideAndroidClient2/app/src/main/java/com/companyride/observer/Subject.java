package com.companyride.observer;

/**
 * Created by Ruslan on 09-Oct-15.
 */
public interface Subject {
    public void registerObserver(Observer observer);
    public void removeObserver(Observer observer);
    public void notifyObservers();
}
