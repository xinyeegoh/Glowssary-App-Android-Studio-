package com.example.mdpcoursework;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.Objects;

//refer to:
//      text Recognition: https://developers.google.com/ml-kit/vision/text-recognition/android
//      access camera and process image: https://developer.android.com/training/permissions/requesting

//purpose: to recognise ingredient names from image captured by the user and return information of the corresponding ingredients
//NOTE:
//while we can access the camera on any emulators,
//emulators API 30 Level 11 or below don't show Virtual Scene to camera
//thus, testing the Text Recognition function using a Virtual Image uploaded to the emulator only work on emulators API 31 (Level 12) and above.
//alternatively, option 1: test on real device
//               option 2: install Emulator Pixel 2 API 31
//                         go to AVD Manager
//                         click "edit" for the Emulator
//                         go to advanced settings
//                         set the Emulator BACK Camera to "Virtual Scene", then save
//                         run the emulator
//                         upload an image as the virtual scene of the emulator
//                for how, see: https://developers.google.com/ar/develop/java/emulator#add_augmented_images_to_the_scene
public class CameraSearchIngredient extends AppCompatActivity implements HideSystemBars{

    View screen;
    ActivityResultLauncher<Intent> cameraLauncher;
    TextView camera_result_text_view;
    Button back_btn, view_result_btn;

    static int retry=0;//record the number of times the camera permission has been asked

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        screen = getWindow().getDecorView();
        hidesystembars_oncreate(screen);

        setContentView(R.layout.camera_ingredient_page);

        camera_result_text_view = findViewById(R.id.camera_result_content);
        back_btn = findViewById(R.id.back_btn);
        view_result_btn = findViewById(R.id.camera_result_view_btn);

        //if back button is clicked, back to previous page
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //set up camera materials
        setup_camera();
        //check user's permission on camera
        check_camera_permission();

        //when clicked, display a list of ingredients detected from images, through recycler view
        view_result_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //view a list of detected ingredients
                //not yet completed
            }
        });

    }


    //method to set up necessary components for camera
    private void setup_camera() {

        //set up camera intent launcher
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        //if intent is successfully executed,
                        if(result.getResultCode() == Activity.RESULT_OK){

                            //get captured image
                            Intent data = result.getData();
                            Bitmap bitmap = (Bitmap) Objects.requireNonNull(data).getExtras().get("data");
                            InputImage image = InputImage.fromBitmap(bitmap, 0);

                            //set up text recognizer (Google's ML Kit)
                            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

                            //process the image using the text recognizer
                            Task<Text> cam_result =
                                    recognizer.process(image)
                                            .addOnSuccessListener(new OnSuccessListener<Text>() {
                                                @Override
                                                public void onSuccess(@NonNull Text visionText) {
                                                // Task completed successfully

                                                    StringBuilder complete_text= new StringBuilder();

                                                    //Text will contain blocks of text,
                                                    //loop through each text block,
                                                    for (Text.TextBlock block : visionText.getTextBlocks()) {
                                                        String blockText = block.getText();
                                                        //append each block to complete text
                                                        complete_text.append(blockText).append("\n");
                                                    }

                                                    //add complete text to view
                                                    camera_result_text_view.setText(complete_text.toString());
                                                }

                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                // Task failed

                                                    // inform user
                                                    camera_result_text_view.setText("Fail to process Image");
                                                    Toast.makeText(CameraSearchIngredient.this,
                                                        "Fail to process image",Toast.LENGTH_SHORT).show();
                                                    }

                                            });
                        }

                    }
                });
    }


    private void access_camera() {

        System.out.println("Here");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    //check user's permission on camera access
    private void check_camera_permission() {

        //if permission is ald granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            System.out.println("granted");
            access_camera();
        }
        //else, ask user for permission
        else {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},1);
        }

    }

    //process user's permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1){

            //if permission is granted, access camera
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Camera Accessed",Toast.LENGTH_SHORT).show();
                access_camera();
            }
            //else show message, ask user whether to retry or go back
            else {

                AlertDialog.Builder builder = new AlertDialog.Builder(CameraSearchIngredient.this);
                builder.setMessage("Sorry, this feature can't work as camera can't be accessed. Click anywhere to dismiss");
                builder.setCancelable(true);

                //if user has never retried before this, ask user whether to retry
                //retry -> ask for permission again
                if (retry<1) {
                    builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            retry += 1;
                            ActivityCompat.requestPermissions(CameraSearchIngredient.this, new String[]{Manifest.permission.CAMERA}, 1);
                        }
                    });
                }

                //if user has previously retried and still deny, the system will stop asking user for permission,
                //thus, we'll ask user whether to go to settings
                else{
                    builder.setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                        }
                    });
                }

                AlertDialog alert = builder.create();
                alert.show();
                alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        finish();
                    }
                });

            }

        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        hidesystembars(screen);
    }
}
