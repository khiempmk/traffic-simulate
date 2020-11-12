ps aux | grep run_script_1.sh | awk '{print $2}'| sudo xargs kill -9  
ps aux | grep run_script_2.sh | awk '{print $2}'| sudo xargs kill -9  
ps aux | grep run_script_3.sh | awk '{print $2}'| sudo xargs kill -9  
