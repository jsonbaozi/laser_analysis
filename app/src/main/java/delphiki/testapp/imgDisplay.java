package delphiki.testapp;

import android.app.Activity;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;

import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.Button;

import java.io.FileNotFoundException;
import java.io.InputStream;


public class imgDisplay extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img_display);

        Intent parent_intent = getIntent();
        imgUri = parent_intent.getData();
        display(imgUri);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_img_display, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class Pixel{
        private final int x;
        private final int y;

        public Pixel(int x, int y)
        {
            this.x = x;
            this.y = y;
        }

        public int getX(){ return x; }

        public int getY(){ return y; }
    }

    protected void display(Uri imgUri){

/*
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(10);
        mPaint.setStrokeCap(Cap.ROUND);
*/

        if (imgUri != null) {
            //decode uri
            try {
                InputStream imgStream = getContentResolver().openInputStream(imgUri);
                bmp = BitmapFactory.decodeStream(imgStream);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            //rotate bitmap
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            rotatedbmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

            //draw bitmap to new canvas
            Bitmap tempBitmap = Bitmap.createBitmap(rotatedbmp.getWidth(),rotatedbmp.getHeight(),
                    Bitmap.Config.RGB_565);
            imgCanvas = new Canvas(tempBitmap);
            imgCanvas.drawBitmap(rotatedbmp, 0, 0, null);

            //set imageView to canvas drawable
            imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));

            imageView.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    int action = event.getAction();
                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            currX = event.getX()*3;
                            currY = event.getY()*3;
                            //drawPoint(imgCanvas);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            currX = event.getX()*3;
                            currY = event.getY()*3;
                            //drawPoint(imgCanvas);
                            break;
                        case MotionEvent.ACTION_UP:
                            currX = event.getX()*3;
                            currY = event.getY()*3;
                            //setPoint(imgCanvas);
                            break;
                        case MotionEvent.ACTION_CANCEL:
                            break;
                        default:
                            break;

                    }
                    return true;
                }
            });

            //setPoints(imgCanvas);
        }
    }

/*    protected void drawPoints(Canvas canvas){
        //Button button = (Button) findViewById(R.id.button2);

        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(25);
        mPaint.setStrokeCap(Cap.ROUND);

    }*/

    public void setPoint(View view){
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(25);
        mPaint.setStrokeCap(Cap.ROUND);


        Button button = (Button) findViewById(R.id.button2);

        imgCanvas.drawPoint(currX,currY,mPaint);

        pixels[iterator] = new Pixel( Math.round(currX), Math.round(currY) );
        if (pixels[iterator].getX() > maxX){ maxX = pixels[iterator].getX();}
        if (pixels[iterator].getX() < minX){ minX = pixels[iterator].getX();}
        if (pixels[iterator].getY() > maxY){ maxY = pixels[iterator].getY();}
        if (pixels[iterator].getY() < minY){ minY = pixels[iterator].getY();}
        xy = Float.toString(currX) + ", " + Float.toString(currY);

        if(iterator < 3){ iterator++; }
        else{ transform();}
        button.setText(xy + ", " + buttonText[iterator]);

    }

    protected void transform(){

        maxX += 50;
        minX -= 50;
        maxY += 50;
        minY -= 50;

        int width = maxX - minX;
        int height = maxY - minY;
        double[] pointArray = new double[8];

        //scale points to cropped array
        for (int i = 0; i < 8; i+=2){
            pointArray[i] = pixels[i/2].getX()-minX;
            pointArray[i+1] = pixels[i/2].getY()-minY;
        }

        //int[] crop = new int[width*height];
        //rotatedbmp.getPixels(crop, 0, width, minX, minY, width, height);

        int[] dimens = new int[4];
        dimens[0] = width;
        dimens[1] = height;
        dimens[2] = minX;
        dimens[3] = minY;

        Intent intent = new Intent(this, projTransform.class);
        //intent.putExtra("cropped", crop);
        intent.setData(imgUri);
        intent.putExtra("points", pointArray);
        intent.putExtra("dimens", dimens);
        startActivity(intent);

    }


    private Uri imgUri;
    private Canvas imgCanvas;
    private Bitmap bmp;
    private Bitmap rotatedbmp;
    private ImageView imageView;
    private float currX, currY;
    private String xy;
    private Pixel[] pixels = new Pixel[4];
    private int minX = 100000, minY = 100000, maxX = 0, maxY = 0;
    private Paint mPaint = new Paint();
    //private Button button = (Button) findViewById(R.id.button2);
    private final String[] buttonText = {"Select point (0,0)", "Select point (0,1)", "Select point (1,0)", "Select point (1,1)"};
    private int iterator = 0;
}
