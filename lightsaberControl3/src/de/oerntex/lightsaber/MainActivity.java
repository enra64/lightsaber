package de.oerntex.lightsaber;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

//colorpicker
import com.buzzingandroid.ui.HSVColorPickerDialog;
import com.buzzingandroid.ui.HSVColorPickerDialog.OnColorSelectedListener;

//amarino
import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;

//alot of android sh*t
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MainActivity extends Activity {
	public Boolean pinzetteAn=false;
	public int layoutPositionCounter=0, colorToWork, seitenAnzahl=0, currentPage=1, wahlInt, helligkeitCurrentPagePercent, durationCurrentPagePercent, anzahlSaves, currentColor, AInit=0;
	public int[] ledRedArray=new int[20], ledGreenArray=new int[20], ledBlueArray=new int[20], timeArray;
	public String tag, tagForButtons, newSaveName, wahlPublic, save="save";
	public ArrayList<int[]> farbArrays=new ArrayList<int[]>();
	
	//ui
	SeekBar durationSeek;
	public static Context appContext;
	public View publicView;
	public Button buttonFarbe, buttonPinsel, buttonPinzette;
	public Fragment StuffFragment, SettingsFragment;
	FragmentManager fragmentManager = getFragmentManager();
	
	//bluetooth vars
	boolean lightsaberRunning=false;
	private ArduinoReceiver arduinoReceiver = new ArduinoReceiver();
	
	//bluetooth statics
	private static final String DEVICE_ADDRESS = "20:13:05:15:38:85";
	
	//usb statics
	private static final String TAG = "MissileLauncherActivity";
    private static final byte LIGHTSABER_CREATEFILE=0x6A;
    private static final byte LIGHTSABER_APPENDLINE=0x6B;
    private static final byte LIGHTSABER_STARTLEDS=0x6C;
    private static final int USBREQUEST_OUT=0x09;
    private static final int USBREQUEST_IN=0x01;
    private static final int USBREQUESTTYPE_OUT=0x24;
    private static final int USBREQUESTTYPE_IN=0xA0;
    /* usb requesttype info
	 * currently: 0 host-dev, 01 class (v-usb), 
	 * 0000/20 device	3
	 * 0001/21 interface -1
	 * 0010/22 endpoint	-1
	 * 0011/23 other	3
	 * 0100/21			3
	 * 
	 * */
    private static final int USBVALUE=0x00;
    PendingIntent mPermissionIntent;
    
    
	//usb vars
	private char[] nameConversion=new char[8];
    private byte[] payload=new byte[32];
	private boolean fail=false;
    private UsbManager mUsbManager;
    private UsbDevice mDevice;
    private UsbDeviceConnection mConnection;
    private static final String ACTION_USB_PERMISSION ="com.android.example.USB_PERMISSION";
    
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		//int[] ledRedArray=new int[19];
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		//ActionBar gets initiated
		ActionBar actionbar = getActionBar();
		actionbar.setDisplayShowTitleEnabled(false);
		actionbar.setDisplayShowHomeEnabled(false);

		//Tell the ActionBar we want to use Tabs.
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		//initiating both tabs and set text to it.
		ActionBar.Tab StuffTab = actionbar.newTab().setText("Stuff");
		ActionBar.Tab SettingsTab = actionbar.newTab().setText("Settings");

		//create the two fragments we want to use for display content
		StuffFragment = new AFragment();
		SettingsFragment = new BFragment();

		//set the Tab listener. Now we can listen for clicks.
		StuffTab.setTabListener(new MyTabsListener(StuffFragment));
		SettingsTab.setTabListener(new MyTabsListener(SettingsFragment));

		//add the two tabs to the actionbar
		actionbar.addTab(StuffTab);
		actionbar.addTab(SettingsTab);
		
		//usb stuff
		mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		registerReceiver(mUsbReceiver, filter);
	}

	private void setLinearLayoutTags()
	{
		String logtag="setLinearLayoutTags";
		Log.w(logtag, "called");
		for(int i=0;i<layoutPositionCounter;i++)
		{
			String tag="page"+i;
			//call fragmentmanager to save every lineN
			FragmentManager fragmentManager = getFragmentManager();
			View viewToTag= fragmentManager.findFragmentByTag(tag).getView();
			//get fields
			LinearLayout setTag=(LinearLayout) viewToTag.findViewById(R.id.layoutLed1);
			setTag.setTag(i);
		}
	}
	
	public void buttonPlus(View v)
	{
		String logtag="buttonPlus";
		//saveCurrentPage(currentPage);
		int[] workarray = new int[10];
		for(int i=0;i<10;i++){
			workarray[i]=-16777216;
		}
		farbArrays.add(seitenAnzahl, workarray);//.add(workarray);
		String buttonPlusPageTag="page"+layoutPositionCounter;
		Log.w(logtag, "NEUE "+buttonPlusPageTag);
		//fragment transactions
		Fragment newFragment = new oneline();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.add(R.id.linecontainer, newFragment, buttonPlusPageTag).commit();
		seitenAnzahl++;
		layoutPositionCounter++;
	}
	
	public void buttonMinus(View v)
	{
		Log.w("#shitty_logcat", "buttonMinus clicked");
		seitenAnzahl--;
		layoutPositionCounter--;
		if(layoutPositionCounter<1){return;}
		String tag="page"+layoutPositionCounter;
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		Fragment toRemove= fragmentManager.findFragmentByTag(tag);
		transaction.remove(toRemove).commit();
		if(layoutPositionCounter<1)	{
			Button btn = (Button)findViewById(R.id.buttonMinus);
			btn.setActivated(false);
		}
		Log.w("BUTTON_DELETE", ""+tag+" entfernt");

	}
	
	public void createListForViewSavePoints(View test)
	{
		List<String> saveListe = new ArrayList<String>();
		final SharedPreferences saveHandlerBert=getSharedPreferences("SAVES", MODE_PRIVATE);
		//Editor saveEditor= saveHandlerBert.edit();
		anzahlSaves=saveHandlerBert.getInt("anzahlSaves", 0);

		for (int i = 1; i <=anzahlSaves; i++)	{
			//string aus sharedprefs holen
			String saveName=saveHandlerBert.getString(i+"name", "fail");
			//liste mit saves befüllen
			saveListe.add(saveName);
		}
		saveListe.add("Neu");

		//liste init
		ListAdapter adapter = new ArrayAdapter<String>(test.getContext(), android.R.layout.simple_list_item_1, saveListe);

		final ListView lv = (ListView)test.findViewById(R.id.listViewSavePoints);
		lv.setAdapter(adapter);

		lv.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				//create dialog to find out whether to save or to load
				if (arg2!=anzahlSaves){
					arg2++;
					buildSaveDialog(saveHandlerBert.getString(arg2+"name", "fail"), arg2);
				}
				else{
					buildSaveNameRequestDialog();
				}
			}
		});
	}
	
	public void copy(File src, File dst) throws IOException {
		FileInputStream inStream = new FileInputStream(src);
		FileOutputStream outStream = new FileOutputStream(dst);
		FileChannel inChannel = inStream.getChannel();
		FileChannel outChannel = outStream.getChannel();
		inChannel.transferTo(0, inChannel.size(), outChannel);
		inStream.close();
		outStream.close();
	}
	
	public void buildSaveNameRequestDialog()
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Neue Datei; max. Länge 8 Zeichen.");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		InputFilter[] FilterArray = new InputFilter[1];
		FilterArray[0] = new InputFilter.LengthFilter(8);
		input.setFilters(FilterArray);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				newSaveName= input.getText().toString();
				SharedPreferences saveHandlerBert=getSharedPreferences("SAVES", MODE_PRIVATE);
				Editor saveEditor= saveHandlerBert.edit();
				anzahlSaves=saveHandlerBert.getInt("anzahlSaves", 0);
				anzahlSaves++;
				saveEditor.putInt("anzahlSaves", anzahlSaves);
				saveEditor.putString(anzahlSaves+"name", newSaveName);
				saveEditor.commit();
			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled.
			}
		});
		alert.show();
	}
	
	private Boolean loadFile(String location){//alternative implementation: erstmal nur alles nach farbArrays, dann beim wechsel ins afragment
		InputStream openThis;
		
		try {
			openThis = new FileInputStream(location);
			BufferedReader bReader=new BufferedReader(new InputStreamReader(openThis));
			String line="";
			Boolean test=true;
			Boolean success=false;
			while(test==true){
			line=bReader.readLine();
			if(line!=null){
				if(line.length()!=0)
					success=loadLineToFarbArray(line);
				if(success==false)
					makeToastSettings("Operation fehlgeschlagen");
			}
			else
				test=false;
			}
			bReader.close();
			return true;
		}
		
		catch (FileNotFoundException e) {
			makeToastSettings("Operation fehlgeschlagen - Datei nicht gefunden");
			e.printStackTrace();
			return false;
		} 
		catch (IOException e) {
			makeToastSettings("EOF (?)");
			e.printStackTrace();
			return false;
		}
	}
	
	private Boolean loadLineToFarbArray(String line){
		if (line=="") {return false;}
		int[] workarray = new int[10];
		String[] splitted=TextUtils.split(line, ",");
		splitted[29]=splitted[29].substring(0, 1);
		int counter=0;
		for(int i=0;i<10;i++){
			int r=Integer.parseInt(splitted[counter]);
			counter++;
			int g=Integer.parseInt(splitted[counter]);
			counter++;
			int b=Integer.parseInt(splitted[counter]);
			counter++;
			workarray[i]=Color.rgb(r, g, b);
			if(workarray[i]==-1)
				workarray[i]=currentColor;
		}
		farbArrays.add(workarray);
		return true;
	}
	
	public void buildSaveDialog(String wahl, int wahlNummer)
	{
		wahlInt=wahlNummer;
		wahlPublic=wahl;
		Log.w("BUTTON", "Zeige dialog für save:");
		ArrayAdapter<String> adapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		adapter.clear();
		adapter.add("Laden");
		adapter.add("Speichern");
		adapter.add("Via USB senden");
		adapter.add("Löschen");

		AlertDialog.Builder builder1=new AlertDialog.Builder(this);
		builder1.setTitle(wahl+"-Aktion?");
		builder1.setAdapter(adapter, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
				case 0:loadNEWFromSD(wahlPublic);break;//load
				case 1:saveToSDUSBSP();break;		//save
				case 2:sendViaUSB(wahlPublic);break;
				case 3:{							//delete
					//save=wahlPublic;
					File file = new File("/data/data/de.oerntex.lightsaber/shared_prefs/"+wahlPublic+".xml");
					file.delete();
					//declare spref handlers
					SharedPreferences saveHandlerMain=getSharedPreferences("SAVES", MODE_PRIVATE);
					Editor saveEditor= saveHandlerMain.edit();
					//do the work
					saveEditor.remove(wahlInt+"save");
					anzahlSaves=saveHandlerMain.getInt("anzahlSaves", 55);
					anzahlSaves--;
					saveEditor.putInt("anzahlSaves", anzahlSaves);
					saveEditor.commit();
				}break;
				}
				Log.w("#shitty_logcat", "buildSaveDialog: "+which+" has been chosen");
			}
		});
		builder1.show();
		Log.w("#shitty_logcat", wahlPublic+" wurde ausgewählt");
	}
	
	public void sendViaUSB(String saveName){
		if(saveName.length()!=8){
			makeToastSettings("Name muss derzeit 8 chars lang sein");
			return;
		}
		//array auf jeden fall voll machen
		for(byte i=0; i<nameConversion.length;i++){
			nameConversion[i]='x';
		}
		saveName.getChars(0, 8, nameConversion, 0);
		synchronized (this) {
        	if (mConnection != null) {
        		payload[0]=LIGHTSABER_CREATEFILE;
        		//payload 1-12: name; 9:punkt
        		for(byte i=1; i<9;i++)
        			payload[i]=(byte) nameConversion[i-1];
        		//9: punkt(im avr); 10-12: endung
        		payload[10]='c';payload[11]='s';payload[12]='v';
        		if(farbArrays.size()>255)
        			payload[13]=0;
        		else
        			payload[13]=(byte) farbArrays.size();
        		
        		//send createfile
             	mConnection.controlTransfer(USBREQUESTTYPE_OUT, USBREQUEST_OUT, USBVALUE, 0x01, payload, payload.length, 0);
            	
            	
        		payload[0]=LIGHTSABER_APPENDLINE;
        		//send
        		for(int i=0; i<farbArrays.size(); i++){
        			int[] workerArray=farbArrays.get(i);
        			int k=0;
        			for(int j=0;j<10;j++){//android farbe: 1 int-> 3 bytes (rgb)
        				k++;
        				payload[k]=(byte) Color.red(workerArray[j]);
        				k++;
        				payload[k]=(byte) Color.green(workerArray[j]);
        				k++;
        				payload[k]=(byte) Color.blue(workerArray[j]);
        			}
        			mConnection.controlTransfer(USBREQUESTTYPE_OUT, USBREQUEST_OUT, USBVALUE, 0x01, payload, payload.length, 0);
                }
        		payload[0]=LIGHTSABER_STARTLEDS;
        		mConnection.controlTransfer(USBREQUESTTYPE_OUT, USBREQUEST_OUT, USBVALUE, 0x01, payload, payload.length, 0);
        		
            	fail=false;
        	}
        	else{
        		fail=true;
        	}
        }
	}
	
	public void connectLightsaber(View v){
		HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		
		while (deviceIterator.hasNext()) {
			// sets usb device
			mDevice = deviceIterator.next();

			// wait for the right device, connect it
			if (mDevice.getVendorId() == 5824 && mDevice.getProductId() == 1503) {
				mUsbManager.requestPermission(mDevice, mPermissionIntent);
			}
		}
		
		
		if(mDevice==null){
			makeToastSettings("Lightsaber nicht gefunden");
			Log.e(TAG, "no device");
			return;
		}
		
		//return if wrong device
		if (mDevice.getVendorId() != 5824) {
			makeToastSettings("Lightsaber nicht gefunden");
			Log.e(TAG, "not lightsaber");
			return;
		}

		Log.d(TAG, "setDevice " + mDevice);
		if (mDevice.getInterfaceCount() != 1) {
			Log.e(TAG, "could not find interface");
			return;
		}
		UsbInterface intf = mDevice.getInterface(0);
		// device should have one endpoint
		if (intf.getEndpointCount() != 1) {
			Log.e(TAG, "could not find endpoint");
			return;
		}
		// endpoint should be of type interrupt
		UsbEndpoint ep = intf.getEndpoint(0);
		if (ep.getType() != UsbConstants.USB_ENDPOINT_XFER_INT) {
			Log.e(TAG, "endpoint is not interrupt type");
			return;
		}

		if (mDevice != null) {
			UsbDeviceConnection connection = mUsbManager.openDevice(mDevice);
			if (connection != null && connection.claimInterface(intf, true)) {
				Log.d(TAG, "open SUCCESS");
				makeToastSettings("Lightsaber geöffnet");
				mConnection = connection;
			} 
			else {
				Log.d(TAG, "open FAIL");
				makeToastSettings("Permission denied.");
				mConnection = null;
			}
		}
	
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		registerReceiver(arduinoReceiver, new IntentFilter(AmarinoIntent.ACTION_RECEIVED));
	}
	
	public void buttonAddString(View v)
	{
		EditText stringInput=(EditText)this.findViewById(R.id.stringEdit);
		String alles=stringInput.getText().toString();
		FragmentTransaction transaction = fragmentManager.beginTransaction();

		for(int buchstabenIndex=0; buchstabenIndex<alles.length();buchstabenIndex++){
			Character charAtIndex=alles.charAt(buchstabenIndex);
			String buchstabeAtIndex=Character.toString(charAtIndex);
			Boolean check=false;
			int addThis=5;
			if(buchstabeAtIndex.trim().length()==0)
				buchstabeAtIndex="leerzeichen";
			
			
			if(Character.isUpperCase(charAtIndex)){
				check=loadFile("/storage/emulated/legacy/lightsaber/res/upper/"+buchstabeAtIndex+".xml");
				
				//noch ne line dazwischen um die buchstaben auseinanderhalten zu können
				int[] workarray = new int[10];
				for(int i=0;i<10;i++){workarray[i]=-16777216;}
				farbArrays.add(workarray);
				addThis++;
				//seitenanzahl erhöhen, da line eingefügt.
				
			}
			else{
				check=loadFile("/storage/emulated/legacy/lightsaber/res/lower/"+buchstabeAtIndex+".xml");
			}
			if(check==false)
				return;
			
			
			int test=seitenAnzahl+addThis;
			String buttonPlusPageTag="";
			
			for(int i=seitenAnzahl;i<test;i++){
				buttonPlusPageTag="page"+i;
				Fragment newFragment = new oneline();
				transaction.add(R.id.linecontainer, newFragment, buttonPlusPageTag);
			}
			seitenAnzahl=layoutPositionCounter=test;
		}
		transaction.commit();
	}
	
	public void loadNEWFromSD(String wahl) {
		farbArrays.clear();
		FragmentTransaction transact=fragmentManager.beginTransaction();
		for(int i=0;i<seitenAnzahl;i++){
			Fragment toRemove=fragmentManager.findFragmentByTag("page"+i);
			transact.remove(toRemove);
		}
		seitenAnzahl=layoutPositionCounter=0;
		transact.commit();
		fragmentManager.executePendingTransactions();
		loadFile("/storage/emulated/legacy/lightsaber/"+wahl+".csv");
		Log.w("loadFromSD", "farbarray cleared, resuming to loadFile");
	}
	
	public String saveToSDUSBSP(){
		//create data string to save
		String data="";
		for(int seite=0;seite<seitenAnzahl;seite++)
		{
			int[] workArray=farbArrays.get(seite);
			for(int button=0;button<10;button++){
				int color=workArray[button];
				
				//sorge dafür das der letzte wert der zeil kein komma mehr hat
				if(button==9)
					data=data+Color.red(color)+","+Color.green(color)+","+Color.blue(color)+";";
				
				else
					data=data+Color.red(color)+","+Color.green(color)+","+Color.blue(color)+",";
			}
			
			//sorgt dafür das hinter der letzten zeile kein umbruch kommt
			if(seite!=seitenAnzahl-1)
				data=data+"\n";
			Log.w("savepage", data);
		}

		//try SD and USB drive writes
		try {
			File myFile = new File("/sdcard/lightsaber/"+wahlPublic+".csv");
			myFile.createNewFile();
			FileOutputStream fOut = new FileOutputStream(myFile);
			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
			myOutWriter.write(data);
			myOutWriter.close();
			fOut.close();
			//check whether there already is a file named data.csv
			myFile = new File("/storage/usbdisk/data.csv");
			if(myFile.exists())
			{
				String timestamp=String.valueOf(System.currentTimeMillis());
				File targetFile=new File("/storage/usbdisk/", timestamp+".csv");
				myFile.renameTo(targetFile);
			}
			//save to usb drive and hope it is connected, otherwise just ignore it^^
			File newData = new File("/storage/usbdisk/data.csv");
			newData.createNewFile();
			fOut = new FileOutputStream(newData);
			myOutWriter = new OutputStreamWriter(fOut);
			myOutWriter.write(data);
			myOutWriter.close();
			fOut.close();
			Log.w("filecreator", "success");
		} catch (Exception e) {
			Log.w("savepage", "finde sdkarte nicht");
			makeToastSettings("Keine externe SD-Karte vorhanden oder nicht gefunden- Trotzdem auf Gerät gespeichert.");
		}
		//save to shared prefs
		SharedPreferences saveHandler=getSharedPreferences(wahlPublic, MODE_PRIVATE);
		Editor saveEditor= saveHandler.edit();
		saveEditor.putString("data", data);
		saveEditor.commit();
		return data;
	}
	
	public void buttonLED(View v){
		setLinearLayoutTags();//this is good practice, trust me
		//get some variables, including number of button clicked and number of fragment containing said button
		String buttonTag=(String) v.getTag();
		Button buttonFarbe=(Button) v.findViewWithTag(buttonTag);
		LinearLayout bert=(LinearLayout) buttonFarbe.getParent();
		int fragmentNumber=(Integer) bert.getTag();
		
		int[] test=farbArrays.get(fragmentNumber);
		int buttonNumber=Integer.parseInt(buttonTag);

		if(!pinzetteAn){
			Log.w("#shitty_logcat", "buttonFarbe "+buttonTag+" clicked");
			test[buttonNumber]=currentColor;
			buttonFarbe.setBackgroundColor(currentColor);
			farbArrays.set(fragmentNumber, test);
			Log.w("buttonFarbe", "in fragment nummer "+fragmentNumber+"bei button "+buttonNumber+" das array in der arraylist erneuert");
		}

		else{
			Log.w("#shitty_logcat", "buttonFarbe "+buttonTag+" clicked: setting currentColor");
			currentColor=test[buttonNumber];
			Log.w("buttonFarbe", "currentColor via Pinzette gesetzt");
			pinzetteAn=false;

			View buttonView= fragmentManager.findFragmentById(R.id.fragment_container).getView();
			buttonPinsel=(Button) buttonView.findViewById(R.id.pinselButton);
			buttonPinzette=(Button) buttonView.findViewById(R.id.buttonPinzette);
			buttonPinsel.getBackground().setColorFilter(currentColor,PorterDuff.Mode.MULTIPLY);
			buttonPinzette.getBackground().setColorFilter(currentColor,PorterDuff.Mode.MULTIPLY);

		}
	}
	
	public void buttonFarbkreis(View v){
		pinzetteAn=false;
		Log.w("#newlayout", "buttonFarbe clicked");
		View view=fragmentManager.findFragmentById(R.id.fragment_container).getView();
		buttonPinsel=(Button) view.findViewById(R.id.pinselButton);
		buttonPinzette=(Button) view.findViewById(R.id.buttonPinzette);
		//buttonFarbe=(Button) v.findViewWithTag(tagForButtons);
		HSVColorPickerDialog colorPicker= new HSVColorPickerDialog(this, 0xFF4488CC, new OnColorSelectedListener(){
			@Override
			public void colorSelected(Integer color) {
				Log.w("#shitty_logcat", "neue Farbe:"+color);
				currentColor=color;
				//set new button tint
				buttonPinsel.getBackground().setColorFilter(color,PorterDuff.Mode.MULTIPLY);
				buttonPinzette.getBackground().setColorFilter(color,PorterDuff.Mode.MULTIPLY);
			}});

		colorPicker.setTitle("Wähle eine Farbe!");
		colorPicker.show();
	}
	
	public void buttonPinzette(View v){
		Log.w("buttonPinzette", "Farbpinzette clicked");
		pinzetteAn=true;
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
	}
	
	public void makeToastSettings(String message){
		Context context;
		if(SettingsFragment.isVisible()){
			context = SettingsFragment.getView().getContext();
		}
		else{
			context=StuffFragment.getView().getContext();
		}
		CharSequence text = message;
		Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
		toast.show();
	}
	
	public void AFragmentinit(View v)
	{
		String logtag="AFRAGInit";
		int sollGroesse=farbArrays.size();
		Log.w(logtag, sollGroesse+": sollgröße");
		
		for(int i=seitenAnzahl;i<sollGroesse;i++){
			Log.w(logtag, "neue spalte bauen: "+i);
			String buttonPlusPageTag="page"+i;
			
			//fragment transactions
			Fragment newFragment = new oneline();
			FragmentTransaction transaction = fragmentManager.beginTransaction();
			transaction.add(R.id.linecontainer, newFragment, buttonPlusPageTag).commit();
		}
		seitenAnzahl=sollGroesse;
		layoutPositionCounter=sollGroesse;
		AInit=0;
	}
	
	public void BFragmentinit(final View v){
		durationSeek=(SeekBar)v.findViewById(R.id.seekBarDuration);
		final TextView durView=(TextView) v.findViewById(R.id.durationView);
		durationSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			@Override       
		    public void onStopTrackingTouch(SeekBar seekBar) {}       

		    @Override       
		    public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
				durView.setText(String.valueOf(progress)+" ms");
			} 
		});
	}
	
	public void bluetoothChangeDuration(View v){
		int duration=durationSeek.getProgress();
		Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'b', duration);
	}
	
	public void setMyButtons(View root) {
		int[] workarray;
		workarray=farbArrays.get(AInit);
		AInit++;
		//Log.w(logtag, "executing");

		for(int i=0;i<10;i++){
			Button setColor=(Button)root.findViewWithTag(String.valueOf(i));
			if(workarray[i]!=-16777216)
				setColor.setBackgroundColor(workarray[i]);
		}
	}
	
	public void connectAmarino(View v){
		Amarino.connect(v.getContext(), DEVICE_ADDRESS);
	}
	
	public void bluetoothChooseSave(View v){
		Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'c', "datadata.csv");//beware: auf arduino -> char array+\0
	}
	
	public void bluetoothStart(View v){
		Button startStop=(Button)v.findViewById(R.id.startStopButton);
		if(lightsaberRunning=false){
			startStop.setText("Stop");
			lightsaberRunning=true;
			Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'a', 1);
		}
		else{
			startStop.setText("Start");
			lightsaberRunning=false;
			Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'a', 0);
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		Amarino.disconnect(this, DEVICE_ADDRESS);
		unregisterReceiver(arduinoReceiver);
	}
	
	
	public class ArduinoReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String data = null;
			
			// the type of data which is added to the intent
			final int dataType = intent.getIntExtra(AmarinoIntent.EXTRA_DATA_TYPE, -1);
			
			// we only expect String data though, but it is better to check if really string was sent
			// later Amarino will support differnt data types, so far data comes always as string and
			// you have to parse the data to the type you have sent from Arduino, like it is shown below
			if (dataType == AmarinoIntent.STRING_EXTRA){
				data = intent.getStringExtra(AmarinoIntent.EXTRA_DATA);
				
				if (data != null){
					try {
						// since we know that our string value is an int number we can parse it to an integer
						final int sensorReading = Integer.parseInt(data);
					} 
					catch (NumberFormatException e) { /* oh data was not an integer */ }
				}
			}
		}
	}
	
	
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        if (ACTION_USB_PERMISSION.equals(action)) {
	            synchronized (this) {
	                UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

	                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
	                    if(device != null){
	                      //call method to set up device communication
	                   }
	                } 
	                else {
	                    Log.d(TAG, "permission denied for device " + device);
	                }
	            }
	        }
	    }
	};
	
}


class MyTabsListener implements ActionBar.TabListener {
	public Fragment fragment;
	public MyTabsListener(Fragment fragment) {
		this.fragment = fragment;
	}
	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {}
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		ft.replace(R.id.fragment_container, fragment);
		Log.w("#shitty_logcat", "Fragment gewechselt: "+fragment.toString());
	}
	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		ft.remove(fragment);
	}
}

