package delphiki.testapp;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.content.ClipboardManager;
import android.util.Log;
import android.widget.ImageView;
import android.graphics.Color;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;


public class projTransform extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proj_transform);

        Intent parent_intent = getIntent();
        //pointArray[0-9] = topleft, topright, bottomright, bottomleft, middle
        pointArray = parent_intent.getDoubleArrayExtra("points");
        rotate = parent_intent.getIntExtra("rotate", 0);
        transform(parent_intent.getData(),pointArray);
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

            //scaling dimensions for estimating fine fitting
            boolean wide = MainActivity.width > MainActivity.length;
            double shorter = wide ? MainActivity.length : MainActivity.width;
            double roughScale = 4*ESTIMATION_SIZE/(shorter);
            int roughWidth = (int) Math.round(MainActivity.width*roughScale);
            int roughLength = (int) Math.round(MainActivity.length*roughScale);
            int ROUGH_SIDE = (roughWidth > roughLength) ? roughLength/4 : roughWidth/4;

            //map for transformed bmp
            double[] destArray = new double[] {0,0,roughWidth,0,roughWidth,roughLength,0,roughLength};
            double[][] destMap = tMap(destArray);

            // C = B*[A^(-1)]
            double[][] finalMap = mMult(sourceMap, mInvert3x3(destMap));

            double[][] reverseMap = mMult(destMap, mInvert3x3(sourceMap));
            int[] middle = pixelMap(reverseMap, pointArray[9], pointArray[8]);

            int[] roughPixels = new int[ROUGH_SIDE*ROUGH_SIDE];
            int i0 = middle[1]-(ROUGH_SIDE/2), i1 = middle[1]+(ROUGH_SIDE/2),
                    j0 = middle[0]-(ROUGH_SIDE/2), j1 = middle[0]+(ROUGH_SIDE/2);
            int[] temp;
            for(int i=i0;i<i1;i++) {
                for (int j=j0;j<j1;j++) {
                    temp = pixelMap(finalMap,i,j);
                    roughPixels[(i-i0)*ROUGH_SIDE + (j-j0)] = rotatedbmp.getPixel(temp[0],temp[1]);
                }
            }

            roughPixels = toGreyscale(roughPixels, ROUGH_SIDE);
            double[] roughValues = lms.runFit(roughPixels, ROUGH_SIDE,
                    new double[]{10, ESTIMATION_SIZE / 2, ESTIMATION_SIZE / 2, 5, 5, 1}, 1e-6, 1000, 20);

            Log.e("rough values", String.valueOf(roughValues[1])+" "+String.valueOf(roughValues[2])+" "
                    +String.valueOf(roughValues[3])+" "+String.valueOf(roughValues[4]));

            int[] fittedMiddle = pixelMap(finalMap, i0 + roughValues[2], j0 + roughValues[1]);
            double rx = (roughValues[3] > roughValues[4]) ?
                    ESTIMATION_SIZE/(roughValues[3]) : ESTIMATION_SIZE/(roughValues[4]);
            scale = Math.abs(rx * FINAL_SIZE / shorter);
            int finalWidth = (int) Math.round(MainActivity.width*scale);
            int finalLength = (int) Math.round(MainActivity.length*scale);

            Log.e("asdf",String.valueOf(finalWidth)+" "+String.valueOf(finalLength));

            destArray = new double[] {0,0,finalWidth,0,finalWidth,finalLength,0,finalLength};
            destMap = tMap(destArray);

            finalMap = mMult(sourceMap, mInvert3x3(destMap));
            reverseMap = mMult(destMap, mInvert3x3(sourceMap));
            middle = pixelMap(reverseMap, fittedMiddle[1], fittedMiddle[0]);

            int[] finalPixels = new int[FINAL_SIZE*FINAL_SIZE];
            i0 = middle[1]-FINAL_SIZE/2; i1 = middle[1]+FINAL_SIZE/2;
            j0 = middle[0]-FINAL_SIZE/2; j1 = middle[0]+FINAL_SIZE/2;

            Log.e("squares",String.valueOf(i0)+" "+String.valueOf(i1)+" "+String.valueOf(j0)+" "+String.valueOf(j1));

            for(int i=i0;i<i1;i++) {
                for (int j=j0;j<j1;j++) {
                    temp = pixelMap(finalMap,i,j);
                    finalPixels[(i-i0)*FINAL_SIZE + (j-j0)] = rotatedbmp.getPixel(temp[0],temp[1]);
                }
            }

/*            finalPixels = toGreyscale(finalPixels, FINAL_SIZE);

            double[] finalValues = lms.runFit(finalPixels, FINAL_SIZE,
                new double[]{10,FINAL_SIZE/2,FINAL_SIZE/2,10,10,1},1e-6,1000,20);

            Log.e("final values", String.valueOf(finalValues[1])+" "+String.valueOf(finalValues[2])+" "
                    +String.valueOf(finalValues[3])+" "+String.valueOf(finalValues[4]));*/

            //display(finalPixels,FINAL_SIZE);
            display2(finalPixels);
        }
    }

    private int[] toGreyscale(int[] pixels, int width){
        int [] grey = new int[pixels.length];
        for(int i=0; i<pixels.length/width; i++){
            for(int j=0; j<width; j++){
                grey[i*width + j] = (Color.green(pixels[(i*width)+j])+Color.red(pixels[(i*width)+j])+Color.blue(pixels[(i*width)+j]))/3;
            }
        }
/*        int i0 = middle[1]-(width/2), i1 = middle[1]+(width/2), j0 = middle[0]-(width/2), j1 = middle[0]+(width/2);
        for(int i=i0;i<i1;i++){
            for(int j=j0;j<j1;j++){
                grey[(i-i0)*width + (j-j0)] = (Color.green(pixels[(i*width)+j])+Color.red(pixels[(i*width)+j])+Color.blue(pixels[(i*width)+j]))/3;
            }
        }*/
        return grey;
    }

    //scale fitted value to <scale> max
    private int[] scaleTo(double scale, int[] pixels){
        double max = 0;
        for(int i=0;i<pixels.length;i++){
            if (max<pixels[i]){ max = pixels[i]; }
        }
        max = (scale/max);

        for(int i=0;i<pixels.length;i++){
            pixels[i]*=max;
        }
        return pixels;
    }

    private int[] graph(double x, double y, int[] fitted, int[] greyScale){

        int ix = (int) Math.round(x);
        int iy = (int) Math.round(y);

        for (int i=0; i<FINAL_SIZE; i++){
            //fitted values graph
            greyScale[(fitted[ix*FINAL_SIZE + i]*(FINAL_SIZE/3)/255)*FINAL_SIZE + i] = -2;
            greyScale[i*FINAL_SIZE + (fitted[i*FINAL_SIZE + iy]*(FINAL_SIZE/3)/255)] = -2;

            //original value graph
            greyScale[(greyScale[ix*FINAL_SIZE + i]*(FINAL_SIZE/3)/255)*FINAL_SIZE + i] = -1;
            greyScale[i*FINAL_SIZE + (greyScale[i*FINAL_SIZE + iy]*(FINAL_SIZE/3)/255)] = -1;
        }

        return greyScale;
    }

    private void display(int[] pixels, int width) {
        Bitmap cropped = Bitmap.createBitmap(pixels, width, pixels.length/width, Bitmap.Config.RGB_565);
        //set imageView
        imageView = (ImageView) findViewById(R.id.imageView2);
        imageView.setImageBitmap(cropped);
    }

    private void display2(int[] pixels) {

        pixels = toGreyscale(pixels, FINAL_SIZE);
        double[] finalValues = lms.runFit(pixels, FINAL_SIZE,
                new double[]{10, FINAL_SIZE / 2, FINAL_SIZE / 2, 10, 10, 1}, 1e-6, 1000,20);

        Log.e("final values", String.valueOf(finalValues[1])+" "+String.valueOf(finalValues[2])+" "
                +String.valueOf(finalValues[3])+" "+String.valueOf(finalValues[4]));

        int[] withGraph = graph(finalValues[1], finalValues[2], scaleTo(255, lms.toPixelArray(lms.beta1)), scaleTo(255, pixels));
        int[] color;
        for(int i=0;i<withGraph.length;i++){
            color = toColor(withGraph[i]);
            withGraph[i] = Color.rgb(color[0], color[1], color[2]);
        }

        Bitmap cropped = Bitmap.createBitmap(withGraph, FINAL_SIZE, FINAL_SIZE, Bitmap.Config.RGB_565);
        //set imageView
        imageView = (ImageView) findViewById(R.id.imageView2);
        imageView.setImageBitmap(cropped);

        //values[0] = wx; values[1] = wy; values[2] = ellipticity;
        double wx = (finalValues[3])/scale;
        double wy = (finalValues[4])/scale;
        MathContext mc = new MathContext(4);

        BigDecimal bdx = new BigDecimal(wx);
        bdx = bdx.round(mc);
        String text = "wx = "+String.valueOf(bdx.doubleValue())+"mm\n";

        BigDecimal bdy = new BigDecimal(wy);
        bdy = bdy.round(mc);
        text += "wy = "+String.valueOf(bdy.doubleValue())+"mm\n";

        double ellipticity;
        ellipticity = (wx > wy ? (wx-wy)/((wx+wy)/2) : (wy-wx)/((wx+wy)/2)) ;
        BigDecimal bde = new BigDecimal(ellipticity);
        bde = bde.round(mc);
        text += "Ellipticity = "+String.valueOf(bde.doubleValue());

        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(text);

        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("beta values", text);
        clipboard.setPrimaryClip(clip);
    }

    //greyscale to RGB based on intensity
    //0 = R, 1 = G, 2 = B
    private int[] toColor(double color) {
        int[] temp = new int[3];
        if (color >= 204) {
            temp[0] = 255;
            temp[1] = (int) Math.round(255 - (color - 204) * 5);
            temp[2] = 0;
        } else if (color >= 153) {
            temp[0] = (int) Math.round((color - 153) * 5);
            temp[1] = 255;
            temp[2] = 0;
        } else if (color >= 102) {
            temp[0] = 0;
            temp[1] = 255;
            temp[2] = (int) Math.round(255 - (color - 102) * 5);
        } else if (color >= 51) {
            temp[0] = 0;
            temp[1] = (int) Math.round((color - 51) * 4);
            temp[2] = 255;
        } else if (color == -1){
            temp[0] = 0;
            temp[1] = 0;
            temp[2] = 0;
        } else if (color == -2){
            temp[0] = 255;
            temp[1] = 0;
            temp[2] = 0;
        } else {
            temp[0] = (int) Math.round(255- color*5);
            temp[1] = 0;
            temp[2] = 255;
        }
        return temp;
    }

    private String toString(double[][] temp){
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
    }

    private lmsFit lms = new lmsFit();
    public static int fitCrop = 120;
    private int ESTIMATION_SIZE = 35;
    private int FINAL_SIZE = 100;
    private double[] pointArray;
    private int rotate;
    private Bitmap tempBmp;
    private ImageView imageView;
    private double scale = 1;
    //private static double roughScale = Math.sqrt(2500/(MainActivity.length*MainActivity.width));
    //destLength destWidth pixel sizes of destination matrix
    //private static int destLength = (int) Math.round(MainActivity.length*scale);
    //private static int destWidth = (int) Math.round(MainActivity.width*scale);
}
