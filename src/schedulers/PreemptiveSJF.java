package schedulers;

import models.Process;
import java.util.*;

public class PreemptiveSJF extends SchedulerBase {

    public PreemptiveSJF(List<Process> processes, int contextSwitchTime) {
        super(processes, contextSwitchTime);
    }

    @Override
    public void schedule() {
        System.out.println("Running Preemptive SJF ...");

        int currentTime = 0;
        int completedProcesses = 0;
        Process currentProcess = null;

        PriorityQueue<Process> readyQueue = new PriorityQueue<>(
                Comparator.comparingInt(Process::getRemainingTime)
                        .thenComparingInt(Process::getArrivalTime)
        );

        Set<Process> arrivedProcesses = new HashSet<>();

        while (completedProcesses < processes.size()) {
            for (Process p : processes) {
                if (p.getArrivalTime() <= currentTime && !arrivedProcesses.contains(p)) {
                    readyQueue.add(p);
                    arrivedProcesses.add(p);
                }
            }

            // 2. Check for Preemption (if a new process is shorter than current)
            if (currentProcess != null) {
                Process shortest = readyQueue.peek();
                // If there is a process in queue shorter than current process
                if (shortest != null && shortest.getRemainingTime() < currentProcess.getRemainingTime()) {
                    // Context Switch Overhead
                    currentTime += contextSwitchTime;
                    readyQueue.add(currentProcess);
                    currentProcess = null;
                    continue;
                }
            }

            // Pick Process if CPU is idle
            if (currentProcess == null && !readyQueue.isEmpty()) {
                currentProcess = readyQueue.poll();
            }

            if (currentProcess != null) {
                currentProcess.setRemainingTime(currentProcess.getRemainingTime() - 1);
                currentTime++;

                if (currentProcess.isComplete()) {
                    currentProcess.setCompletionTime(currentTime);
                    completedProcesses++;
                    currentProcess = null;

                    if (completedProcesses < processes.size()) {
                        currentTime += contextSwitchTime;
                    }
                }
            } else {
//                pass time if no process is ready
                currentTime++;
            }
        }
        calculateTimes();
    }
}