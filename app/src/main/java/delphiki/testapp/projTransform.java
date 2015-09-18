package delphiki.testapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;
import android.widget.ImageView;
import android.graphics.Color;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class projTransform extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proj_transform);

        Intent parent_intent = getIntent();
        Uri imgUri = parent_intent.getData();
        pointArray = parent_intent.getDoubleArrayExtra("points");
        rotate = parent_intent.getIntExtra("rotate", 0);
        transform(imgUri,pointArray);
    }
    //A*B = C
    private static double[][] mMult(double[][] A, double[][] B){
        int mA = A.length;
        int nA = A[0].length;
        int mB = B.length;
        int nB = B[0].length;
        if (nA != mB) throw new RuntimeException("Illegal matrix dimensions.");
        double[][] C = new double[mA][nB];
            for (int i = 0; i < mA; i++)
            for (int j = 0; j < nB; j++)
                for (int k = 0; k < nA; k++)
                    C[i][j] += A[i][k] * B[k][j];
        return C;
    }
    //A*x = y
    private static double[] mMult(double[][] A, double[] x){
        int m = A.length;
        int n = A[0].length;
        if (x.length != n) throw new RuntimeException("Illegal matrix dimensions.");
        double[] y = new double[m];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                y[i] += A[i][j] * x[j];
        return y;
    }
    //A^(-1)
    private static double[][] mInvert3x3(double[][] X){
        double[][] Y = new double[3][3];
        double A,B,C,D,E,F,G,H,I,detX;
        A =   X[1][1]*X[2][2] - X[1][2]*X[2][1];
        B = -(X[1][0]*X[2][2] - X[1][2]*X[2][0]);
        C =   X[1][0]*X[2][1] - X[1][1]*X[2][0];
        D = -(X[0][1]*X[2][2] - X[0][2]*X[2][1]);
        E =   X[0][0]*X[2][2] - X[0][2]*X[2][0];
        F = -(X[0][0]*X[2][1] - X[0][1]*X[2][0]);
        G =   X[0][1]*X[1][2] - X[0][2]*X[1][1];
        H = -(X[0][0]*X[1][2] - X[0][2]*X[1][0]);
        I =   X[0][0]*X[1][1] - X[0][1]*X[1][0];
        detX = X[0][0]*A + X[0][1]*B + X[0][2]*C;

        Y[0][0] = A/detX;
        Y[1][0] = B/detX;
        Y[2][0] = C/detX;
        Y[0][1] = D/detX;
        Y[1][1] = E/detX;
        Y[2][1] = F/detX;
        Y[0][2] = G/detX;
        Y[1][2] = H/detX;
        Y[2][2] = I/detX;

        return Y;
    }

    private void transform(Uri data, double[] sourceArray){

        if (data != null) {
            try {
                InputStream imgStream = getContentResolver().openInputStream(data);
                tempBmp = BitmapFactory.decodeStream(imgStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            Matrix matrix = new Matrix();
            matrix.postRotate(rotate);
            Bitmap rotatedbmp = Bitmap.createBitmap(tempBmp, 0, 0, tempBmp.getWidth(), tempBmp.getHeight(), matrix, true);


            //map for original bmp
            double[][] sourceMap = tMap(sourceArray);

            //map for transformed bmp
            double[] destArray = new double[] {0,0,destWidth,0,destWidth,destHeight,0,destHeight};
            double[][] destMap = tMap(destArray);

            // C = B*[A^(-1)]
            double[][] finalMap = mMult(sourceMap, mInvert3x3(destMap));

            int[] destPixels = new int[destHeight*destWidth];
            int[] temp;
            for(int i=0; i<destHeight-1; i++){
                for(int j=0; j<destWidth-1; j++){
                    temp = pixelMap(finalMap,i,j);
                    destPixels[(i*destWidth)+j] = rotatedbmp.getPixel(temp[0],temp[1]);
                }
            }
            display(destPixels);
            display2(destPixels);
        }
    }

    //produces mapping matrix given corners
    //A,B in SE post
    private double[][] tMap(double[] pointArray){
        double[][] tempArray = new double[3][3];
        tempArray[0][0] = pointArray[0];
        tempArray[1][0] = pointArray[1];
        tempArray[0][1] = pointArray[2];
        tempArray[1][1] = pointArray[3];
        tempArray[0][2] = pointArray[4];
        tempArray[1][2] = pointArray[5];
        for(int i=0; i<3; i++) {
            tempArray[2][i] = 1;
        }

        double[] tempVector = new double[] {pointArray[6], pointArray[7], 1};

        double[][] inverted = mInvert3x3(tempArray);

        double[] coef = mMult(inverted, tempVector);

        double[][] tran = new double[3][3];

        for(int i=0; i<3; i++){
            for (int j=0; j<3; j++){
                tran[i][j] = tempArray[i][j]*coef[j];
            }
        }
        return tran;
    }

    private int[] pixelMap(double[][] map, double x, double y){
        double[] tempVector = new double[] {y,x,1};
        double[] primeVector = mMult(map,tempVector);
        //ret (x'',y'')
        return new int[] {(int) Math.round(primeVector[0]/primeVector[2]), (int) Math.round(primeVector[1]/primeVector[2])};
    }

    private void display(int[] pixels) {

        Bitmap cropped = Bitmap.createBitmap(pixels, destWidth, destHeight, Bitmap.Config.RGB_565);
        //set imageView
        imageView = (ImageView) findViewById(R.id.imageView2);
        imageView.setImageBitmap(cropped);
    }

    private void display2(int[] pixels) {
        int[] grey = new int[pixels.length];
        for(int i=0;i<pixels.length;i++){
            grey[i] = (Color.green(pixels[i])+Color.red(pixels[i])+Color.blue(pixels[i]))/3;
        }
        pixelArray = grey;
        lmsFit lms = new lmsFit();
        int[] lmsFitted = lms.minSolve(new double[]{10,175,100,10,10,1},1e-6,1000,20);
        int[] fittedPixels = new int[lmsFitted.length];
        for(int i=0;i<grey.length;i++){
            fittedPixels[i] = Color.rgb(lmsFitted[i], lmsFitted[i], lmsFitted[i]);
        }

        Bitmap cropped = Bitmap.createBitmap(fittedPixels, destWidth, destHeight, Bitmap.Config.RGB_565);
        //set imageView
        imageView = (ImageView) findViewById(R.id.imageView3);
        imageView.setImageBitmap(cropped);
    }

/*    private String toString(double[][] temp){
        String string = " \n";
        for(int i=0;i<temp.length;i++){
            for(int j=0;j<temp[0].length;j++){
                string += String.valueOf(temp[i][j])+" ";
            }
            string += "\n";
        }
        return string;
    }

    private String toString(double[] temp){
        String string = " \n";
        for(int i=0;i<temp.length;i++){
            string += String.valueOf(temp[i])+" ";
        }
        return string;
    }*/

    private double[] pointArray;
    public static int[] pixelArray;
    private int rotate;
    private Bitmap tempBmp;
    private ImageView imageView;
    public static int destHeight = (int) Math.round(MainActivity.length*MainActivity.scale);
    public static int destWidth = (int) Math.round(MainActivity.width*MainActivity.scale);

}
