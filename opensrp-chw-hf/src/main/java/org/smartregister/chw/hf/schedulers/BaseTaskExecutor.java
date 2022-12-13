package org.smartregister.chw.hf.schedulers;

import org.joda.time.LocalDate;
import org.smartregister.chw.core.contract.ScheduleService;
import org.smartregister.chw.core.contract.ScheduleTask;
import org.smartregister.chw.core.domain.BaseScheduleTask;
import org.smartregister.chw.hf.HealthFacilityApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public abstract class BaseTaskExecutor implements ScheduleService {

    @Override
    public void scheduleMaintenance() {
        LocalDate localDate = new LocalDate();
        localDate.plusDays(-31);

        HealthFacilityApplication.getInstance().getScheduleRepository().deleteSchedulesByName(getScheduleName(), localDate.toDate());
    }

    @Override
    public void resetSchedule(String baseEntityID, String scheduleName) {
        // delete from the repo all the old schedules by this name
        HealthFacilityApplication.getInstance().getScheduleRepository().deleteScheduleByName(scheduleName, baseEntityID);
    }

    protected BaseScheduleTask prepareNewTaskObject(String baseEntityID) {
        BaseScheduleTask baseScheduleTask = new BaseScheduleTask();
        baseScheduleTask.setBaseEntityID(baseEntityID);
        baseScheduleTask.setCreatedAt(new Date());
        baseScheduleTask.setUpdatedAt(new Date());
        baseScheduleTask.setID(UUID.randomUUID().toString());
        baseScheduleTask.setScheduleName(getScheduleName());
        baseScheduleTask.setScheduleGroupName(getScheduleGroup());
        return baseScheduleTask;
    }

    protected List<ScheduleTask> toScheduleList(ScheduleTask task, ScheduleTask... tasks) {
        List<ScheduleTask> res = new ArrayList<>();
        res.add(task);
        if (tasks != null && tasks.length > 0)
            res.addAll(Arrays.asList(tasks));

        return res;
    }
}
