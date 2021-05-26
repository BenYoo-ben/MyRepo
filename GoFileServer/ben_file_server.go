package main 

import(
    "fmt"
    "os"
    "bufio"
    "net"
    "strconv"
	"io"
)

const (
    CONN_HOST = "localhost"
    CONN_TYPE = "tcp"

)
func main(){
 

    tcp_port := getPort()
	fmt.Println("tcpport:",tcp_port)
    listener, err := net.Listen(CONN_TYPE, CONN_HOST+":"+strconv.FormatUint(uint64(tcp_port),10))
    if( err!= nil){
        fmt.Println("Error listening:",err.Error())
        os.Exit(1)
    }

    defer listener.Close()
    
    fmt.Println("This Server listening on :",strconv.FormatUint(uint64(tcp_port),10))

   for{
        new_conn, err := listener.Accept()
        if err != nil {
             fmt.Println("Error Accepting:",err.Error())
            os.Exit(1)
        }else{
        
        fmt.Println("Handle for ",new_conn.LocalAddr()," start.")
        go ClientHandler(new_conn)

        
        }
   
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
        fmt.Println(tmp_bytes[i])
		if(tmp_bytes[i]==10 || tmp_bytes[i] == 13) {
            return num
        }else{
            num= num*10 + uint16(tmp_bytes[i]-'0')
        }
        i++
    }

    
}
//go docs : fmt, bufio, net, os
func ClientHandler(con net.Conn) {
//   var str string
   recvBuf := make([]byte,1024)
   con.Read(recvBuf[:])
	if(string(recvBuf)=="1"){
	//request for download
		con.Read(recvBuf[:])
		file, err := os.Open("storage/"+string(recvBuf))
			if err != nil {
				fmt.Println("Error read file:",string(recvBuf))
				con.Write([]byte("ERR"))
				return
			}else{
				defer file.Close()
				n, err  := io.Copy(con,file)
				if( err != nil){
				fmt.Println("Error send file")
				return
				}
			fmt.Println("Successfully sent :",n)
			}
		return
	}
}
