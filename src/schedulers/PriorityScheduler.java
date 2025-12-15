package schedulers;

import models.Process;
import java.util.*;

public class PriorityScheduler extends SchedulerBase {

    public PriorityScheduler(List<Process> processes, int contextSwitchTime) {
        super(processes, contextSwitchTime);
    }

    @Override
    public void schedule() {
        System.out.println("Running Priority Scheduling...");
        // TODO: Implement Priority Scheduling here
        // Remember to call calculateTimes() at the end
    }
}
