![](https://github.com/bysoul/la-rpc/blob/master/display/newrpclogo.jpg)
# Properties:  
* High Performance Networking Based on Netty  
* High Performance Serializing Based on Protobuf and Protostuff  
* HeartBeat and Reconnection 
* Solve Packet Fragmentation and Assembly  
* Multiplexing of Client Channel   
* Integrate Spring  
* Call Methods in Server ThreadPool  

# Properties Coming Soon:  
* Integrate ZooKeeper   

# Frame:
![](https://github.com/bysoul/la-rpc/blob/master/display/frame.jpg)

# Usage:
```JAVA
Interface rpc=(Interface) new RpcProxy().get("InterfaceImpl",Interface.class);  
Object res=rpc.method(Object... args);
```
# Proto:
```
syntax = "proto2";
option java_outer_classname = "RpcProto";
message RequestMessage {
  required int32 requestId = 1;
  required string className = 2;
  required string methodName = 3;
  repeated string argTypes = 4;
  optional bytes args = 5;
}
message ResponseMessage {
  required int32 requestId = 1;
  required bool status=2;
  optional bytes result = 3;
}
```
