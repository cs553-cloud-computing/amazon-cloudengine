Cloud Task Execution Framework
==============================

*with Amazon EC2, S3, SQS, and DynamoDB*

###Dependencies:  
aws-java-sdk-1.9.6  
json-simple-1.1.1.jar  
commons-cli-1.2  

--Client side--  
1.Download AWS java sdk from the AWS website, install the eclipse plugin for the AWS sdk.  
2.Import java project to the eclipse  
3.Compile each project in eclipse

client:  
Command line interface: Client -s <IP_ADDR:PORT> -w workload  
IP_ADDR: IP address of the server  
PORT: Server port number  
workload: workload file path  

Scheduler:  
Command line interface: FrontEndScheduler -s <PORT> -lw N_threads -rw  
PORT: open port number for server socket  
-lw: local worker mode  
N_threads: number of local woker threads  
-rw: remote worker mode  
*NOTE: pass either -lw or -rw as argument option  

Remote worker:  
Command Line Interface: RemoteWorker -i Time -s N_threads  
Time: idle time  
N_threads: number of worker threads  

Dynamic provisioner:  
XXX [minWorkerNum (default=0)] [maxWorkerNum (default=50)]  

the two parameters should be given (or not given) in the same time

--Server side--  
1.Download AWS java sdk in the home directory  
2.move all the third-party libraries in the AWS sdk to the lib directory  
3.Download json and apache library dependencies.  
4.Upload java project code to the server.  
5.Compile code with compile scrip in each java project directory.  
6.Run the java code with run scrip in each java project derectory.  

###License:

Copyright (c) 2014 Long Nangong, Jiada Tu

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
