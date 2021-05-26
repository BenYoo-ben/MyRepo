package main

import(
    "fmt"
    "net"
    "os"
    "bufio"
    "strconv"
	"io"
)

const (
	FBUFFER_SIZE = 1024		
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
   /* str := "HEELO"
    data := []byte(str)
    conn_socket.Write(data)*/

    
    fmt.Println("Input FileName:")
    var filename string
    fmt.Scanf("%s",&filename)


    folderInfo, err := os.Stat("./storage/"+filename)
    if os.IsNotExist(err) {
        fmt.Println("File Not Exist",err.Error())
        os.Exit(1)
    }else {
        fmt.Println(folderInfo)
    }
    
    fmt.Println("Choose action:\n1. Upload\n2.Download\n")
    var decision string
    fmt.Scanf("%s",&decision)

    conn_socket.Write([]byte(decision))
    
    if(decision=="1"){
		 fmt.Println("Input filename to get:")
		 fmt.Scanf("%s",&decision)

		 file, err := os.Create("storage/D"+string(decision))
		 defer file.Close()
		 if err != nil{
			fmt.Println("Error creating file.")
			os.Exit(1)
		 }else{
	//		fileBuffer := make([]byte, FBUFFER_SIZE)
			 conn_socket.Write([]byte(decision))
			io.Copy(file,conn_socket)
			os.Exit(0)
		 }
		 

		 

    
    }else if(decision=="2"){
    
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
        if(tmp_bytes[i]==10 || tmp_bytes[i]==13){
            return num
        }else{
            num= num*10 + uint16(tmp_bytes[i]-'0')
        }
        i++
    }

    
}


