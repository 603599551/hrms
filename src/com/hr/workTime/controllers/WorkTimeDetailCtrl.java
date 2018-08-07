package com.hr.workTime.controllers;

import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import java.util.List;

public class WorkTimeDetailCtrl extends BaseCtrl {

    public void getWorkTimeDetail(){

        String sql = "select wt.date date, wt.start from h_work_time_detail wt";

    }

}
