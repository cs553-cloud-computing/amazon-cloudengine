package cloudworker;

public class WorkerThread implements Runnable {
	int task_id;
	long sleepLength;
	
	public WorkerThread(long sleepLength){
		this.sleepLength = sleepLength;
	}
	
	@Override
	public void run() {	
        try {
        	Thread.sleep(sleepLength);
        	
        	System.out.println(Thread.currentThread().getName()+" sleep done!");
        	
        } catch (Exception e) {
             System.out.println(e);
        }
	}

}
