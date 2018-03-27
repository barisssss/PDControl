package com.baris.osctest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.net.*;
import java.util.*;

import com.illposed.osc.*;

import xdroid.toaster.Toaster;

import static xdroid.toaster.Toaster.toast;
import static xdroid.toaster.Toaster.toastLong;



public class MainActivity extends AppCompatActivity {

    private EditText ipEt, portEt;
    private Button playButton;
    private SeekBar freqBar, atkBar, dcyBar, sstBar, rlsBar;
    private TextView freqTxt, atkTxt, dcyTxt, sstTxt, rlsTxt;

    // These two variables hold the IP address and port number.
    private String myIP;
    private int myPort;

    // This is used to send messages
    private OSCPortOut oscPortOut;

    // This thread will contain all the code that pertains to OSC
    private Thread oscThread = new Thread() {
        @Override
        public void run() {
            // The first part of the run() method initializes the OSCPortOut for sending messages.

            try {
                // Connect to some IP address and port
                oscPortOut = new OSCPortOut(InetAddress.getByName(myIP), myPort);
                Toaster.toast("connected");
            } catch(UnknownHostException e) {
                // Error handling when your IP isn't found
                Toaster.toast("IP not found");
            } catch(Exception e) {
                // Error handling for any other errors
                Toaster.toast("error");
            }

            // The second part of the run() method loops infinitely
            final boolean[] toggleState = {false};
            while(true) {
                if (oscPortOut != null){
                    final Object[] toggle = new Object[1];

                    playButton.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            switch (motionEvent.getAction()){
                                case MotionEvent.ACTION_DOWN:
                                case MotionEvent.ACTION_CANCEL:
                                case MotionEvent.ACTION_MOVE:
                                    toggle[0] = 1;
                                    break;
                                case MotionEvent.ACTION_UP:
                                    toggle[0] = 0;
                                    break;
                            }
                            toggleState[0] = true;
                            return false;
                        }
                    });




                    Object[] thingsToSend = new Object[5];
                    thingsToSend[0] = freqBar.getProgress();
                    thingsToSend[1] = atkBar.getProgress();
                    thingsToSend[2] = dcyBar.getProgress();
                    thingsToSend[3] = (float)sstBar.getProgress()/100;
                    thingsToSend[4] = rlsBar.getProgress();

                    OSCMessage adsrmsg = new OSCMessage("/adsr", Arrays.asList(thingsToSend));
                    OSCMessage togglemsg = new OSCMessage("/toggle", Arrays.asList(toggle));
                    try {
                        // Send the messages
                        oscPortOut.send(adsrmsg);
                        if(toggleState[0]){
                            oscPortOut.send(togglemsg);
                            toggleState[0] = false;
                        }



                    } catch (Exception e) {
                        // Error handling for some error
                    }
                } else return;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ipEt = (EditText) findViewById(R.id.ipEditText);
        portEt = (EditText) findViewById(R.id.portEditText);

        playButton = (Button) findViewById(R.id.playBtn);

        freqBar = (SeekBar) findViewById(R.id.freqBar);
        atkBar = (SeekBar) findViewById(R.id.attackBar);
        dcyBar = (SeekBar) findViewById(R.id.decayBar);
        sstBar = (SeekBar) findViewById(R.id.sustainBar);
        rlsBar = (SeekBar) findViewById(R.id.releaseBar);

        freqTxt = (TextView) findViewById(R.id.freqText);
        atkTxt = (TextView) findViewById(R.id.atkText);
        dcyTxt = (TextView) findViewById(R.id.dcyText);
        sstTxt = (TextView) findViewById(R.id.sstText);
        rlsTxt = (TextView) findViewById(R.id.rlsText);

        freqTxt.setText("Frequency: " + freqBar.getProgress() + " Hz");
        atkTxt.setText("Attack: " + atkBar.getProgress() + " ms");
        dcyTxt.setText("Decay: " + dcyBar.getProgress() + " ms");
        sstTxt.setText("Sustain: " + sstBar.getProgress() + "%");
        rlsTxt.setText("Release: " + rlsBar.getProgress() + " ms");

        freqBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int prog_val = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                prog_val = progress;
                freqTxt.setText("Frequency: " + progress + " Hz");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                freqTxt.setText("Frequency: " + prog_val + " Hz");
            }
        });

        atkBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int prog_val = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                prog_val = progress;
                atkTxt.setText("Attack: " + progress + " ms");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                atkTxt.setText("Attack: " + prog_val + " ms");
            }
        });

        dcyBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int prog_val = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                prog_val = progress;
                dcyTxt.setText("Decay: " + progress + " ms");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                dcyTxt.setText("Decay: " + prog_val + " ms");
            }
        });

        sstBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int prog_val = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                prog_val = progress;
                sstTxt.setText("Sustain: " + progress + "%");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sstTxt.setText("Sustain: " + prog_val + "%");
            }
        });

        rlsBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int prog_val = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                prog_val = progress;
                rlsTxt.setText("Release: " + progress + " ms");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                rlsTxt.setText("Release: " + prog_val + " ms");
            }
        });
    }

    public void startOsc(View v){
        myIP = ipEt.getText().toString();
        myPort = Integer.parseInt(portEt.getText().toString());
        oscThread.start();
    }
}
