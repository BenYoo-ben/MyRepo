#!/bin/bash

echo -e "1. Check System Resources\ntakes a few seconds to finish...\n" 

#check CPU / Memory / Load Average 확인(top, 3 seconds)

TOP=$(top -b -n 2 -p 1 -d 3.00) 

CPU=$(echo "$TOP" | fgrep "Cpu(s)" | tail -1 | awk -F'id,' '{ split($1, vs, ",");v=vs[length(vs)]; sub("%", "", v); printf "%d\n",v }' )

MEM=$(echo "$TOP" | fgrep "MiB Mem" | tail -1)
USED_MEM=$(echo "$MEM" | awk -F'used,' '{ split($1, vs, ","); v=vs[length(vs)]; printf "%d\n", v} ')

FREE_MEM=$(echo "$MEM" | awk -F'free,' '{ split($1, vs, ","); v=vs[length(vs)]; printf "%d\n", v} ')

LOAD_AVERAGE=$(echo "$TOP" | fgrep "average:" | tail -1 | awk -F',' '{ split($6, vs, ","); v=vs[length(vs)]; printf "%d\n", v} ')

#CPU 정보 로드
CORE=$(grep -c "^processor" "/proc/cpuinfo")

# ZOMBIE 프로세스 확인
IS_ZOMBIE=$(ps axo "stat="| grep 'Z' | wc -l)

# DEAD 프로세스 확인
IS_DEAD=$(ps axo "stat=" | grep 'X' | wc -l)



# top 기준 CPU 여유 비율(Idle)
if [ $CPU -lt 10 ];
then
    echo -e "\nCPU IDLE:$CPU% : FAIL"
else
    echo -e "\nCPU IDLE:$CPU% OK"
fi

# top 기준 메모리free, used 비교 

if [ $FREE_MEM -lt $USED_MEM ];
then
    echo -e "\nFREE_MEM:$FREE_MEM / USED_MEM:$USED_MEM : FAIL"
else
    echo -e "\nFREE_MEM:$FREE_MEM OK"
fi

# ps 프로세스 체크

if [ $IS_ZOMBIE -ne 0 ];
then
    echo -e "\nZOMBIE PROCESS FOUND:$IS_ZOMBIE : FAIL"
elif [ $IS_DEAD -ne 0 ];
then
    echo -e "\nDEAD PROCESS FOUND:$IS_DEAD : FAIL"
else
    echo -e "\nPROCESS OK"
fi

# top 기준 load average

if [ $LOAD_AVERAGE -gt $CORE ];
then
    echo -e "\nLoad Average:$LOAD_AVERAGE : FAIL"
else
    echo -e "\nLoad Average:$LOAD_AVERAGE OK"
fi
echo -e "\n System Check Done ! "
