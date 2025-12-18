package schedulers;

import models.Process;

import java.util.*;

public class PriorityScheduler extends SchedulerBase {

    private final int AGING_LIMIT;

    private final List<String> executionOrder = new ArrayList<>();

    public PriorityScheduler(List<Process> processes, int contextSwitchTime, int agingInterval) {
        super(processes, contextSwitchTime);
        AGING_LIMIT = agingInterval;
    }

    @Override
    public void schedule() {
        System.out.println("Running Priority Scheduling...");
        Queue<Process> readyQueue = new LinkedList<>();
        List<Process> arrivalList = new ArrayList<>(processes);
        arrivalList.sort(Comparator.comparingInt(Process::getArrivalTime));
        int currentTime = 0;
        Process currentProcess = null;
        String lastProcess = "";

        while (!arrivalList.isEmpty() || !readyQueue.isEmpty() || currentProcess != null) {
            Iterator<Process> iterator = arrivalList.iterator();
            while (iterator.hasNext()) {
                Process process = iterator.next();
                if (process.getArrivalTime() <= currentTime) {
                    readyQueue.add(process);
                    iterator.remove();
                }
            }

            if (currentTime > 0 && currentTime % AGING_LIMIT == 0) {
                for (Process p : readyQueue) {
                    if (p.getPriority() > 1) {
                        p.setPriority(p.getPriority() - 1);
                    }
                }
            }

            if (!readyQueue.isEmpty()) {
                Process bestPriority = getBestPriorityProcess(readyQueue);

                if (currentProcess == null) {
                    currentProcess = bestPriority;
                    readyQueue.remove(bestPriority);
                    if (!lastProcess.isEmpty() && !lastProcess.equals(currentProcess.getName())) {
                        currentTime += contextSwitchTime;
                    }
                } else if (bestPriority.getPriority() < currentProcess.getPriority()) {
                    readyQueue.add(currentProcess);
                    currentProcess = bestPriority;
                    readyQueue.remove(currentProcess);
                    currentTime += contextSwitchTime;
                }
            }
            if (currentProcess != null) {
                if (!lastProcess.equals(currentProcess.getName())) {
                    executionOrder.add(currentProcess.getName());
                    lastProcess = currentProcess.getName();
                }
                currentProcess.setRemainingTime(currentProcess.getRemainingTime() - 1);
                currentTime++;
                if (currentProcess.isComplete()) {
                    currentProcess.setCompletionTime(currentTime);
                    executionOrder.add(currentProcess.getName());
                    currentProcess = null;
                }
            } else {
                currentTime++;
            }
        }
        calculateTimes();
    }

    private Process getBestPriorityProcess(Queue<Process> queue) {
        Process bestProcess = null;
        for (Process p : queue) {
            if (bestProcess == null || p.getPriority() < bestProcess.getPriority()) {
                bestProcess = p;
            }
        }
        return bestProcess;
    }

    public List<String> getExecutionOrder() {
        return executionOrder;
    }
}
