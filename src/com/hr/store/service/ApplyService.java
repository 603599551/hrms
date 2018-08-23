package com.hr.store.service;

import com.common.service.BaseService;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.utils.UserSessionUtil;
import easy.util.DateTool;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.Date;

public class ApplyService extends BaseService {

    @Before(Tx.class)
    public JsonHashMap deal(String id, String status, String desc, String type, UserSessionUtil usu) {
        JsonHashMap jhm = new JsonHashMap();

        try {
            //判断通知记录是否存在
            Record noticeRecord = Db.findFirst("select * from h_notice where id = ?", id);
            if (noticeRecord == null) {
                jhm.putCode(0).putMessage("此记录不存在");
                return jhm;
            }
            //更改通知类型为已办理
            if (StringUtils.equals(noticeRecord.getStr("status"), "0")) {
                noticeRecord.set("status", "1");
                Db.update("h_notice", noticeRecord);
            }

            Record infoRecord = Db.findFirst("SELECT move.id as id , move.`status` as `status`  FROM h_apply_move move , h_notice n WHERE move.id = ? AND n.type = ?", noticeRecord.getStr("fid"), type);
            //判断通知是否已撤销
            if (StringUtils.equals("1", infoRecord.getStr("status"))) {
                jhm.putCode(0).putMessage("此申请已被撤销");
                return jhm;
            }
            infoRecord.set("status",Integer.valueOf(status)  + 2);
            infoRecord.set("review_result", desc);
            infoRecord.set("reviewer_id", usu.getUserId());
            infoRecord.set("review_time", DateTool.GetDateTime());
            Db.update("h_apply_move", infoRecord);

            jhm.putCode(1).putMessage("操作成功!");

        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage("服务器发生异常");
        }
        return jhm;
    }
}
