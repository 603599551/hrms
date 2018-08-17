package com.hr.mobile.train.service;

import com.common.service.BaseService;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import easy.util.DateTool;
import easy.util.StringUtils;
import easy.util.UUIDTool;
import utils.bean.JsonHashMap;

import java.util.Map;

public class TrainService extends BaseService {
    /**
     * 岗位名字中文转英文
     */
    public String translate(String str){
        String sql="SELECT value FROM h_dictionary WHERE name=?";
        Record r=Db.findFirst(sql,str);
        if (r==null){
            return null;
        }
        return r.getStr("value");
    }

    /*
    增加事务
     */
    @Before(Tx.class)
    public JsonHashMap finish(Map paraMap){
        JsonHashMap jhm = new JsonHashMap();
        String staff_id =(String)paraMap.get("staff_id");
        String type_id = (String)paraMap.get("type_id");

        try {
            String search = "select count(*) as c from h_staff where id = ? ";
            Record countR = Db.findFirst(search, staff_id);
            if(countR.getInt("c") != 0){
                String repeatSearch = "select status from h_staff_train where staff_id = ? and type_2 = ? ";
                Record repeatR = Db.findFirst(repeatSearch, staff_id, type_id);
                if (repeatR==null){
                    jhm.putCode(0).putMessage("培训记录为空！");
                    return jhm;
                }

                if(repeatR.getStr("status").equals("1")){
                    jhm.putCode(1).putMessage("培训已完成！");
                } else {
                    //查train_type表找到name为“岗位培训”对应的id
                    String sql1="SELECT id FROM h_train_type WHERE name='岗位培训'";
                    Record r1=Db.findFirst(sql1);
                    if (r1==null){
                        jhm.putCode(0).putMessage("一级列表“岗位培训”不存在！");
                        return jhm;
                    }
                    String parentId=r1.getStr("id");

                    //根据二级培训id type_id查询其parent_id
                    String sql2="SELECT parent_id FROM h_train_type WHERE id=?";
                    Record r2=Db.findFirst(sql2,type_id);
                    if (r2==null){
                        jhm.putCode(0).putMessage("该二级培训id不存在parent_id");
                        return jhm;
                    }
                    String typeParentId=r2.getStr("parent_id");
                    //非岗位培训
                    if (!parentId.equals(typeParentId)){
                        Db.update("UPDATE h_staff_train SET status=? WHERE staff_id=? AND type_2=?","1",staff_id,type_id);
                        jhm.putCode(1).putMessage("培训完成！");
                    }else {
                        //岗位培训
                        //根据train_type的二级培训id查找name
                        String sql3="SELECT name FROM h_train_type WHERE id=?";
                        Record r3=Db.findFirst(sql3,type_id);
                        if (r3==null){
                            jhm.putCode(0).putMessage("该二级培训id不存在name!");
                            return jhm;
                        }
                        String chineseName=r3.getStr("name");
                        //中文->英文
                        String englishName=translate(chineseName);

                        //根据train_type的name和staffId 查询h_exam表的staff_id和kind_id
                        String sql4="SELECT result FROM h_exam WHERE staff_id=? AND kind_id=?";
                        Record result=Db.findFirst(sql4,staff_id,englishName);
                        String sql5="UPDATE h_staff_train SET status=? WHERE staff_id=? AND type_2=?";
                        //若不存在考核记录说明未申请考核
                        if (result==null||"1".equals(result.getStr("result"))){
                            Db.update(sql5,"2",staff_id,type_id);
                            jhm.putCode(1).putMessage("请申请考核！");
                            return jhm;
                        }
                    }

                }
            } else {
                jhm.putCode(0).putMessage("员工不存在！");
            }
        } catch (Exception e){
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        return jhm;
    }
}
