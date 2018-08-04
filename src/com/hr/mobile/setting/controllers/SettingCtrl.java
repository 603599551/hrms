package com.hr.mobile.setting.controllers;

import com.common.controllers.BaseCtrl;
import com.hr.mobile.setting.controllers.service.SettingCtrlSrv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.apache.commons.lang.StringUtils;
import java.util.HashMap;
import utils.bean.JsonHashMap;
import java.util.Map;

public class SettingCtrl extends BaseCtrl {
    /**
     * 员工端申请离职
     */
    public void modifyPhone() {
        JsonHashMap jhm = new JsonHashMap();
        String staffId = getPara("staffid");
        String stepId = getPara("stepid");
        String reason = getPara("reason");

        if (StringUtils.isEmpty(staffId) || StringUtils.isEmpty(stepId)) {
            jhm.putCode(0).putMessage("员工不存在！");
            renderJson(jhm);
            return;
        }
        if (StringUtils.isEmpty(reason) || StringUtils.isBlank(reason)) {
            jhm.putCode(0).putMessage("请填写离职原因！");
            renderJson(jhm);
            return;
        } else if (reason.length() > 50) {
            //输入不能超过50个字
            jhm.putCode(0).putMessage("离职原因不能超过50个字！");
            renderJson(jhm);
            return;
        }
        try {
            //检查是否已经提交过离职申请
            String sql = "select count(*) c from h_notice n where n.sender_id=? and n.type='3' ";
            Record record = Db.findFirst(sql, staffId);
            if (record.getInt("c") != 0) {
                jhm.putCode(0).putMessage("您已经提交过离职申请！");
                renderJson(jhm);
                return;
            }
            Map paraMap = new HashMap();
            paraMap.put("staffid", staffId);
            paraMap.put("stepid", stepId);
            paraMap.put("reason", reason);
            SettingCtrlSrv srv = enhance(SettingCtrlSrv.class);
            jhm = srv.modifyPhone(paraMap);
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        renderJson(jhm);

    }
}
