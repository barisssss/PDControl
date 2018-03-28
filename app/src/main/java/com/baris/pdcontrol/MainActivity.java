package com.baris.pdcontrol;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;

import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.net.*;
import java.util.*;

import com.illposed.osc.*;
import com.ramotion.fluidslider.FluidSlider;

import org.honorato.multistatetogglebutton.MultiStateToggleButton;
import org.honorato.multistatetogglebutton.ToggleButton;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

import xdroid.toaster.Toaster;

import static xdroid.toaster.Toaster.toast;
import static xdroid.toaster.Toaster.toastLong;



public class MainActivity extends AppCompatActivity {

    private EditText ipEt, portEt;
    private SeekBar atkBar, dcyBar, sstBar, rlsBar;
    private TextView atkTxt, dcyTxt, sstTxt, rlsTxt;
    private FluidSlider freqFluid;
    private MultiStateToggleButton wavesBtn;


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
            final boolean[] wavesState = {false};
            final Object[] waves = new Object[1];
            waves[0] = 0;
            while(true) {
                if (oscPortOut != null){
                    final Object[] values = new Object[5];
                    final Object[] toggle = new Object[1];


                    freqFluid.setBeginTrackingListener(new Function0<Unit>() {
                        @Override
                        public Unit invoke() {
                            Log.d("D", "setBeginTrackingListener");
                            toggle[0] = 1;
                            toggleState[0] = true;
                            return Unit.INSTANCE;
                        }
                    });

                    freqFluid.setEndTrackingListener(new Function0<Unit>() {
                        @Override
                        public Unit invoke() {
                            Log.d("D", "setEndTrackingListener");
                            toggle[0] = 0;
                            toggleState[0] = true;
                            return Unit.INSTANCE;

                        }
                    });

                    wavesBtn.setOnValueChangedListener(new ToggleButton.OnValueChangedListener() {
                        @Override
                        public void onValueChanged(int position) {
                            waves[0] = position;
                            wavesState[0] = true;
                            Log.d("D", "Position: " + position);
                        }
                    });


                    values[0] = freqFluid.getPosition() * 2000;
                    values[1] = atkBar.getProgress();
                    values[2] = dcyBar.getProgress();
                    values[3] = (float)sstBar.getProgress()/100;
                    values[4] = rlsBar.getProgress();

                    OSCMessage adsrmsg = new OSCMessage("/adsr", Arrays.asList(values));
                    OSCMessage togglemsg = new OSCMessage("/toggle", Arrays.asList(toggle));
                    OSCMessage wavesmsg = new OSCMessage("/waves", Arrays.asList(waves));

                    try {
                        // Send the messages
                        oscPortOut.send(adsrmsg);

                        if(toggleState[0]){
                            oscPortOut.send(togglemsg);
                            toggleState[0] = false;
                        }
                        if(wavesState[0]){
                            oscPortOut.send(wavesmsg);
                            wavesState[0] = false;
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


        atkBar = (SeekBar) findViewById(R.id.attackBar);
        atkBar.setProgress(100);
        dcyBar = (SeekBar) findViewById(R.id.decayBar);
        dcyBar.setProgress(50);
        sstBar = (SeekBar) findViewById(R.id.sustainBar);
        sstBar.setProgress(40);
        rlsBar = (SeekBar) findViewById(R.id.releaseBar);
        rlsBar.setProgress(400);

        atkTxt = (TextView) findViewById(R.id.atkText);
        dcyTxt = (TextView) findViewById(R.id.dcyText);
        sstTxt = (TextView) findViewById(R.id.sstText);
        rlsTxt = (TextView) findViewById(R.id.rlsText);


        atkTxt.setText("Attack: " + atkBar.getProgress() + " ms");
        dcyTxt.setText("Decay: " + dcyBar.getProgress() + " ms");
        sstTxt.setText("Sustain: " + sstBar.getProgress() + "%");
        rlsTxt.setText("Release: " + rlsBar.getProgress() + " ms");

        freqFluid = (FluidSlider) findViewById(R.id.freqFluid);


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

        wavesBtn = (MultiStateToggleButton) findViewById(R.id.wavesToggle);
        wavesBtn.setValue(0);
    }

    public void startOsc(View v){
        myIP = ipEt.getText().toString();
        myPort = Integer.parseInt(portEt.getText().toString());
        oscThread.start();
    }
}
