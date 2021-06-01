package main

import(
    "fmt"
    "net"
    "os"
    "bufio"
	"io"
    "strings"
)

const (
	FBUFFER_SIZE = 1024		
)

func main(){

   // tcp_port := getPort()


    new_reader := bufio.NewReader(os.Stdin)
    fmt.Println("Input Server ip:port :") 
    server_ip_string,_ := new_reader.ReadString('\n')
    server_ip_string = strings.TrimRight(server_ip_string,"\n")
    fmt.Println("Connecting to Server:",server_ip_string)
    conn_socket, err := net.Dial("tcp",server_ip_string)

   if err!= nil {
    fmt.Println("Error on Dialing :",err.Error())
    os.Exit(1)
   }else{
   
	   /* str := "HEELO"
    data := []byte(str)
    conn_socket.Write(data)*/
    
    fmt.Println("Choose action:\n1. Download\n2. Upload\n")
    var decision string
    fmt.Scanf("%s",&decision)

    conn_socket.Write([]byte(decision))
    
    if(decision=="1"){
		 fmt.Println("Input filename to download:")
		 fmt.Scanf("%s",&decision)

		 file, err := os.Create("storage/"+string(decision))
		 defer file.Close()
		 if err != nil{
			fmt.Println("Error creating file.")
			os.Exit(1)
		 }else{
			fmt.Println("Download Start...") 
            conn_socket.Write([]byte(decision))
			io.Copy(file,conn_socket)
            fmt.Println("Download Finish...!")
			os.Exit(0)
		 }
		 

		 

    
    }else if(decision=="2"){
     
    fmt.Println("Input fileName to upload:")
    var filename string
    fmt.Scanf("%s",&filename)


    _, err := os.Stat("./storage/"+filename)
    if os.IsNotExist(err) {
        fmt.Println("File Not Exist",err.Error())
        os.Exit(1)
    }else {
        file, err := os.Open("storage/"+filename)
        if err != nil {
            fmt.Println("Error opening file")
            os.Exit(1)
        }else {
        conn_socket.Write([]byte(filename))

        defer file.Close()
        fmt.Println("Upload Start...")
        _, err := io.Copy(conn_socket,file)
        if err != nil {
            fmt.Println("Error upload")
            os.Exit(1)
        }
        fmt.Println("Upload finish...!")
        conn_socket.Close()
        os.Exit(0) 
        }
     }
    

 }

}
}




