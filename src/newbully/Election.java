package newbully;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.locks.ReentrantLock;

public class Election {

	public static ReentrantLock pingLock = new ReentrantLock();
	public static ReentrantLock electionLock = new ReentrantLock();
	private static boolean electionFlag = false; //By default no election is going on
	private static boolean pingFlag = true; //By default I am allowed to ping 
	public static Process electionDetector;
	public static ArrayList<Integer> priorities=new ArrayList<Integer>();
	public static int[] coordgroup = new int[3];
	public static int[] downflag;
	public static int existsingroup=-1;

	public static int nextCoOrdinatorGroup(int number) {
		for (int i=0;i<2;i++){
			if (coordgroup[i]==number){
				return coordgroup[i+1];
			}
		}
		return -1;
	}
	public static int firstCoOrdinatorGroup() {
		return coordgroup[0];
	}
	public static int getCoOrdGroupMemb(int number) {
		return coordgroup[number];
	}

	public static void setCoOrdinatorGroup(int number,int value) {
		coordgroup[number] = value;
	}



	public static Process getElectionDetector() {
		return electionDetector;
	}

	public static void setElectionDetector(Process electionDetector) {
		Election.electionDetector = electionDetector;
	}

	public static boolean isPingFlag() {
		return pingFlag;
	}

	public static void setPingFlag(boolean pingFlag) {
		Election.pingFlag = pingFlag;
	}

	public static boolean isElectionFlag() {
		return electionFlag;
	}

	public static void setElectionFlag(boolean electionFlag) {
		Election.electionFlag = electionFlag;
	}

	public static void initialElection(RunningThread[] t) {
		downflag = new int[t.length];
		Process temp = new Process(-1, -1);
		for (int i = 0; i < t.length; i++) {
			priorities.add(t[i].getProcess().getPriority());
			downflag[i]=0;
			if (temp.getPriority() < t[i].getProcess().getPriority()) {
				temp = t[i].getProcess();
			}
		}

		t[temp.pid - 1].getProcess().CoOrdinatorFlag = true;
		Collections.sort(priorities);


		int total=t.length;
		for (int j = 0; j < 3; j++) {
			setCoOrdinatorGroup(j,priorities.get(--total));
		}

		System.out.println("Process "+ temp.pid +" choosen as Coordinator");
		System.out.print("Coordinator Group is :");
		for (int j = 0; j<3; j++) {
			System.out.print("- " + getCoOrdGroupMemb(j) +" ");			
		}
		System.out.println("");					
	}
}