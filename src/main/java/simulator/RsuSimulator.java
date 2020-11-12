package simulator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.distribution.PoissonDistribution;

import javax.management.timer.Timer;
import java.util.*;

@Slf4j
public class RsuSimulator {
    double xcord ;
    double ycord ;
    double zcord ;
    int id ;
    public RsuSimulator(int id ,double xcord, double ycord, double zcord) {
        this.id = id ;
        this.xcord = xcord;
        this.ycord = ycord;
        this.zcord = zcord;
    }

    private Random random = new Random();
    private double pR =Simulator.pR;
    private static double speed = ConfigurationLoader.getInstance().getAsDouble("car_speed",100);
    private static long NUM_MESSAGE_PER_SECOND = ConfigurationLoader.getInstance().getAsLong("rsu_process_per_second",10L);
    static double mean_gnb = ConfigurationLoader.getInstance().getAsDouble("rsu_gnb_mean_tranfer", 80.0);
    static double mean_car = ConfigurationLoader.getInstance().getAsDouble("rsu_car_mean_tranfer", 80.0);
    private double lamda = 1.0 / NUM_MESSAGE_PER_SECOND;
    public List<Message>   inputList = new ArrayList<>();
    public List<Message> outputLisst = new ArrayList<>();
    public void startWorking(){
        log.info("RSU is processing ....");
        Collections.sort(inputList);  // Sort lại queue theo thời gian gửi gói tin
        getCarSentRate();
        simulateTransferTime();
        Collections.sort(inputList, new Comparator<Message>() {
            @Override
            public int compare(Message o1, Message o2) {
                if (o1.receiverTime > o2.receiverTime) return 1 ;
                if (o1.receiverTime < o2.receiverTime) return -1;
                return 0;
            }
        });
        getArriveRate();
        double preTme = 0 ;
        double pTime = 0;
        int num = 0 ;
        for (int i = 0 ; i < inputList.size() ; i ++){
            Message message = inputList.get(i);
            double receiverTime= message.receiverTime;

            double rand = random.nextDouble();
            if (rand < pR) {
                double selectedTime = Math.max(preTme , receiverTime);
                preTme = selectedTime ;
                message.sendTime = selectedTime ;
                message.type = "TYPE_3" ;
                Simulator.gNB.inputFromRSU.add(message);
            } else {
                num ++ ;
                double processTime = getNext(1.0/ lamda);
                double selectedTime = Math.max(preTme, receiverTime) ;
                double  processedTime = selectedTime + processTime ; //Thời điểm RSU xử lý xong gói tin thứ n
                preTme = Math.max(preTme, processedTime);
                message.sendTime = processedTime ;
                pTime += (processedTime- receiverTime);
                outputLisst.add(message);
            }
        }
        log.info("Process time RSU {} : {}", id, pTime/num);
        CarSimulator.rsuRecQueue.add(outputLisst);
    }
    public void simulateTransferTime(){
        // Mô phỏng thời gian truyền + nghẽn bằng mô hình queue
        double preReceiver = 0 ; // Thời điểm RSU nhận được gói tin thứ n-1
        double tV2I = 0 ;
        for (int i = 0 ; i < inputList.size(); i++){
            // Tìm thời điểm RSU nhận được gói tin thứ n
            Message message = inputList.get(i);
            double sentTime = message.sendTime ;
            double tranferTime = getNext(1.0/ mean_car);
            double selectedTime = Math.max(preReceiver,sentTime);
            double receiverTime = selectedTime + tranferTime;
            message.receiverTime = receiverTime ;
            tV2I+= (receiverTime - selectedTime) / inputList.size();
            preReceiver = Math.max(preReceiver, receiverTime);
        }
        log.info("Average tV2I: {} Size {} ", tV2I, inputList.size());
    }
    public void getArriveRate(){
        double tb = 0 ;
        for (int i = 1; i < inputList.size() ; i++){
            double lastReceiver = inputList.get(i-1).receiverTime ;
            double receiving = inputList.get(i).receiverTime ;
            tb += (receiving - lastReceiver) / (inputList.size() - 1);
        }
        log.info("RSU Arrive rate : {}", 1.0/tb);
    }
    public void getCarSentRate(){
        double tb = 0 ;
        for (int i = 1; i < inputList.size() ; i++){
            double lastReceiver = inputList.get(i-1).sendTime ;
            double receiving = inputList.get(i).sendTime ;
            tb += (receiving - lastReceiver) / (inputList.size() - 1);
        }
        log.info("Car send rate : {}", 1.0/tb);
    }
    static double getNext(double x){
        Random random = new Random();
        return -Math.log(1.0 -  random.nextDouble()) / x ;
    }
}
