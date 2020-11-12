package simulator;

public class Message implements Comparable<Message>{
    public double startTime ;      // Thời điểm xe bắt đầu chạy vào đoạn đường mô phỏng
    public double firstSentTime ; // Thời điểm gửi từ car
    public double sendTime ;   // Thời điểm gửi từ nút trước đó
    public double receiverTime ; //  Thời điểm nhận gói tin
    public int rsuID ;
    String type ;   // TYPE_1 : CAR - rSU - CAR
                    // TYPE_2 : CAR - gNB - CAR
                    // TYPE_3 : CAR  - rSU - gNB - CAR
    public boolean isDropt ;

    @Override
    public int compareTo(Message o) {
        if (sendTime > o.sendTime ) return 1 ;
        else if (sendTime < o.sendTime ) return -1;
        return 0;
    }
}
