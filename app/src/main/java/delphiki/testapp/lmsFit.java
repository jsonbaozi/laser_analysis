package delphiki.testapp;

import android.util.Log;

import Jama.Matrix;
import java.lang.Math;

public class lmsFit {

    public int[] minSolve(double[] beta, double epsilon, double lambda0, double nu){
        double dS = epsilon+1;
        Matrix beta1 = new Matrix(beta,beta.length);
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
                Log.e("n",String.valueOf(n));
                Log.e("beta2 value", toString(beta2.getRowPackedCopy()));
                beta1 = beta2;
                dx0 = dx;
                s0 = s1;
            }
        }
        return toPixelArray(beta1.getRowPackedCopy());

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
        Matrix diffs = new Matrix(projTransform.destHeight, projTransform.destWidth);
        for (int i=0;i<projTransform.destHeight;i++){
            for (int j=0;j<projTransform.destWidth;j++){
                diffs.set(i,j,gaussian(i,j,beta) - projTransform.pixelArray[i*projTransform.destWidth+j]);
            }
        }
        return diffs;
    }

    private Matrix F(Matrix beta){
        double[] temp = beta.getRowPackedCopy();
        Matrix diffs = new Matrix(projTransform.destHeight, projTransform.destWidth);
        for (int i=0;i<projTransform.destHeight;i++){
            for (int j=0;j<projTransform.destWidth;j++){
                diffs.set(i,j,gaussian(i,j,temp) - (double) projTransform.pixelArray[i*projTransform.destWidth+j]);
            }
        }
        return diffs;
    }

    private double gaussian ( double i, double j, double[] beta){
        double e = Math.E;
        return beta[0] * Math.exp(-2 * (Math.pow(j-beta[1],2)/Math.pow(beta[3],2) + Math.pow(i-beta[2],2)/Math.pow(beta[4],2))) + beta[5];
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

    private int[] toPixelArray(double[] beta){
        int[] temp = new int[projTransform.destHeight*projTransform.destWidth];

        for (int i=0;i<projTransform.destHeight;i++){
            for (int j=0;j<projTransform.destWidth;j++){
                temp[i*projTransform.destWidth + j] = (int) Math.round(gaussian(i,j,beta));
            }
        }
        return temp;
    }

    private String toString(double[] temp){
        String string = " \n";
        for(int i=0;i<temp.length;i++){
            string += String.valueOf(temp[i])+" ";
        }
        return string;
    }

    private double d = 1e-6;
}
