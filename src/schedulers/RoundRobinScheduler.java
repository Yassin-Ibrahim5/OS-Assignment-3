package schedulers;

import models.Process;
import java.util.*;

public class RoundRobinScheduler extends SchedulerBase {
    private int timeQuantum;

    public RoundRobinScheduler(List<Process> processes, int contextSwitchTime, int timeQuantum) {
        super(processes, contextSwitchTime);
        this.timeQuantum = timeQuantum;
    }

    @Override
    public void schedule() {
        System.out.println("Running Round Robin...");
        // TODO: Implement Round Robin algorithm here
        // Remember to call calculateTimes() at the end
    }
}