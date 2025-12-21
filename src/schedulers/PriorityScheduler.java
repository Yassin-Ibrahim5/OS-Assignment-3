package schedulers;

import models.Process;

import java.util.*;

public class PriorityScheduler extends SchedulerBase {
    private static final int HIGHEST_PRIORITY = 1;

    private final int agingLimit;
    private final List<String> executionOrder = new ArrayList<>();
    private final Map<String, Integer> lastAgedTime = new HashMap<>();

    public PriorityScheduler(List<Process> processes, int contextSwitchTime, int agingLimit) {
        super(processes, contextSwitchTime);
        this.agingLimit = agingLimit;
        initializeAgingTracker();
    }

    @Override
    public void schedule() {
        System.out.println("Running Priority Scheduling with Aging (Limit: " + agingLimit + ")...");

        Process currentProcess;
        Process previousProcess = null;
        int currentTime = 0;
        int completedCount = 0;

        while (completedCount < processes.size()) {
            Process selectedProcess = selectNextProcess(currentTime, previousProcess);

            if (selectedProcess == null) {
                handleIdleTime(currentTime);
                currentTime++;
                continue;
            }

            if (previousProcess != null && isProcessSwitchRequired(selectedProcess, previousProcess)) {
                currentTime = performContextSwitch(currentTime);
                selectedProcess = selectNextProcess(currentTime, previousProcess);
                if (selectedProcess == null) continue;
            }

            logExecutionIfNewProcess(selectedProcess, previousProcess);
            currentProcess = selectedProcess;

            executeProcess(currentProcess, currentTime);
            currentTime++;

            if (currentProcess.isComplete()) {
                completeProcess(currentProcess, currentTime);
                completedCount++;
            }

            previousProcess = currentProcess;
        }

        calculateTimes();
    }

    private void initializeAgingTracker() {
        for (Process process : processes) {
            lastAgedTime.put(process.getName(), process.getArrivalTime());
        }
    }

    private Process selectNextProcess(int currentTime, Process previousProcess) {
        List<Process> readyProcesses = getReadyProcesses(currentTime);

        if (readyProcesses.isEmpty()) {
            return null;
        }

        return findHighestPriorityProcess(readyProcesses);
    }

    private List<Process> getReadyProcesses(int currentTime) {
        List<Process> readyProcesses = new ArrayList<>();

        for (Process process : processes) {
            if (isProcessReady(process, currentTime)) {
                readyProcesses.add(process);
            }
        }

        return readyProcesses;
    }

    private boolean isProcessReady(Process process, int currentTime) {
        return !process.isComplete() && process.getArrivalTime() <= currentTime;
    }

    private Process findHighestPriorityProcess(List<Process> readyProcesses) {
        Process bestProcess = null;

        for (Process process : readyProcesses) {
            if (bestProcess == null || hasHigherPriority(process, bestProcess)) {
                bestProcess = process;
            }
        }

        return bestProcess;
    }

    private boolean hasHigherPriority(Process process1, Process process2) {
        if (process1.getPriority() < process2.getPriority()) {
            return true;
        }

        if (process1.getPriority() == process2.getPriority()) {
            return process1.getArrivalTime() < process2.getArrivalTime();
        }

        return false;
    }

    private boolean isProcessSwitchRequired(Process nextProcess, Process previousProcess) {
        return !nextProcess.getName().equals(previousProcess.getName());
    }

    private void handleIdleTime(int currentTime) {
        ageWaitingProcesses(currentTime, null);
    }

    private int performContextSwitch(int currentTime) {
        for (int i = 0; i < contextSwitchTime; i++) {
            currentTime++;
            ageWaitingProcesses(currentTime, null);
        }
        return currentTime;
    }

    private void logExecutionIfNewProcess(Process currentProcess, Process previousProcess) {
        if (previousProcess == null || !previousProcess.getName().equals(currentProcess.getName())) {
            executionOrder.add(currentProcess.getName());
        }
    }

    private void executeProcess(Process process, int currentTime) {
        process.setRemainingTime(process.getRemainingTime() - 1);
        updateAgingTracker(process, currentTime + 1); // +1 because time increments after execution
        ageWaitingProcesses(currentTime + 1, process);
    }

    private void updateAgingTracker(Process process, int currentTime) {
        lastAgedTime.put(process.getName(), currentTime);
    }

    private void ageWaitingProcesses(int currentTime, Process runningProcess) {
        if (agingLimit <= 0) return;

        for (Process process : processes) {
            if (shouldSkipAging(process, currentTime, runningProcess)) {
                continue;
            }

            ageProcessIfNeeded(process, currentTime);
        }
    }

    private boolean shouldSkipAging(Process process, int currentTime, Process runningProcess) {
        return process.isComplete() ||
                process.getArrivalTime() > currentTime ||
                (runningProcess != null && process.getName().equals(runningProcess.getName()));
    }

    private void ageProcessIfNeeded(Process process, int currentTime) {
        int lastAged = lastAgedTime.getOrDefault(process.getName(), process.getArrivalTime());

        if (currentTime - lastAged >= agingLimit) {
            increaseProcessPriority(process);
            lastAgedTime.put(process.getName(), currentTime);
        }
    }

    private void increaseProcessPriority(Process process) {
        if (process.getPriority() > HIGHEST_PRIORITY) {
            process.setPriority(process.getPriority() - 1);
        }
    }

    private void completeProcess(Process process, int completionTime) {
        process.setCompletionTime(completionTime);
        lastAgedTime.remove(process.getName());
    }

    public List<String> getExecutionOrder() {
        return Collections.unmodifiableList(executionOrder);
    }

}