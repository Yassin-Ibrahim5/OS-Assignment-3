package tests;

import models.Process;
import schedulers.AGScheduler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AGTestRunner {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String[] testFiles = {
                "test_cases/AG/AG_test1.json",
                "test_cases/AG/AG_test2.json",
                "test_cases/AG/AG_test3.json",
                "test_cases/AG/AG_test4.json",
                "test_cases/AG/AG_test5.json",
                "test_cases/AG/AG_test6.json"};

        while (true) {
            System.out.println("\n========================================");
            System.out.println("Select a Test Case to Run:");
            for (int i = 0; i < testFiles.length; i++) {
                System.out.println((i + 1) + ". " + testFiles[i].replace("test_cases/AG/", ""));
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
            System.err.println("Make sure " + fileName + " is in the project root folder.");
            return;
        }

        List<Process> processes = parseProcesses(jsonContent);
        if (processes.isEmpty()) {
            System.out.println("No processes found in JSON.");
            return;
        }

        AGScheduler scheduler = new AGScheduler(processes, 0);
        scheduler.schedule();

        Map<String, ExpectedResult> expectedMap = parseExpectedResults(jsonContent);
        List<String> expectedExecutionOrder = parseExpectedExecutionOrder(jsonContent);

        List<String> actualExecutionOrder = scheduler.getExecutionOrder();

        System.out.println("\n--- Execution Order Check ---");
        System.out.println("Actual:   " + actualExecutionOrder);
        System.out.println("Expected: " + expectedExecutionOrder);

        boolean orderPassed = actualExecutionOrder.equals(expectedExecutionOrder);
        System.out.println("Order Status: " + (orderPassed ? "[PASS]" : "[FAIL]"));

        System.out.println("\n--- Test Results: " + fileName + " ---");
        boolean metricsPassed = true;

        List<Process> actualProcesses = scheduler.getProcesses();
        actualProcesses.sort(Comparator.comparing(Process::getName));

        System.out.printf("%-5s | %-15s | %-15s | %-30s | %s%n", "PID", "Wait Time", "Turnaround", "Quantum History", "Status");
        System.out.println("------------------------------------------------------------------------------------------------");

        for (Process p : actualProcesses) {
            ExpectedResult exp = expectedMap.get(p.getName());
            if (exp == null) {
                System.out.println(p.getName() + " not found in expected output.");
                continue;
            }

            boolean waitPass = p.getWaitingTime() == exp.waitingTime;
            boolean turnPass = p.getTurnaroundTime() == exp.turnaroundTime;
            boolean histPass = p.getQuantumHistory().equals(exp.quantumHistory);

            String status = (waitPass && turnPass && histPass) ? "[PASS]" : "[FAIL]";
            if (!waitPass || !turnPass || !histPass) metricsPassed = false;

            System.out.printf("%-5s | %-15s | %-15s | %-30s | %s%n", p.getName(), p.getWaitingTime() + " (Exp:" + exp.waitingTime + ")", p.getTurnaroundTime() + " (Exp:" + exp.turnaroundTime + ")", p.getQuantumHistory().toString(), status);

            if (!histPass) {
                System.out.println("      -> Expected Hist: " + exp.quantumHistory);
            }
        }
        System.out.println("------------------------------------------------------------------------------------------------");
        if (metricsPassed && orderPassed) {
            System.out.println("✅ RESULT: TEST PASSED");
        } else {
            System.out.println("❌ RESULT: TEST FAILED");
        }
    }

    private static List<Process> parseProcesses(String json) {
        List<Process> list = new ArrayList<>();
        Pattern p = Pattern.compile("\\{\\s*\"name\"\\s*:\\s*\"([^\"]+)\",\\s*\"arrival\"\\s*:\\s*(\\d+),\\s*\"burst\"\\s*:\\s*(\\d+),\\s*\"priority\"\\s*:\\s*(\\d+),\\s*\"quantum\"\\s*:\\s*(\\d+)\\s*}");
        Matcher m = p.matcher(json);

        while (m.find()) {
            String name = m.group(1);
            int arrival = Integer.parseInt(m.group(2));
            int burst = Integer.parseInt(m.group(3));
            int priority = Integer.parseInt(m.group(4));
            int quantum = Integer.parseInt(m.group(5));
            list.add(new Process(name, arrival, burst, priority, quantum));
        }
        return list;
    }

    private static Map<String, ExpectedResult> parseExpectedResults(String json) {
        Map<String, ExpectedResult> map = new HashMap<>();

        int startResults = json.indexOf("\"processResults\"");
        if (startResults == -1) return map;
        String resultSection = json.substring(startResults);

        Pattern p = Pattern.compile("\\{\\s*\"name\"\\s*:\\s*\"([^\"]+)\",\\s*\"waitingTime\"\\s*:\\s*(\\d+),\\s*\"turnaroundTime\"\\s*:\\s*(\\d+),\\s*\"quantumHistory\"\\s*:\\s*\\[(.*?)]\\s*}");
        Matcher m = p.matcher(resultSection);

        while (m.find()) {
            String name = m.group(1);
            int wait = Integer.parseInt(m.group(2));
            int turn = Integer.parseInt(m.group(3));
            String histString = m.group(4);

            List<Integer> hist = new ArrayList<>();
            if (!histString.trim().isEmpty()) {
                String[] nums = histString.split(",");
                for (String num : nums) {
                    hist.add(Integer.parseInt(num.trim()));
                }
            }

            map.put(name, new ExpectedResult(name, wait, turn, hist));
        }
        return map;
    }

    private static List<String> parseExpectedExecutionOrder(String json) {
        List<String> order = new ArrayList<>();
        Pattern p = Pattern.compile("\"executionOrder\"\\s*:\\s*\\[(.*?)]");
        Matcher m = p.matcher(json);
        if (m.find()) {
            String content = m.group(1);
            String[] elements = content.split(",");
            for (String element : elements) {
                order.add(element.trim().replace("\"", ""));
            }
        }
        return order;
    }

    static class ExpectedResult {
        String name;
        int waitingTime;
        int turnaroundTime;
        List<Integer> quantumHistory;

        public ExpectedResult(String name, int waitingTime, int turnaroundTime, List<Integer> quantumHistory) {
            this.name = name;
            this.waitingTime = waitingTime;
            this.turnaroundTime = turnaroundTime;
            this.quantumHistory = quantumHistory;
        }
    }
}