package schedulers;

import models.Process;

import java.util.*;

public class PriorityScheduler extends SchedulerBase {

    private final int AGING_LIMIT;
    private final List<String> executionOrder = new ArrayList<>();
    private final Map<Process, Integer> lastAgedTime = new HashMap<>();

    public PriorityScheduler(List<Process> processes, int contextSwitchTime, int agingInterval) {
        super(processes, contextSwitchTime);
        AGING_LIMIT = agingInterval;
    }

    @Override
    public void schedule() {
        System.out.println("Running Priority Scheduling...");
        List<Process> readyQueue = new ArrayList<>();
        List<Process> arrivalList = new ArrayList<>(processes);
        arrivalList.sort(Comparator.comparingInt(Process::getArrivalTime));
        int currentTime = 0;
        Process currentProcess = null;
        String lastProcess = "";
        int timeInCurrentProcess = 0;

        while (!arrivalList.isEmpty() || !readyQueue.isEmpty() || currentProcess != null) {
            Iterator<Process> iterator = arrivalList.iterator();
            while (iterator.hasNext()) {
                Process process = iterator.next();
                if (process.getArrivalTime() <= currentTime) {
                    readyQueue.add(process);
                    lastAgedTime.put(process, currentTime);
                    iterator.remove();
                }
            }

            if (AGING_LIMIT > 0) {
                for (Process p : readyQueue) {
                    Integer lastAged = lastAgedTime.get(p);
                    if (lastAged != null && currentTime - lastAged >= AGING_LIMIT) {
                        if (p.getPriority() > 0) {
                            p.setPriority(p.getPriority() - 1);
                        }
                        lastAgedTime.put(p, currentTime);
                    }
                }
            }

            Process bestReady = getBestPriorityProcess(readyQueue);

            if (currentProcess == null) {
                if (bestReady != null) {
                    currentProcess = bestReady;
                    readyQueue.remove(bestReady);
                    lastAgedTime.remove(currentProcess);

                    if (!lastProcess.isEmpty() && !lastProcess.equals(currentProcess.getName())) {
                        currentTime += contextSwitchTime;
                    }

                    executionOrder.add(currentProcess.getName());
                    lastProcess = currentProcess.getName();
                    timeInCurrentProcess = 0;
                }
            } else {
                if (bestReady != null && bestReady.getPriority() < currentProcess.getPriority()) {
                    // Preempt current process
                    readyQueue.add(currentProcess);
                    lastAgedTime.put(currentProcess, currentTime);
                    currentProcess = bestReady;
                    readyQueue.remove(bestReady);
                    lastAgedTime.remove(currentProcess);

                    // Context switch for preemption
                    currentTime += contextSwitchTime;

                    executionOrder.add(currentProcess.getName());
                    lastProcess = currentProcess.getName();
                    timeInCurrentProcess = 0;
                }
            }

            // Execute current process
            if (currentProcess != null) {
                currentProcess.setRemainingTime(currentProcess.getRemainingTime() - 1);
                currentTime++;
                timeInCurrentProcess++;

                if (currentProcess.isComplete()) {
                    currentProcess.setCompletionTime(currentTime);
                    currentProcess = null;
                }
            } else {
                // Idle
                currentTime++;
            }
        }
        calculateTimes();
    }

    private Process getBestPriorityProcess(List<Process> queue) {
        if (queue.isEmpty()) return null;

        Process bestProcess = queue.get(0);
        for (int i = 1; i < queue.size(); i++) {
            Process p = queue.get(i);
            if (p.getPriority() < bestProcess.getPriority()) {
                bestProcess = p;
            } else if (p.getPriority() == bestProcess.getPriority()) {
                if (p.getArrivalTime() < bestProcess.getArrivalTime()) {
                    bestProcess = p;
                }
            }
        }
        return bestProcess;
    }

    public List<String> getExecutionOrder() {
        return executionOrder;
    }
}