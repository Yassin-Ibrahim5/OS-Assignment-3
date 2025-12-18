package utils;

import models.Process;
import schedulers.SchedulerBase;
import schedulers.PreemptiveSJF;
import java.util.*;

public class OutputFormatter {

    public static void printResults(SchedulerBase scheduler) {
        List<Process> processes = scheduler.getProcesses();

        System.out.println("\n========================================");
        System.out.println("RESULTS");
        System.out.println("========================================\n");

        // Print execution order if available
        if (scheduler instanceof PreemptiveSJF) {
            PreemptiveSJF sjf = (PreemptiveSJF) scheduler;
            System.out.println("Execution Order: " + sjf.getExecutionOrder());
            System.out.println();
        }

        // Print each process details
        for (Process p : processes) {
            System.out.println("Process: " + p.getName());
            System.out.println("  Waiting Time: " + p.getWaitingTime());
            System.out.println("  Turnaround Time: " + p.getTurnaroundTime());
            System.out.println("  Completion Time: " + p.getCompletionTime());
            if (!p.getQuantumHistory().isEmpty()) {
                System.out.println("  Quantum History: " + p.getQuantumHistory());
            }
            System.out.println();
        }

        // Calculate averages
        double totalWaiting = 0;
        double totalTurnaround = 0;

        for (Process p : processes) {
            totalWaiting += p.getWaitingTime();
            totalTurnaround += p.getTurnaroundTime();
        }

        System.out.printf("Average Waiting Time: %.2f%n", (totalWaiting / processes.size()));
        System.out.printf("Average Turnaround Time: %.2f%n", (totalTurnaround / processes.size()));
        System.out.println("========================================\n");
    }
}