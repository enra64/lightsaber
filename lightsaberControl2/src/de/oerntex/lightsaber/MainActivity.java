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
import java.util.List;//colorpicker

import com.buzzingandroid.ui.HSVColorPickerDialog;
import com.buzzingandroid.ui.HSVColorPickerDialog.OnColorSelectedListener;







//alot of android sh*t
import android.os.Bundle;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.PorterDuff;
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
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity {
	public static Context appContext;
	public View publicView;
	public Button buttonFarbe, buttonPinsel, buttonPinzette;
	public Fragment StuffFragment, SettingsFragment;
	FragmentManager fragmentManager = getFragmentManager();
	public Boolean pinzetteAn=false;
	public int layoutPositionCounter=0, colorToWork, seitenAnzahl=0, currentPage=1, wahlInt, helligkeitCurrentPagePercent, durationCurrentPagePercent, anzahlSaves;
	public int[] ledRedArray=new int[20], ledGreenArray=new int[20], ledBlueArray=new int[20], timeArray;
	public String tag, tagForButtons, newSaveName, wahlPublic, save="save";
	public int currentColor;
	public ArrayList<int[]> farbArrays=new ArrayList<int[]>();

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
	}

	private void setLinearLayoutTags()
	{
		Log.w("#shitty_logcat", "setFarbButtonTag called line:89");
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

	public void builldSaveDialog()//obsolet?
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Dateinamen festlegen");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				newSaveName= input.getText().toString();

				//saves ... something i guess
				String data="";
				for(int seite=0;seite<seitenAnzahl;seite++)
				{
					String pagetag="page"+seite;
					Log.w("saveapge", pagetag);
					int[] workArray=farbArrays.get(seite);
					for(int button=0;button<10;button++)
					{
						/*get each fragment by tag
						 * then get the buttons out of it
						 * eg get their color int
						 * */
						int color=workArray[button];
						data=data+Color.red(color)+";"+Color.green(color)+";"+Color.blue(color)+";";
					}
					Log.w("saveapge", "read "+pagetag);
					if(seite!=seitenAnzahl-1)
					{
						data=data+"\n\r";
					}
					//Log.w("saveapge", data);
				}
				try {
					File myFile = new File("/sdcard/lightsaber/"+newSaveName+".csv");
					myFile.createNewFile();
					FileOutputStream fOut = new FileOutputStream(myFile);
					OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
					myOutWriter.write(data);
					myOutWriter.close();
					fOut.close();
					Log.w("saveapge", data);
				} catch (Exception e) {
					Log.w("saveapgef", data);
				}
			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled.
			}
		});
		alert.show();
	}

	public void buttonPlus(View v)
	{
		Log.w("BUTTONPLUS", "buttonPlus clicked");

		//saveCurrentPage(currentPage);
		int[] workarray = new int[10];
		for(int i=0;i<10;i++){
			workarray[i]=0;
		}
		farbArrays.add(seitenAnzahl, workarray);//.add(workarray);
		String buttonPlusPageTag="page"+layoutPositionCounter;
		Log.w("buttonPlus", "NEUE "+buttonPlusPageTag);
		//fragment transactions
		Fragment newFragment = new oneline();
		FragmentManager fragmentManager = getFragmentManager();
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

		alert.setTitle("Neuer Speicherpunkt");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
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

	public void loadFile(String location){//alternative implementation: erstmal nur alles nach farbArrays, dann beim wechsel ins afragment
		//getActionBar().setSelectedNavigationItem(0);
		InputStream openThis;
		Log.w("loadFile", location);
		
		try {
			openThis = new FileInputStream(location);
			BufferedReader bReader=new BufferedReader(new InputStreamReader(openThis));
			String line="";
			
			line=bReader.readLine();

			if(line!=null){
				Boolean success=loadLineToFarbArray(line);

				if(success==false){
					makeToastSettings("Operation fehlgeschlagen");
				}
			}
			
			bReader.close();
		} 
		
		catch (FileNotFoundException e) {
			makeToastSettings("Operation fehlgeschlagen - Datei nicht gefunden");
			e.printStackTrace();
		} 
		catch (IOException e) {
			makeToastSettings("EOF (?)");
			e.printStackTrace();
		}

	}
	
	private Boolean loadLineToFarbArray(String line)
	{
		Log.w("loadLineToFarbArray", line);
		if (line=="") return false;
		
		int[] workarray = new int[10];
		String[] splitted=TextUtils.split(line, ";");
		
		int counter=0;
		for(int i=0;i<10;i++)
		{
			int r=Integer.parseInt(splitted[counter]);
			counter++;
			int g=Integer.parseInt(splitted[counter]);
			counter++;
			int b=Integer.parseInt(splitted[counter]);
			counter++;
			workarray[i]=Color.rgb(r, g, b);
			Log.w("loadLineToFarbArray", ""+workarray[i]);
		}
		farbArrays.add(seitenAnzahl, workarray);
		
		return true;
	}

	public Boolean loadLine(String line)
	{
		Log.w("loadLine", line);
		
		if(line!="")
		{
			//saveCurrentPage(currentPage);
			int[] workarray = new int[10];
			String[] splitted=TextUtils.split(line, ";");
			int counter=0;
			for(int i=0;i<10;i++)
			{
				int r=Integer.parseInt(splitted[counter]);
				counter++;
				int g=Integer.parseInt(splitted[counter]);
				counter++;
				int b=Integer.parseInt(splitted[counter]);
				counter++;
				workarray[i]=Color.rgb(r, g, b);
				Log.w("loadLine", ""+workarray[i]);
			}
			farbArrays.add(seitenAnzahl, workarray);
			String buttonPlusPageTag="page"+layoutPositionCounter;
			Log.w("loadline", "NEU: "+buttonPlusPageTag);
			
			//fragment transactions
			Fragment newFragment = new oneline();
			FragmentTransaction transaction = fragmentManager.beginTransaction();
			transaction.add(R.id.linecontainer, newFragment, buttonPlusPageTag).commit();
			
			Fragment setButtonColors=fragmentManager.findFragmentByTag("page"+seitenAnzahl);
			if(setButtonColors!=null){
				Log.w("loadLine", "setButtonColor is not null");
				for(int i=0;i<10;i++)
				{
					Button setColor=(Button)newFragment.getView().findViewWithTag(String.valueOf(i));
					setColor.setBackgroundColor(workarray[i]);
				}
			}
			else{
				Log.w("loadLine", "fragment has not yet been created");
			}
			layoutPositionCounter++;
			seitenAnzahl++;
			return true;
		}
		else
		{
			return false;
		}
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
		adapter.add("Löschen");

		AlertDialog.Builder builder1=new AlertDialog.Builder(this);
		builder1.setTitle(wahl+"-Aktion?");
		builder1.setAdapter(adapter, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
				case 0:loadFromSD(wahlPublic);break;//load
				case 1:saveToSDUSBSP();break;		//save
				case 2:{							//delete
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

	public void loadFromSD(String wahl) {
		// TODO Auto-generated method stub
		loadFile("/storage/emulated/legacy/lightsaber/"+wahl+".csv");
	}

	public String saveToSDUSBSP(){
		//create data string to save
		String data="";
		for(int seite=0;seite<seitenAnzahl;seite++)
		{
			int[] workArray=farbArrays.get(seite);
			for(int button=0;button<10;button++)
			{
				/*get each fragment by tag
				 * then get the buttons out of it
				 * eg get their color int
				 * */
				int color=workArray[button];
				data=data+Color.red(color)+";"+Color.green(color)+";"+Color.blue(color)+";";
			}
			if(seite!=seitenAnzahl-1)
			{
				data=data+"\n\r";
			}
			Log.w("saveapge", data);
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
			makeToastSettings("Keine SD-Karte vorhanden oder nicht gefunden- Trotzdem auf Gerät gespeichert.");
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
		Context context = SettingsFragment.getView().getContext();
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
			buttonPlus(v);
			}
	}
	
	public void setMyButtons(View root) {
		String logtag="setMyButtons";
		
		View v=null;
		
		
		int[] workarray;
		workarray=farbArrays.get(seitenAnzahl-1);
		
		Log.w(logtag, "pray");

		for(int i=0;i<10;i++)
		{
			Log.w(logtag, "For button "+i+" "+workarray[i]+" was used");
			Button setColor=(Button)root.findViewWithTag(String.valueOf(i));
			if(workarray[i]!=0){
				setColor.setBackgroundColor(workarray[i]);
			}
		}

	}
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