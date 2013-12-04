package newbully;

public class Process {

    int pid, priority;    
     
    boolean downflag,CoOrdinatorFlag;    
    
    
    public boolean isCoOrdinatorFlag() {
        return CoOrdinatorFlag;
    }

    public void setCoOrdinatorFlag(boolean isCoOrdinator) {
        this.CoOrdinatorFlag = isCoOrdinator;
    }
    
    public boolean isDownflag() {
        return downflag;
    }

    public void setDownflag(boolean downflag) {
        this.downflag = downflag;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Process() {
    }

    public Process(int pid, int priority) {
        this.pid = pid;
        this.downflag = false;
        this.priority = priority;
        this.CoOrdinatorFlag = false;
    }
}