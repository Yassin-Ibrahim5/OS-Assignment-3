// ============================================
// schedulers/PreemptiveSJF.java
// ============================================
package schedulers;

import models.Process;
import java.util.*;

public class PreemptiveSJF extends SchedulerBase {
    private List<String> executionOrder;

    public PreemptiveSJF(List<Process> processes, int contextSwitchTime) {
        super(processes, contextSwitchTime);
        this.executionOrder = new ArrayList<>();
    }

    @Override
    public void schedule() {
        System.out.println("Running Preemptive SJF ...");

        int currentTime = 0;
        int completedProcesses = 0;
        Process currentProcess = null;
        String lastSegmentProcess = null;

        PriorityQueue<Process> readyQueue = new PriorityQueue<>(
                Comparator.comparingInt(Process::getRemainingTime)
                        .thenComparingInt(Process::getArrivalTime)
        );

        Set<Process> arrived = new HashSet<>();

        while (completedProcesses < processes.size()) {
            // Add newly arrived processes
            for (Process p : processes) {
                if (p.getArrivalTime() <= currentTime && !arrived.contains(p)) {
                    readyQueue.add(p);
                    arrived.add(p);
                }
            }

            // If CPU idle and queue empty, jump to next arrival
            if (currentProcess == null && readyQueue.isEmpty()) {
                int nextArrival = Integer.MAX_VALUE;
                for (Process p : processes) {
                    if (!arrived.contains(p)) {
                        nextArrival = Math.min(nextArrival, p.getArrivalTime());
                    }
                }
                if (nextArrival != Integer.MAX_VALUE) {
                    currentTime = nextArrival;
                }
                continue;
            }

            // Pick next process if CPU is idle
            if (currentProcess == null && !readyQueue.isEmpty()) {
                // Add context switch time if switching from different process
                if (lastSegmentProcess != null) {
                    currentTime += contextSwitchTime;

                    // IMPORTANT: Check for arrivals during context switch
                    for (Process p : processes) {
                        if (p.getArrivalTime() <= currentTime && !arrived.contains(p)) {
                            readyQueue.add(p);
                            arrived.add(p);
                        }
                    }
                }

                // Now select the shortest job
                currentProcess = readyQueue.poll();

                // Record execution segment only if different from last
                if (lastSegmentProcess == null || !lastSegmentProcess.equals(currentProcess.getName())) {
                    executionOrder.add(currentProcess.getName());
                }
                lastSegmentProcess = currentProcess.getName();
            }

            // Execute current process for 1 time unit
            if (currentProcess != null) {
                currentProcess.setRemainingTime(currentProcess.getRemainingTime() - 1);
                currentTime++;

                // Add any processes that arrived at this exact time
                for (Process p : processes) {
                    if (p.getArrivalTime() == currentTime && !arrived.contains(p)) {
                        readyQueue.add(p);
                        arrived.add(p);
                    }
                }

                // Check if current process completed
                if (currentProcess.isComplete()) {
                    currentProcess.setCompletionTime(currentTime);
                    completedProcesses++;
                    currentProcess = null;
                } else {
                    // Check for preemption after new arrivals
                    if (!readyQueue.isEmpty()) {
                        Process shortest = readyQueue.peek();
                        if (shortest.getRemainingTime() < currentProcess.getRemainingTime()) {
                            readyQueue.add(currentProcess);
                            currentProcess = null;
                        }
                    }
                }
            }
        }

        // Store execution order
        for (Process p : processes) {
            p.setExecutionOrder(new ArrayList<>(executionOrder));
        }

        calculateTimes();
    }

    public List<String> getExecutionOrder() {
        return executionOrder;
    }
}
