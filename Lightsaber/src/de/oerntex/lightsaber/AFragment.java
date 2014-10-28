package de.oerntex.lightsaber;


import java.lang.ref.WeakReference;

import android.os.Bundle;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class AFragment extends Fragment {
	private SeekBar seekBarHelligkeit, seekBarDuration;
	View root;
	private LayoutInflater mInflater;
	private WeakReference<View> mRootView = null;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
               
        if (inflater != null)
            mInflater = inflater;
          else
            mInflater = LayoutInflater.from(getActivity());
        
          View rootView = mRootView == null ? null : mRootView.get();
          if (rootView != null) {
              final ViewParent parent = rootView.getParent();
              if (parent != null && parent instanceof ViewGroup)
                ((ViewGroup) parent).removeView(rootView);  
              }
          else {
               rootView = mInflater.inflate(R.layout.afragment, container, false);
               mRootView = new WeakReference<View>(rootView);
               }
        
        root=rootView;
        //seeeekbars
        seekBarHelligkeit = (SeekBar) root.findViewById(R.id.seekBarHelligkeit);
		seekBarDuration = (SeekBar) root.findViewById(R.id.seekBarDuration);
		seekBarHelligkeit.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			int progressChanged = 0;

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
				progressChanged = progress;
				((MainActivity)getActivity()).setSeek(1,progressChanged, false);
			}
			public void onStartTrackingTouch(SeekBar seekBar) {}
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});
		seekBarDuration.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			int progressChanged = 0;

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
				progressChanged = progress;
				((MainActivity)getActivity()).setSeek(2, progressChanged, false);
			}

			public void onStartTrackingTouch(SeekBar seekBar) {}

			public void onStopTrackingTouch(SeekBar seekBar) {
				
			}
		});
		
		return root;
    }
    
    @Override
    public void onPause()
    {
    	((MainActivity)getActivity()).saveCurrentPage(0);
    	//((MainActivity)getActivity()).deleteMe();
    	super.onPause();
    }
    
    @Override
    public void onResume()
    {
    	//((MainActivity)getActivity()).loadPage(0);
    	super.onResume();
    }
	
}
