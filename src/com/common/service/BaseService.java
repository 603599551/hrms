package com.common.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class BaseService {
    protected SimpleDateFormat sdf_ymd = new SimpleDateFormat("yyyy-MM-dd");
    protected SimpleDateFormat sdf_year = new SimpleDateFormat("yyyy");
    protected static final long ONE_DAY_TIME = 1000 * 60 * 60 *24;

    protected int nextSort(int sort){
        int i=sort;
        while(true){
            i++;
            if(i%10==0){
                break;
            }
        }
        return i;
    }

    protected String nextDay(String date, int nextDay){
        String result = "";
        try {
            Date today = sdf_ymd.parse(date);
            today = new Date(today.getTime() + nextDay * ONE_DAY_TIME);
            result = sdf_ymd.format(today);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

}
