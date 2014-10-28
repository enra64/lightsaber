package com.example.lightsaberbluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.EditText;  
import android.widget.Button;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {
	BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;
    
    Button startButton, changeDurationButton;
    SeekBar durationSeekbar;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        startButton=(Button)findViewById(R.id.startStopButton);
        changeDurationButton=(Button)findViewById(R.id.durButton);
        durationSeekbar=(SeekBar)findViewById(R.id.seekBarDuration);
    }
    
    
    public void changeDurationButtonClicked(View v){
    	if(mmDevice!=null){
    	try{
            sendData(String.valueOf(durationSeekbar.getProgress()));
        }
        catch (IOException ex) { }
    	}
    }
    
    public void startButtonClicked(View v){
    	if(mmDevice!=null){
    	try{
            sendData("1111");
        }
        catch (IOException ex) { }
    	}
    }
    
    @Override
    public void onStop(){
    	super.onStop();
    	if(mmDevice!=null){
	    	try{
	            closeBT();
	        }
	        catch (Exception ex) {
	        	Log.d("onstop", "exception occured");
	        }
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.menuaction_connect:
            	try{
                    findBT();
                }
                catch (IOException ex) { }
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    void findBT() throws IOException{
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        	sendToast("No bluetooth adapter available");
        
        
        if(!mBluetoothAdapter.isEnabled()){
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }
        
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
            	if(device.getAddress().equals("20:13:05:15:38:85")) 
                {
                    mmDevice = device;
                    openBT();
                    sendToast("Bluetooth-Gerät verbunden");
                    break;
                }
                else
                	sendToast("Falsches BT-Gerät verbunden");
            }
        }
        else
        	sendToast("Kein Bluetooth-Gerät verbunden");
    }
    
    void openBT() throws IOException{
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);        
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();
        
        beginListenForData();
        activateUI();
    }
    
    private void activateUI() {
		startButton.setEnabled(true);
		changeDurationButton.setEnabled(true);
		durationSeekbar.setEnabled(true);
	}

	void beginListenForData(){
		final String TAG="beginListenForData";
        final Handler handler = new Handler(); 
        final byte delimiter = 10; //This is the ASCII code for a newline character
        
        stopWorker = false;
        readBufferPosition = 0;  
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run(){                
               while(!Thread.currentThread().isInterrupted() && !stopWorker){
                    try{
                        int bytesAvailable = mmInputStream.available();                        
                        if(bytesAvailable > 0){
                        	Log.d(TAG, "bytes Available: "+String.valueOf(bytesAvailable));
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++){
                            	byte b = packetBytes[i];
                                Log.d("received byte: ", String.valueOf(b));
                                if(b == delimiter){
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;
                                    
                                    handler.post(new Runnable()
                                    {
                                        public void run(){
                                            sendToast(data);
                                            Log.d("runnable receive", data);
                                        }
                                    });
                                }
                                else{
                                    readBuffer[readBufferPosition] = b;
                                }
                            }
                        }
                        //else
                        	//Log.d(TAG, "no bytes Available");
                    } 
                    catch (IOException ex) 
                    {
                        stopWorker = true;
                    }
               }
            }
        });
        workerThread.start();
    }
    
    void sendData(String msg) throws IOException{
    	byte[] test=new byte[4];
    	test[0]=(byte) 0;test[1]=(byte) 81;test[2]=(byte) 82;test[3]=(byte) 10;
        if(mmOutputStream!=null)
        	mmOutputStream.write(msg.getBytes());
        Log.d("send data", "data sent");
    }
    
    void closeBT() throws IOException{
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
        sendToast("Bluetooth Closed");
    }
    
	private void sendToast(String message){
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}
}

