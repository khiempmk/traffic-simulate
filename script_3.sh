car_speed=$1
car_packet_strategy=$2
car_appear_strategy=$3
pl=$4
sed -i -e "s/\(rsu_numbers=\).*/\1$car_speed/" \
       -e "s/\(list_rsu_xcoord=\).*/\1$car_packet_strategy/" \
       -e "s/\(list_rsu_ycoord=\).*/\1$car_appear_strategy/" \
       -e "s/\(list_rsu_zcoord=\).*/\1$pl/"   conf/default-config.cfg
