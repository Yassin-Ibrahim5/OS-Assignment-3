import models.Process;
import schedulers.*;
import utils.InputParser;
import utils.OutputFormatter;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        while (true) {

            System.out.println("CPU Schedulers Simulator\n");

            InputParser parser = new InputParser();

            // Get input
            List<Process> processes = parser.parseProcesses();
            int rrQuantum = parser.readInt("Round Robin time quantum: ");
            int contextSwitch = parser.readInt("Context switch time: ");

            // Menu
            System.out.println("\n1. Preemptive SJF");
            System.out.println("2. Round Robin");
            System.out.println("3. Priority Scheduling");
            System.out.println("4. AG Scheduling");
            System.out.println("5. exit");
            int choice = parser.readInt("Choose (1-4): ");

            // Run selected scheduler
            SchedulerBase scheduler = null;

            if (choice == 1) {
                scheduler = new PreemptiveSJF(processes, contextSwitch);
            } else if (choice == 2) {
                scheduler = new RoundRobinScheduler(processes, contextSwitch, rrQuantum);
            } else if (choice == 3) {
                scheduler = new PriorityScheduler(processes, contextSwitch);
            } else if (choice == 4) {
                scheduler = new AGScheduler(processes, contextSwitch);
            } else if(choice == 5) {
                break;
            }

            if (scheduler != null) {
                scheduler.schedule();
                OutputFormatter.printResults(scheduler);
            }
        }
    }
}