package schedulers;

import models.Process;
import java.util.*;

public abstract class SchedulerBase {
    protected List<Process> processes;
    protected int contextSwitchTime;

    public SchedulerBase(List<Process> processes, int contextSwitchTime) {
        this.processes = processes;
        this.contextSwitchTime = contextSwitchTime;
    }

    // Each scheduler must implement this
    public abstract void schedule();

    // Calculate waiting and turnaround times
    protected void calculateTimes() {
        for (Process p : processes) {
            p.setTurnaroundTime(p.getCompletionTime() - p.getArrivalTime());
            p.setWaitingTime(p.getTurnaroundTime() - p.getBurstTime());
        }
    }

    public List<Process> getProcesses() {
        return processes;
    }
}
