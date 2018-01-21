package com.jsoft.crm.services;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jsoft.crm.utils.UserSessionUtil;
import easy.util.DateTool;
import utils.UUIDTool;

import java.util.ArrayList;
import java.util.List;

public class StudentService {

    static StudentService me=new StudentService();

    public static StudentService getMe() {
        return me;
    }

    /**
     * 修改学员的所有者
     * @param ownerIdArray
     * @param studentIdArray
     * @param usu
     */
    @Before(Tx.class)
    public void modifyOwnerByStudentId(String[] ownerIdArray, String[] studentIdArray, UserSessionUtil usu){

        String dateTime= DateTool.GetDateTime();

        List<Record> newOwnerRList=new ArrayList();
        for(int i=0,size=studentIdArray.length;i<size;i++){
            for(int j=0,sizeJ=ownerIdArray.length;j<sizeJ;j++){
                String ownerId=ownerIdArray[j];
                Record r=new Record();
                r.set("id", UUIDTool.getUUID());
                r.set("student_id",studentIdArray[i]);
                r.set("staff_id",ownerId);
                r.set("creator",usu.getUserId());
                r.set("creator_name",usu.getRealName());
                r.set("create_time",dateTime);

                newOwnerRList.add(r);
            }
        }

        //查询变动学员的原所有者
        List<Record> originalOwnerList=queryStudentOwners(studentIdArray);
        /*
        如果将学员共享给别人，那么传入的所有者中包含原所有者，此时不删除原所有者
        如果将学员移交给别人，那么传入的所有者中不包含原所有者，此时应删除原所有者

        此处查询学生的原所有者，并与学员的新所有者比对，如果有相同的，那么不删除原所有者
        如果没有相同的，删除原所有者
         */
        //将要删除的原作者放入到该list中
        List<Record> deleteOwnerList=new ArrayList();
        //需要添加的新所有者保存到此list中。学员的原所有者和新所有者如果相同，都会从此list中移出
        List<Record> addOwnerList=new ArrayList(newOwnerRList);
        //学员的新所有者和原所有者是同一人的，放在此list中
//        List<Record> existOwnerList=new ArrayList();
        for(Record originaR:originalOwnerList){
            String originaId=originaR.getStr("staff_id");
            String originaName=originaR.getStr("staff_name");
            String studentId=originaR.getStr("student_id");
            boolean has=false;
            for(Record newOwnerR:newOwnerRList){
                String newStaffId=newOwnerR.getStr("staff_id");
                String newStudentId=newOwnerR.getStr("student_id");
                if(originaId.equals(newStaffId) && studentId.equals(newStudentId)){
                    addOwnerList.remove(newOwnerR);
//                    existOwnerList.add(newOwnerR);
                    has=true;
                    break;
                }
            }
            if(has){

            }else{
                deleteOwnerList.add(originaR);
            }
        }
        /*
        如果新添加的所有者，不包含原所有者，那么删除原所有者
         */
        if(!deleteOwnerList.isEmpty()){
            StringBuilder clearSQL=new StringBuilder(" delete from student_owner where staff_id=? and student_id =?");
            for(Record r:deleteOwnerList){
                String staffId=r.getStr("staff_id");
                String studentId=r.getStr("student_id");
                int sqlNum=Db.update(clearSQL.toString(),staffId,studentId);
            }

        }

        List<Record> studentList= getListByStudentId(studentIdArray);
        List<Record> ownerList=getListByStaffId(ownerIdArray);
        //生成消息、日志内容
        String content=buildContent(usu,studentList,ownerList);

        /*
        保存所有者
         */
        for(Record r:addOwnerList){
            Db.save("student_owner",r);
        }
        //给学员新所有者发送通知
        List<String> staffList=new ArrayList();
        for(Record r:newOwnerRList){
            String staff_id=r.get("staff_id");
            if(staffList.contains(staff_id)){
                continue;
            }
            staffList.add(staff_id);
            Record messageR=new Record();
            messageR.set("id",UUIDTool.getUUID());
            messageR.set("staff_id",staff_id);
            messageR.set("content",content.toString());
            messageR.set("create_time",dateTime);
            messageR.set("read","0");
            messageR.set("creator",usu.getUserId());
            messageR.set("creator_name",usu.getRealName());
            Db.save("message",messageR);
        }
        //给学员原所有者发送通知
        for(Record originaR:originalOwnerList){
            String originaId=originaR.getStr("staff_id");
            String originaName=originaR.getStr("staff_name");
            String studentId=originaR.getStr("student_id");
            if(staffList.contains(originaId)){
                continue;
            }
            staffList.add(originaId);

            Record messageR=new Record();
            messageR.set("id",UUIDTool.getUUID());
            messageR.set("staff_id",originaId);
            messageR.set("content",content.toString());
            messageR.set("create_time",dateTime);
            messageR.set("read","0");
            messageR.set("creator",usu.getUserId());
            messageR.set("creator_name",usu.getRealName());
            Db.save("message",messageR);
        }
        //保存日志
        Record logR=new Record();
        logR.set("id",UUIDTool.getUUID());
        logR.set("content",content.toString());
        logR.set("creator",usu.getUserId());
        logR.set("creator_name",usu.getRealName());
        logR.set("create_time",dateTime);
        Db.save("log",logR);

    }

    /**
     * 查询原所有者
     * @param studentIdArray
     * @return
     */
    public List<Record> queryStudentOwners(String[] studentIdArray){
        StringBuilder sql=new StringBuilder("select s.id staff_id,s.name as staff_name,so.student_id from staff s,student_owner so where s.id=so.staff_id and so.student_id in (");
        for(int i=0,size=studentIdArray.length;i<size;i++){
            sql.append(" ? ");
            if(i<(size-1)){
                sql.append(" , ");
            }
        }
        sql.append(" ) ");
        List<Record> list=Db.find(sql.toString(),studentIdArray);
        return list;
    }

    /**
     *
     * @param usu
     * @param studentList
     * @param ownerList
     * @return
     */
    private String buildContent(UserSessionUtil usu,List<Record> studentList,List<Record> ownerList){
        StringBuilder content=new StringBuilder(usu.getRealName()+" 将学员 ");
        for(int i=0,size=studentList.size();i<size;i++){
            Record temp=studentList.get(i);
            String name=temp.getStr("name");
            content.append(name);
            if(i<(size-1)){
                content.append(",");
            }
        }
        content.append(" 分配给 ");

        for(int i=0,size=ownerList.size();i<size;i++){
            String name=ownerList.get(i).getStr("name");
            content.append(name);
            if(i<(size-1)){
                content.append(",");
            }
        }
        return content.toString();
    }
    /**
     * 查询学员的名称，用于添加消息、日志
     * @param studentIdArray
     * @return
     */
    public List<Record> getListByStudentId(String[] studentIdArray){

        StringBuilder selectStudentSQL=new StringBuilder(" select id,name from student where id in (");
//        List selectParaList=new ArrayList();
        for(int i=0,size=studentIdArray.length;i<size;i++){
            selectStudentSQL.append(" ? ");
            if(i<(size-1)){
                selectStudentSQL.append(" , ");
            }
//            selectParaList.add(studentIdArray[i]);
        }
        selectStudentSQL.append(" ) ");
        List<Record> studentList=Db.find(selectStudentSQL.toString(),studentIdArray);
        return studentList;
    }

    /**
     * 查询传入所有者姓名，用于消息、日志
     * @param ownerIdArray
     * @return
     */
    public List<Record> getListByStaffId(String[] ownerIdArray){

        StringBuilder selectOwnerSQL=new StringBuilder("select id,name from staff where id in (");
//        List selectOwnerParaList=new ArrayList();
        for(int i=0,size=ownerIdArray.length;i<size;i++){
            selectOwnerSQL.append(" ? ");
//            selectOwnerParaList.add(ownerIdArray[i]);
            if(i<(size-1)){
                selectOwnerSQL.append(",");
            }
        }
        selectOwnerSQL.append(")");
        List<Record> ownerList=Db.find(selectOwnerSQL.toString(),ownerIdArray);

        return ownerList;
    }
}
