package simulator;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

@Slf4j
public class Simulator {
    public static double pL =ConfigurationLoader.getInstance().getAsDouble("default_pl",0.5);//=0.5;
    public static double pR =ConfigurationLoader.getInstance().getAsDouble("default_pr",0.5);
    public static double simTime = ConfigurationLoader.getInstance().getAsDouble("simTime", 60000);
    private static String appearStrategy = ConfigurationLoader.getInstance().getAsString("car_appear_strategy","car_poisson.inp");
    public static GnbSimulator gNB  = new GnbSimulator();
    public static List<RsuSimulator> rsuList  = new ArrayList<>();
    public static List<Message> output = new ArrayList<>();

    public static void main(String[] args) {
        // Lấy config của RSU
        int numRSU = ConfigurationLoader.getInstance().getAsInteger("rsu_numbers", 5);
        String xList = ConfigurationLoader.getInstance().getAsString("list_rsu_xcoord","200;400;600;800;1000");
        String yList = ConfigurationLoader.getInstance().getAsString("list_rsu_ycoord","1;1;1;1;1");
        String zList = ConfigurationLoader.getInstance().getAsString("list_rsu_zcoord","10;10;10;10;10");
        try {
            String[] xs = xList.split(";");
            String[] ys = yList.split(";");
            String[] zs = zList.split(";");
            for (int i = 0; i < numRSU; i++) {
                double xcord = Double.parseDouble(xs[i]);
                double ycord = Double.parseDouble(ys[i]);
                double zcord = Double.parseDouble(zs[i]);
                RsuSimulator rsu = new RsuSimulator(i,xcord,ycord,zcord);
                rsuList.add(rsu);
            }

            // Xe chạy gửi gói tin lên
            carAppear();
            // Các rsu chạy xử lý gói tin trong queue từ car gửi lên
            for (RsuSimulator rsu : rsuList){
                rsu.startWorking();
            }
            // gNB chạy xử lý gói tin trong queue từ car & rsu gửi lên
            gNB.startWorking();

            // Xe nhận lại các gói tin từ gNB và rSU gửi về
            CarSimulator.receiverMessage();
            // Trả kết quả của mô phỏng
            dumpOutput();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }
    private static void carAppear(){
        try {
            // Mô phỏng thời điểm xuất hiện xe theo file input đầu vào ( phân phối poisson)
            double timeNow =0 ;
            Scanner scanner = new Scanner(new File(appearStrategy));
            while (scanner.hasNext()){
                double tmp = scanner.nextDouble();
                double startCar =timeNow + tmp ;
                timeNow = startCar;
                if (startCar > simTime ){
                    return ;
                }
                CarSimulator car = new CarSimulator(startCar);
                car.startWorking();
            }
        } catch (Exception e){
            log.error(e.getMessage(),e);
        }
    }
    public static String inputFile =ConfigurationLoader.getInstance().getAsString("car_packet_strategy","") ;

    public static String filePath = "dumpDelay.tr";
    private static FileWriter fileWriter ;
    private FileWriter sendWriter ;
    public static void dumpOutput(){
        try{
            double su = 0;
            log.info("Dumping output ...");
            Collections.sort(output);
            double count = 0 ;
            for (Message msg : output){
                su = su +( msg.receiverTime - msg.firstSentTime )/  output.size();
                if (!msg.isDropt) count++ ;
            }
            fileWriter = new FileWriter(new File(filePath));
            FileWriter fileWriter1 = new FileWriter(new File("Output.tr"), true);
//            sendWriter = new FileWriter(new File("dumpSend.tr"),true);
            fileWriter.write(su+ "\n");
            fileWriter.write(count+ "\t" + output.size() + "\n");
            fileWriter1.write(inputFile+"\t" + appearStrategy + "\t" + pL + "\t" + pR + "\t");
            fileWriter1.write(su+ "\t" +count+ "\t" + output.size() + "\n");
            fileWriter1.flush();
            for (Message msg : output){
                double sendTime = msg.firstSentTime;
                double receiverTime = msg.receiverTime;
                double delay = receiverTime -sendTime ;
                String message = sendTime +"\t" + receiverTime + "\t" +delay+"\t" + msg.type+"\n" ;
                fileWriter.write(message);
                fileWriter.flush();
            }
        } catch (Exception e){
            log.error(e.getMessage(),e);
        }
    }
}
