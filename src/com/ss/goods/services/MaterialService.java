package com.ss.goods.services;

import com.jfinal.plugin.activerecord.Db;
import com.ss.services.BaseService;
import com.utils.SQLUtil;

public class MaterialService extends BaseService {
    /**
     * 删除原材料，逻辑删除
     * 配方中的原材料，是真删除
     */
    public void deleteByIds(String[] idArray){
        SQLUtil sqlUtil = new SQLUtil("update material set status=-1 ");
        sqlUtil.in(" id in ", idArray);
        int i = Db.update(sqlUtil.toString(), sqlUtil.getParameterArray());

        SQLUtil sqlUtil12=new SQLUtil("delete from goods_material ");
        sqlUtil12.in (" and material_id in ",idArray);
        int j = Db.update(sqlUtil12.toString(), sqlUtil12.getParameterArray());
    }
}
