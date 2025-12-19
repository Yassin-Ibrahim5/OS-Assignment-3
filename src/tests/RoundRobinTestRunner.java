package tests;

import models.Process;
import schedulers.RoundRobinScheduler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RoundRobinTestRunner {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String[] testFiles = {
                "test_cases/Other_Schedulers/test_1.json",
                "test_cases/Other_Schedulers/test_2.json",
                "test_cases/Other_Schedulers/test_3.json",
                "test_cases/Other_Schedulers/test_4.json",
                "test_cases/Other_Schedulers/test_5.json",
                "test_cases/Other_Schedulers/test_6.json"
        };

        while (true) {
            System.out.println("\n========================================");
            System.out.println("Select a Round Robin Test Case to Run:");
            for (int i = 0; i < testFiles.length; i++) {
                System.out.println((i + 1) + ". " + testFiles[i].replace("test_cases/Other_Schedulers/", ""));
            }
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
                continue;
            }

            if (choice == 0) break;
            if (choice < 1 || choice > testFiles.length) {
                System.out.println("Invalid choice.");
                continue;
            }

            String selectedFile = testFiles[choice - 1];
            runTest(selectedFile);
        }
        scanner.close();
    }

    public static void runTest(String fileName) {
        System.out.println("\nLoading " + fileName + "...");
        String jsonContent;
        try {
            jsonContent = new String(Files.readAllBytes(Paths.get(fileName)));
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.err.println("Make sure " + fileName + " is in the correct location.");
            return;
        }

        // Parse test name
        String testName = parseTestName(jsonContent);
        System.out.println("Test: " + testName);

        // Parse Input Processes
        List<Process> processes = parseProcesses(jsonContent);
        if (processes.isEmpty()) {
            System.out.println("No processes found in JSON.");
            return;
        }

        // Parse Parameters
        int rrQuantum = parseParameter(jsonContent, "rrQuantum");
        int contextSwitch = parseParameter(jsonContent, "contextSwitch");

        if (rrQuantum == -1) {
            System.out.println("rrQuantum not found in JSON.");
            return;
        }

        System.out.println("RR Quantum: " + rrQuantum);
        System.out.println("Context Switch: " + contextSwitch);

        // Run Scheduler
        RoundRobinScheduler scheduler = new RoundRobinScheduler(processes, contextSwitch, rrQuantum);
        scheduler.schedule();

        // Parse Expected Results for RR
        Map<String, ExpectedResult> expectedMap = parseExpectedResults(jsonContent);
        List<String> expectedOrder = parseExecutionOrder(jsonContent);
        double expectedAvgWait = parseAverage(jsonContent, "averageWaitingTime");
        double expectedAvgTurn = parseAverage(jsonContent, "averageTurnaroundTime");

        // Compare Results
        System.out.println("\n--- Test Results: Round Robin ---");
        boolean allPassed = true;

        List<Process> actualProcesses = scheduler.getProcesses();
        actualProcesses.sort(Comparator.comparing(Process::getName));

        System.out.printf("%-8s | %-10s | %-12s | %-15s | %-15s | %s%n",
                "Process", "Arrival", "Burst", "Wait Time", "Turnaround", "Status");
        System.out.println("------------------------------------------------------------------------------------");

        for (Process p : actualProcesses) {
            ExpectedResult exp = expectedMap.get(p.getName());
            if (exp == null) {
                System.out.println(p.getName() + " not found in expected output.");
                allPassed = false;
                continue;
            }

            boolean waitPass = p.getWaitingTime() == exp.waitingTime;
            boolean turnPass = p.getTurnaroundTime() == exp.turnaroundTime;

            String status = (waitPass && turnPass) ? "[PASS]" : "[FAIL]";
            if (!waitPass || !turnPass) allPassed = false;

            System.out.printf("%-8s | %-10d | %-12d | %-15s | %-15s | %s%n",
                    p.getName(),
                    p.getArrivalTime(),
                    p.getBurstTime(),
                    p.getWaitingTime() + " (Exp:" + exp.waitingTime + ")",
                    p.getTurnaroundTime() + " (Exp:" + exp.turnaroundTime + ")",
                    status
            );
        }

        // Calculate and display averages
        double avgWait = actualProcesses.stream()
                .mapToInt(Process::getWaitingTime)
                .average()
                .orElse(0.0);
        double avgTurnaround = actualProcesses.stream()
                .mapToInt(Process::getTurnaroundTime)
                .average()
                .orElse(0.0);

        System.out.println("------------------------------------------------------------------------------------");
        System.out.printf("Average Waiting Time: %.1f (Expected: %.1f) %s%n",
                avgWait, expectedAvgWait,
                (Math.abs(avgWait - expectedAvgWait) < 0.1) ? "[PASS]" : "[FAIL]");
        System.out.printf("Average Turnaround Time: %.1f (Expected: %.1f) %s%n",
                avgTurnaround, expectedAvgTurn,
                (Math.abs(avgTurnaround - expectedAvgTurn) < 0.1) ? "[PASS]" : "[FAIL]");

        if (Math.abs(avgWait - expectedAvgWait) >= 0.1 || Math.abs(avgTurnaround - expectedAvgTurn) >= 0.1) {
            allPassed = false;
        }

        // Check execution order if available
        if (!expectedOrder.isEmpty()) {
            System.out.println("\nExpected Execution Order: " + expectedOrder);
            System.out.println("(Note: Verify manually or implement execution tracking)");
        }

        System.out.println("------------------------------------------------------------------------------------");
        if (allPassed) {
            System.out.println("✅ RESULT: TEST PASSED");
        } else {
            System.out.println("❌ RESULT: TEST FAILED");
        }
    }

    private static String parseTestName(String json) {
        Pattern p = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return "Unknown Test";
    }

    private static List<Process> parseProcesses(String json) {
        List<Process> list = new ArrayList<>();

        // Find the input section
        int inputStart = json.indexOf("\"input\"");
        int processesStart = json.indexOf("\"processes\"", inputStart);
        if (processesStart == -1) return list;

        Pattern p = Pattern.compile("\\{\\s*\"name\"\\s*:\\s*\"([^\"]+)\",\\s*\"arrival\"\\s*:\\s*(\\d+),\\s*\"burst\"\\s*:\\s*(\\d+),\\s*\"priority\"\\s*:\\s*(\\d+)\\s*}");
        Matcher m = p.matcher(json.substring(processesStart));

        while (m.find()) {
            String name = m.group(1);
            int arrival = Integer.parseInt(m.group(2));
            int burst = Integer.parseInt(m.group(3));
            int priority = Integer.parseInt(m.group(4));

            list.add(new Process(name, arrival, burst, priority));
        }
        return list;
    }

    private static int parseParameter(String json, String paramName) {
        int inputStart = json.indexOf("\"input\"");
        if (inputStart == -1) return -1;

        String inputSection = json.substring(inputStart, json.indexOf("\"expectedOutput\""));
        Pattern p = Pattern.compile("\"" + paramName + "\"\\s*:\\s*(\\d+)");
        Matcher m = p.matcher(inputSection);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return paramName.equals("contextSwitch") ? 0 : -1;
    }

    private static Map<String, ExpectedResult> parseExpectedResults(String json) {
        Map<String, ExpectedResult> map = new HashMap<>();

        // Find RR section in expectedOutput
        int rrStart = json.indexOf("\"RR\"");
        if (rrStart == -1) return map;

        int resultsStart = json.indexOf("\"processResults\"", rrStart);
        if (resultsStart == -1) return map;

        // Find the end of RR section (next scheduler or end of expectedOutput)
        int rrEnd = json.indexOf("\"Priority\"", rrStart);
        if (rrEnd == -1) rrEnd = json.indexOf("}", resultsStart + 100);

        String rrSection = json.substring(resultsStart, rrEnd);

        Pattern p = Pattern.compile("\\{\\s*\"name\"\\s*:\\s*\"([^\"]+)\",\\s*\"waitingTime\"\\s*:\\s*(\\d+),\\s*\"turnaroundTime\"\\s*:\\s*(\\d+)\\s*}");
        Matcher m = p.matcher(rrSection);

        while (m.find()) {
            String name = m.group(1);
            int wait = Integer.parseInt(m.group(2));
            int turn = Integer.parseInt(m.group(3));

            map.put(name, new ExpectedResult(name, wait, turn));
        }
        return map;
    }

    private static List<String> parseExecutionOrder(String json) {
        List<String> order = new ArrayList<>();

        int rrStart = json.indexOf("\"RR\"");
        if (rrStart == -1) return order;

        int orderStart = json.indexOf("\"executionOrder\"", rrStart);
        if (orderStart == -1) return order;

        Pattern p = Pattern.compile("\"executionOrder\"\\s*:\\s*\\[([^\\]]+)\\]");
        Matcher m = p.matcher(json.substring(rrStart));

        if (m.find()) {
            String orderStr = m.group(1);
            Pattern namePattern = Pattern.compile("\"([^\"]+)\"");
            Matcher nameMatcher = namePattern.matcher(orderStr);
            while (nameMatcher.find()) {
                order.add(nameMatcher.group(1));
            }
        }
        return order;
    }

    private static double parseAverage(String json, String avgName) {
        int rrStart = json.indexOf("\"RR\"");
        if (rrStart == -1) return 0.0;

        Pattern p = Pattern.compile("\"" + avgName + "\"\\s*:\\s*([\\d.]+)");
        Matcher m = p.matcher(json.substring(rrStart));
        if (m.find()) {
            return Double.parseDouble(m.group(1));
        }
        return 0.0;
    }

    static class ExpectedResult {
        String name;
        int waitingTime;
        int turnaroundTime;

        public ExpectedResult(String name, int waitingTime, int turnaroundTime) {
            this.name = name;
            this.waitingTime = waitingTime;
            this.turnaroundTime = turnaroundTime;
        }
    }
}