package delphiki.testapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
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
        viewW = imageView.getWidth();
        viewH = imageView.getHeight();
    }

    protected void display(Uri imgUri){

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
            rotate = getCameraPhotoOrientation(imgDisplay.this, imgUri);
            matrix.postRotate(rotate);
            rotatedbmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

            //draw bitmap to new canvas
            Bitmap tempBitmap = Bitmap.createBitmap(rotatedbmp.getWidth(),rotatedbmp.getHeight(),
                    Bitmap.Config.RGB_565);
            imgCanvas = new Canvas(tempBitmap);
            imgCanvas.drawBitmap(rotatedbmp, 0, 0, null);

            //set imageView to canvas drawable
            imageView = (DrawView) findViewById(R.id.DrawView);
            imageView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
        }
    }

    final float[] getPointerCoords(ImageView view, float x, float y){
        final float[] tempCoords = new float[] {x,y};
        Matrix matrix = new Matrix();
        view.getImageMatrix().invert(matrix);
        matrix.postTranslate(view.getScrollX(), view.getScrollY());
        matrix.mapPoints(tempCoords);
        return tempCoords;
    }

    public void transform(View view){

        double[] pointArray = new double[10];

        float[] topLeft = getPointerCoords(imageView,imageView.topLeft.x,imageView.topLeft.y);
        float[] topRight = getPointerCoords(imageView,imageView.topRight.x,imageView.topRight.y);
        float[] bottomRight = getPointerCoords(imageView,imageView.bottomRight.x,imageView.bottomRight.y);
        float[] bottomLeft = getPointerCoords(imageView,imageView.bottomLeft.x,imageView.bottomLeft.y);
        float[] middle = getPointerCoords(imageView,imageView.middle.x,imageView.middle.y);

        pointArray[0] = topLeft[0];
        pointArray[1]= topLeft[1];
        pointArray[2] = topRight[0];
        pointArray[3] = topRight[1];
        pointArray[4] = bottomRight[0];
        pointArray[5] = bottomRight[1];
        pointArray[6] = bottomLeft[0];
        pointArray[7] = bottomLeft[1];
        pointArray[8] = middle[0];
        pointArray[9] = middle[1];

        Log.i("topleft",String.valueOf(topLeft[0])+" "+String.valueOf(topLeft[1]));
        Log.i("mid",String.valueOf(middle[0])+" "+String.valueOf(middle[1]));

        Intent intent = new Intent(this, projTransform.class);
        intent.setData(imgUri);
        intent.putExtra("points", pointArray);
        intent.putExtra("rotate", rotate);
        startActivity(intent);

    }

    public int getCameraPhotoOrientation(Context context, Uri imageUri){
        int temp = 0;
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(imgUri, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String imagePath = cursor.getString(columnIndex);
        cursor.close();
        try {
            context.getContentResolver().notifyChange(imageUri, null);
            File imageFile = new File(imagePath);

            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    temp = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    temp = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    temp = 90;
                    break;
            }

            Log.i("RotateImage", "Exif orientation: " + orientation);
            Log.i("RotateImage", "Rotate value: " + temp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return temp;
    }


    private Uri imgUri;
    private Canvas imgCanvas;
    private Bitmap bmp;
    private Bitmap rotatedbmp;
    private DrawView imageView;
    private int rotate;
    public static int viewW, viewH;

}
