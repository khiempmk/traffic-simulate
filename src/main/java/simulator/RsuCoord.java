package simulator;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RsuCoord {
    private int id ;
    private double xCoord ;
    private double yCoord;
    private double zCoord;

    public RsuCoord(double xCoord, double yCoord, double zCoord) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.zCoord = zCoord;
    }
}
