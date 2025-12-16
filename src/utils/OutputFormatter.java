package utils;

import models.Process;
import schedulers.SchedulerBase;
import java.util.*;

public class OutputFormatter {

    public static void printResults(SchedulerBase scheduler) {
        List<Process> processes = scheduler.getProcesses();

        System.out.println("\n========================================");
        System.out.println("RESULTS");
        System.out.println("========================================\n");

        // Print each process details
        for (Process p : processes) {
            System.out.println("Process: " + p.getName());
            System.out.println("  Waiting Time: " + p.getWaitingTime());
            System.out.println("  Turnaround Time: " + p.getTurnaroundTime());
            System.out.println("  Completion Time: " + p.getCompletionTime());
            System.out.println("  Quantum History: " + p.getQuantumHistory());
            System.out.println();
        }

        // Calculate averages
        double totalWaiting = 0;
        double totalTurnaround = 0;

        for (Process p : processes) {
            totalWaiting += p.getWaitingTime();
            totalTurnaround += p.getTurnaroundTime();
        }

        System.out.println("Average Waiting Time: " + (totalWaiting / processes.size()));
        System.out.println("Average Turnaround Time: " + (totalTurnaround / processes.size()));
        System.out.println("========================================\n");
    }
}