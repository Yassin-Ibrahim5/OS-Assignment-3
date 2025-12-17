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

        // Create a queue for ready processes
        Queue<Process> readyQueue = new LinkedList<>();

        // Track remaining burst time for each process
        Map<String, Integer> remainingTime = new HashMap<>();
        for (Process p : processes) {
            remainingTime.put(p.getName(), p.getBurstTime());
        }

        // Sort processes by arrival time
        List<Process> sortedProcesses = new ArrayList<>(processes);
        sortedProcesses.sort(Comparator.comparingInt(Process::getArrivalTime));

        int currentTime = 0;
        int completedProcesses = 0;
        int processIndex = 0;
        Process currentProcess = null;

        // Track which processes have been added to queue
        Set<String> inQueue = new HashSet<>();

        while (completedProcesses < processes.size()) {
            // Add newly arrived processes to ready queue
            while (processIndex < sortedProcesses.size() &&
                    sortedProcesses.get(processIndex).getArrivalTime() <= currentTime) {
                Process p = sortedProcesses.get(processIndex);
                if (!inQueue.contains(p.getName())) {
                    readyQueue.add(p);
                    inQueue.add(p.getName());
                }
                processIndex++;
            }

            // If queue is empty, jump to next arrival time
            if (readyQueue.isEmpty()) {
                if (processIndex < sortedProcesses.size()) {
                    currentTime = sortedProcesses.get(processIndex).getArrivalTime();
                }
                continue;
            }

            // Get next process from queue
            currentProcess = readyQueue.poll();

            // Apply context switch time (except for the very first process)
            if (currentTime > 0) {
                currentTime += contextSwitchTime;
            }

            // Check again for arrivals during context switch
            while (processIndex < sortedProcesses.size() &&
                    sortedProcesses.get(processIndex).getArrivalTime() <= currentTime) {
                Process p = sortedProcesses.get(processIndex);
                if (!inQueue.contains(p.getName())) {
                    readyQueue.add(p);
                    inQueue.add(p.getName());
                }
                processIndex++;
            }

            // Execute process for time quantum or remaining time (whichever is smaller)
            int remaining = remainingTime.get(currentProcess.getName());
            int executionTime = Math.min(timeQuantum, remaining);

            currentTime += executionTime;
            remaining -= executionTime;
            remainingTime.put(currentProcess.getName(), remaining);

            // Check for new arrivals during execution
            while (processIndex < sortedProcesses.size() &&
                    sortedProcesses.get(processIndex).getArrivalTime() <= currentTime) {
                Process p = sortedProcesses.get(processIndex);
                if (!inQueue.contains(p.getName())) {
                    readyQueue.add(p);
                    inQueue.add(p.getName());
                }
                processIndex++;
            }

            // If process is finished
            if (remaining == 0) {
                currentProcess.setCompletionTime(currentTime);
                completedProcesses++;
                inQueue.remove(currentProcess.getName());
            } else {
                // Process not finished, add back to queue
                readyQueue.add(currentProcess);
            }
        }

        // Calculate waiting and turnaround times
        calculateTimes();

        System.out.println("Round Robin scheduling completed.");
    }
}