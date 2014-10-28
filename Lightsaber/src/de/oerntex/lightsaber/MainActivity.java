package de.oerntex.lightsaber;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;//colorpicker
import com.buzzingandroid.ui.HSVColorPickerDialog;
import com.buzzingandroid.ui.HSVColorPickerDialog.OnColorSelectedListener;
//amarino connect
import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;
//alot of android sh*t
import android.os.Bundle;
import android.os.Handler;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity {
public static Context appContext;
public Boolean received=false;
String save="save";
public int[] ledRedArray=new int[21], ledGreenArray=new int[21], ledBlueArray=new int[21], timeArray;
public int[] ledAArray=new int[3], ledBArray=new int[3], ledCArray=new int[3], ledDArray=new int[3], ledEArray=new int[3], ledFArray=new int[3], ledGArray=new int[3], ledHArray=new int[3], ledIArray=new int[3], ledJArray=new int[3], ledKArray=new int[3], ledLArray=new int[3], ledMArray=new int[3], ledNArray=new int[3], ledOArray=new int[3], ledPArray=new int[3], ledQArray=new int[3], ledRArray=new int[3], ledSArray=new int[3], ledTArray=new int[3];
public int[] bigLedArray=new int[5], infoArray=new int[2];
public int layoutPositionCounter=0, colorToWork, seitenAnzahl=1, currentPage=1;
public int red, green, blue, anzahlSaves, wahlInt;
public int helligkeitCurrentPagePercent, durationCurrentPagePercent;
public Fragment StuffFragment, SettingsFragment;
public Button buttonFarbe;
BluetoothAdapter mBluetoothAdapter;
BluetoothSocket btSocket;
FragmentManager fragmentManager = getFragmentManager();
public String tag, tagForButtons, newSaveName, wahlPublic;
public View publicView;
private static final String DEVICE_ADDRESS = "20:13:05:15:38:85";

@Override
public void onCreate(Bundle savedInstanceState) 
{
		//int[] ledRedArray=new int[19];
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		CheckBt();
		//connect to arduino via amarino
		Amarino.connect(this, DEVICE_ADDRESS);
		
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
		Log.w("#shitty_logcat", "MainActivity load complete");
}

public void buttonCreateRow(View v)
{
	layoutPositionCounter++;
	tag="line"+layoutPositionCounter;
	Log.w("BUTTON_CREATE", "NEUE "+tag);
	//fragment transactions
	Fragment newFragment = new oneline();
	FragmentManager fragmentManager = getFragmentManager();
	FragmentTransaction transaction = fragmentManager.beginTransaction();
	transaction.add(R.id.linecontainer, newFragment, tag).commit();
}

public void setFarbButtonTag()
{
	Log.w("#shitty_logcat", "setFarbButtonTag called line:99");
	for(int i=1;i<=layoutPositionCounter;i++)
	{
	String tag="line"+i;
	//call fragmentmanager to save every lineN
	FragmentManager fragmentManager = getFragmentManager();
	View viewToTag= fragmentManager.findFragmentByTag(tag).getView();
	//get fields
	Button FarbButton=(Button) viewToTag.findViewById(R.id.buttonFarbe);
	FarbButton.setTag("farbButton"+i);
	}
}

public void buttonDeleteRow(View v)
{
	if(layoutPositionCounter<=1){return;}
	String tag="line"+layoutPositionCounter;
	//Fragment newFragment = new oneline();
	FragmentManager fragmentManager = getFragmentManager();
	FragmentTransaction transaction = fragmentManager.beginTransaction();
	// Replace whatever is in the fragment_container view with this fragment,
	// and add the transaction to the back stack
	Fragment toRemove= fragmentManager.findFragmentByTag(tag);
	transaction.remove(toRemove);
	// Commit the transaction
	transaction.commit(); 
	if(layoutPositionCounter<2)
	{
		Button btn = (Button)findViewById(R.id.buttonDeleteRow);
		btn.setActivated(false);
	}
	SharedPreferences saveHandler=getSharedPreferences(save+"_page"+currentPage, MODE_PRIVATE);
	Editor saveEditor= saveHandler.edit();
	tagForButtons="farbButton"+layoutPositionCounter;
	saveEditor.remove(tag+"|datafield");
	saveEditor.remove(tag+"|heightfield");
	saveEditor.remove(tagForButtons+"|color");
	layoutPositionCounter--;
	saveEditor.putInt("numberOfLines", layoutPositionCounter);
	saveEditor.commit();
	
	Log.w("BUTTON_DELETE", "linesaves für"+tag+" entfernt");
}

public void buttonSavePage(View v)
{
	saveCurrentPage(0);
}

public void saveCurrentPage(int page)
{
	if(page==0)	{
		page=currentPage;
	}
	Log.w("#shitty_logcat", "saveCurrentPage called; currentpage="+page);
	FragmentManager fragmentChildManager = getFragmentManager();
	
	//shared preferences handler aufmachen
	SharedPreferences saveHandler=getSharedPreferences(save+"_page"+page, MODE_PRIVATE);
	Editor saveEditor= saveHandler.edit();
	
	//page variablen speichern
	saveEditor.putInt("numberOfLines", layoutPositionCounter);
	saveEditor.putInt("brightness", helligkeitCurrentPagePercent);
	saveEditor.putInt("durationOfPage", durationCurrentPagePercent);
	Log.w("#shitty_logcat", "saveCurrentPage:"+layoutPositionCounter+helligkeitCurrentPagePercent+durationCurrentPagePercent);
	
	//alle lines speichern
	for(int i=1;i<=layoutPositionCounter;i++)
	{
	String tag="line"+i;
	View toSave= fragmentChildManager.findFragmentByTag(tag).getView();
	EditText height=(EditText) toSave.findViewById(R.id.editTextHeight);
	EditText Character=(EditText) toSave.findViewById(R.id.editTextCharacter);
	//convert to strings for pref page
	String heightString=height.getText().toString();
	String CharacterString=Character.getText().toString();
	//save to sharedpreferences
	//PREFCONFIG:
	//level 1: CONFIGS:vom nutzer festgelegter dateiname in shared_prefs; enthält anzahl der seiten sowie länge der config
	//level 2: PAGES: level1name_page1, level1name_page2 usw; enthält die anzahl der lines, lineconfigs etc.b
	saveEditor.putString(tag+"|datafield", CharacterString);
	//nur wenn die höhe festgelegt wurde auch abspeichern
	if(!heightString.equals("")){
	saveEditor.putInt(tag+"|heightfield", Integer.parseInt(heightString));
	}
	else
	{
		saveEditor.putInt(tag+"|heightfield", 0);
	}
	//delete old rows
	for(int b=layoutPositionCounter+1;b<=21;b++)
	{
	String deleteTag="line"+b;
	saveEditor.remove(deleteTag+"|datafield");
	saveEditor.remove(deleteTag+"|heightfield");
	}
	saveEditor.commit();
	}
}

public void removeAllChildAFragments()
{
	FragmentManager fragmentManager = getFragmentManager();
	FragmentTransaction transaction = fragmentManager.beginTransaction();
	for(int i=2;i<=layoutPositionCounter;i++)
	{
	String tag="line"+i;
	Fragment toRemove= fragmentManager.findFragmentByTag(tag);
	if(toRemove!=null){transaction.remove(toRemove);}
	}
	transaction.commit();
	fragmentManager.executePendingTransactions();
	Log.w("#shitty_logcat", "removeAllChildFragments called successfully");
}

public void buttonPlus(View v)
{
	Log.w("BUTTONPLUS", "buttonPlus clicked");
	seitenAnzahl++;
	saveCurrentPage(currentPage);
	
	//erstelle savedata für neue seite
	Log.w("BUTTONPLUS", "NEUE SEITE: erstelle savedata");
	//FragmentManager fragmentChildManager = getFragmentManager();
	//shared preferences handler aufmachen
	SharedPreferences saveHandler=getSharedPreferences(save+"_page"+seitenAnzahl, MODE_PRIVATE);
	Editor saveEditor= saveHandler.edit();
	//page variablen speichern
	saveEditor.putInt("numberOfLines", 1);
	saveEditor.putInt("brightness", 100);
	saveEditor.putInt("durationOfPage", 100/seitenAnzahl);
	saveEditor.commit();
}

public void buttonMinus(View v)
{
	Log.w("#shitty_logcat", "buttonMinus clicked");
	String pageToEdit=save+"_page"+seitenAnzahl;
	SharedPreferences saveHandler=getSharedPreferences(pageToEdit, MODE_PRIVATE);
	Editor saveEditor= saveHandler.edit();
	saveEditor.clear().commit();
	new File("/data/data/de.oerntex.lightsaber/shared_prefs/"+pageToEdit+".xml").delete();
	removeAllChildAFragments();
	seitenAnzahl--;
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
    	if (arg2!=anzahlSaves)   	{
    		arg2++;
    		buildSaveDialog(saveHandlerBert.getString(arg2+"name", "fail"), arg2);
     	}
    	else    	{
    		buildSaveNameRequestDialog();
    	}
    }
    });
}

public void buttonListe(View v) 
{
	Log.w("BUTTON", "buttonListe clicked");
	ArrayAdapter<String> adapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
	adapter.clear();
	publicView=v;
	for(int i=1; i<=seitenAnzahl;i++)
	{
		adapter.add(""+i);
	}
	AlertDialog.Builder builder1=new AlertDialog.Builder(this);
	builder1.setTitle("Wähle!");
	builder1.setAdapter(adapter, new DialogInterface.OnClickListener() {
	
		  @Override
		  public void onClick(DialogInterface dialog, int which) {
			  currentPage=which+1;
			  loadPage(currentPage);
			  Button btn=(Button)publicView;
			  btn.setText(String.valueOf(currentPage));
			  Log.w("#shitty_logcat", "SEITE GELADEN(aus dialog): "+currentPage);
		}
		  });
	builder1.show();
}

public void buttonKlonen(View v)
{
	String pageToEdit=save+"_page"+currentPage;
	File source = new File("/data/data/de.oerntex.lightsaber/shared_prefs/"+pageToEdit+".xml");
	currentPage++;
	seitenAnzahl++;
	pageToEdit=save+"_page"+currentPage;
	File destination = new File("/data/data/de.oerntex.lightsaber/shared_prefs/"+pageToEdit+".xml");
	//("/data/data/de.oerntex.lightsaber/shared_prefs/"+pageToEdit+".xml")
	try {
		copy(source, destination);
		loadPage(0);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
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
	alert.setMessage("Nochmal auf Settings wechseln um Speicherpunkt anzuzeigen");

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
	adapter.add("An Arduino senden");
	
	AlertDialog.Builder builder1=new AlertDialog.Builder(this);
	builder1.setTitle(wahl+"-Aktion?");
	builder1.setAdapter(adapter, new DialogInterface.OnClickListener() {
	
		  @Override
		  public void onClick(DialogInterface dialog, int which) {
			  if(which==0)//LADEN
			  {
				  removeAllChildAFragments();
					save=wahlPublic;//was soll geladen werden?
					SharedPreferences saveHandler=getSharedPreferences(save+"_MAIN", MODE_PRIVATE);
					File file = new File("/data/data/de.oerntex.lightsaber/shared_prefs/"+save+"_MAIN.xml");
					if(file.exists()==true)
					{
						save=saveHandler.getString("nameOfSave", "fail");
						seitenAnzahl=saveHandler.getInt("numberOfPages", 666);
						Context context = SettingsFragment.getView().getContext();
						CharSequence text = save+" mit "+seitenAnzahl+" Seiten geladen.";
						Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
						toast.show();
					}
					Log.w("#shitty_logcat", "save loaded: "+save+" with "+seitenAnzahl+" pages");
			  }
			  if(which==1)//speichern
			  {
				  	save=wahlPublic;//save was soll geladen werden???
					SharedPreferences saveHandler=getSharedPreferences(save+"_MAIN", MODE_PRIVATE);
					Editor saveEditor= saveHandler.edit();
					saveEditor.putInt("numberOfPages", seitenAnzahl);
					saveEditor.putString("nameOfSave", save);
					saveEditor.commit();
					Log.w("#shitty_logcat", "save saved: "+save+" with "+seitenAnzahl+" pages");
			  }
			  if(which==2)//löschen
			  {
				  save=wahlPublic;
				  File file = new File("/data/data/de.oerntex.lightsaber/shared_prefs/"+save+"_MAIN.xml");
				  file.delete();
				  //declare spref handlers
				  SharedPreferences saveHandlerMain=getSharedPreferences("SAVES", MODE_PRIVATE);
				  Editor saveEditor= saveHandlerMain.edit();
				  //do the work
				  saveEditor.remove(wahlInt+"line");
				  anzahlSaves=saveHandlerMain.getInt("anzahlSaves", 55);
				  anzahlSaves--;
				  saveEditor.putInt("anzahlSaves", anzahlSaves);
				  saveEditor.commit();
			  }
			  if (which==3)
			  {
				  //dauer der pages checken
				  //in neuem thread die neuen daten zeitgenau übertragen, so dass der ui thread nicht blockiert wird
				  //zeitgenau-> android berechnet zeitpunkte, nicht arduino
				  //nachteil: aandroid muss bei arduino sein  vorteil: datenstrukturen auf android sind wesentlich besser.
				  if(sendDataToArduino()!=true)
				  {
					  makeToastSettings("Arduinokommunikation fehlgeschlagen.");
				  }
			  }
			  Log.w("#shitty_logcat", "buildSaveDialog: "+which+" has been chosen");
		}
		  });
	builder1.show();
	Log.w("#shitty_logcat", save+" wurde ausgewählt");
}

public boolean sendDataToArduino()
{
	
	final Handler handler=new Handler();
	//Thread toArduino = new Thread()
	//{
	    //@Override
	    //public void run() {
	
	
	        //do
	    	//save all pages, nicht über onpause weil ich nicht weiß was das tut o.O
	    	for(int i=1; i<=seitenAnzahl;i++)	{
	    	Log.w("#shitty_logcat", "save all "+seitenAnzahl+" Pages");
	    	saveCurrentPage(i);
	    	}
	    	Log.w("AMARINO", "BEGINNE KOMMUNIKATION; "+seitenAnzahl+" Seiten.");
	    	
	    	//debug nur eine seite
	    	for(int i=1;i<=seitenAnzahl;i++)
	    	{
	    		
	    		//int i=1;	
	    		Log.w("AMARINO", "seitenAnzahl: "+seitenAnzahl);
	    		//int ledCounter=0;
	    		SharedPreferences ash=getSharedPreferences(save+"_page"+i, MODE_PRIVATE);
	    		int lineAnzahl=ash.getInt("numberOfLines", 42);
	    		int seitenLaenge=ash.getInt("durationOfPage", 666);
	    		
	    		if(seitenLaenge==666||seitenLaenge==0)		{
	    			//failtoast schicken
	    			makeToastSettings("Keine Seitenlänge für Seite "+i+" angegeben.");
	    			Log.w("AMARINO", "Keine Seitenlänge für Seite "+i+" angegeben.");
	    			
	    		}
	    		Log.w("AMARINO", lineAnzahl+"la::sl"+seitenLaenge);
	    		//alle lines abarbeiten, für jede height/led pro line array füllen
	    		//höhe hier setzen
	    		int height=0;
	    		for(int l=1;l<=lineAnzahl;l++)	{
	    			//höhe addieren, damit forschleife gleich arbeiten kann
	    			height=height+ash.getInt("line"+l+"|heightfield", 1);
	    			colorToWork=ash.getInt("farbButton"+l+"|color", 0);
	    			Log.w("AMARINO", "ausgelesene farbe für line"+l+": "+colorToWork+", ausgelesene höhe: "+height);
	    			
	    			Log.w("AMARINO", "bin bei sendDataToArduino");
	    		
	    			bigLedArray[0]=50;//Color.red(colorToWork);
	    			bigLedArray[1]=50;//Color.green(colorToWork);
	    			bigLedArray[2]=50;//Color.blue(colorToWork);
	    			//line
	    			bigLedArray[3]=l;
	    			//height
	    			bigLedArray[4]=height;
	    			makeToastSettings("r:"+bigLedArray[0]+"g:"+bigLedArray[1]+"b:"+bigLedArray[2]+"lN:"+bigLedArray[3]+"hgt:"+bigLedArray[4]);
	    			Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'a', bigLedArray);
	    			for(int h=1;h<=3;h++)	{try {Thread.sleep(600);} catch (InterruptedException e) {e.printStackTrace();}}
	    			Log.w("AMARINO", "versuche an arduino zu senden");
	    		
	    		
	    		}
	    		try {Thread.sleep(seitenLaenge);} catch (InterruptedException e) {e.printStackTrace();}
	    	}
	    //}
	//};
	    	return true;
	//toArduino.start();
	
	
}

public void buttonFarbe(View v){
	Log.w("#shitty_logcat", "buttonFarbe clicked");
	//farbbuttons benennen
	setFarbButtonTag();
	tagForButtons=(String)v.getTag();
	buttonFarbe=(Button) v.findViewWithTag(tagForButtons);
	HSVColorPickerDialog colorPicker= new HSVColorPickerDialog(this, 0xFF4488CC, new OnColorSelectedListener(){
		@Override
		public void colorSelected(Integer color) {
			//save color to sharedPrefs
			String debugSaveName=save+"_page"+currentPage;
			Editor saveHandler=getSharedPreferences(debugSaveName, MODE_PRIVATE).edit();
			saveHandler.putInt(tagForButtons+"|color", color);
			saveHandler.commit();
			Log.w("#shitty_logcat", tagForButtons+"|color"+color+" gesetzt line:447");
			//set new button tint
			buttonFarbe.getBackground().setColorFilter(color,PorterDuff.Mode.MULTIPLY);
		}});
	
	colorPicker.setTitle("Wähle eine Farbe!");
	colorPicker.show();
}

public void deleteMe() {
	Log.w("#shitty_logcat", "deleteMe called");
	FragmentManager fragmentManager = getFragmentManager();
	FragmentTransaction transaction = fragmentManager.beginTransaction();
	// Replace whatever is in the fragment_container view with this fragment,
	// and add the transaction to the back stack
	Fragment toRemove= fragmentManager.findFragmentById(R.id.fragment_container);
	transaction.remove(toRemove).commit();
}

public boolean loadPage(int page)
{
	Log.w("LOADPAGE", "seite: "+page);
	if(page==0)	{
		page=currentPage;
	}
	//leert page-> platz für fragmente
	removeAllChildAFragments();
	//deleteMe();
	SharedPreferences saveHandler=getSharedPreferences(save+"_page"+page, MODE_PRIVATE);
	//werte auslesen
	helligkeitCurrentPagePercent=saveHandler.getInt("brightness", 0);
	layoutPositionCounter=saveHandler.getInt("numberOfLines", 666);if(layoutPositionCounter==666){return false;}
	durationCurrentPagePercent=saveHandler.getInt("durationOfPage", 0);
	
	//debug output
	Log.w("LOADPAGE", "GELADEN:    layoutPositionCounter: "+layoutPositionCounter+" helligkeit: "+helligkeitCurrentPagePercent+" duration: "+durationCurrentPagePercent);
	//seekBars setzen
	setSeek(2, durationCurrentPagePercent, true);setSeek(1, helligkeitCurrentPagePercent, true);
	FragmentManager fragmentManager = getFragmentManager();
	//create the rows
	for (int i=1; i<=layoutPositionCounter; i++)
	{
		//create rows
		tag="line"+i;
		Log.w("LOADPAGE", "Loadpage lines erstellen: "+tag);
		//fragment transactions
		Fragment newFragment = new oneline();
		
		if(fragmentManager.findFragmentByTag(tag) != null){
			FragmentTransaction newLine=fragmentManager.beginTransaction().replace(R.id.linecontainer, newFragment, tag);			
			newLine.commit();
			Log.w("LOADPAGE", "loadPage; "+tag+" existed; executing pending transactions");
		}
		else{
			FragmentTransaction newLine=fragmentManager.beginTransaction().add(R.id.linecontainer, newFragment, tag);
			newLine.commit();			
			Log.w("LOADPAGE", "loadPage; "+tag+" didnt exist; executing pending transactions");
		}
		fragmentManager.executePendingTransactions();
		//identify new row
		Fragment fragmentToLoad= fragmentManager.findFragmentByTag(tag);
		View toLoad=fragmentToLoad.getView();
		//get fields; color gets saved automatically, needs to be loaded though
		EditText heightField=(EditText) toLoad.findViewById(R.id.editTextHeight);
		EditText dataField=(EditText) toLoad.findViewById(R.id.editTextCharacter);
		Button farbButton=(Button) toLoad.findViewById(R.id.buttonFarbe);
		//get Values
		String heightString=String.valueOf(saveHandler.getInt(tag+"|heightfield", 0));
		String dataString=saveHandler.getString(tag+"|datafield", "-1");
		int color=saveHandler.getInt("farbButton"+i+"|color", 0xFF00F000);
		Log.w("LOADPAGE", i+"pageloader ist an farbsetzer vorbeigekommen line:517 color: "+color);
		//felder setzen
		farbButton.getBackground().setColorFilter(color,PorterDuff.Mode.MULTIPLY);
		heightField.setText(String.valueOf(heightString));
		dataField.setText(dataString);
	}
	return true;
}

public void setSeek(int i, int progress, boolean setLine) {
	if(setLine==false)
	{
	if(i==2){
		TextView duration=(TextView)getFragmentManager().findFragmentById(R.id.fragment_container).getView().findViewById(R.id.textViewDurationEinheit);
		duration.setText(progress+" ms");
		durationCurrentPagePercent=progress;
	}
	if(i==1){
		TextView helligkeit=(TextView)getFragmentManager().findFragmentById(R.id.fragment_container).getView().findViewById(R.id.textViewHelligkeitEinheit);
		helligkeit.setText(progress+" %");
		helligkeitCurrentPagePercent=progress;
	}
	}
	else
	{
		if(i==2){
			TextView duration=(TextView)getFragmentManager().findFragmentById(R.id.fragment_container).getView().findViewById(R.id.textViewDurationEinheit);
			duration.setText(progress+" %");
			SeekBar helligkeitSeek=(SeekBar)getFragmentManager().findFragmentById(R.id.fragment_container).getView().findViewById(R.id.seekBarDuration);
			helligkeitSeek.setProgress(progress);
			durationCurrentPagePercent=progress;
		}
		if(i==1){
			TextView helligkeit=(TextView)getFragmentManager().findFragmentById(R.id.fragment_container).getView().findViewById(R.id.textViewHelligkeitEinheit);
			helligkeit.setText(progress+" %");
			SeekBar helligkeitSeek=(SeekBar)getFragmentManager().findFragmentById(R.id.fragment_container).getView().findViewById(R.id.seekBarHelligkeit);
			helligkeitSeek.setProgress(progress);
			helligkeitCurrentPagePercent=progress;
		}
	}
}

@Override
public void onPause()
{
	for(int i=1; i<=seitenAnzahl;i++)
	{
	Log.w("#shitty_logcat", "save all "+seitenAnzahl+" Pages");
	saveCurrentPage(i);
	}
	super.onPause();
}

@Override
public void onDestroy()
{
	super.onStop();
	// if you connect in onStart() you must not forget to disconnect when your app is closed
	Amarino.disconnect(this, DEVICE_ADDRESS);
}

public void makeToastSettings(String message)
{
	Context context = SettingsFragment.getView().getContext();
	CharSequence text = message;
	Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
	toast.show();
}
private void CheckBt() {
	mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

	if (!mBluetoothAdapter.isEnabled()) {
		Toast.makeText(getApplicationContext(), "Bluetooth Disabled !",
				Toast.LENGTH_SHORT).show();
               /* It tests if the bluetooth is enabled or not, if not the app will show a message. */
	}

	if (mBluetoothAdapter == null) {
		Toast.makeText(getApplicationContext(),
				"Bluetooth null!", Toast.LENGTH_SHORT)
				.show();
	}
}

public void Connect() {
	BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);
	Log.d("", "Connecting to ... " + device);
	mBluetoothAdapter.cancelDiscovery();
	try {
			//	btSocket = device.createRfcommSocketToServiceRecord(00001101-0000-1000-8000-00805F9B34FB);
/* Here is the part the connection is made, by asking the device to create a RfcommSocket (Unsecure socket I guess), It map a port for us or something like that */
		btSocket.connect();
		Log.d("", "Connection made.");
	} catch (IOException e) {
		try {
			btSocket.close();
		} catch (IOException e2) {
			Log.d("", "Unable to end the connection");
		}
		Log.d("", "Socket creation failed");
	}
	
	/*beginListenForData();
           /* this is a method used to read what the Arduino says for example when you write Serial.print("Hello world.") in your Arduino code */
}



}

//mainactivity end

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