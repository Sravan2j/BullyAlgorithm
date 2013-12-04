package newbully;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

public class RunningThread implements Runnable {

	private Process process;
	private int total_processes;
	//public int existsingroup=-1;
	private static boolean messageFlag[];
	ServerSocket[] sock;
	Random r;

	public Process getProcess() {
		return process;
	}

	public void setProcess(Process process) {
		this.process = process;
	}

	public RunningThread(Process process, int total_processes) {
		this.process = process;
		this.total_processes = total_processes;
		this.r = new Random();
		this.sock = new ServerSocket[total_processes];
		RunningThread.messageFlag = new boolean[total_processes];
		for (int i = 0; i < total_processes; i++) {
			RunningThread.messageFlag[i] = false;
		}
	}

	synchronized private void recovery() {
		//System.out.println("[client:]recovery block entered"+Election.isElectionFlag()+ this.process.getPid());
		while (Election.isElectionFlag());//if election is going on then wait
		//System.out.println("[client:]recovery block while crossed"+Election.isElectionFlag()+ this.process.getPid());
		//Election.isPingFlag();
		System.out.println("Process[" + this.process.getPid() + "]: -> Recovered from Crash");
		//Find current co-ordinator.         
		try {
			Election.pingLock.lock();
			Election.setPingFlag(false);
			Socket outgoing = new Socket(InetAddress.getLocalHost(), 12345);			
			Scanner scan = new Scanner(outgoing.getInputStream());
			PrintWriter out = new PrintWriter(outgoing.getOutputStream(), true);
			System.out.println("Process[" + this.process.getPid() + "]:-> Who is the co-ordinator?");
			out.println("Who is the co-ordinator?");
			out.flush();

			this.process.setDownflag(false);
			Election.downflag[this.process.pid-1]=0;
			Election.setPingFlag(false);

			//System.out.println("reading reply");
			String pid = scan.nextLine();
			//System.out.println("reading reply2");

			String priority = scan.nextLine();
			//System.out.println("reading reply3");
			if (this.process.getPriority() > Integer.parseInt(priority)) { //Bully Condition
				out.println("Resign");
				out.flush();
				System.out.println("Process[" + this.process.getPid() + "]: Resign -> Process[" + pid + "]");
				String resignStatus = scan.nextLine();
				if (resignStatus.equals("Successfully Resigned")) {
					this.process.setCoOrdinatorFlag(true);
					//sock[this.process.getPid() - 1] = new ServerSocket(10000 + this.process.getPid());
					System.out.println("Process[" + this.process.getPid() + "]: -> Bullyed current co-ordinator Process[" + pid + "]");
					int j=0;
					while (this.process.getPriority()<Election.coordgroup[j]) j++;
					boolean modified = false;
					if (this.process.getPriority()>Election.coordgroup[j])
					{
						if(j==0){
							Election.coordgroup[2]=Election.coordgroup[1];
							Election.coordgroup[1]=Election.coordgroup[0];
							Election.coordgroup[0]=this.process.getPriority();
							modified = true;
						}
						else if(j==1)
						{
							Election.coordgroup[2]=Election.coordgroup[1];
							Election.coordgroup[1]=this.process.getPriority();
							modified = true;
						}
						else if(j==0)
						{
							Election.coordgroup[2]=this.process.getPriority();
							modified = true;
						}
					}					
					if (modified == true) System.out.print("Modified ");
					System.out.print("Coordinator Group is :");
					for (j = 0; j<3 && Election.coordgroup[j]!=-1 ; j++) {
						System.out.print("- " + Election.coordgroup[j] +" ");			
					}
					System.out.println("");	
				}
			} else {
				//System.out.println("Don't Resign message sent"+this.process.getPriority());
				//System.out.println("Don't Resign message sent"+Election.coordgroup[1]);
				//System.out.println("Don't Resign message sent"+Election.coordgroup[2]);
				out.println("Don't Resign");
				out.flush();				
				sock[this.process.getPid() - 1] = new ServerSocket(10000 + this.process.getPid());
				boolean modified=false;
				if (this.process.getPriority() > Election.coordgroup[1] && this.process.getPriority() < Election.coordgroup[0])
				{
					Election.coordgroup[2]=Election.coordgroup[1];
					Election.coordgroup[1]=this.process.getPriority();
					modified=true;
				}
				else if (this.process.getPriority() > Election.coordgroup[2] && this.process.getPriority() < Election.coordgroup[1])
				{
					Election.coordgroup[2]=this.process.getPriority();
					modified=true;
				}
				if(modified==true)
				{
					System.out.print("Modified Coordinator Group is :");
					for (int j = 0; j<3 && Election.coordgroup[j]!=-1; j++) {
						System.out.print("- " + Election.coordgroup[j] +" ");			
					}
					System.out.println("");
				}
				else
				{
					System.out.print("Coordinator Group is not Modified. It remains same :");
					for (int j = 0; j<3 && Election.coordgroup[j]!=-1; j++) {
						System.out.print("- " + Election.coordgroup[j] +" ");			
					}
					System.out.println("");
				}
				Election.setPingFlag(true);
				//System.out.println("going to unlock"+this.process.getPid());
				/*Election.setElectionFlag(true);//Election is Done
				if (!sendMessage()) {//elect self as co-ordinator
					Election.setElectionFlag(false);//Election is Done
					System.out.println("New Co-Ordinator: Process[" + this.process.getPid() + "]");
					this.process.setCoOrdinatorFlag(true);
					for (int i = 0; i < total_processes; i++) {
						RunningThread.setMessageFlag(false, i);
					}
					//break;
				}*/
			}
			//System.out.println("outofcode");
			//System.out.println("unlock is done"+this.process.getPid());

			Election.pingLock.unlock();
			return;

		} catch (IOException ex) {
			Election.pingLock.unlock();
			Election.setPingFlag(true);
			System.out.println(ex.getMessage());
		}

	}

	synchronized private void pingCoOrdinator() {
		try {
			Election.pingLock.lock();
			if (Election.isPingFlag()) {
				System.out.println("Process[" + this.process.getPid() + "]: Are you alive?");
				Socket outgoing = new Socket(InetAddress.getLocalHost(), 12345);
				outgoing.close();
			}
		} catch (Exception ex) {
			for(int j=0;j<3;j++)
			{
				//System.out.println("[client:]waiting latest check:" +Election.downflag[Election.coordgroup[j]-1]);
				//System.out.println("[client:]waiting latest check:" +Election.coordgroup[j]);
				if(Election.coordgroup[j]!=-1 && Election.downflag[Election.coordgroup[j]-1]==0){
					//System.out.println("[client:]entered latest check:" +Election.downflag[Election.coordgroup[j]-1]);
					Election.existsingroup=Election.coordgroup[j];
					//System.out.println("[client:]entered latest check:" +Election.existsingroup);
					break;					
				}
			}
			Election.setPingFlag(false);
			Election.setElectionFlag(true);
			Election.setElectionDetector(this.process);

			if(Election.existsingroup==-1){				
				//Initiate Election
				System.out.println("process[" + this.process.getPid() + "]: -> Co-Ordinator is down\n" + "process[" + this.process.getPid() + "]: ->Initiating Election");
			}
			else
			{
				System.out.println("process[" + this.process.getPid() + "]: -> Co-Ordinator is down\n" + "process[" + this.process.getPid() + "]: -> Checking Alternatives in Coordinator Group");
			}
		} finally {
			Election.pingLock.unlock();
		}
	}

	private void executeJob() {
		/*try{
			Socket incoming = null;
			incoming = sock[this.process.getPid() - 1].accept();
			Scanner scan = new Scanner(incoming.getInputStream());
			PrintWriter out = new PrintWriter(incoming.getOutputStream(), true);
			while (scan.hasNextLine()) {
				String line = scan.nextLine();
				if (line.equals("What is the status table?")) {
					System.out.print("Coordinator Group is :");

					for (int j = 0; j<3; j++) {
						System.out.print("- " + this.process.getCoOrdGroupMemb(j) +" ");			
					}
					System.out.println("");	
					System.out.println("Process[" + this.process.getPid() + "]:-> " + this.process.getPid());
					out.println(this.process.getPid());
					out.flush();					
				}
			}
			try {
				incoming.close();
				scan.close();				
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		finally
		{}
		 */		
		int temp = r.nextInt(20);
		//System.out.println("[Client]: Inside Execute Job-"+this.process.getPid()+"-"+temp);
		if(temp>13 && temp <15){
			//if (this.process.getPid()==3){
			if (sock[this.process.getPid() - 1]!=null && !sock[this.process.getPid() - 1].isClosed()){
				try {
					sock[this.process.getPid() - 1].close();
					this.process.setDownflag(true);
					Election.downflag[this.process.pid-1]=1;
					System.out.println("Process[" + this.process.getPid() + "]: -> Crashed");
					Thread.sleep(8000);//(this.r.nextInt(10) + 1) * 10000);//going down   
					//System.out.println("[client]: Process[" + this.process.getPid() + "]: -> recovered in the same block");
					//sock[this.process.getPid() - 1] = new ServerSocket(10000 + this.process.getPid());

					recovery();
					//System.out.println("[client:]recovery block executed"+ this.process.getPid());

				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}
		}
		else{
			//System.out.println("[Client]: Inside else block of Execute Job-"+this.process.getPid());
			for (int i = 0; i <= temp; i++) {
				try {
					Thread.sleep((temp + 1) * 100);
				} catch (InterruptedException e) {
					System.out.println("Error Executing Thread:" + process.getPid());
					System.out.println(e.getMessage());
				}
			}
		}
	}
	private void nodeRecovery() {
		while (Election.isElectionFlag());//if election is going on then wait
		System.out.println("Process[" + this.process.getPid() + "]: -> Recovered from Crash");
		//Find current co-ordinator.         
		try {
			Election.pingLock.lock();
			Election.setPingFlag(false);

			Socket outgoing = new Socket(InetAddress.getLocalHost(), 12345);
			Scanner scan = new Scanner(outgoing.getInputStream());
			PrintWriter out = new PrintWriter(outgoing.getOutputStream(), true);
			System.out.println("Process[" + this.process.getPid() + "]:-> Who is the co-ordinator?");
			out.println("Who is the co-ordinator?");
			out.flush();
			String pid = scan.nextLine();
			String priority = scan.nextLine();
			if (this.process.getPriority() > Integer.parseInt(priority)) { //Bully Condition
				out.println("Resign");
				out.flush();
				System.out.println("Process[" + this.process.getPid() + "]: Resign -> Process[" + pid + "]");
				String resignStatus = scan.nextLine();
				if (resignStatus.equals("Successfully Resigned")) {
					this.process.setCoOrdinatorFlag(true);
					sock[this.process.getPid() - 1] = new ServerSocket(10000 + this.process.getPid());
					System.out.println("Process[" + this.process.getPid() + "]: -> Bullyed current co-ordinator Process[" + pid + "]");
				}
			} else {
				//out.println("Don't Resign");
				//out.flush();
				if (!sendMessage()) {//elect self as co-ordinator
					Election.setElectionFlag(false);//Election is Done
					System.out.println("New Co-Ordinator: Process[" + this.process.getPid() + "]");
					this.process.setCoOrdinatorFlag(true);
					for (int i = 0; i < total_processes; i++) {
						RunningThread.setMessageFlag(false, i);
					}
					//break;
				}
			}
			Election.pingLock.unlock();
			return;

		} catch (IOException ex) {
			System.out.println(ex.getMessage());
		}

	}


	synchronized private boolean sendMessage() {
		boolean response = false;
		try {
			Election.electionLock.lock();
			if (Election.isElectionFlag() && !RunningThread.isMessageFlag(this.process.getPid() - 1) && this.process.priority >= Election.getElectionDetector().getPriority() && Election.downflag[this.process.pid-1]!=1) {
				for (int i = this.process.getPid() + 1; i <= this.total_processes; i++) {
					try {
						Socket electionMessage = new Socket(InetAddress.getLocalHost(), 10000 + i);
						System.out.println("Process[" + this.process.getPid() + "] -> Process[" + i + "]  responded to election message successfully");
						electionMessage.close();
						response = true;
					} catch (IOException ex) {
						System.out.println("Process[" + this.process.getPid() + "] -> Process[" + i + "] did not respond to election message");
					} catch (Exception ex) {
						System.out.println(ex.getMessage());
					}
				}
				this.setMessageFlag(true, this.process.getPid() - 1);//My message sending is done
				Election.electionLock.unlock();
				return response;
			} else {
				throw new Exception();
			}
		} catch (Exception ex1) {
			Election.electionLock.unlock();
			return true;
		}
	}

	public static boolean isMessageFlag(int index) {
		return RunningThread.messageFlag[index];
	}

	public static void setMessageFlag(boolean messageFlag, int index) {
		RunningThread.messageFlag[index] = messageFlag;
	}

	synchronized private void serve() {
		try {

			Socket incoming = null;
			ServerSocket s = new ServerSocket(12345);
			Election.setPingFlag(true);
			int temp = this.r.nextInt(5) + 5;// min 5 requests and max 10 requests

			for (int counter = 0; counter < temp; counter++) 
			{

				boolean done = false;
				//System.out.println("[Server]: Waiting for an incoming connection.");
				incoming = s.accept();
				//System.out.println("[Server]: Got incoming connection.");
				if (Election.isPingFlag()) 
				{
					System.out.println("Process[" + this.process.getPid() + "]:Yes");
				}
				Scanner scan = new Scanner(incoming.getInputStream());
				PrintWriter out = new PrintWriter(incoming.getOutputStream(), true);

				while ( !done) {

					if(!scan.hasNextLine()) {
						break;
					}

					String line = scan.nextLine();
					//System.out.println("[Server]: Line received '" + line.toString() + "'");
					if (line.equals("Who is the co-ordinator?")) {

						System.out.println("Process[" + this.process.getPid() + "]:-> " + this.process.getPid());
						out.println(this.process.getPid());
						out.flush();
						out.println(this.process.getPriority());
						out.flush();
					} else if (line.equals("Resign")) {
						this.process.setCoOrdinatorFlag(false);
						out.println("Successfully Resigned");
						out.flush();
						incoming.close();
						s.close();
						System.out.println("Process[" + this.process.getPid() + "]:-> Successfully Resigned");
						return;
					} else if (line.equals("Don't Resign")) {
						//System.out.println("[Server]: Got don't resign.");
						done = true;
						//return;
					}
				}
				//System.out.println("[Server]:Outside while loop.");
			}

			//after serving 5-10 requests go down for random time
			this.process.setCoOrdinatorFlag(false);
			this.process.setDownflag(true);
			Election.downflag[this.process.pid-1]=1;
			try {
				System.out.println("Process[" + this.process.getPid() + "] is Crashed");
				incoming.close();
				s.close();
				sock[this.process.getPid() - 1].close();
				Thread.sleep(15000);//(this.r.nextInt(10) + 1) * 10000);//going down
				recovery();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}

		} catch (IOException ex) {
			System.out.println(ex.getMessage());
		}
	}

	@Override
	public void run() {
		try {
			sock[this.process.getPid() - 1] = new ServerSocket(10000 + this.process.getPid());
		} catch (IOException ex) {
			System.out.println(ex.getMessage());
		}
		while (true) {
			if (process.isCoOrdinatorFlag()) {
				//serve other processes				
				serve();
			} else {
				while (true) {
					//Execute some task
					executeJob();
					//Ping the co-ordinator
					//System.out.println("Before pingCoOrdinator"+this.process.getPid());
					pingCoOrdinator();
					//Do Election
					//System.out.println("[client:]"+this.process.pid+":"+Election.existsingroup);
					if (Election.isElectionFlag() && Election.existsingroup==-1) {
						if (!sendMessage()) {//elect self as co-ordinator
							int j=0;
							Election.coordgroup[0]=-1;
							Election.coordgroup[1]=-1;
							Election.coordgroup[2]=-1;
							for (int k = total_processes-1; k >= 0; k--) {
								//System.out.println("[client:]"+k+"value:"+Election.downflag[k]);
								if (Election.downflag[k]==0) Election.coordgroup[j++]=k+1;
								if (j==3){
									break;
								}								
							}				
							System.out.print("New Coordinator Group, after election is :");
							for (j = 0; j<3 && Election.coordgroup[j]!=-1; j++) {
								System.out.print("- " + Election.coordgroup[j] +" ");			
							}
							System.out.println("");
							Election.setElectionFlag(false);//Election is Done
							System.out.println("New Co-Ordinator: Process[" + this.process.getPid() + "]");
							this.process.setCoOrdinatorFlag(true);

							for (int i = 0; i < total_processes; i++) {
								RunningThread.setMessageFlag(false, i);
							}

							break;
						}
					}
					else if (Election.isElectionFlag() && Election.existsingroup==this.getProcess().getPid()){
						//System.out.println("[client:]"+this.process.pid+":"+Election.existsingroup);
						Election.setElectionFlag(false);//Election is Done
						this.process.setCoOrdinatorFlag(true);
						System.out.println("New Co-Ordinator: Process[" + this.process.getPid() + "], choosen from coordinator group");						
						Election.existsingroup=-1;
						break;
					}
				}
			}
		}
	}
}