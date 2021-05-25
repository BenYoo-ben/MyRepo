package main

import(
    "fmt"
    "net"
    "os"
    "bufio"
    "strconv"
)

func main(){

    tcp_port := getPort()


    new_reader := bufio.NewReader(os.Stdin)
    fmt.Println("Input Server ip :") 
    server_ip_string,_ := new_reader.ReadString('\n')

   conn_socket, err := net.Dial("tcp",server_ip_string+":"+strconv.FormatUint(uint64(tcp_port),10))

   if err!= nil {
    fmt.Println("Error on Dialing :",err.Error())
    os.Exit(1)
   }else{
   fmt.Fprintf(conn_socket,"Hello Server!") 
   }

}



func getPort() uint16{
     std_reader := bufio.NewReader(os.Stdin)
   
    var tmp_bytes []byte
    fmt.Println("Input TCP Port to be used :")
    tmp_bytes, _ = std_reader.ReadBytes('\n')
   
    var num uint16
    i := 0
    for {
        if(tmp_bytes[i]==10){
            return num
        }else{
            num= num*10 + uint16(tmp_bytes[i]-'0')
        }
        i++
    }

    
}


