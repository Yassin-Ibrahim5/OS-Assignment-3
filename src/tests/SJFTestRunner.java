package tests;

import models.Process;
import schedulers.PreemptiveSJF;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SJFTestRunner {

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
            System.out.println("SJF Test Runner - Select a Test Case:");
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
        System.out.println("\n========================================");
        System.out.println("Loading " + fileName + " for SJF...");
        System.out.println("========================================");

        String jsonContent;
        try {
            jsonContent = new String(Files.readAllBytes(Paths.get(fileName)));
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.err.println("Make sure " + fileName + " is in the project root folder.");
            return;
        }

        // 1. Parse Input
        List<Process> processes = parseProcesses(jsonContent);
        if (processes.isEmpty()) {
            System.out.println("No processes found in JSON.");
            return;
        }

        int contextSwitch = parseContextSwitch(jsonContent);
        System.out.println("Context Switch Time: " + contextSwitch);
        System.out.println("Number of Processes: " + processes.size());

        // 2. Run Scheduler
        PreemptiveSJF scheduler = new PreemptiveSJF(processes, contextSwitch);
        scheduler.schedule();

        // 3. Parse Expected Results
        Map<String, ExpectedResult> expectedMap = parseExpectedResults(jsonContent);
        List<String> expectedExecutionOrder = parseExpectedExecutionOrder(jsonContent);
        double expectedAvgWait = parseExpectedAverage(jsonContent, "averageWaitingTime");
        double expectedAvgTurnaround = parseExpectedAverage(jsonContent, "averageTurnaroundTime");

        List<String> actualExecutionOrder = scheduler.getExecutionOrder();

        // 4. Check Execution Order
        System.out.println("\n--- Execution Order Check ---");
        System.out.println("Actual:   " + actualExecutionOrder);
        System.out.println("Expected: " + expectedExecutionOrder);

        boolean orderPassed = actualExecutionOrder.equals(expectedExecutionOrder);
        System.out.println("Order Status: " + (orderPassed ? "✅ [PASS]" : "❌ [FAIL]"));

        // 5. Compare Process Results
        System.out.println("\n--- Process Metrics Check ---");
        boolean metricsPassed = true;

        List<Process> actualProcesses = scheduler.getProcesses();
        actualProcesses.sort(Comparator.comparing(Process::getName));

        System.out.printf("%-8s | %-18s | %-18s | %-18s | %s%n",
                "Process", "Waiting Time", "Turnaround Time", "Completion Time", "Status");
        System.out.println("---------------------------------------------------------------------------------------------");

        for (Process p : actualProcesses) {
            ExpectedResult exp = expectedMap.get(p.getName());
            if (exp == null) {
                System.out.println(p.getName() + " not found in expected output.");
                metricsPassed = false;
                continue;
            }

            boolean waitPass = p.getWaitingTime() == exp.waitingTime;
            boolean turnPass = p.getTurnaroundTime() == exp.turnaroundTime;

            String status = (waitPass && turnPass) ? "✅ [PASS]" : "❌ [FAIL]";
            if (!waitPass || !turnPass) metricsPassed = false;

            System.out.printf("%-8s | %5d (Exp: %5d) | %6d (Exp: %5d) | %6d              | %s%n",
                    p.getName(),
                    p.getWaitingTime(), exp.waitingTime,
                    p.getTurnaroundTime(), exp.turnaroundTime,
                    p.getCompletionTime(),
                    status
            );
        }

        // 6. Check Averages
        double actualAvgWait = actualProcesses.stream()
                .mapToInt(Process::getWaitingTime)
                .average()
                .orElse(0.0);
        double actualAvgTurnaround = actualProcesses.stream()
                .mapToInt(Process::getTurnaroundTime)
                .average()
                .orElse(0.0);

        boolean avgWaitPass = Math.abs(actualAvgWait - expectedAvgWait) < 0.01;
        boolean avgTurnPass = Math.abs(actualAvgTurnaround - expectedAvgTurnaround) < 0.01;

        System.out.println("---------------------------------------------------------------------------------------------");
        System.out.printf("Average Waiting Time:    %.2f (Expected: %.2f) %s%n",
                actualAvgWait, expectedAvgWait, avgWaitPass ? "✅" : "❌");
        System.out.printf("Average Turnaround Time: %.2f (Expected: %.2f) %s%n",
                actualAvgTurnaround, expectedAvgTurnaround, avgTurnPass ? "✅" : "❌");

        // 7. Final Result
        System.out.println("\n========================================");
        if (orderPassed && metricsPassed && avgWaitPass && avgTurnPass) {
            System.out.println("✅ OVERALL RESULT: TEST PASSED");
        } else {
            System.out.println("❌ OVERALL RESULT: TEST FAILED");
            if (!orderPassed) System.out.println("   - Execution order mismatch");
            if (!metricsPassed) System.out.println("   - Process metrics mismatch");
            if (!avgWaitPass || !avgTurnPass) System.out.println("   - Average metrics mismatch");
        }
        System.out.println("========================================");
    }

    private static List<Process> parseProcesses(String json) {
        List<Process> list = new ArrayList<>();
        Pattern p = Pattern.compile("\\{\\s*\"name\"\\s*:\\s*\"([^\"]+)\",\\s*\"arrival\"\\s*:\\s*(\\d+),\\s*\"burst\"\\s*:\\s*(\\d+),\\s*\"priority\"\\s*:\\s*(\\d+)\\s*}");
        Matcher m = p.matcher(json);

        while (m.find()) {
            String name = m.group(1);
            int arrival = Integer.parseInt(m.group(2));
            int burst = Integer.parseInt(m.group(3));
            int priority = Integer.parseInt(m.group(4));
            list.add(new Process(name, arrival, burst, priority));
        }
        return list;
    }

    private static int parseContextSwitch(String json) {
        Pattern p = Pattern.compile("\"contextSwitch\"\\s*:\\s*(\\d+)");
        Matcher m = p.matcher(json);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return 0;
    }

    private static Map<String, ExpectedResult> parseExpectedResults(String json) {
        Map<String, ExpectedResult> map = new HashMap<>();

        int sjfStart = json.indexOf("\"SJF\"");
        if (sjfStart == -1) return map;

        int sjfEnd = json.indexOf("\"RR\"", sjfStart);
        if (sjfEnd == -1) sjfEnd = json.indexOf("\"Priority\"", sjfStart);
        if (sjfEnd == -1) sjfEnd = json.length();

        String sjfSection = json.substring(sjfStart, sjfEnd);

        Pattern p = Pattern.compile("\\{\\s*\"name\"\\s*:\\s*\"([^\"]+)\",\\s*\"waitingTime\"\\s*:\\s*(\\d+),\\s*\"turnaroundTime\"\\s*:\\s*(\\d+)\\s*}");
        Matcher m = p.matcher(sjfSection);

        while (m.find()) {
            String name = m.group(1);
            int wait = Integer.parseInt(m.group(2));
            int turn = Integer.parseInt(m.group(3));
            map.put(name, new ExpectedResult(name, wait, turn));
        }
        return map;
    }

    private static List<String> parseExpectedExecutionOrder(String json) {
        List<String> order = new ArrayList<>();

        int sjfStart = json.indexOf("\"SJF\"");
        if (sjfStart == -1) return order;

        int sjfEnd = json.indexOf("\"RR\"", sjfStart);
        if (sjfEnd == -1) sjfEnd = json.indexOf("\"Priority\"", sjfStart);
        if (sjfEnd == -1) sjfEnd = json.length();

        String sjfSection = json.substring(sjfStart, sjfEnd);

        Pattern p = Pattern.compile("\"executionOrder\"\\s*:\\s*\\[(.*?)]");
        Matcher m = p.matcher(sjfSection);
        if (m.find()) {
            String content = m.group(1);
            String[] elements = content.split(",");
            for (String element : elements) {
                String cleaned = element.trim().replace("\"", "");
                if (!cleaned.isEmpty()) {
                    order.add(cleaned);
                }
            }
        }
        return order;
    }

    private static double parseExpectedAverage(String json, String key) {
        int sjfStart = json.indexOf("\"SJF\"");
        if (sjfStart == -1) return 0.0;

        int sjfEnd = json.indexOf("\"RR\"", sjfStart);
        if (sjfEnd == -1) sjfEnd = json.indexOf("\"Priority\"", sjfStart);
        if (sjfEnd == -1) sjfEnd = json.length();

        String sjfSection = json.substring(sjfStart, sjfEnd);

        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*([0-9.]+)");
        Matcher m = p.matcher(sjfSection);
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