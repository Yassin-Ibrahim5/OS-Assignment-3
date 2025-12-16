package schedulers;

import models.Process;

import java.util.*;

public class AGScheduler extends SchedulerBase {

    public AGScheduler(List<Process> processes, int contextSwitchTime) {
        super(processes, contextSwitchTime);
    }

    @Override
    public void schedule() {
        System.out.println("Running AG Scheduling...");
        // TODO: Implement AG Scheduling here
        int currentTime = 0;
        int completedProcesses = 0;
        Queue<Process> readyQueue = new LinkedList<>();
        Set<Process> arrivedProcesses = new HashSet<>();
        Process currentProcess = null;
        int timeByCurrentProcess = 0;

        while (completedProcesses < processes.size()) {
            for (Process p : processes) {
                if (p.getArrivalTime() <= currentTime && !arrivedProcesses.contains(p)) {
                    readyQueue.add(p);
                    arrivedProcesses.add(p);
                }
            }
            if (currentProcess == null) {
                if (!readyQueue.isEmpty()) {
                    currentProcess = readyQueue.poll();
                    timeByCurrentProcess = 0;
                } else {
                    currentTime++;
                    continue;
                }
            }

            int q = currentProcess.getQuantum();
            int limit25 = (int) Math.ceil(q * 0.25);
            int limit50 = (int) Math.ceil(q * 0.50);

            boolean preempted = false;

            // zone 2: Priority Scheduling [limit25 to limit50]
            if (timeByCurrentProcess >= limit25 && timeByCurrentProcess < limit50) {
                Process bestPriorityProcess = getBestPriorityProcess(readyQueue);

                if (bestPriorityProcess != null && bestPriorityProcess.getPriority() < currentProcess.getPriority()) {
                    preempted = true;

                    int unused = q - timeByCurrentProcess;
                    int nextQuantum = q + (int) Math.ceil(unused / 2.0);
                    preemptive(currentProcess, nextQuantum, readyQueue);
                    currentProcess = bestPriorityProcess;
                    readyQueue.remove(currentProcess);
                }
            }
            // zone 3: Shortest Remaining Time First [limit50 to end]
            else if (timeByCurrentProcess >= limit50) {
                Process shortestProcess = getShortestProcess(readyQueue);
                if (shortestProcess != null && shortestProcess.getRemainingTime() < currentProcess.getRemainingTime()) {
                    preempted = true;
                    int unused = q - timeByCurrentProcess;
                    int nextQuantum = q + unused;

                    preemptive(currentProcess, nextQuantum, readyQueue);
                    currentProcess = shortestProcess;
                    readyQueue.remove(currentProcess);
                }
            }

            if (preempted) {
                timeByCurrentProcess = 0;
                currentTime += contextSwitchTime;
                continue;
            }

            currentProcess.setRemainingTime(currentProcess.getRemainingTime() - 1);
            timeByCurrentProcess++;
            currentTime++;

            if (currentProcess.isComplete()) {
                currentProcess.setCompletionTime(currentTime);
                updateQuantum(currentProcess, 0);
                completedProcesses++;
                currentProcess = null;
                // add context switch time only if there is another process ready to run now
                if (!readyQueue.isEmpty() && completedProcesses < processes.size()) {
                    currentTime += contextSwitchTime;
                }
            } else if (timeByCurrentProcess == q) {
                int nextQuantum = q + 2;
                updateQuantum(currentProcess, nextQuantum);
                readyQueue.add(currentProcess);
                currentProcess = null;
                currentTime += contextSwitchTime;
            }
        }
        calculateTimes();
        // Remember to call calculateTimes() at the end
    }

    private void preemptive(Process p, int nextQuantum, Queue<Process> readyQueue) {
        updateQuantum(p, nextQuantum);
        readyQueue.add(p);
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

    private Process getShortestProcess(Queue<Process> queue) {
        Process shortestProcess = null;
        for (Process p : queue) {
            if (shortestProcess == null || p.getRemainingTime() < shortestProcess.getRemainingTime()) {
                shortestProcess = p;
            }
        }
        return shortestProcess;
    }

    private void updateQuantum(Process p, int newQuantum) {
        p.setQuantum(newQuantum);
        p.addQuantumToHistory(newQuantum);
    }
}