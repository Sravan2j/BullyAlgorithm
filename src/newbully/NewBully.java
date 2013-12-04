package newbully;

import java.util.Scanner;

public class NewBully {

    public static void main(String[] args) {
    	System.out.println("Enter the number of nodes");
		Scanner in = new Scanner(System.in);		 
        //int total_processes = 6;
		int total_processes = in.nextInt();
        RunningThread[] t = new RunningThread[total_processes];
        for (int i = 0; i < total_processes; i++) {
            t[i] = new RunningThread(new Process(i+1, i+1), total_processes);//passing process id, priority, total no. of processes to running thread
        }
        try {
            Election.initialElection(t);
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
        for (int i = 0; i < total_processes; i++) {
        	
        	Thread thread = new Thread(t[i]);
        	thread.setName("Client-" + (i+1));        	
            thread.start();//start every thread
        }
    }
}