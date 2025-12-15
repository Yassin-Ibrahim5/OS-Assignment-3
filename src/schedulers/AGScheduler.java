package schedulers;

import models.Process;
import java.util.*;

public class AGScheduler extends SchedulerBase {

    public AGScheduler(List<Process> processes, int contextSwitchTime) {
        super(processes, contextSwitchTime);
    }

    @Override
    public void schedule() {
        System.out.println("Running AG Scheduling...");
        // TODO: Implement AG Scheduling here
        // Remember to call calculateTimes() at the end
    }
}