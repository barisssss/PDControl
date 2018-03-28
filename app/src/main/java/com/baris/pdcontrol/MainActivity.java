package com.baris.pdcontrol;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;


import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.net.*;
import java.util.*;

import com.illposed.osc.*;
import com.ramotion.fluidslider.FluidSlider;

import kotlin.Unit;

import xdroid.toaster.Toaster;



public class MainActivity extends AppCompatActivity {

    private EditText ipEt, portEt;
    private SeekBar atkBar, dcyBar, sstBar, rlsBar;
    private TextView atkTxt, dcyTxt, sstTxt, rlsTxt;
    private FluidSlider freqFluid;
    private Switch wavesSwitch;


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

            // Variables for use in the loop
            final boolean[] toggleState = {false};
            final Object[] waves = new Object[2]; //for sending sine-saw switch state
            waves[0] = 1;
            waves[1] = 0;

            // The second part of the run() method loops infinitely
            while(true) {
                if (oscPortOut != null){
                    final Object[] values = new Object[5]; // for sending frequency and adsr
                    final Object[] toggle = new Object[1]; // for sending on/off signal

                    //Listeners for the frequency slider
                    freqFluid.setBeginTrackingListener(() -> {
                        Log.d("D", "setBeginTrackingListener");
                        toggle[0] = 1;
                        toggleState[0] = true;
                        return Unit.INSTANCE;
                    });

                    freqFluid.setEndTrackingListener(() -> {
                        Log.d("D", "setEndTrackingListener");
                        toggle[0] = 0;
                        toggleState[0] = true;
                        return Unit.INSTANCE;

                    });

                    //Listener for the wave switch
                    wavesSwitch.setOnClickListener(view -> {
                        if(wavesSwitch.isChecked()){
                            waves[0] = 0;
                            waves[1] = 1;
                        } else {
                            waves[0] = 1;
                            waves[1] = 0;
                        }
                    });

                    values[0] = freqFluid.getPosition() * 2000;
                    values[1] = atkBar.getProgress();
                    values[2] = dcyBar.getProgress();
                    values[3] = (float)sstBar.getProgress()/100;
                    values[4] = rlsBar.getProgress();

                    //Building the messages with the OSC addresses and the arrays
                    OSCMessage adsrmsg = new OSCMessage("/fadsr", Arrays.asList(values));
                    OSCMessage togglemsg = new OSCMessage("/toggle", Arrays.asList(toggle));
                    OSCMessage wavesmsg = new OSCMessage("/waves", Arrays.asList(waves));

                    try {
                        // Send the messages
                        oscPortOut.send(adsrmsg);
                        oscPortOut.send(wavesmsg);

                        //only send on/off signal if it has changed
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

        ipEt = findViewById(R.id.ipEditText);
        portEt = findViewById(R.id.portEditText);

        atkBar = findViewById(R.id.attackBar);
        atkBar.setProgress(100);
        dcyBar = findViewById(R.id.decayBar);
        dcyBar.setProgress(50);
        sstBar = findViewById(R.id.sustainBar);
        sstBar.setProgress(40);
        rlsBar = findViewById(R.id.releaseBar);
        rlsBar.setProgress(400);

        atkTxt = findViewById(R.id.atkText);
        dcyTxt = findViewById(R.id.dcyText);
        sstTxt = findViewById(R.id.sstText);
        rlsTxt = findViewById(R.id.rlsText);

        atkTxt.setText("Attack: " + atkBar.getProgress() + " ms");
        dcyTxt.setText("Decay: " + dcyBar.getProgress() + " ms");
        sstTxt.setText("Sustain: " + sstBar.getProgress() + "%");
        rlsTxt.setText("Release: " + rlsBar.getProgress() + " ms");

        freqFluid = findViewById(R.id.freqFluid);

        freqFluid.setStartText("0");
        freqFluid.setBubbleText("0");
        freqFluid.setPosition(0);
        freqFluid.setEndText("2000");
        freqFluid.setPositionListener(pos -> {
            final String value = String.valueOf( (int)((pos) * 2000) );
            freqFluid.setBubbleText(value);
            return Unit.INSTANCE;
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

        wavesSwitch = findViewById(R.id.wavesToggle);
    }

    public void startOsc(View v){
        myIP = ipEt.getText().toString();
        myPort = Integer.parseInt(portEt.getText().toString());
        oscThread.start();
    }
}
