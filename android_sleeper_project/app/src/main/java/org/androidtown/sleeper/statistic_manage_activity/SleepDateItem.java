package org.androidtown.sleeper.statistic_manage_activity;

/**
 * Created by Administrator on 2015-08-19.
 */
@Deprecated
public class SleepDateItem {

    String date;
    String sleep;
    String wake;

    public SleepDateItem(String date, String sleep, String wake) {
        this.date = date;
        this.sleep = sleep;
        this.wake = wake;
    }

    public String getDate() {

        return date;
    }

    public void setDate(String date) {

        this.date = date;
    }

    public String getSleep() {

        return sleep;
    }

    public void setSleep(String sleep) {

        this.sleep = sleep;
    }

    public String getWake() {

        return wake;
    }

    public void setWake(String wake) {

        this.wake = wake;
    }
}
