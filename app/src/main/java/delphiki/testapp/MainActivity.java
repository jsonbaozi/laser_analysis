package delphiki.testapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends Activity {

    final Context context = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dimensButton = (Button) findViewById(R.id.dimensButton);
        dimensButton.setText("Length = " + String.valueOf(length) + "   Width = " + String.valueOf(width));

        dimensButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater li = LayoutInflater.from(context);
                View promptView = li.inflate(R.layout.dimens_prompt, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptView);

                final EditText lInput = (EditText) promptView
                        .findViewById(R.id.LengthInput);
                final EditText wInput = (EditText) promptView
                        .findViewById(R.id.WidthInput);

                // set dialog message
                alertDialogBuilder
                        .setPositiveButton("Set Dimensions",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        String lString = lInput.getText().toString();
                                        String wString = wInput.getText().toString();
                                        if (!lString.isEmpty() && !wString.isEmpty()) {
                                            length = Double.parseDouble(lString);
                                            width = Double.parseDouble(wString);
                                            dimensButton.setText("Length = " + String.valueOf(length) + "   Width = " + String.valueOf(width));
                                        }
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        });
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
    private Button dimensButton;
    public static double length = 1;
    public static double width = 1;
    public static double scale = Math.sqrt(70000/(length*width));
}
