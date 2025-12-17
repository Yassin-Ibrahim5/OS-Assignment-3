package utils;

import models.Process;
import java.util.*;

public class InputParser {
    private Scanner scanner;

    public InputParser() {
        this.scanner = new Scanner(System.in);
    }

    public List<Process> parseProcesses() {
        List<Process> processes = new ArrayList<>();

        System.out.print("Enter number of processes: ");
        int n = scanner.nextInt();

        for (int i = 0; i < n; i++) {
            System.out.println("\n--- Process " + (i + 1) + " ---");
            System.out.print("Name: ");
            String name = scanner.next();
            System.out.print("Arrival Time: ");
            int arrival = scanner.nextInt();
            System.out.print("Burst Time: ");
            int burst = scanner.nextInt();
            System.out.print("Priority: ");
            int priority = scanner.nextInt();

            processes.add(new Process(name, arrival, burst, priority));
        }

        return processes;
    }

    public int readInt(String prompt) {
        System.out.print(prompt);
        return scanner.nextInt();
    }
}