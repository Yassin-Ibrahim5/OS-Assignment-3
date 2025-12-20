package models;

import java.util.ArrayList;
import java.util.List;

public class Process {
    private String name;
    private int arrivalTime;
    private int burstTime;
    private int remainingTime;
    private int priority;
    private int quantum;
    private int waitingTime;
    private int turnaroundTime;
    private int completionTime;
    private List<Integer> quantumHistory;
    private List<String> executionOrder;

    public Process(String name, int arrivalTime, int burstTime, int priority) {
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.priority = priority;
        this.quantum = 0;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
        this.completionTime = 0;
        this.quantumHistory = new ArrayList<>();
        this.executionOrder = new ArrayList<>();
    }

    // Constructor with quantum for AG Scheduler
    public Process(String name, int arrivalTime, int burstTime, int priority, int quantum) {
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.priority = priority;
        this.quantum = quantum;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
        this.completionTime = 0;
        this.quantumHistory = new ArrayList<>();
        this.quantumHistory.add(quantum);
        this.executionOrder = new ArrayList<>();
    }

    // Getters
    public String getName() {
        return name;
    }
    public int getArrivalTime() {
        return arrivalTime;
    }
    public int getBurstTime() {
        return burstTime;
    }
    public int getRemainingTime() {
        return remainingTime;
    }
    public int getPriority() {
        return priority;
    }
    public int getQuantum() {
        return quantum;
    }
    public int getWaitingTime() {
        return waitingTime;
    }
    public int getTurnaroundTime() {
        return turnaroundTime;
    }
    public int getCompletionTime() {
        return completionTime;
    }
    public List<Integer> getQuantumHistory() {
        return quantumHistory;
    }
    public List<String> getExecutionOrder() {
        return executionOrder;
    }

    // Setters
    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }
    public void setQuantum(int quantum) {
        this.quantum = quantum;
    }
    public void setPriority(int priority) {
        this.priority = priority;
    }
    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }
    public void setTurnaroundTime(int turnaroundTime) {
        this.turnaroundTime = turnaroundTime;
    }
    public void setCompletionTime(int completionTime) {
        this.completionTime = completionTime;
    }
    public void setQuantumHistory(List<Integer> quantumHistory) {
        this.quantumHistory = quantumHistory;
    }
    public void setExecutionOrder(List<String> executionOrder) {
        this.executionOrder = executionOrder;
    }

    public void addQuantumToHistory(int quantum) {
        quantumHistory.add(quantum);
    }
    public boolean isComplete() {
        return remainingTime == 0;
    }
    // Add these to your existing Process class
    private int startTime = -1;

    public boolean hasStarted() {
        return startTime != -1;
    }

    public void setStartTime(int time) {
        if (this.startTime == -1) this.startTime = time;
    }

    public void executeFor(int units) {
        this.remainingTime -= units;
    }
}
