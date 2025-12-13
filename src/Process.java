import java.util.List;

public class Process {
    public String processName;
    public Integer arrivalTime;
    public Integer burstTime;
    public Integer initialPriority;
    public Integer initialQuantum;

    public Integer remainingTime;
    public Integer currentQuantum;
    public Integer currentPriority;
    public List<Integer> quantumHistory;
    public Integer completionTime;

}
