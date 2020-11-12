package simulator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.distribution.PoissonDistribution;

import javax.management.timer.Timer;
import java.util.*;

@Slf4j
public class GnbSimulator {
    private Random random = new Random();
    private static long NUM_MESSAGE_PER_SECOND = ConfigurationLoader.getInstance().getAsLong("gnb_process_per_second",10L);
    static double mean_car = ConfigurationLoader.getInstance().getAsDouble("car_gnb_mean_tranfer", 80.0);
    static double mean_rsu = ConfigurationLoader.getInstance().getAsDouble("rsu_gnb_mean_tranfer", 80.0);

    private double lamda = 1.0 / NUM_MESSAGE_PER_SECOND ;
    public List<Message> inputFromCar = new ArrayList<>();
    public List<Message> inputFromRSU = new ArrayList<>();
    public List<Message> inputList = new ArrayList<>();
    public void startWorking(){
        log.info("GNB is running ...");
        Collections.sort(inputFromCar); // Sort lại queue theo thời gian gửi gói tin
        double preSelect = 0 ;
        double tV2N = 0 ;
        // Tính thời gian truyền qua đường truyền CAR- GNB
        for (int i = 0 ; i < inputFromCar.size(); i++){
            Message message = inputFromCar.get(i);
            double sentTime= message.sendTime ;
            if (sentTime == 0){
                log.info("Something wrong!!!");
            }
            double selectTime = Math.max(preSelect, sentTime);
            double transferTimeInSec = getNext(1.0/ mean_car);

            double receiverTime = selectTime + transferTimeInSec ;
            tV2N += (receiverTime - sentTime)/ inputFromCar.size();
            message.receiverTime = receiverTime ;
            preSelect = Math.max(preSelect, receiverTime);
        }
        log.info("Average tV2N: {} Size : {}", tV2N, inputFromCar.size());
        Collections.sort(inputFromRSU);
        preSelect = 0 ;
        double tI2N = 0 ;
        // Tính thời gian truyền qua đường truyền RSU- GNB
        for (int i = 0 ; i < inputFromRSU.size(); i++){
            Message message = inputFromRSU.get(i);
            double sentTime= message.sendTime ;
            if (sentTime == 0){
                log.info("Something wrong!!!");
            }
            double selectTime = Math.max(preSelect, sentTime);
            double tranferTimeInSec = getNext(1.0 / mean_rsu);
            double receiverTime = selectTime + tranferTimeInSec ;
            tI2N += (receiverTime - sentTime) / inputFromRSU.size() ;
            message.receiverTime = receiverTime ;
            preSelect = Math.max(preSelect, receiverTime);
        }
        log.info("Average tI2N: {} Size {}", tI2N, inputFromRSU.size());

        inputList.addAll(inputFromCar);
        inputList.addAll(inputFromRSU);
        Collections.sort(inputList, (o1, o2) -> {
            if (o1.receiverTime > o2.receiverTime) return 1 ;
            if (o1.receiverTime < o2.receiverTime) return -1;
            return 0;
        });
        double preTme = 0 ;
        double pTime = 0 ;
        for (int i = 0 ; i < inputList.size() ; i ++) {
            Message message = inputList.get(i);
            double receiverTime = message.receiverTime;
            double processTime = getNext(1.0 / lamda);
            double selectedTime = Math.max(preTme, receiverTime);
            double processedTime = selectedTime + processTime;
            preTme = Math.max(preTme, processedTime);
            pTime += (processedTime - receiverTime)/ inputList.size() ;
            message.sendTime = processedTime ;
            CarSimulator.gnbRecQueue.add(message);
        }
        log.info("ProcessTime gNB : {}", pTime);
    }
    static double getNext(double x){
        Random random = new Random();
        return -Math.log(1.0 -  random.nextDouble()) / x ;
    }
}
