package delphiki.laser;

import android.util.Log;

import Jama.Matrix;
import java.lang.Math;

//finalBeta[0..5] = [a, middle(x), middle(y), wx, wy, b]
public class lmsFit {

    public double[] runFit(int[] pixels, int arrayWidth, double[] beta, double epsilon, double lambda0, double nu){
        pixelArray = pixels;
        width = arrayWidth;
        length = pixels.length/(arrayWidth);

/*        String string = " \n";

        for (int i=0;i<length;i++){
            for (int j=0;j<width;j++){
                string += String.valueOf(pixelArray[i*width + j])+" ";
            }
            string += "\n";
        }
        Log.e("pixels", string);*/
        //Log.e("length, width", String.valueOf(length)+" "+String.valueOf(width)+" "+String.valueOf(pixelArray.length));
        return minSolve(beta, epsilon, lambda0, nu);
    }

    private double[] minSolve(double[] beta, double epsilon, double lambda0, double nu){
        double dS = epsilon+1;
        beta1 = new Matrix(beta,beta.length);
        int n = 0;
        double lambda = lambda0;
        Matrix dx0 = new Matrix(6,1);
        double s0 = dot(F(beta).getRowPackedCopy());
        Matrix dx, beta2, fbeta1, fbeta2, df;
        double s1;

        while ((1+lambda)*dS > epsilon && n<1000){
            dx = delta(beta1.getRowPackedCopy(), lambda);
            beta2 = beta1.plus(dx);
            fbeta1 = F(beta1);
            fbeta2 = F(beta2);
            df = fbeta1.minus(fbeta2);
            dS = dot(df.getRowPackedCopy());
            s1 = dot(fbeta2.getRowPackedCopy());

            if (s1 > s0){
                lambda *= 10.;
            } else{
                if (dot(dx.getRowPackedCopy(),dx0.getRowPackedCopy())>0){
                    lambda /= nu;
                } else {
                    lambda *= nu;
                }
                n++;
                //Log.e("n",String.valueOf(n));
                //Log.e("beta2 value", toString(beta2.getRowPackedCopy()));
                beta1 = beta2;
                dx0 = dx;
                s0 = s1;
            }
        }
        return beta1.getRowPackedCopy();
        //return toPixelArray(beta1.getRowPackedCopy());
    }

    private Matrix delta(double[] beta, double lambda){
        int n = beta.length;
        double[] betam, betap;
        Matrix[] dFdx = new Matrix[n];
        for (int i=0;i<n;i++){
            betam = beta.clone();
            betap = beta.clone();
            betam[i] -= d;
            betap[i] += d;
            dFdx[i] = (F(betap).minus(F(betam))).times(1/(2*d));
        }

        Matrix A = new Matrix(n,n);
        for (int i=0;i<n;i++){
            for(int j=0;j<n;j++){
                A.set(i,j,dot(dFdx[i].getRowPackedCopy(),dFdx[j].getRowPackedCopy()));
            }
        }
        double[] b = new double[n];
        for (int i=0;i<n;i++){
            b[i] = dot((F(beta).times(-1)).getRowPackedCopy(), dFdx[i].getRowPackedCopy());
        }
        return ((A.plus(Matrix.identity(n, n).times(lambda*A.trace()/n)))).inverse().times((new Matrix(b, b.length)));
    }

    private Matrix F(double[] beta){
        Matrix diffs = new Matrix(length, width);
        for (int i=0;i<length;i++){
            for (int j=0;j<width;j++){
                diffs.set(i,j,gaussian(i,j,beta) - pixelArray[i*width + j]);
            }
        }
        //Log.e("diffs", toString(diffs.getRowPackedCopy()));
        return diffs;
    }

    private Matrix F(Matrix beta){
        double[] temp = beta.getRowPackedCopy();
        Matrix diffs = new Matrix(length, width);
        for (int i=0;i<length;i++){
            for (int j=0;j<width;j++){
                diffs.set(i,j,gaussian(i,j,temp) - pixelArray[i*width + j]);
            }
        }
        //Log.e("diffs", toString(diffs.getRowPackedCopy()));
        return diffs;
    }

    private double gaussian ( double i, double j, double[] beta){
        double e = Math.E;
        return Math.min(beta[0] * Math.exp(-2 * (Math.pow(j-beta[1],2)/Math.pow(beta[3],2) +
                Math.pow(i-beta[2],2)/Math.pow(beta[4],2))) + beta[5], 255);
    }

    private double dot(double[] a, double[] b){
        if (a.length != b.length){
            Log.e("lmsFit.dot", "cannot dot 2 vectors of different length");
        }
        double sum = 0;
        for(int i=0;i<a.length;i++){
            sum += a[i]*b[i];
        }
        return sum;
    }

    private double dot(double[] a){
        double sum = 0;
        for(int i =0;i<a.length;i++){
            sum += Math.pow(a[i],2);
        }
        return sum;
    }

    public int[] toPixelArray(Matrix beta1){
        double[] beta = beta1.getRowPackedCopy();
        int[] temp = new int[length*width];
        for (int i=0;i<length;i++){
            for (int j=0;j<width;j++){
                temp[i*width + j] = (int) Math.round(gaussian(i,j,beta));
            }
        }
        return temp;
    }

    public void getFittedData(){}

    private String toString(double[] temp){
        String string = " \n";
        for(int i=0;i<temp.length;i++){
            string += String.valueOf(temp[i])+" ";
        }
        return string;
    }

    private String toString(int[] temp){
        String string = " \n";
        for(int i=0;i<temp.length;i++){
            string += String.valueOf(temp[i])+" ";
        }
        return string;
    }


    private double d = 1;
    //double[] finalBeta;
    int[] pixelArray;
    Matrix beta1;
    int length;
    int width;
}
