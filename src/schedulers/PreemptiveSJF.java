package schedulers;

import models.Process;
import java.util.*;

public class PreemptiveSJF extends SchedulerBase {

    public PreemptiveSJF(List<Process> processes, int contextSwitchTime) {
        super(processes, contextSwitchTime);
    }

    @Override
    public void schedule() {
        System.out.println("Running Preemptive SJF...");
        // TODO: Implement SJF algorithm here
        // Remember to call calculateTimes() at the end
    }
}