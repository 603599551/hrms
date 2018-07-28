package com.hr.store.service;

import com.common.service.BaseService;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.utils.SelectUtil;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.UUIDTool;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StoreMgrSrv extends BaseService {

    /**
     * pc端，hr或店长操作，让员工离职
     * 修改staff表的状态
     * 在h_staff_log表增加员工调动记录
     *
     * @param map
     * @return
     */
    @Before(Tx.class)
    public JsonHashMap fire(Map map){
        String staffId=(String)map.get("staffId");
        String date=(String)map.get("date");
        String desc=(String)map.get("desc");
        UserSessionUtil usu=(UserSessionUtil)map.get("usu");

        JsonHashMap jhm=new JsonHashMap();
        if(StringUtils.isBlank(staffId)){
            jhm.putCode(0).putMessage("请选择离职员工！");
            return  jhm;
        }

        if(StringUtils.isBlank(desc)){
            desc="PC端操作，让员工离职";
        }

        String[] array=staffId.split(",");
        SelectUtil selectUtil=new SelectUtil("select * from staff ");
        selectUtil.in(" id in ",array);

        List<Record> list= Db.find(selectUtil.toString(),selectUtil.getParameters());
        List<Record> staffLogList=new ArrayList<>(list.size());
        Object[][] updateDataArray=new Object[list.size()][2];

        String datetime= DateTool.GetDateTime();
        int i=0;
        for(Record staffR:list){
            String staffRId=staffR.getStr("id");

            updateDataArray[i][0]="530";//离职状态
            updateDataArray[i][1]=staffRId;

            String staffLogId= UUIDTool.getUUID();
            Record staffLog=new Record();
            staffLog.setColumns(staffR);
            staffLog.set("id",staffLogId);
            staffLog.set("staff_id",staffRId);
            staffLog.set("operater_id",usu.getUserId());
            staffLog.set("input_time",date);
            staffLog.set("operate_time",datetime);
            staffLog.set("operate_type","fire");
            staffLog.set("desc",desc);
            staffLogList.add(staffLog);

            i++;
        }
        Db.batch("update h_staff set status=? where id=?",updateDataArray,10);
        Db.batchSave("h_staff_log",staffLogList,10);

        jhm.putCode(1).putMessage("操作成功！");
        return jhm;
    }
}
