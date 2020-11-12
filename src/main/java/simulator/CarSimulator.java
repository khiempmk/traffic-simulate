package simulator;

import ch.qos.logback.classic.pattern.ClassNameOnlyAbbreviator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.distribution.PoissonDistribution;

import java.io.File;
import java.util.*;

@Slf4j
public class CarSimulator {
    double startTime; // Thời điểm xe xuất hiện
    private static double cover_radius = ConfigurationLoader.getInstance().getAsDouble("rsu_cover_radius",100);
    public static String inputFile = Simulator.inputFile;
    public static double simTime = Simulator.simTime;
    private double pL =Simulator.pL;//=0.5;
    static double mean_gnb = ConfigurationLoader.getInstance().getAsDouble("car_gnb_mean_tranfer", 80.0);
    static double mean_rsu = ConfigurationLoader.getInstance().getAsDouble("rsu_car_mean_tranfer", 80.0);
    private double MAX_XCORD = ConfigurationLoader.getInstance().getAsDouble("road_length",1000);
    private static List<RsuCoord> rsuList = new ArrayList<>();
    private static double speed = ConfigurationLoader.getInstance().getAsDouble("car_speed",100);
    Random random = new Random();

    public CarSimulator(double startTime){
        this.startTime = startTime ;
    }
    static {
        // Lấy config của các RSU có trong mạng
        int numRSU = ConfigurationLoader.getInstance().getAsInteger("rsu_numbers", 5);
        String xList = ConfigurationLoader.getInstance().getAsString("list_rsu_xcoord", "200;400;600;800;1000");
        String yList = ConfigurationLoader.getInstance().getAsString("list_rsu_ycoord", "1;1;1;1;1");
        String zList = ConfigurationLoader.getInstance().getAsString("list_rsu_zcoord", "10;10;10;10;10");
        try {
            String[] xs = xList.split(";");
            String[] ys = yList.split(";");
            String[] zs = zList.split(";");
            for (int i = 0; i < numRSU; i++) {
                double xcord = Double.parseDouble(xs[i]);
                double ycord = Double.parseDouble(ys[i]);
                double zcord = Double.parseDouble(zs[i]);
                RsuCoord rsuCoord = new RsuCoord(xcord, ycord, zcord);
                rsuCoord.setId(i);
                rsuList.add(rsuCoord);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
    public void sendMessageToGNB(double sendTime){
        Message message = new Message();
        message.sendTime = sendTime ;
        message.firstSentTime= sendTime;
        message.type = "TYPE_2" ;
        Simulator.gNB.inputFromCar.add(message);
    }
    public void sendMessageToRSU(double sendTime){
        double minDistance = 1000000;
        int rsuID = 0;
        double xcord = getPosition(sendTime - startTime);
        for (RsuCoord rsu : rsuList){
            double distance = calculateDistance(rsu.getXCoord(), rsu.getYCoord(), rsu.getZCoord(), xcord,0,0);
            if (distance < minDistance){
                minDistance =  distance ;
                rsuID = rsu.getId();
            }

        }
        Message message = new Message();
        message.startTime = startTime ;
        message.sendTime = sendTime ;
        message.firstSentTime= sendTime;
        message.type = "TYPE_1" ;
        Simulator.rsuList.get(rsuID).inputList.add(message);
    }
    private static int id =0;
    public void startWorking(){
        // công việc : Sinh gói tin ở mỗi xe, gửi gói tin lên gNB và RSU theo xác xuất pL
        try{
            log.info("{} - Car {} appear", startTime, id++);
            // Mỗi xe sinh gói tin theo phân phối poisson cb sẵn trong file inputFile .
            Scanner scanner = new Scanner(new File(inputFile));
            double preTime =  startTime ;
            while (scanner.hasNext()){
                double nextTime = scanner.nextDouble();

                double sendTime = preTime + nextTime ;
                preTime = sendTime ;
                // nếu thời gian gửi > ngưỡng thời gian mô phỏng or  xe đi qua đoạn đường mô phỏng thì dừng
                if (sendTime > simTime || getPosition(sendTime-startTime) > MAX_XCORD ){
                    return;
                }
                double rad = random.nextDouble();
                if (rad < pL){
                    // Gửi gói tin lên gNB
                    sendMessageToGNB(sendTime);
                } else {
                    // gửi gói tin lên RSU
                    sendMessageToRSU(sendTime);
                }
            }
        } catch (Exception e){
            log.error(e.getMessage(),e);
        }
    }
    // Queue chứa gói tin từ RSU gửi về
    public static List<List<Message>> rsuRecQueue = new ArrayList<>();
    // Queue chứa gói tin từ gNB gửi về
    public static List<Message> gnbRecQueue = new ArrayList<>();

    // Hàm nhận gói tin từ RSU và gNB gửi về
    public static void receiverMessage(){
        double totalDrop = 0 ;
        int totalPacket = 0;
        double preTime = 0 ;
        for (int i = 0 ; i < rsuRecQueue.size() ; i++){
            List<Message> list = rsuRecQueue.get(i);
            totalPacket += list.size() ;
            Collections.sort(list);
            preTime = 0 ;
            double tI2V = 0 ;
            double numDrop = 0 ;
            for (int j = 0 ; j< list.size() ; j++){
                Message message = list.get(j);
                double sentTime = message.sendTime ;
                double tranferTimeInSec = getNext(1.0/ mean_rsu) ;
                double selectTime = Math.max(preTime, sentTime);
                double receiverTime = selectTime + tranferTimeInSec ;
                preTime = Math.max(preTime, receiverTime);
                message.receiverTime = receiverTime;
                tI2V += (receiverTime - sentTime) / list.size() ;
                double currentCarXcord = getPosition(receiverTime - message.startTime);
                double xcord = rsuList.get(i).getXCoord();
                double ycord = rsuList.get(i).getYCoord();
                double zcord = rsuList.get(i).getZCoord();
                double distance = calculateDistance(xcord,ycord,zcord,currentCarXcord,0,0);
                if (distance < cover_radius){
                    message.isDropt= false;

                }
                else {
                    message.isDropt = true;
                    numDrop++;
                }
                Simulator.output.add(message);
            }
            log.info("RSU {} Average tI2V: {} , drop percent {} % , numDrop {} , listSize {} ",  i, tI2V, (double) numDrop / list.size() *100, numDrop, list.size() );
            totalDrop += numDrop ;
        }
        log.info("Total drop percent {}", (double) totalDrop / totalPacket *100);
        preTime = 0 ;
        double tN2V = 0 ;
        Collections.sort(gnbRecQueue);
        for (int i = 0 ; i < gnbRecQueue.size() ; i++){
            Message message = gnbRecQueue.get(i);
            double sentTime = message.sendTime ;

            double selectTime = Math.max(preTime, sentTime);
            double tranferTimeInSec = getNext(1.0/mean_gnb);

            double receiverTime = selectTime + tranferTimeInSec ;
            tN2V += (receiverTime -sentTime) / gnbRecQueue.size();
            message.receiverTime = receiverTime;
            preTime = Math.max(preTime, receiverTime);
            message.isDropt = false ;
            Simulator.output.add(message);
        }
        log.info("Average tN2V: {} , Size {}" , tN2V, gnbRecQueue.size());
    }
    private static double getPosition(double simTime){
        try {
            double xcord = speed * simTime;
            return xcord ;
        } catch (Exception e){
            log.error(e.getMessage(),e);
        }
        return 0;
    }
    private static double calculateDistance(double x ,double y, double z, double XCORD, double YCORD, double ZCORD){
        return Math.sqrt(Math.pow(x - XCORD, 2) + Math.pow(y - YCORD, 2) + Math.pow(z - ZCORD, 2));
    }
    static double getNext(double x){
        Random random = new Random();
        return -Math.log(1.0 -  random.nextDouble()) / x ;
    }
}
