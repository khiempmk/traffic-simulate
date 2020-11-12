package simulator;

public class Runner {
    public static void main(String[] args) {
        String inp="poisson_20.inp";
        double pl = 0;
        double pr = 0.397717;
        run(args, inp, pl, pr);

        inp="poisson_22.inp";
        pl = 0;
        pr = 0.448671;
        run(args, inp, pl, pr);

        inp="poisson_24.inp";
        pl = 0;
        pr = 0.4692985;
        run(args, inp, pl, pr);

        inp="poisson_26.inp";
        pl = 0.45672787;
        pr = 0;
        run(args, inp, pl, pr);

        inp="poisson_28.inp";
        pl = 0.4488858;
        pr = 0;
        run(args, inp, pl, pr);
    }

    public static void run(String[] args,String inp , double pl, double pr) {
        Simulator simulator = new Simulator();
        Simulator.inputFile= inp ;
        Simulator.pL = pl ;
        Simulator.pR = pr;
        Simulator.main(args);
    }
}
