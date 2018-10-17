package com.hr.wxapplet.staff.controller;

import com.common.controllers.BaseCtrl;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.oreilly.servlet.DaemonHttpServlet;
import easy.util.DateTool;
import easy.util.UUIDTool;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import utils.bean.JsonHashMap;

import java.util.*;

public class StaffCtrl extends BaseCtrl {
    /**
     * url:https://ip:port/context/wx/staff/showTrainTypeList
     * 1000.A.获取培训类别（紧急a）
     */
    public void showTrainTypeList() {
        JsonHashMap jhm = new JsonHashMap();
        try {
            //查询出所有的id和对应的parent_id记录
            String sql = " select id, parent_id,name from h_train_type ";
            List<Record> dataList = Db.find(sql);

            /*仿照文章类型的接口写的*/
            List<Map> treeList = new ArrayList();
            buildTree(dataList, treeList);
            List reList = new ArrayList();
            toWeb(treeList, reList);

            //定义往前台回显的集合
            List<Map> typeList = new ArrayList();
            //定义sum  用来计数
            int sum = 0;
            //定义level  与集合中的level作比较
            int level1 = 0;
            //定义一个集合用来放sum
            List<Integer> sumList = new ArrayList<>();
            //遍历每一个产品项
            for (int i = 0, size = reList.size(); i < size; i++) {
                //把reList的每一条记录放在map中
                Map m = (Map) reList.get(i);
                System.out.println(reList.get(i));
                // 取出集合的level的值，判断他的level值，如果level值大，计数
                Integer value = (Integer) m.get("level");
                //如果level为0，则是一级节点，直接将记录放入集合中
                if (0 == value) {
                    typeList.add(m);
                    sumList.add(sum);
                    sum = 0;
                    level1 = value;
                }
                if (level1 < value) {
                    level1 = value;
                    sum = 0;
                    sum++;
                } else if (level1 == value) {
                    sum++;
                }
                //如果是集合的最后一个，直接将sum放到sumList中
                if (i == size - 1) {
                    sumList.add(sum);
                }
            }
            //集合的第一条的参数为0，直接移除掉
            sumList.remove(0);
            //遍历typeList
            for (int j = 0, size = typeList.size(); j < size; j++) {
                //将无关的参数移除掉，将sum的值添加进去
                Map map = (Map) typeList.get(j);
                map.remove("children");
                map.remove("level");
                map.remove("parent_id");
                map.put("sum", sumList.get(j));
            }
            jhm.put("list", typeList);

        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
//        renderJson("{\"code\":1,\"list\":[{\"id\":\"abc\",\"name\":\"产品培训\",\"sum\":20},{\"id\":\"abc\",\"name\":\"产品培训\",\"sum\":20}]}");
    }


    private void buildTree(List<Record> list, List<Map> treeList) {
        if (list.isEmpty()) {
            return;
        }
        //如果为空，取出顶级节点
        if (treeList.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                Record r = list.get(i);
                String parentId = r.get("parent_id");
                if ("-1".equalsIgnoreCase(parentId)) {
                    treeList.add(r.getColumns());
                    list.remove(r);
                    i--;
                }
            }
            buildTree(list, treeList);
        } else {
            //取出下面的节点
            for (int j = 0; j < treeList.size(); j++) {
                Map treeMap = treeList.get(j);
                String id = (String) treeMap.get("id");
                List childrenList = (List) treeMap.get("children");
                if (childrenList == null) {
                    childrenList = new ArrayList();
                    treeMap.put("children", childrenList);
                }
                for (int i = 0; i < list.size(); i++) {
                    Record r = list.get(i);
                    String parentId = r.get("parent_id");
                    if (id.equalsIgnoreCase(parentId)) {
                        childrenList.add(r.getColumns());
                        list.remove(r);
                        i--;
                    }
                }
                if (!childrenList.isEmpty()) {
                    buildTree(list, childrenList);
                }
            }
        }

    }

    private int level = 0;

    private void toWeb(List<Map> treeList, List reList) {

        for (int j = 0, size = treeList.size(); j < size; j++) {
            Map map = (Map) treeList.get(j);
            List<Map> children = (List) map.get("children");
            if (level == 0) {
                map.put("level", level);
            } else {
                String nameTemp = "";
                for (int i = 0; i < level; i++) {
                    nameTemp = "　" + nameTemp;
                }
                if (j == size - 1) {
                    map.put("level", level);
                } else {
                    map.put("level", level);
                }
            }
            Map node = new HashMap();
            node.putAll(map);
            node.remove("children");
            reList.add(node);
            level++;
            if (children != null) {
                toWeb(children, reList);
            }
            level--;
        }
    }

    public static String md5(String text, String key) throws Exception {
        //加密后的字符串
        String encodeStr= DigestUtils.md5Hex(text + key);
        return encodeStr;
    }


    /**
     * url:https://ip:port/context/wx/staff/queryTrainGoods
     * 1001.A.查询类别下的产品列表及详情
     */
    public void queryTrainGoods() {
        JsonHashMap jhm=new JsonHashMap();
        //类别id
        String id=getPara("id");
        //类别名
        String name=getPara("name");
        //员工id
        String staffId=getPara("staffId");

        if (StringUtils.isEmpty(id)){
            jhm.putCode(0).putMessage("类别id不能为空!");
            renderJson(jhm);
            return;
        }
        if (StringUtils.isEmpty(name)){
            jhm.putCode(0).putMessage("类别名不能为空!");
            renderJson(jhm);
            return;
        }
//        if (StringUtils.isEmpty(staffId)){
//            jhm.putCode(0).putMessage("员工id不能为空!");
//            renderJson(jhm);
//            return;
//        }

        //单表查询h_train_article
        String sql1="SELECT ta.id,ta.type_2,ta.title AS name,ta.video,ta.pdf_path,ta.pdf_org_name FROM h_train_article ta WHERE ta.type_1=?";
        //根据员工id和培训id查询是否考核和考核状态
        String sql2="SELECT result FROM h_exam WHERE staff_id=? AND kind_id=(SELECT value FROM h_dictionary WHERE name=(SELECT name FROM h_train_type WHERE id=?)) ORDER BY create_time DESC LIMIT 1";

        try{
            List<Record> trainList=Db.find(sql1,id);
            if (trainList==null){
                jhm.putCode(0).putMessage("培训内容为空！");
                renderJson(jhm);
                return;
            }
            for (Record train:trainList){
                //将多个视频/pdf地址以逗号为分隔符拆分开来
                String []videos=train.getStr("video").split(",");
                String []pdfs=train.getStr("pdf_path").split(",");
                String []pdfsName=train.getStr("pdf_org_name").split(",");
                int vLen=videos.length;
                int pLen=pdfs.length;
                train.set("videoSum",vLen);
                train.set("fileSum",pLen);

                Record detail=new Record();
                List<Record> videoList=new ArrayList<>();
                List<Record> pdfList=new ArrayList<>();
                for (int i=0;i<vLen;i++){
                    Record video=new Record();
                    String videoName="培训视频"+i+1;
                    video.set("title",videoName);
                    video.set("url",videos[i]);
                    //这里的id=培训记录id+videoName
                    video.set("id",md5(train.getStr("id"),videoName));
                    videoList.add(video);
                }

                for (int j=0;j<pLen;j++){
                    Record pdf=new Record();
                    pdf.set("title",pdfsName[j]);
                    pdf.set("url",pdfs[j]);
                    //这里的id=培训记录id+pdfName
                    pdf.set("id",md5(train.getStr("id"),pdfsName[j]));
                    pdfList.add(pdf);
                }
                detail.set("video",videoList);
                detail.set("file",pdfList);
                train.set("detail",detail);
                train.remove("video");
                train.remove("pdf_path");
                train.remove("pdf_org_name");
                if (StringUtils.equals(name,"岗位培训")){
                    Record r=Db.findFirst(sql2,staffId,train.getStr("type_2"));
                    if (r==null){
                        train.set("status","2");
                        train.set("status_text","未审核");
                    }else {
                        String result=r.getStr("result");
                        if (StringUtils.isEmpty(result)){
                            train.set("status","1");
                            train.set("status_text","待审核");
                        }else {
                            if (StringUtils.equals(result,"1")){
                                train.set("status","1");
                                train.set("status_text","已通过");
                            }else if (StringUtils.equals(result,"0")){
                                train.set("status","2");
                                train.set("status_text","未审核");
                            }
                        }
                    }
                }
                train.remove("type_2");
            }
            jhm.put("list",trainList);
            if (!StringUtils.equals(name,"岗位培训")){
                jhm.put("isGoods","0");
            }else {
                jhm.put("isGoods","1");
            }
        }catch (Exception e){
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
//        renderJson("{\"code\":1,\"list\":[{\"id\":\"123\",\"name\":\"口水鸡\",\"videoSum\":5,\"fileSum\":5,\"status\":\"0\",\"status_text\":\"已通过\",\"detail\":{\"video\":[{\"title\":\"培训视频1\",\"id\":\"abc4\",\"url\":\"http://7o50kb.com2.z0.glb.qiniucdn.com/c1.1.mp4\"},{\"title\":\"培训视频2\",\"id\":\"abc3\",\"url\":\"http://7o50kb.com2.z0.glb.qiniucdn.com/c1.1.mp4\"}],\"file\":[{\"title\":\"培训文档1\",\"id\":\"abc2\",\"url\":\"http://192.168.1.117:8080/hrms/upload/pdf/52c5c23fa98c448db5544d4f11e281ad.pdf\"},{\"title\":\"培训文档2\",\"id\":\"abc1\",\"url\":\"http://192.168.1.117:8080/hrms/upload/pdf/52c5c23fa98c448db5544d4f11e281ad.pdf\"}]}}]}");
    }

    /**
     * url:https://ip:port/context/wx/staff/applyCheck
     * 1003.A.申请考核
     */
    public void applyCheck() {
        JsonHashMap jhm = new JsonHashMap();
        String userId = getPara("userId");
        String typeId = getPara("typeId");
        if (StringUtils.isEmpty(userId)) {
            jhm.putCode(0).putMessage("员工id不能为空！");
            renderJson(jhm);
            return;
        }
        if (StringUtils.isEmpty(typeId)) {
            jhm.putCode(0).putMessage("岗位id不能为空！");
            renderJson(jhm);
            return;
        }

        String kindSql = "SELECT name FROM h_dictionary WHERE value=?";
        String receiverSql = "SELECT id FROM h_staff WHERE dept_id=(SELECT dept_id FROM h_staff WHERE id=?) AND job='store_manager'";
        String time = DateTool.GetDateTime();
        try {
            Record notice = new Record();
            notice.set("id", UUIDTool.getUUID());
            //title----申请考核的岗位名
            notice.set("title", Db.findFirst(kindSql, typeId).getStr("name"));
            notice.set("sender_id", userId);
            notice.set("receiver_id", Db.findFirst(receiverSql, userId).getStr("id"));
            notice.set("create_time", time);
            notice.set("modify_time", time);
            notice.set("status", "0");
            notice.set("type", "examine");

            boolean flag = Db.save("h_notice", notice);
            if (flag) {
                jhm.putCode(1).putMessage("申请成功！");
            } else {
                jhm.putCode(0).putMessage("申请失败！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
//        renderJson("{\"code\":1,\"message\":\"申请成功！\"}");
    }

    /**
     * url:https://ip:port/context/wx/staff/queryNotice
     * 1004.B.消息查询（最新50条）
     */
    public void queryNotice() {
        JsonHashMap jhm = new JsonHashMap();
        String id = getPara("userId");
        if (StringUtils.isEmpty(id)) {
            jhm.putCode(0).putMessage("员工id不能为空!");
            renderJson(jhm);
            return;
        }

        String sql = "SELECT n.title AS job,n.content AS address,n.create_time AS `time`,n.`status` FROM h_notice n WHERE receiver_id=? AND `type`='examine'  ORDER BY n.create_time DESC LIMIT 50";

        try {
            List<Record> list = Db.find(sql, id);
            if (list != null) {
                for (Record r : list) {
                    String status = r.getStr("status");
                    if (StringUtils.equals(status, "3")) {
                        r.set("status_text", "已通过");
                    } else if (StringUtils.equals(status, "1")) {
                        r.set("status_text", "已同意");
                    } else if (StringUtils.equals(status, "2")) {
                        r.set("status_text", "被拒绝");
                    } else if (StringUtils.equals(status, "4")) {
                        r.set("status_text", "未通过");
                    }
                }
            }
            jhm.putCode(1);
            jhm.put("list", list);
        } catch (Exception e) {
            e.printStackTrace();
            jhm.putCode(-1).putMessage(e.toString());
        }
        renderJson(jhm);
    }

}
