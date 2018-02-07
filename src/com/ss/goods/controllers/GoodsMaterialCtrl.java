package com.ss.goods.controllers;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.ss.controllers.BaseCtrl;
import com.ss.services.SettingService;
import com.utils.HanyuPinyinHelper;
import com.utils.RequestTool;
import com.utils.SQLUtil;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.NumberUtils;
import easy.util.UUIDTool;
import utils.NextInt;
import utils.bean.JsonHashMap;
import utils.jfinal.DbUtil;

import java.util.List;

public class GoodsMaterialCtrl extends BaseCtrl {
    @Override
    public void add() {
        JsonHashMap jhm=new JsonHashMap();
        String uuid= UUIDTool.getUUID();
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        String datetime= DateTool.GetDateTime();
        try {
            JSONObject jsonObject = RequestTool.getJson(getRequest());
            String code=jsonObject.getString("goods_id");
            String name=jsonObject.getString("name");
            String priceStr=jsonObject.getString("price");
            String wm_type=jsonObject.getString("wm_type");//库存类型
            String attribute_1=jsonObject.getString("attribute_1");
            String attribute_2=jsonObject.getString("attribute_2");
            String unit=jsonObject.getString("unit");
            String sortStr=jsonObject.getString("sort");
            String type_1=jsonObject.getString("type_1");
            String type_2=jsonObject.getString("type_2");

            String pinyin= HanyuPinyinHelper.getFirstLettersLo(name);
            double price= 0;
            try{
                price=NumberUtils.parseDouble(priceStr,0);
            }catch (Exception e){
                jhm.putCode(-1).putMessage("请输入正确的价格");
                renderJson(jhm);
                return;
            }
            if(org.apache.commons.lang.StringUtils.isEmpty(code)){
                code= ""+SettingService.me.getGoodsNum();
            }
            if(org.apache.commons.lang.StringUtils.isEmpty(name)){
                jhm.putCode(-1).putMessage("请输入商品名称！");
                renderJson(jhm);
                return;
            }
            if(org.apache.commons.lang.StringUtils.isEmpty(priceStr)){
                jhm.putCode(-1).putMessage("请输入商品价格！");
                renderJson(jhm);
                return;
            }
            if(org.apache.commons.lang.StringUtils.isEmpty(wm_type)){
                jhm.putCode(-1).putMessage("请选择库存类型！");
                renderJson(jhm);
                return;
            }
            if(org.apache.commons.lang.StringUtils.isEmpty(unit)){
                jhm.putCode(-1).putMessage("请选择单位！");
                renderJson(jhm);
                return;
            }
            if(org.apache.commons.lang.StringUtils.isEmpty(type_1)){
                jhm.putCode(-1).putMessage("请选择分类！");
                renderJson(jhm);
                return;
            }
            if(org.apache.commons.lang.StringUtils.isEmpty(type_2)){
                jhm.putCode(-1).putMessage("请选择分类！");
                renderJson(jhm);
                return;
            }
            int sort=0;
            if(org.apache.commons.lang.StringUtils.isEmpty(sortStr)){
                int maxSort=DbUtil.queryMax("goods","sort");
                sort=NextInt.nextSortTen(maxSort);
            }
            Record r=new Record();
            r.set("code",code);
            r.set("name",name);
            r.set("price",price);
            r.set("wm_type",wm_type);
            r.set("attribute_1",attribute_1);
            r.set("attribute_2",attribute_2);
            r.set("unit",unit);
            r.set("sort",sort);
            r.set("type_1",type_1);
            r.set("type_2",type_2);
            r.set("creater_id",usu.getUserId());
            r.set("modifier_id",usu.getUserId());
            r.set("create_time",datetime);
            r.set("modify_time",datetime);
            r.set("status",1);

            boolean b=Db.save("goods",r);
            if(b){
                jhm.putCode(1).putMessage("保存成功！");
            }else{
                jhm.putCode(-1).putMessage("保存失败！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);

    }

    /**
     * 停用商品
     * 将goods表的status字段改为2
     */
    public void stop() {
        String id=getPara("id");
        JsonHashMap jhm=new JsonHashMap();
        try {
            int i = Db.update("update goods set status=? where id=?", 2, id);
            if (i == 1) {
                jhm.putCode(1).putMessage("操作成功！");
            } else {
                jhm.putCode(-1).putMessage("操作失败！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());

        }
        renderJson(jhm);
    }

    @Override
    public void showById() {
        String id=getPara("id");
        JsonHashMap jhm=new JsonHashMap();
        try{
            Record r=Db.findById("goods",id);
            if(r!=null){
                jhm.putCode(1).put("data",r);
            }else{
                jhm.putCode(-1).putMessage("操作失败！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());

        }
        renderJson(jhm);
    }

    @Override
    public void updateById() {
        super.updateById();
    }

    @Override
    public void query() {
        String key=getPara("key");
        String type=getPara("type");
        String status=getPara("status");
        String pageNumStr=getPara("pageNum");
        String pageSizeStr=getPara("pageSize");

        int pageNum=NumberUtils.parseInt(pageNumStr,1);
        int pageSize=NumberUtils.parseInt(pageSizeStr,10);

        JsonHashMap jhm=new JsonHashMap();
        try {
            SQLUtil sqlUtil = new SQLUtil(" from goods ");
            sqlUtil.addWhere("and type_2=?", SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING, type);
            sqlUtil.addWhere("and status=?", SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING, status);


            StringBuilder sql=sqlUtil.getSelectSQL();
            List list=sqlUtil.getParameterList();

            if(org.apache.commons.lang.StringUtils.isNotEmpty(key)) {
                String key2 = key + "%";
                if (list != null && !list.isEmpty()) {
                    sql.append(" and (code like ? or name like ? )");
                } else {
                    sql.append(" where (code like ? or name like ? )");

                }
                list.add(key2);
                list.add(key2);
            }
            Page<Record> page = Db.paginate(pageNum, pageSize, "select *",sql.toString(),list.toArray() );
            if(page!=null){
                jhm.putCode(1).put("data",page);
            }else{
                jhm.putCode(-1).putMessage("请重试！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }
}
