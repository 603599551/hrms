package com.hr.mobile.storeMgr.controllers;

import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.List;

public class SignCtrl extends BaseCtrl {

    /**
     * 经理端查看签到记录
     */
    public void list() {
        JsonHashMap jhm = new JsonHashMap();

        String id = getPara("id");
        if (StringUtils.isEmpty(id)) {
            jhm.putCode(0).putMessage("请选择经理");
            renderJson(jhm);
            return;
        }
        String date = getPara("date");
        if (StringUtils.isEmpty(date)) {
            jhm.putCode(0).putMessage("请选择日期");
            renderJson(jhm);
            return;
        }

        String selectDept = "SELECT staff.dept_id as dept FROM h_staff staff WHERE staff.id = ?";
        String selectList = "SELECT c.id as sign_id, c.staff_id as staff_id, c.sign_in_time as sign , c.is_deal as status , s.`name` as `name`,UPPER(left(s.pinyin,1)) as intial  FROM h_staff_clock c ,h_staff s WHERE s.id = c.staff_id AND c.`status` = '1' AND c.date = ? AND c.store_id = ?";

        try {
            Record deptId = Db.findFirst(selectDept, id);
            List<Record> staffList = Db.find(selectList, date, deptId.getStr("dept"));
            List<Record> untreatedList = new ArrayList<>();
            List<Record> treatedList = new ArrayList<>();
            for (int i = 0; i < staffList.size(); ++i) {
                if (StringUtils.equals(staffList.get(i).getStr("status"), "0")) {
                    staffList.get(i).getStr("sign").substring(0,5);
                    untreatedList.add(staffList.get(i));
                } else {
                    staffList.get(i).getStr("sign").substring(0,5);
                    treatedList.add(staffList.get(i));
                }
            }
            jhm.put("untreatedList", untreatedList);
            jhm.put("treatedList", treatedList);
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }

        renderJson(jhm);
    }


    /**
     * 处理签到情况
     */
    public void deal() {
        JsonHashMap jhm = new JsonHashMap();

        String signId = getPara("sign_id");
        if (StringUtils.isEmpty(signId)) {
            jhm.putCode(0).putMessage("请选择签到");
            renderJson(jhm);
            return;
        }
        String date = getPara("date");
        if (StringUtils.isEmpty(date)) {
            jhm.putCode(0).putMessage("请选择日期");
            renderJson(jhm);
            return;
        }
        String status = getPara("status");
        if (StringUtils.isEmpty(status)) {
            jhm.putCode(0).putMessage("请选择处理状态");
            renderJson(jhm);
            return;
        }

        try {
            Record record = Db.findById("h_staff_clock", signId);

            if (StringUtils.equals(status, "2")) {
                record.set("is_deal", "2");
            } else {
                record.set("is_deal", "1");
            }

            boolean flag = Db.update("h_staff_clock", record);
            if (flag) {
                jhm.putMessage("修改成功");
            } else {
                jhm.putMessage("修改失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }

        renderJson(jhm);
    }
}
