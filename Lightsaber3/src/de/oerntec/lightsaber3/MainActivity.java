package de.oerntec.lightsaber3;

import java.util.List;

import de.oerntec.lightsaber3.DrawingView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MainActivity extends Activity {
	//wordclock draw area
	private DrawingView drawView;
	// ambilwarna
	int color, alpha, marginOffset, positionOffset;
	View viewHue, pointerView;
	SeekBar positionSeek;
	BluetoothComm bt;
	MyDB db;
	private float[] colorConverter={0 ,1 ,1};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//db init
		db=new MyDB(this);
		
		//bluetooth init
		bt=new BluetoothComm(this, "20:14:04:16:14:70");//16:14:70||16:25:33
		
		//position seekbar init
		positionSeek=(SeekBar)findViewById(R.id.positionSeekbar);
		
		//drawview init
		drawView = (DrawingView) findViewById(R.id.drawing);
		pointerView = findViewById(R.id.pointerView);
		
		// ambil
		viewHue = findViewById(R.id.ambilwarna_viewHue);
		color = 0xff0000ff | 0xff000000;
		//ambil:offset the imgview needs to use to avoid misplacing the view
		marginOffset =(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        
		positionSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			@Override       
			public void onStopTrackingTouch(SeekBar seekBar) {}       

			@Override       
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
				positionOffset=progress;
				drawView.moveAbs(progress);
			} 
		});
		
		viewHue.setOnTouchListener(new View.OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			@Override public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE
						|| event.getAction() == MotionEvent.ACTION_DOWN
						|| event.getAction() == MotionEvent.ACTION_UP) {
					float hue, x;
					int hueWidth=viewHue.getMeasuredWidth();
					x = event.getX();
					if(x<0.f) 
						x=0.f;
					if(x>hueWidth)
						x=hueWidth - 0.001f; // to avoid looping from end to start.
					hue= 360.f - 360.f / hueWidth * x;
					if(hue==360.f) 
						hue=0.f;
					drawView.setColor(hue);
					ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) pointerView.getLayoutParams();
			        int left=(int)x-marginOffset;
			        p.setMargins(left, 0, 0, 0);//trb
			        pointerView.requestLayout();
					return true;
				}
				return false;
			}
		});
	}

	public void addPixels(View v){
		positionSeek.setMax(drawView.addPixels());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void uploadDrawing(){
		if(!bt.btConnected){
			switch(bt.open()){
				case 1:
				case 2:
					Log.e("bt open", "fucked up with code 1 or 2");
					break;
				case 3:
					toast("lightsaber not paired (Key: 0808)\nPair with lightsaber in phone settings");
					Log.e("bt open", "code 3; lightsaber not paired");
					break;
				case 4:
					toast("lightsaber paired, but not within reach.\nStop and restart the app and the lightsaber", true);
					break;
				case 5:
					toast("The lightsaber has been disconnected!");
					break;
				case 6:
					toast("Enable Bluetooth to connect to lightsaber!");
					break;
			}
		}
		//fuck this, bt connect fail
		if(!bt.btConnected)
			return;
		List<float[]> listOfColorLists=drawView.getListOfColorLists();
		for(int i=0; i<listOfColorLists.size(); i++){
			float[] yColors=listOfColorLists.get(i);
			int ledCount=yColors.length;

			byte[] msg= new byte[ledCount*3+2];
			msg[0]=(byte) 170;
			int counter=1;
			for(int g=0; g<ledCount;g++){
				colorConverter[0]=yColors[g];
				int col=Color.HSVToColor(colorConverter);
	    		msg[counter]=(byte) Color.red(col);counter++;
	    		msg[counter]=(byte) Color.green(col);counter++;
	    		msg[counter]=(byte) Color.blue(col);counter++;
	    	}
			msg[counter]=(byte) i;
			if(!bt.sendData(msg))
				toast("Datenpaket "+i+" konnte nicht gesendet werden.");
		}
	}
	
	private void toast(String message){
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	private void toast(String message, boolean lengthLong){
		int l=Toast.LENGTH_SHORT;
		if(lengthLong)
			l=Toast.LENGTH_LONG;
		Toast.makeText(this, message, l).show();
	}
	
	private void initSave(){
		//create alertdialog layout
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		builder.setTitle("Where to save?")
		.setPositiveButton("Existing File", new OnClickListener() {
			@Override
		    public void onClick(DialogInterface dialog, int which) {
				saveDialog();
			}
		})
		.setNegativeButton("New File", new OnClickListener() {
			@Override
		    public void onClick(DialogInterface dialog, int which) {
				saveAsDialog();
			}
		});
		AlertDialog dialog=builder.create();
		dialog.show();
	}
	
	private void saveAsDialog(){
		LayoutInflater inflater = this.getLayoutInflater();
		AlertDialog.Builder b=new AlertDialog.Builder(this);
		final View dialogView=inflater.inflate(R.layout.dialog_saveas, null);
		b.setView(dialogView)
		.setTitle("Show name?")
		.setNegativeButton("Cancel", new OnClickListener() {
			@Override
		    public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.setPositiveButton("Save", new OnClickListener() {
			@Override
		    public void onClick(DialogInterface dialog, int which) {
				EditText saveRaw=(EditText) dialogView.findViewById(R.id.saveAsText);
				String saveName=saveRaw.getText().toString();
				db.storeRecord(drawView.currentDataForSave(), saveName);
			}
		});
		AlertDialog dialog=b.create();
		dialog.show();
	}
	
	private void saveDialog(){
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		final Cursor nameCursor=db.allRecordNames();
		builder.setCursor(nameCursor, new DialogInterface.OnClickListener() {	
			@Override
			public void onClick(DialogInterface dialog, int item) {
				nameCursor.moveToPosition(item);
				String saveName = nameCursor.getString(nameCursor.getColumnIndex("savename"));
				db.storeRecord(drawView.currentDataForSave(), saveName);
			}
		}, "savename");
		builder.setTitle("Overwrite save");
		builder.setNegativeButton("Cancel", new OnClickListener() {
			@Override
		    public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog d=builder.create();
		d.show();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch(item.getItemId()){
		case R.id.menu_update:
			uploadDrawing();
			return true;
		case R.id.menu_save:
			initSave();
			return true;
		case R.id.menu_load:
			Intent intent = new Intent(this, LoadActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
