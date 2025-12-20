package schedulers;

import models.Process;
import java.util.*;

public class PriorityScheduler extends SchedulerBase {
    private int agingInterval;

    public PriorityScheduler(List<Process> processes, int contextSwitchTime) {
        this(processes, contextSwitchTime, 5);
    }

    public PriorityScheduler(List<Process> processes, int contextSwitchTime, int agingInterval) {
        super(processes, contextSwitchTime);
        this.agingInterval = agingInterval;
    }

    // Helper method to increment waiting and apply aging
    private void incrementWaitingAndAge(Map<Process, Integer> waitingCounter) {
        for (Process p : processes) {
            if (p.getArrivalTime() <= currentTime && !p.isComplete()) {
                int currentWait = waitingCounter.get(p);
                waitingCounter.put(p, currentWait + 1);

                // Check if this process should age NOW (after incrementing)
                int newWait = currentWait + 1;
                if (agingInterval > 0 && newWait > 0 && newWait % agingInterval == 0 && p.getPriority() > 0) {
                    p.setPriority(p.getPriority() - 1);
                    System.out.println("  [AGING] " + p.getName() + " aged to priority " + p.getPriority() + " (wait=" + newWait + ")");
                }
            }
        }
    }

    @Override
    public void schedule() {
        int completed = 0;
        int n = processes.size();
        Process currentProcess = null;
        int segmentStartTime = 0;
        Map<Process, Integer> waitingCounter = new HashMap<>();

        for (Process p : processes) {
            waitingCounter.put(p, 0);
        }

        while (completed < n) {
            // Find highest priority process
            Process highestPriorityProcess = null;
            int highestPriority = Integer.MAX_VALUE;

            for (Process p : processes) {
                if (p.getArrivalTime() <= currentTime && !p.isComplete()) {
                    if (p.getPriority() < highestPriority) {
                        highestPriority = p.getPriority();
                        highestPriorityProcess = p;
                    }
                    else if (p.getPriority() == highestPriority && highestPriorityProcess != null) {
                        // FIFO: earlier arrival wins
                        if (p.getArrivalTime() < highestPriorityProcess.getArrivalTime()) {
                            highestPriorityProcess = p;
                        }
                    }
                }
            }

            if (highestPriorityProcess == null) {
                currentTime++;
                continue;
            }

            // DEBUG OUTPUT
            System.out.println("\nTime " + currentTime + ": Selected " + highestPriorityProcess.getName());
            System.out.println("  Current process: " + (currentProcess != null ? currentProcess.getName() : "None"));
            System.out.println("  Available:");
            for (Process p : processes) {
                if (p.getArrivalTime() <= currentTime && !p.isComplete()) {
                    System.out.println("    " + p.getName() +
                            ": pri=" + p.getPriority() +
                            ", arr=" + p.getArrivalTime() +
                            ", wait=" + waitingCounter.get(p));
                }
            }

            // Check if we need to switch
            if (currentProcess != null && !currentProcess.getName().equals(highestPriorityProcess.getName())) {
                System.out.println("  → SWITCHING from " + currentProcess.getName() + " to " + highestPriorityProcess.getName());
                addToExecutionOrder(currentProcess.getName(), segmentStartTime, currentTime);

                // Context switch - increment waiting AND apply aging
                for (int cs = 0; cs < contextSwitchTime; cs++) {
                    currentTime++;
                    incrementWaitingAndAge(waitingCounter);
                }

                segmentStartTime = currentTime;
            } else if (currentProcess == null) {
                System.out.println("  → STARTING " + highestPriorityProcess.getName());
                segmentStartTime = currentTime;
            } else {
                System.out.println("  → CONTINUING " + currentProcess.getName());
            }

            currentProcess = highestPriorityProcess;

            if (!currentProcess.hasStarted()) {
                currentProcess.setStartTime(currentTime);
            }

            // Execute for 1 unit
            currentProcess.executeFor(1);
            currentTime++;

            // Update waiting for others - AND apply aging!
            for (Process p : processes) {
                if (p.getArrivalTime() <= currentTime && !p.isComplete() && p != currentProcess) {
                    int currentWait = waitingCounter.get(p);
                    waitingCounter.put(p, currentWait + 1);

                    // Check aging after incrementing
                    int newWait = currentWait + 1;
                    if (agingInterval > 0 && newWait > 0 && newWait % agingInterval == 0 && p.getPriority() > 0) {
                        p.setPriority(p.getPriority() - 1);
                        System.out.println("  [AGING] " + p.getName() + " aged to priority " + p.getPriority() + " (wait=" + newWait + ")");
                    }
                }
            }

            // Check if completed
            if (currentProcess.isComplete()) {
                System.out.println("  → " + currentProcess.getName() + " COMPLETED");
                currentProcess.setCompletionTime(currentTime);
                addToExecutionOrder(currentProcess.getName(), segmentStartTime, currentTime);
                completed++;

                // Context switch - increment waiting AND apply aging
                for (int cs = 0; cs < contextSwitchTime; cs++) {
                    currentTime++;
                    incrementWaitingAndAge(waitingCounter);
                }

                currentProcess = null;
            }
        }

        calculateTimes();
    }
}


/*
==================================================================
THE KEY FIX: Apply Aging IMMEDIATELY After Each Wait Increment
==================================================================

OLD PROBLEM:
- Waiting counter increments from 4 → 5 → 6
- Aging check only happens once (at 6)
- Misses the aging opportunity at 5!

NEW SOLUTION:
- EVERY TIME we increment waiting counter, check for aging
- This catches aging at exactly wait=5, 10, 15, etc.
- No more missed aging opportunities!

==================================================================
*/