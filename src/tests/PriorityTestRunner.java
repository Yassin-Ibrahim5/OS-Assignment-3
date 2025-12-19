package tests;

import models.Process;
import schedulers.PriorityScheduler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PriorityTestRunner {

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
            System.out.println("Select a Test Case to Run:");
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
            runTest(testFiles[choice - 1]);
        }
        scanner.close();
    }

    public static void runTest(String filename) {
        System.out.println("\nLoading " + filename + "...");
        String jsonContent;
        try {
            jsonContent = new String(Files.readAllBytes(Paths.get(filename)));
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return;
        }
        int contextSwitchTime = parseSingleInt(jsonContent, "contextSwitch");
        int agingInterval = parseSingleInt(jsonContent, "agingInterval");
        List<Process> processes = parseProcesses(jsonContent);
        if (processes.isEmpty()) {
            System.err.println("No processes found!");
            return;
        }

        PriorityScheduler scheduler = new PriorityScheduler(processes, contextSwitchTime, agingInterval);
        scheduler.schedule();

        Map<String, ExpectedResult> expectedResultMap = parsePriorityResults(jsonContent);
        List<String> expectedExecutionOrder = parsePriorityExecutionOrder(jsonContent);
        double expectedAvgWait = parseExpectedAverage(jsonContent, "averageWaitingTime");
        double expectedAvgTurn = parseExpectedAverage(jsonContent, "averageTurnaroundTime");

        List<String> actualExecutionOrder = scheduler.getExecutionOrder();

        System.out.println("\n--- Execution Order Check ---");
        System.out.println("Actual:   " + actualExecutionOrder);
        System.out.println("Expected: " + expectedExecutionOrder);

        boolean orderPassed = actualExecutionOrder.equals(expectedExecutionOrder);
        System.out.println("Order Status: " + (orderPassed ? "[PASS]" : "[FAIL]"));

        System.out.println("\n--- Test Results: " + filename + " ---");
        boolean metricsPassed = true;
        List<Process> actualProcesses = scheduler.getProcesses();
        actualProcesses.sort(Comparator.comparing(Process::getName));

        System.out.printf("%-5s | %-18s | %-18s | %s%n", "PID", "Wait Time", "Turnaround", "Status");
        System.out.println("--------------------------------------------------------------------------------");

        for (Process p : actualProcesses) {
            ExpectedResult expectedResult = expectedResultMap.get(p.getName());
            if (expectedResult == null) {
                continue;
            }
            boolean waitPass = (p.getWaitingTime() == expectedResult.waitingTime);
            boolean turnPass = (p.getTurnaroundTime() == expectedResult.turnaroundTime);
            String status = (waitPass && turnPass) ? "[PASS]" : "[FAIL]";
            if (!waitPass || !turnPass) {
                metricsPassed = false;
            }
            System.out.printf("%-8s | %-10d | %-12d | %-15s | %-15s | %s%n",
                    p.getName(),
                    p.getArrivalTime(),
                    p.getBurstTime(),
                    p.getWaitingTime() + " (Exp:" + expectedResult.waitingTime + ")",
                    p.getTurnaroundTime() + " (Exp:" + expectedResult.turnaroundTime + ")",
                    status
            );
        }

        double actualAvgWaitTime = actualProcesses.stream()
                .mapToInt(Process::getWaitingTime)
                .average()
                .orElse(0.0);

        double actualAvgTurnaroundTime = actualProcesses.stream()
                .mapToInt(Process::getTurnaroundTime)
                .average()
                .orElse(0.0);

        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("Average Waiting Time: %.1f (Expected: %.1f) %s%n",
                actualAvgWaitTime, expectedAvgWait, (Math.abs(actualAvgWaitTime - expectedAvgWait) < 0.1) ? "[PASS]" : "[FAIL]");
        System.out.printf("Average Turnaround Time: %.1f (Expected: %.1f) %s%n",
                actualAvgTurnaroundTime, expectedAvgTurn, (Math.abs(actualAvgTurnaroundTime - expectedAvgTurn) < 0.1) ? "[PASS]" : "[FAIL]");
        if (Math.abs(actualAvgWaitTime - expectedAvgWait) >= 0.1 || Math.abs(actualAvgTurnaroundTime - expectedAvgTurn) >= 0.1) {
            metricsPassed = false;
        }
        if (metricsPassed && orderPassed) {
            System.out.println("✅ RESULT: TEST PASSED");
        } else {
            System.out.println("❌ RESULT: TEST FAILED");
        }
    }

    private static int parseSingleInt(String json, String key) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*(\\d+)");
        Matcher m = p.matcher(json);
        return m.find() ? Integer.parseInt(m.group(1)) : -1;
    }

    private static List<Process> parseProcesses(String json) {
        List<Process> processes = new ArrayList<>();
        Pattern p = Pattern.compile("\\{\\s*\"name\"\\s*:\\s*\"([^\"]+)\",\\s*\"arrival\"\\s*:\\s*(\\d+),\\s*\"burst\"\\s*:\\s*(\\d+),\\s*\"priority\"\\s*:\\s*(\\d+)\\s*}");
        Matcher m = p.matcher(json);
        while (m.find()) {
            processes.add(new Process(m.group(1), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)), Integer.parseInt(m.group(4))));
        }
        return processes;
    }

    private static Map<String, ExpectedResult> parsePriorityResults(String json) {
        Map<String, ExpectedResult> map = new HashMap<>();
        int startPriority = json.indexOf("\"Priority\"");
        if (startPriority == -1) {
            return map;
        }
        String prioritySection = json.substring(startPriority);
        Pattern p = Pattern.compile("\\{\\s*\"name\"\\s*:\\s*\"([^\"]+)\",\\s*\"waitingTime\"\\s*:\\s*(\\d+),\\s*\"turnaroundTime\"\\s*:\\s*(\\d+)\\s*}");
        Matcher m = p.matcher(prioritySection);
        while (m.find()) {
            map.put(m.group(1), new ExpectedResult(m.group(1), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3))));
        }
        return map;
    }

    private static List<String> parsePriorityExecutionOrder(String json) {
        List<String> order = new ArrayList<>();
        int startPriority = json.indexOf("\"Priority\"");
        if (startPriority == -1) {
            return order;
        }
        String prioritySection = json.substring(startPriority);
        Pattern p = Pattern.compile("\"executionOrder\"\\s*:\\s*\\[(.*?)]");
        Matcher m = p.matcher(prioritySection);
        if (m.find()) {
            String content = m.group(1);
            for (String element : content.split(",")) {
                order.add(element.trim().replace("\"", ""));
            }
        }
        return order;
    }

    private static double parseExpectedAverage(String json, String key) {
        int priorityStart = json.indexOf("\"Priority\"");
        if (priorityStart == -1) return 0.0;

        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*([\\d.]+)");
        Matcher m = p.matcher(json.substring(priorityStart));
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
