/*
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and limitations under the License.
 */

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