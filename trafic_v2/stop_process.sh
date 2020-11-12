ps aux | grep java |  grep Simulator | awk '{print $2}' | sudo xargs kill -9

