package com.ss.goods.services;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.utils.HanyuPinyinHelper;
import easy.util.NumberUtils;
import utils.NextInt;
import utils.bean.JsonHashMap;
import utils.jfinal.DbUtil;

import java.util.List;
import java.util.Map;

public class GoodsMgrService {
    public JsonHashMap saveGoods(Record r, String sortStr, String code,String type_2) {
        JsonHashMap jhm = new JsonHashMap();
        if (org.apache.commons.lang.StringUtils.isEmpty(code)) {
            code = buildCode(null) + "";
        } else {
            List<Record> list = Db.find("select * from goods where code=? ", code);
            if (list != null && list.size() > 0) {
                jhm.putCode(-1).putMessage("编码不能重复，请重新填写！");
                return jhm;
            }
        }
        int sort = 0;
        if (org.apache.commons.lang.StringUtils.isEmpty(sortStr)) {
            int maxSort = DbUtil.queryMax("goods", "sort");
            sort = NextInt.nextSortTen(maxSort);
        } else {
            sort = NumberUtils.parseInt(sortStr, 1);
        }

        String type_1 = Db.queryFirst("select parent_id from goods_type where id=?", type_2);


        r.set("sort", sort);
        r.set("code", code);
        r.set("type_1",type_1);

        boolean b = Db.save("goods", r);
        if(b){
            jhm.putCode(1).putMessage("保存成功！");
        }else{
            jhm.putCode(-1).putMessage("保存失败！");
        }
        return jhm;
    }

    /**
     * 构建goods商品编号
     * @param id
     * @return
     */
    public int buildCode(String id) {
        String key = "goods_code";
        int codeInt = 0;

        Object codeObj = Db.queryFirst("select value_int from setting where `key`=?", key);
        codeInt = NumberUtils.parseInt(codeObj, 1000) + 1;

        if (org.apache.commons.lang.StringUtils.isEmpty(id)) {
            String sql = "select count(*) from goods where code=? ";

            Object countObj = Db.queryFirst(sql, codeInt);
            int count = Integer.parseInt(countObj.toString());
            while (count > 0) {
                countObj = Db.queryFirst(sql, codeInt);
                count = Integer.parseInt(countObj.toString());
            }

        } else {
            String sql = "select count(*) from goods where id<>? and code=? ";

            Object countObj = Db.queryFirst(sql, id, codeInt);
            int count = Integer.parseInt(countObj.toString());
            while (count > 0) {
                countObj = Db.queryFirst(sql, id, codeInt);
                count = Integer.parseInt(countObj.toString());
            }
        }

        Db.update("update setting set value_int=? where `key`=?", codeInt, key);
        return codeInt;
    }
}
