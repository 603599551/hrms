package com.quartz;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.IPlugin;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class QuartzPlugin implements IPlugin {
    private List<JobBean> jobs = new ArrayList<JobBean>();
    private SchedulerFactory sf;
    private static Scheduler scheduler;
    private String jobConfig;
    private String confConfig;
    private Map<String, String> jobProp;

    public QuartzPlugin(String jobConfig, String confConfig) {
        this.jobConfig = jobConfig;
        this.confConfig = confConfig;
    }

    public QuartzPlugin(String jobConfig) {
        this.jobConfig = jobConfig;
        this.confConfig = "/quartz_config.properties";
    }

    public QuartzPlugin() {
        this.jobConfig = "/quartz_job.properties";
        this.confConfig = "/quartz_config.properties";
    }

    public static void addJob(JobBean job) {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(job.getJobDesc(), job.getJobGroup());
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            // 不存在，创建一个
            if (null == trigger) {
                Class<Job> j2 = (Class<Job>) Class.forName(job.getJobClass());
                JobDetail jobDetail = JobBuilder.newJob(j2).withIdentity(job.getJobDesc(), job.getJobGroup()).build();
                jobDetail.getJobDataMap().put("scheduleJob", job);

                // 表达式调度构建器
                CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());

                // 按新的cronExpression表达式构建一个新的trigger
                trigger = TriggerBuilder.newTrigger().withIdentity(job.getJobDesc(), job.getJobGroup())
                        .withSchedule(scheduleBuilder).build();
                try {
                    scheduler.scheduleJob(jobDetail, trigger);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // Trigger已存在，那么更新相应的定时设置
                // 表达式调度构建器
                CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());

                // 按新的cronExpression表达式重新构建trigger
                trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();

                // 按新的trigger重新设置job执行
                scheduler.rescheduleJob(triggerKey, trigger);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean start() {
        try {
            loadJobsFromProperties();
        } catch (Exception e) {
            e.printStackTrace();
        }
        startJobs();
        return true;
    }

    private void startJobs() {
        try {
            if (StrKit.notBlank(confConfig)) {
                sf = new StdSchedulerFactory(confConfig);
            } else {
                sf = new StdSchedulerFactory();
            }
            scheduler = sf.getScheduler();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        for (JobBean entry : jobs) {
            addJob(entry);
        }
        try {
            scheduler.start();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    private void loadJobsFromProperties() throws Exception {
        if (StrKit.isBlank(jobConfig)) {
            return;
        }
        Properties props = new Properties();
        try {
            InputStream is=QuartzPlugin.class.getClassLoader().getResourceAsStream(jobConfig);
            InputStreamReader isr=new InputStreamReader(is, "GBK");
            props.load(isr);
            Iterator<Map.Entry<Object,Object>> it=props.entrySet().iterator();
            jobProp=new HashMap();
            while(it.hasNext()){
                Map.Entry<Object,Object> en=it.next();
                jobProp.put((String)en.getKey(),(String)en.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String jobArray = jobProp.get("jobArray");
        if (StrKit.isBlank(jobArray)) {
            return;
        }
        String[] jobArrayList = jobArray.split(",");
        for (String jobName : jobArrayList) {
            jobs.add(createJobBean(jobName));
        }
    }

    private JobBean createJobBean(String key) {
        JobBean job = new JobBean();
        job.setJobClass(jobProp.get(key + ".job"));
        job.setCronExpression(jobProp.get(key + ".cron"));
        job.setJobGroup(jobProp.get(key));
        job.setJobDesc(jobProp.get(key + ".desc"));
        return job;
    }

    @Override
    public boolean stop() {
        try {
            scheduler.shutdown();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static void main(String[] args) {
        Properties props = new Properties();
        try {
            props.load(QuartzPlugin.class.getClassLoader().getResourceAsStream("quartz_job.properties"));
            String jobArray=props.getProperty("jobArray");
            System.out.println(jobArray);
        } catch (IOException e) {
            e.printStackTrace();
        }

        File file=new File("quartz_job.properties");
        System.out.println(file.exists());
    }
}
