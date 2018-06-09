package com.store.order.services;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import easy.util.DateTool;
import easy.util.UUIDTool;

public class AttachmentSrv {

    public static final String STORE_SCRAP_TYPE = "store_scrap";

    public void save(String file_path, String res_id, String userId){
        Record attachment = new Record();
        attachment.set("id", UUIDTool.getUUID());
        attachment.set("file_path", file_path);
        attachment.set("res_id", res_id);
        attachment.set("creater_id", userId);
        attachment.set("create_time", DateTool.GetDateTime());
        attachment.set("type", STORE_SCRAP_TYPE);
        Db.save("attachment", attachment);
    }

}
