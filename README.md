# Properties:  
High Performance Networking Based on Netty  
High Performance Serializing Based on Protobuf and Protostuff  
Multiplexing of Client Channel   
Call Methods in Server ThreadPool  
Solve Packet Fragmentation and Assembly  

# Properties Coming Soon:  
Integrate Spring  
Integrate ZooKeeper  
Recover from Disconnection  

# Usage:
Interface rpc=(Interface) new RpcProxy().get("InterfaceImpl",Interface.class);  
rpc.method(Object args...);
