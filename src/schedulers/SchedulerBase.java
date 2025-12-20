package schedulers;

import models.Process;
import java.util.*;

public abstract class SchedulerBase {
    protected List<Process> processes;
    protected int contextSwitchTime;
    protected int currentTime = 0;
    protected List<String> executionOrder = new ArrayList<>();

    public SchedulerBase(List<Process> processes, int contextSwitchTime) {
        this.processes = processes;
        this.contextSwitchTime = contextSwitchTime;
    }

    public abstract void schedule();

    protected void addToExecutionOrder(String name, int start, int end) {
        if (start != end) {
            // Prevent duplicate adjacent entries in the log
            if (executionOrder.isEmpty() || !executionOrder.get(executionOrder.size() - 1).equals(name)) {
                executionOrder.add(name);
            }
        }
    }

    public List<String> getExecutionOrder() { return executionOrder; }
    public List<Process> getProcesses() { return processes; }

    protected void calculateTimes() {
        for (Process p : processes) {
            p.setTurnaroundTime(p.getCompletionTime() - p.getArrivalTime());
            p.setWaitingTime(p.getTurnaroundTime() - p.getBurstTime());
        }
    }
}