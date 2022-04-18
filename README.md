## Some useful mini-programs(tools) for debugging 

### Network 
 ##### C-FileTransfer
 FTP like c program.  
 Simply read/write on a socket to transfer a file.  
 Support receive & send.  
  
  
 ##### GoFileServer  
 Very simple file send & receive program written in golang  
 
 ##### MiniJavaFileServer  
 TCP based JAVA file server.  
 Server communicates with its clients to support: file search, download, upload and etc.  
   
 ##### URLFilter  
 Implementation of DNS protocol.  
 Send DNS request to a DNS server asking for specific host.   
 add filter rules in iptables to drop traffic from the host.  
 use temporary file in /var to keep track of filtered IP lists.  
 
### OS 
 ##### DynamicIptables  
 prints results of iptables command in specific durations.  
 could be useful when you have to see rules that packets are going through  
 
 ##### Smaptrack  
 this program keeps track of certain process's smap.  
 although smap doesn't offer you best information about memory being used  
 it's possible to make quite accurate guesses on the flow of memory in your process.  
 
### C_snippets  
 ##### null_checked_sprintf
 sprintf~ like functions results in undefiend behavior when NULL input is given.  
 This function can check for NULL values before running sprintf~ like functions
 (implented through va_args, va_list, etc)

 ##### daemon
 simple illustration of creating a daemon process.
 Detaching terminal, session leader operations, redirecting stdios and logging pid.

 
 
