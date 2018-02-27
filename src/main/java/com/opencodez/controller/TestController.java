package com.opencodez.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.opencodez.configuration.ConfigureQuartz;
import com.opencodez.quartz.jobs.DynamicJob;
import com.opencodez.util.AppUtil;
import com.opencodez.util.PropertiesUtils;

@RestController
public class TestController {
	
	@Value("${con.key2}")
	String conKey2;
	
	@Autowired
	private SchedulerFactoryBean schedFactory;
	
	@RequestMapping("/getval")
    public String getVal(@RequestParam(value="key", defaultValue="World") String key) {
		Map<String, String> mapOfKeyValue = new HashMap<String, String>();
		mapOfKeyValue.put(key, PropertiesUtils.getProperty(key));
		mapOfKeyValue.put("con.key2", conKey2);
		return AppUtil.getBeanToJsonString(mapOfKeyValue);
    }
	
	@RequestMapping("/schedule/{id}")
	public String schedule(@PathVariable Integer id) {
		String scheduled = "Job is Scheduled!!";
		id = id==null?0:id;
		try {
			final long ONE_MINUTE_IN_MILLIS=60000;//millisecs

			Calendar date = Calendar.getInstance();
			long t= date.getTimeInMillis();
			Date afterAddingTenMins=new Date(t + (1 * ONE_MINUTE_IN_MILLIS));

			JobDetailFactoryBean jdfb = ConfigureQuartz.createJobDetail(DynamicJob.class);
			jdfb.setBeanName("dynamicJobBean"+id);
			jdfb.afterPropertiesSet();
			
			//SimpleTriggerFactoryBean stfb = ConfigureQuartz.createTrigger(jdfb.getObject(),5000L);

			SimpleTriggerFactoryBean stfb = ConfigureQuartz.createTrigger(jdfb.getObject(),1000L,0);
			stfb.setBeanName("dynamicJobBeanTrigger"+id);
			stfb.setStartTime(afterAddingTenMins);
			stfb.afterPropertiesSet();
			
			schedFactory.getScheduler().scheduleJob(jdfb.getObject(), stfb.getObject());
			
		} catch (Exception e) {
			scheduled = "Could not schedule a job. " + e.getMessage();
		}
		return scheduled;
	}
	
	@RequestMapping("/unschedule")
	public String unschedule() {
		String scheduled = "Job is Unscheduled!!";
		TriggerKey tkey = new TriggerKey("dynamicJobBeanTrigger");
		JobKey jkey = new JobKey("dynamicJobBean"); 
		try {
			schedFactory.getScheduler().unscheduleJob(tkey);
			schedFactory.getScheduler().deleteJob(jkey);
		} catch (SchedulerException e) {
			scheduled = "Error while unscheduling " + e.getMessage();
		}
		return scheduled;
	}
}