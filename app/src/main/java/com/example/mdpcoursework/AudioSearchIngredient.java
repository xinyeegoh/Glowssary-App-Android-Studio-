package com.example.mdpcoursework;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

//refer to:
//   speech recognition: https://developer.android.com/reference/android/speech/SpeechRecognizer
//   access microphone: https://developer.android.com/training/permissions/requesting

//purpose: to allow users search ingredient by voice input
//NOTE:
//while we can access the microphone on any emulators,
//emulators API 30 Level 11 or below don't support Virtual Microphone
//thus, testing speech recognition or voice recording using emulators directly only works on emulators API 12 (Level 31) onwards
//alternatively, option 1: test on real device
//               option 2: change to Emulator Pixel 2 API 31
//                         enable "Virtual Microphone uses host input" of the emulator.
public class AudioSearchIngredient extends AppCompatActivity implements HideSystemBars{

    Intent intent;
    SpeechRecognizer speechRecognizer;
    View screen;

    Button audio_btn, back_btn;
    TextView display;
    static int retry=0; //record the number of times the audio-record permission has been asked

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        screen = getWindow().getDecorView();
        hidesystembars_oncreate(screen);

        setContentView(R.layout.audio_ingredient_page);

        audio_btn = findViewById(R.id.audio_btn);
        back_btn = findViewById(R.id.back_btn);
        display = findViewById(R.id.audio_ingredient_title);


        //if back button is clicked, back to previous page
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //set up audio materials
        setup_audio();
        //check user's permission for microphone / audio recording
        check_audio_permission();

        //when audio-recording button is pressed or released
        audio_btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                //if pressed, speech recogniser starts listening
                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){ //pressed
                    audio_btn.setText("Pressing...");
                    display.setText("Listening...");
                    speechRecognizer.startListening(intent);
                }
                //if released, speech recogniser stops listening
                else if (motionEvent.getAction()==MotionEvent.ACTION_UP){ //released
                    audio_btn.setText("PRESS ME");
                    display.setText("Processing...");
                    speechRecognizer.stopListening();

                }
                return false;
            }
        });


    }

    //set up necessary components for audio-record
    private void setup_audio() {

        //create a Speech Recognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        //set up intent for recognizing speech
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");

        //set up listener for speech recognizer -> to process audio
        //called when recogniser startListening(), stopListening()
        speechRecognizer.setRecognitionListener(new RecognitionListener() {

            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {
                System.out.println("Start Listening");
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {
                System.out.println("Stop Listening");

            }

            //inform user when speech recogniser is having errors detecting their speech
            @Override
            public void onError(int i) {
                display.setText("Error listening, please retry");
            }

            //when speech recogniser is done processing users' speech
            @Override
            public void onResults(Bundle bundle) {

                //retrieve result
                ArrayList<String> results = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String result = results.get(0);

                //display result on view
                display.setText(results.get(0));
                Toast.makeText(getApplicationContext(),result,Toast.LENGTH_SHORT).show();

                //go to View Ingredients page
                Intent results_intent = new Intent(AudioSearchIngredient.this, ViewIngredients.class);
                //pass the audio result to View Ingredient page
                results_intent.putExtra("audio_result",result);
                //notify View Ingredients page that the intent is coming from Audio Search
                results_intent.putExtra("source","ingredient_audio");
                startActivity(results_intent);

                //SpeechRecognizer isn't "smart" / accurate enough in detecting voice,
                // to test the basic idea / functionality,
                // try declare and execute the intent above at audio_btn.setOnTouchListener, after line 93
                // for intent Extras, replace with:
                //     results_intent.putExtra("audio_result","benzoyl peroxide");
                //     or "salicylic acid", "retinol", "glycerin",...

            }

            @Override
            public void onPartialResults(Bundle bundle) {
                Toast.makeText(getApplicationContext(),"" +
                        "incomplete Listening...",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

    }


    //check user's permission to record audio
    private void check_audio_permission() {

        //Determine whether your app is already granted with the permission to access user's device audio
        //if PERMISSION is not GRANTED yet,
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            //ask for permission to access and record audio,
            //requestCode is set to retrieve result later in onRequestPermissionsResult()
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},1);
        }

    }

    //method called when user allows/denies the permission to access audio record
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //if requestcode==1, we are fetching the right permission result
        //since we set our requestCode as 1 earlier in onCreate()
        if(requestCode==1){

            //if grantResults isn't empty (permission is not cancelled) and grantResults == permission granted
            //we can access audio
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Microphone Accessed",Toast.LENGTH_SHORT).show();
            }

            //else, permission denied, show message, ask user whether to retry or go back to Main Page
            else {

                AlertDialog.Builder builder = new AlertDialog.Builder(AudioSearchIngredient.this);
                builder.setMessage("Sorry, this feature can't work as microphone can't be accessed. Click anywhere to dismiss.");
                builder.setCancelable(true);

                //if user has never retried before, ask user whether to retry
                //retry -> ask for permission again
                if(retry<1){
                    builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            retry+=1;
                            ActivityCompat.requestPermissions(AudioSearchIngredient.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                        }
                    });
                }

                //if user has previously retried but still deny the permission,
                //the system will stop asking user for permission.
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

    //stop recording audio once users leave this activity
    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.stopListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hidesystembars(screen);
    }



}
