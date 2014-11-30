package cloudworker;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;

public class BlockingExecutor {  
    private final Executor exec;  
    private final Semaphore semaphore;  
    
    public BlockingExecutor(Executor exec, int bound) {  
        this.exec = exec;  
        this.semaphore = new Semaphore(bound);  
    }  
    
    public void submitTask(final Runnable worker) throws InterruptedException {  
        semaphore.acquire();  
        
        try {  
            exec.execute(new Runnable() {  
                public void run() {  
                    try {  
                        worker.run();  
                    } finally {  
                        semaphore.release();  
                    }  
                }  
            });  
        } catch (RejectedExecutionException e) {  
            semaphore.release();  
        }  
    }
    
}  