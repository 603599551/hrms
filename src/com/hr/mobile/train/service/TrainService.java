package com.hr.mobile.train.service;

import com.common.service.BaseService;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import easy.util.DateTool;
import easy.util.UUIDTool;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.Map;

public class TrainService extends BaseService {


    /**
     * 岗位名字中文转英文
     */
    public String translate(String str) {
        String sql = "SELECT value FROM h_dictionary WHERE name=?";
        Record r = Db.findFirst(sql, str);
        if (r == null) {
            return null;
        }
        return r.getStr("value");
    }

    /*
    增加事务
     */
    @Before(Tx.class)
    public JsonHashMap finish(Map paraMap) {
        JsonHashMap jhm = new JsonHashMap();
        String staff_id = (String) paraMap.get("staff_id");
        String type_id = (String) paraMap.get("type_id");
        String flag = (String) paraMap.get("flag");

        try {
            String search = "select count(*) as c from h_staff where id = ? ";
            Record countR = Db.findFirst(search, staff_id);
            if (countR.getInt("c") != 0) {
                //寻找产品培训的id
                Record proudctIdRecord = Db.findFirst("SELECT id as id FROM h_train_type where `name` = '产品培训' AND parent_id = '-1'");
                //查train_type表找到name为“岗位培训”对应的id
                String sql1 = "SELECT id FROM h_train_type WHERE name='岗位培训'";
                Record r1 = Db.findFirst(sql1);
                if (r1 == null) {
                    jhm.putCode(0).putMessage("一级列表“岗位培训”不存在！");
                    return jhm;
                }
                String parentId = r1.getStr("id");

                //根据二级培训id type_id查询其parent_id
                String sql2 = "SELECT parent_id FROM h_train_type WHERE id=?";
                Record r2 = Db.findFirst(sql2, type_id);
                if (r2 == null) {
                    jhm.putCode(0).putMessage("该二级培训id不存在parent_id");
                    return jhm;
                }
                String typeParentId = r2.getStr("parent_id");

//                //用于判断是否为产品培训的验证 如果是产品培训 那么还存在上一级分类 其parent_id 不为-1
//                Record isProduct = Db.findFirst("SELECT parent_id FROM h_train_type WHERE id=?",typeParentId);

                Record newR = new Record();
                newR.set("id", UUIDTool.getUUID());
                newR.set("staff_id", staff_id);
                newR.set("type_1", typeParentId);
                newR.set("type_2", type_id);
                newR.set("create_time", DateTool.GetDateTime());

                if(StringUtils.equals(flag,"product")){
                    //为产品培训
                    Record productRecord = new Record();
                    productRecord.set("id", UUIDTool.getUUID());
                    productRecord.set("staff_id", staff_id);
                    productRecord.set("type_1", proudctIdRecord.getStr("id"));
                    productRecord.set("type_2", typeParentId);
                    productRecord.set("type_3", type_id);
                    productRecord.set("create_time", DateTool.GetDateTime());
                    productRecord.set("status","1");
                    productRecord.set("article_id","");
                    Db.save("h_staff_train", productRecord);
                    jhm.putCode(1).putMessage("培训完成！");
                    //非岗位培训
                }else if (!parentId.equals(typeParentId)) {
                    newR.set("status", "1");
                    Db.save("h_staff_train", newR);
                    jhm.putCode(1).putMessage("培训完成！");
                } else {
                    //岗位培训
                    //根据train_type的二级培训id查找name
                    String sql3 = "SELECT name FROM h_train_type WHERE id=?";
                    Record r3 = Db.findFirst(sql3, type_id);
                    if (r3 == null) {
                        jhm.putCode(0).putMessage("该二级培训id不存在name!");
                        return jhm;
                    }
                    String chineseName = r3.getStr("name");
                    //中文->英文
                    String englishName = translate(chineseName);

                    //根据train_type的name和staffId 查询h_exam表的staff_id和kind_id
                    String sql4 = "SELECT result FROM h_exam WHERE staff_id=? AND kind_id=?";
                    Record result = Db.findFirst(sql4, staff_id, englishName);
                    //若不存在考核记录说明未申请考核
                    if (result == null || "1".equals(result.getStr("result"))) {
                        Record find = Db.findFirst("SELECT * FROM h_staff_train WHERE type_2=? AND staff_id=? ", type_id, staff_id);
                        if (find == null) {
                            newR.set("status", "2");
                            Db.save("h_staff_train", newR);
                        } else {
                            Db.update("UPDATE h_staff_train SET status='2' WHERE type_2=? AND staff_id=? ", type_id, staff_id);
                        }
                        jhm.putCode(1).putMessage("请申请考核！");
                        return jhm;
                    }
                }
            } else {
                jhm.putCode(0).putMessage("员工不存在！");
            }
        } catch (Exception e) {
            jhm.putCode(-1).putMessage("服务器发生异常！");
        }
        return jhm;
    }
}
