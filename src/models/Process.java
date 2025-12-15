package models;

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
    }

    // Getters
    public String getName() { return name; }
    public int getArrivalTime() { return arrivalTime; }
    public int getBurstTime() { return burstTime; }
    public int getRemainingTime() { return remainingTime; }
    public int getPriority() { return priority; }
    public int getQuantum() { return quantum; }
    public int getWaitingTime() { return waitingTime; }
    public int getTurnaroundTime() { return turnaroundTime; }
    public int getCompletionTime() { return completionTime; }

    // Setters
    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
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

    public boolean isComplete() {
        return remainingTime == 0;
    }
}