package com.ss.goods.controllers;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.json.Json;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.ss.controllers.BaseCtrl;
import com.ss.services.SettingService;
import com.utils.HanyuPinyinHelper;
import com.utils.RequestTool;
import com.utils.SQLUtil;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import easy.util.NumberUtils;
import easy.util.StringUtils;
import easy.util.UUIDTool;
import org.apache.poi.util.StringUtil;
import utils.NextInt;
import utils.bean.JsonHashMap;
import utils.jfinal.DbUtil;
import utils.jfinal.RecordUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GoodsCtrl extends BaseCtrl {
    @Before(Tx.class)
    @Override
    public void add() {
        JsonHashMap jhm=new JsonHashMap();
        String uuid= UUIDTool.getUUID();
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        String datetime= DateTool.GetDateTime();
        try {
            JSONObject jsonObject = RequestTool.getJson(getRequest());
            String code=jsonObject.getString("code");
            String name=jsonObject.getString("name");
            String priceStr=jsonObject.getString("price");
            String wm_type=jsonObject.getString("wm_type");//库存类型
            String attribute_1=jsonObject.getString("attribute_1");
            String attribute_2=jsonObject.getString("attribute_2");
            String unit=jsonObject.getString("unit");
            String sortStr=jsonObject.getString("sort");
            String type_2=jsonObject.getString("type");
            String state=jsonObject.getString("state");
            String desc=jsonObject.getString("desc");


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
            if(org.apache.commons.lang.StringUtils.isEmpty(type_2)){
                jhm.putCode(-1).putMessage("请选择分类！");
                renderJson(jhm);
                return;
            }
            String pinyin= HanyuPinyinHelper.getFirstLettersLo(name);
            double price= 0d;
            try{
                price=NumberUtils.parseDouble(priceStr,0);
            }catch (Exception e){
                jhm.putCode(-1).putMessage("请输入正确的价格");
                renderJson(jhm);
                return;
            }
            if(org.apache.commons.lang.StringUtils.isEmpty(code)){
                code=buildCode(null)+"";
            }else{
                List<Record> list = Db.find("select * from goods where code=? ", code );
                if(list != null && list .size() > 0){
                    jhm.putCode(-1).putMessage("编码不能重复，请重新填写！");
                    renderJson(jhm);
                    return;
                }
            }
            int sort=0;
            if(org.apache.commons.lang.StringUtils.isEmpty(sortStr)){
                int maxSort=DbUtil.queryMax("goods","sort");
                sort=NextInt.nextSortTen(maxSort);
            }else{
                sort=NumberUtils.parseInt(sortStr,1);
            }

            String type_1=Db.queryFirst("select parent_id from goods_type where id=?",type_2);
            Record r=new Record();
            r.set("id",uuid);
            r.set("code",code);
            r.set("name",name);
            r.set("pinyin",pinyin);
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
            r.set("status",state);
            r.set("desc",desc);

            boolean b=Db.save("goods",r);
            if(b){
                jhm.putCode(1).putMessage("保存成功！");
            }else{
                jhm.putCode(-1).putMessage("保存失败！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
            renderJson(jhm);
            throw e;
        }
        renderJson(jhm);

    }

    /**
     * 停用商品
     * 将goods表的status字段改为2
     */
    public void stop() {
        String id=getPara("id");
        String state=getPara("state");
        JsonHashMap jhm=new JsonHashMap();
        if(org.apache.commons.lang.StringUtils.isEmpty(state)){
            jhm.putCode(-1).putMessage("请传状态！");
            renderJson(jhm);
            return;
        }
        try {
            int i = Db.update("update goods set status=? where id=?", state, id);
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
                Map<String,Object> map=r.getColumns();
                Iterator<Map.Entry<String,Object>> it=map.entrySet().iterator();
                while(it.hasNext()){
                    Map.Entry<String,Object> en=it.next();
                    Object value=en.getValue();
                    String valueStr="";
                    if(value!=null){
                        valueStr=String.valueOf(value);
                    }
                    en.setValue(valueStr);
                }
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
        JsonHashMap jhm=new JsonHashMap();
        UserSessionUtil usu=new UserSessionUtil(getRequest());
        String datetime= DateTool.GetDateTime();
        try {
            JSONObject jsonObject = RequestTool.getJson(getRequest());
            String id= jsonObject.getString("id");
//            String code=jsonObject.getString("code");
            String name=jsonObject.getString("name");
            String priceStr=jsonObject.getString("price");
//            String wm_type=jsonObject.getString("wm_type");//库存类型
            String attribute_1=jsonObject.getString("attribute_1");
            String attribute_2=jsonObject.getString("attribute_2");
//            String unit=jsonObject.getString("unit");
            String sortStr=jsonObject.getString("sort");
//            String type_1=jsonObject.getString("type_1");
            String type_2=jsonObject.getString("type");
            String state=jsonObject.getString("state");
            String desc=jsonObject.getString("desc");


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
//            if(org.apache.commons.lang.StringUtils.isEmpty(wm_type)){
//                jhm.putCode(-1).putMessage("请选择库存类型！");
//                renderJson(jhm);
//                return;
//            }
//            if(org.apache.commons.lang.StringUtils.isEmpty(unit)){
//                jhm.putCode(-1).putMessage("请选择单位！");
//                renderJson(jhm);
//                return;
//            }
//            if(org.apache.commons.lang.StringUtils.isEmpty(type_1)){
//                jhm.putCode(-1).putMessage("请选择分类！");
//                renderJson(jhm);
//                return;
//            }
            if(org.apache.commons.lang.StringUtils.isEmpty(type_2)){
                jhm.putCode(-1).putMessage("请选择分类！");
                renderJson(jhm);
                return;
            }
            String pinyin= HanyuPinyinHelper.getFirstLettersLo(name);
            double price= 0;
            try{
                price=NumberUtils.parseDouble(priceStr,0);
            }catch (Exception e){
                jhm.putCode(-1).putMessage("请输入正确的价格！");
                renderJson(jhm);
                return;
            }

            int sort=0;
            if(org.apache.commons.lang.StringUtils.isEmpty(sortStr)){

            }else{
                try{
                    sort=NumberUtils.parseInt(sortStr,1);
                }catch (Exception e){
                    jhm.putCode(-1).putMessage("请输入正确的序号！");
                    renderJson(jhm);
                    return;
                }
            }
            String type_1=Db.queryFirst("select parent_id from goods_type where id=?",type_2);
            Record r=new Record();
            r.set("id",id);
//            r.set("code",code);
            r.set("name",name);
            r.set("pinyin",pinyin);
            r.set("price",price);
//            r.set("wm_type",wm_type);
            r.set("attribute_1",attribute_1);
            r.set("attribute_2",attribute_2);
//            r.set("unit",unit);
            if(org.apache.commons.lang.StringUtils.isEmpty(sortStr)) {
//                r.set("sort", sort);
            }else{
                r.set("sort", sort);
            }
            r.set("type_1",type_1);
            r.set("type_2",type_2);
            r.set("modifier_id",usu.getUserId());
            r.set("modify_time",datetime);
            r.set("status",state);
            r.set("desc",desc);

            boolean b=Db.update("goods",r);
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

    @Override
    public void query() {
        String key=getPara("keyword");
        String type=getPara("type");
        String status=getPara("status");
        String pageNumStr=getPara("pageNum");
        String pageSizeStr=getPara("pageSize");

        int pageNum=NumberUtils.parseInt(pageNumStr,1);
        int pageSize=NumberUtils.parseInt(pageSizeStr,10);

        //当type为0时，表示选择全部，所以将type设置为空字符串
        if("0".equals(type)){
            type="";
        }
        JsonHashMap jhm=new JsonHashMap();
        try {
            SQLUtil sqlUtil = new SQLUtil(" from goods g");
            if(org.apache.commons.lang.StringUtils.isNotEmpty(status)) {
                sqlUtil.addWhere("and status=?", SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING, status);
            }else{
                sqlUtil.in("and status in", new Object[]{0,1});
            }

            StringBuilder sql=sqlUtil.getSelectSQL();
            List paraList=sqlUtil.getParameterList();

            if(org.apache.commons.lang.StringUtils.isNotEmpty(key)) {
                String key2 = key + "%";
                if (paraList != null && !paraList.isEmpty()) {
                    sql.append(" and (code like ? or name like ? or pinyin like ?)");
                } else {
                    sql.append(" where (code like ? or name like ? or pinyin like ?)");

                }
                paraList.add(key2);
                paraList.add(key2);
                paraList.add(key2);
            }
            if(org.apache.commons.lang.StringUtils.isNotEmpty(type)){

                //sqlUtil.addWhere("and type_2=?", SQLUtil.NOT_NULL_AND_NOT_EMPTY_STRING, type);
                if (paraList != null && !paraList.isEmpty()) {
                    sql.append(" and (type_1=? or type_2=? )");
                } else {
                    sql.append(" where (type_1=? or type_2=? )");

                }
                paraList.add(type);
                paraList.add(type);
            }
            sql.append(" order by status desc,sort,create_time desc,id");
            String select="select g.*,(select name from goods_type gt where gt.id=g.type_1) as type_1_text,(select name from goods_type gt where gt.id=g.type_2) as type_2_text,case g.status when 1 then '启用' when 0 then '停用' end as status_text,(select name from wm_type where id=g.wm_type) as wm_type_text,(select name from goods_unit where id=g.unit) as goods_unit_text";
            Page<Record> page = Db.paginate(pageNum, pageSize, select,sql.toString(),paraList.toArray() );
            if(page!=null){
                List<Record> list=page.getList();
                if(list!=null && !list.isEmpty()){
                    for(Record r:list){
                        RecordUtils.obj2str(r);
                    }
                }
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

    /**
     * 显示商品类别树形结构（添加商品表单页面）
     */
    public void showGoodsTypeTree(){
        JsonHashMap jhm = new JsonHashMap();
        List<Record> reList = new ArrayList();
        try {
            List<Record> list = Db.find("select id,parent_id,code,name,sort from goods_type order by sort");
            for (Record r : list) {
                String parent_id = r.get("parent_id");
                if ("0".equals(parent_id)) {
                    r.set("name", "┗ " + r.get("name"));
                    reList.add(r);
                }
            }

            for (int i = 0; i < reList.size(); i++) {
                Record rootR = reList.get(i);
                String id = rootR.get("id");
                int x = 1;
                for (int j = 0; j < list.size(); j++) {
                    Record r = list.get(j);
                    String parent_id = r.getStr("parent_id");
                    if (id.equals(parent_id)) {
                        r.set("name", "　┣ " + r.get("name"));
                        reList.add(i + x, r);
                        x++;
                    }
                }
            }
            //将最后一个节点的开头符号改成┗
            Record r = reList.get(reList.size() - 1);
            String name = r.get("name");
            name = "　┗ " + name.substring(2, name.length());
            r.set("name", name);

            //插入第一个节点
            Record firstR = new Record();
            firstR.set("id", "0");
            firstR.set("name", "请选择商品分类");
            reList.add(0, firstR);
            jhm.putCode(1).put("list",reList);
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

    public void deleteByIds(){
        String[] idArray=getParaValues("ids");
        JsonHashMap jhm=new JsonHashMap();
        try {
            if (idArray==null || idArray.length==0) {
                jhm.putCode(-1).putMessage("请选择要删除的商品！");
                renderJson(jhm);
                return;
            }
            /*
            判断要删除的商品，如果已经设置原材料，就不删除
            SQLUtil sqlUtil2=new SQLUtil("select count(id) as count,goods_id,(select name from goods where id=gm.goods_id) as name from goods_material gm ");
            sqlUtil2.in(" and goods_id in ",idArray);
            sqlUtil2.addWhere("group by goods_id");
            List<Record> list=Db.find(sqlUtil2.toString(),sqlUtil2.getParameterArray());
            if(list!=null && !list.isEmpty()){
                StringBuilder str=new StringBuilder();
                for(Record r:list){
                    Object countObj=r.get("count");
                    int count=NumberUtils.parseInt(countObj,0);
                    String name=r.get("name");
                    if(count>0) {
                        str.append(name);
                        str.append("、");
                    }
                }
                str.delete(str.length()-1,str.length());
                str.append(" 已经设置配方，不能删除！");
                jhm.putCode(-1).putMessage(str.toString());
                renderJson(jhm);
                return;
            }
            */

            SQLUtil sqlUtil = new SQLUtil("update goods set status=-1 ");
            sqlUtil.in(" id in ", idArray);

            int i = Db.update(sqlUtil.toString(), sqlUtil.getParameterArray());
            if (i > 0) {
                jhm.putCode(1).putMessage("删除成功！");
            } else {
                jhm.putCode(-1).putMessage("删除失败！");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }
    private int buildCode(String id){
        String key="goods_code";
        int codeInt=0;

        Object codeObj=Db.queryFirst("select value_int from setting where `key`=?",key);
        codeInt= NumberUtils.parseInt(codeObj,1000)+1;

        if(org.apache.commons.lang.StringUtils.isEmpty(id)){
            String sql="select count(*) from goods where code=? ";

            Object countObj = Db.queryFirst(sql, codeInt );
            int count=Integer.parseInt(countObj.toString());
            while(count>0){
                countObj = Db.queryFirst(sql, codeInt );
                count=Integer.parseInt(countObj.toString());
            }

        }else{
            String sql="select count(*) from goods where id<>? and code=? ";

            Object countObj = Db.queryFirst(sql, id,codeInt );
            int count=Integer.parseInt(countObj.toString());
            while(count>0){
                countObj = Db.queryFirst(sql, id,codeInt );
                count=Integer.parseInt(countObj.toString());
            }
        }

        Db.update("update setting set value_int=? where `key`=?",codeInt,key);
        return codeInt;
    }
}
