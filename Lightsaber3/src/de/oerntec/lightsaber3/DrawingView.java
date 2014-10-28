package de.oerntec.lightsaber3;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class DrawingView extends View{
	private final String TAG="drawingView";
	ArrayList<String[]> lines;
	//..
	public final int VERTICAL_LED_COUNT=18;
	//canvas n shit
	private Paint  canvasPaint, rectPaint;
	private int paintColor = Color.RED, colorOffset;
	private float[] colorConverter={0 ,1 ,1};
	private Canvas drawCanvas;
	private Bitmap canvasBitmap;
	//rect und color arrays
	private List<float[]> yColorListsList;
	Rect xRectArray[]=new Rect[VERTICAL_LED_COUNT];
	private List<Rect[]> yRectList;
	//current drawing color
	private float currentHue;

	public DrawingView(Context context, AttributeSet attrs){
		super(context, attrs);
		//init rects
		yRectList=new ArrayList<Rect[]>();
		//init words
		yColorListsList= new ArrayList<float[]>();
		for(int i=0; i<VERTICAL_LED_COUNT; i++){
			float[] temp=new float[VERTICAL_LED_COUNT];
			for(int g=0; g<VERTICAL_LED_COUNT; g++)//so we have at least enough colors for the first draw
				temp[g]=Color.RED;
			yColorListsList.add(temp);
		}
		//get drawing area setup for interaction
		rectPaint=new Paint();
		//set initial color
		rectPaint.setColor(paintColor);
		rectPaint.setStyle(Paint.Style.FILL);
		rectPaint.setStrokeWidth(3);
		canvasPaint = new Paint(Paint.DITHER_FLAG);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		//view given size
		super.onSizeChanged(w, h, oldw, oldh);
		canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		drawCanvas = new Canvas(canvasBitmap);
		//Log.d(TAG, "w: "+w+", h: "+h);
		int rectDimen=w/VERTICAL_LED_COUNT;
		yRectList.clear();
		//create rects, because we only do that once to be able to simply redraw them
		int yOffset=0;
		for(int i=0; i<VERTICAL_LED_COUNT; i++){//iterate y
			int xOffset=0;
			Rect[] currentArray=new Rect[18];
			for(int g=0; g<VERTICAL_LED_COUNT; g++){//iterate x in y
				Rect currentRect=new Rect(xOffset, yOffset, xOffset+rectDimen-5, yOffset+rectDimen-5);
				currentArray[g]=currentRect;
				xOffset+=rectDimen;
			}
			yRectList.add(currentArray);
			yOffset+=rectDimen;
		}
		redraw();
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float touchX = event.getX();
		float touchY = event.getY();
		int action = event.getAction();
		// Log.i("touchcoor", "X: "+touchX+" Y: "+touchY);
		// decide how to handle click

		switch (action) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_MOVE:
			//startTime = SystemClock.elapsedRealtime() + 600;
			//Log.i("touchcoor", "word: down");
			handleTouch(touchX, touchY);
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			break;
		default:
			return false;
		}
		return true;
	}
	
	public String currentDataForSave(){
		String builtString="";
		return builtString;
	}
	
	public void load(String data){
		yColorListsList.clear();
		String[] verticalLines=TextUtils.split(data, "::");
		//for every vertical line
		for(int i=0; i<verticalLines.length; i++){
			//split the line into byte values
			String[] pixelRgb=TextUtils.split(verticalLines[i], ";");
			//convert to int for usability
			int rgb[]=new int[pixelRgb.length];
			for(int h=0;h<pixelRgb.length;h++)
				rgb[h]=Integer.getInteger(pixelRgb[h]);
			//convert int rgb to float hue, save comlete line as floats in array, add to list
			int counter=0;
			float[] temp=new float[VERTICAL_LED_COUNT];
			for(int g=0;g<pixelRgb.length;g+=3){
				Color.RGBToHSV(rgb[g], rgb[g+1], rgb[g+2], colorConverter);
				temp[counter]=colorConverter[0];
				counter++;
			}
			yColorListsList.add(temp);
			redraw();
		}
	}
	
	public void moveAbs(int x){
		colorOffset=x;
		redraw();
	}
	
	public int getXCount(){
		return yColorListsList.size();
	}
	
	public List<float[]> getListOfColorLists(){
		return yColorListsList;
	}
	
	public int addPixels(){
		for(int i=0; i<5; i++){
			float[] temp=new float[18];
			for(int g=0; g<VERTICAL_LED_COUNT; g++)//so we have at least enough colors for the first draw
				temp[g]=Color.RED;
			yColorListsList.add(temp);
		}
		return yColorListsList.size()-18;
	}
	
	public void moveRel(int x){
		colorOffset+=x;
	}

	public void redraw(){
		//long benchmarkTest=System.currentTimeMillis();
		//blank screen
		drawCanvas.drawColor(Color.WHITE);
		int offset=colorOffset;
		//draw the precreated rectangles, using a (y)list of (x)lists with a horizontal offset
		for(int i=0; i<VERTICAL_LED_COUNT; i++){//y
			Rect[] currentXList=yRectList.get(i);//get xList
			for(int g=0; g<currentXList.length; g++){//x step in y
				Rect currentRect=currentXList[g];
				float[] columnColors=yColorListsList.get(g+offset);
				colorConverter[0]=columnColors[i];
				rectPaint.setColor(Color.HSVToColor(colorConverter));
				drawCanvas.drawRect(currentRect, rectPaint);
			}
		}
		//reeeedraw
		invalidate();
		//Log.i(TAG, ""+(System.currentTimeMillis()-benchmarkTest));
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
	}

	public void handleTouch(float xPos, float yPos){
		for(int i=0; i<VERTICAL_LED_COUNT; i++){//iterate y
			Rect[] currentArray=yRectList.get(i);
			for(int g=0; g<VERTICAL_LED_COUNT; g++){//iterate x in y
				if(currentArray[g].contains((int) xPos, (int) yPos)){
					Log.i(TAG, "touched rect (y, x): "+i+", "+g);	
					float[] columnColors=yColorListsList.get(g+colorOffset);
					columnColors[i]=(float)currentHue;
					yColorListsList.set(g+colorOffset, columnColors);
					redraw();
				}
			}
		}
	}
	
	public void setColor(float hue){
		//Log.d(TAG, "setColor");
		currentHue=hue;
		redraw();
	}
}

