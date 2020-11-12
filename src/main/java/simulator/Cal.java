package simulator;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.integration.RombergIntegrator;
import org.apache.commons.math.analysis.integration.SimpsonIntegrator;

/*------------------------------------------------------------------------------------------------------
 * Small program that numerically calculates an integral according to
 * Simpson's algorithm. Before executing it, you must enter:
 *  - the expression of the function f: line 12;
 *  - the lower and upper limits b of the integral: lines 39 and 40;
 *  - the number of measurements n (n is integer !!!): line 41.
 *------------------------------------------------------------------------------------------------------*/
// Class function: Defines Simpson's rule
class Function{

    // Define the function to integrate
    static double nV2I=2097.152 ;
    static double nV2N=1024.0 ;
    static double nI2V=2097.152 ;
    static double nN2V=1024.0 ;
    static double nI2N=20971.52 ;
    static double nr = 320.0 ;
    static double ng = 1280.0 ;
    static double x = 0.4 ;
    static double y = 0.1 ;
    static double v = 12.0 ;
    static double n = 5.0 ;
    static double rL = 1500.0 ;
    static double lamdaV = 0.2 ;
    static double lamdaD = 100.0 ;
    static double muyV = (v * n  + rL * lamdaV) / rL ;
    static double pV = ( lamdaV / muyV );
    static double k = pV / (1-pV)   * lamdaD ;
    static double A = muyV - lamdaV ;
    static double tV2N =  1/ (nV2N - k*n*x) ;
    static double tN2V =  1/ (nN2V - k*n*x - k*n*(1-x)*y ) ;
    static double tV2I =  1/ (nV2I - k*(1-x)) ;
    static double tI2V =  1/ (nI2V - k*(1-x)*(1-y) ) ;
    static double tI2N =  1/ (nI2N - k*(1-x)*y) ;
    static double gNB =  1/(ng - k*n*(1-x)*y - k*n*x) ;
    static double rSU =  1/ (nr - k*(1-x)*(1-y)) ;
    static double b1 =  1 / tV2I ;
    static double b2 =  1 / rSU ;
    static double b3 =  1/ tI2V ;
    static double B =  b1 + b2 + b3 ;
    double f (double x) {
        return ( Math.exp(-A *x ) + Math.exp(-(A+B)*x) ) / x ;
    }

    // Simpson's method for integral calculus
    // a = lower bound
    // b = upper bound of integration
    // n = number of passes (higher = less margin of error, but takes longer)
    double IntSimpson(double a, double b,int n){
        int i,z;
        double h,s;

        n=n+n;
        s = f(a)*f(b);
        h = (b-a)/n;
        z = 4;

        for(i = 1; i<n; i++){
            s = s + z * f(a+i*h);
            z = 6 - z;
        }
        return (s * h)/3;
    }

    public static void main(String[] args) throws FunctionEvaluationException, MaxIterationsExceededException {
        double[] mins = {0.0000001, 0.00000001 } ;
        double[] maxx = {1000} ;
        final RombergIntegrator si = new RombergIntegrator();
        for (double i : mins)
            for (double  j :maxx) {
                double result2 = si.integrate(x -> ((Math.exp(-A * x) - Math.exp(-(A + B) * x)) / x), i, j);
//        final double result1 = si.integrate(10, x -> 2*Math.pow(x, 1), 0.0, 10.0);
//        System.out.println(result1 + " should be 100.0");
//        final double result2 = si.integrate(1000, x -> Math.sin(x), 0.0, Math.PI);
                System.out.println(i+ "\t" + j + "\t" + result2 * A/ B *100 + "\t" +  result2 * A/ B * tI2N );
            }
    }
}


public class Cal{

    // Class result: calculates the integral and displays the result.
    public static void main(String args[]) throws FunctionEvaluationException, MaxIterationsExceededException {
        // Call class function
        Function function;
        function = new Function();

        // ENTER the desired values of a, b and n !!!
        double a = 0.0001 ;
        double b = Double.MAX_VALUE ;
        int n = 100000 ;
        // Applies simpson method to function
        double result = function.IntSimpson(a,b,n);




        // Show results
        System.out.println("Integral is: " + result);


        ////

    }
}