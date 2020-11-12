car_speed=$1
car_packet_strategy=$2
car_appear_strategy=$3
pl=$4
pr=$5
mode=$6
send_name=$7
receiver_name=$8
sed -i -e "s/\(car_speed=\).*/\1$car_speed/" \
       -e "s/\(car_packet_strategy=\).*/\1$car_packet_strategy/" \
       -e "s/\(car_appear_strategy=\).*/\1$car_appear_strategy/" \
       -e "s/\(default_pl=\).*/\1$pl/" \
       -e "s/\(default_pr=\).*/\1$pr/" \
       -e "s/\(run_mode=\).*/\1$mode/"  conf/default-config.cfg
bash start_process.sh
inputname="dumpSend.tr"
outputname="dumpDelay.tr"

mv "$inputname" "$send_name"
mv "$outputname" "$receiver_name"
bash clean_process.sh
