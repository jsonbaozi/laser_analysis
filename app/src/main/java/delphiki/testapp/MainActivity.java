package delphiki.testapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class MainActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lButton = (Button) findViewById(R.id.l_button);
        wButton = (Button) findViewById(R.id.w_button);
        lButton.setText("Length = " + String.valueOf(length));
        wButton.setText("Width = " + String.valueOf(width));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void pickPhoto(View view) {

        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    public void takePhoto(View view){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, TAKE_IMAGE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Intent intent = new Intent(this, imgDisplay.class);

            Uri imageUri = data.getData();
            intent.setData(imageUri);

            startActivity(intent);
        }

        if (requestCode == TAKE_IMAGE && resultCode == RESULT_OK && data != null ) {
            Intent intent = new Intent(this, imgDisplay.class);

            Uri imageUri = data.getData();
            intent.setData(imageUri);

            startActivity(intent);
        }
    }

    public final static int PICK_IMAGE = 100;
    public final static int TAKE_IMAGE = 101;
    private Button lButton;
    private Button wButton;
    public static double length = 2;
    public static double width = 3.5;
    public static double scale = Math.sqrt(70000/(length*width));
}
/* stackoverflow posts:
screen coord -> bitmap: http://stackoverflow.com/questions/4933612/how-to-convert-coordinates-of-the-image-view-to-the-coordinates-of-the-bitmap/9945896#9945896
projective transform:   http://math.stackexchange.com/questions/296794/finding-the-transform-matrix-from-4-projected-points-with-javascript
screen orientation:     http://stackoverflow.com/questions/12726860/android-how-to-detect-the-image-orientation-portrait-or-landscape-picked-fro/12727053#12727053
3x3 invert:             https://en.wikipedia.org/wiki/Invertible_matrix#Inversion_of_3.C3.973_matrices


 */