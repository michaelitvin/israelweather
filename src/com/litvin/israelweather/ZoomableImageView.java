//This code was adapted from http://stackoverflow.com/questions/4407072/pinch-zoom-in-android-2-1/12390141#12390141

package com.litvin.israelweather;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;

public class ZoomableImageView extends View {
    private static final String TAG = "ZoomableImageView";
    private final float INIT_SCALE_COEFF = 1.2f;
    private final int MAX_INIT_SCALE = 2;
   
    private Bitmap imgBitmap = null;
   
    private int containerWidth;
    private int containerHeight;
       
    Paint background;   
   
    //Matrices will be used to move and zoom image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();
   
    PointF start = new PointF();       
   
    float currentScale;
    float initialScale = 1.0f;
    float curX;
    float curY;
    private boolean haveMovedDragging = false;
   
    //We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;
   
    //For animating stuff   
    float targetX;
    float targetY;
    float targetScale;
    float targetScaleX;
    float targetScaleY;
    float scaleChange;
    float targetRatio;
    float transitionalRatio;
   
    float easing = 0.2f;   
    boolean isAnimating = false;
   
    float scaleDampingFactor = 0.5f;
   
    //For pinch and zoom
    float oldDist = 1f;   
    PointF mid = new PointF();
   
    private Handler mHandler = new Handler();       
   
    float minScale = 0.5f;
    float maxScale = 4.0f;
   
    float wpRadius = 25.0f;
    float wpInnerRadius = 20.0f;
   
    float screenDensity;
   
    private GestureDetector gestureDetector;
   
    public static final int DEFAULT_SCALE_FIT_INSIDE = 0;
    public static final int DEFAULT_SCALE_ORIGINAL = 1;
    public static final int DEFAULT_SCALE_FIT_INTEGER = 2;
   
    private int defaultScale;
   
    public int getDefaultScale() {
        return defaultScale;
    }

    public void setDefaultScale(int defaultScale) {
        this.defaultScale = defaultScale;
    }

    public ZoomableImageView(Context context) {
        super(context);       
        setFocusable(true);
        setFocusableInTouchMode(true);
       
        maxScale = context.getResources().getInteger(R.integer.max_image_scale);
        screenDensity = context.getResources().getDisplayMetrics().density;

        initPaints();
        gestureDetector = new GestureDetector(context, new MyGestureDetector());
    }
   
    public ZoomableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
       
        screenDensity = context.getResources().getDisplayMetrics().density;       
        initPaints();
        gestureDetector = new GestureDetector(context, new MyGestureDetector());
       
        defaultScale = ZoomableImageView.DEFAULT_SCALE_FIT_INTEGER;
    }
   
    private void initPaints() {
        background = new Paint();
    }
   
    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
           
        //Reset the width and height. Will draw bitmap and change
        containerWidth = width;
        containerHeight = height;
       
        if(imgBitmap != null) {
            int imgHeight = imgBitmap.getHeight();
            int imgWidth = imgBitmap.getWidth();
           
            float scale;
            int initX = 0;
            int initY = 0;           
           
            if(defaultScale == ZoomableImageView.DEFAULT_SCALE_FIT_INSIDE) {               
                if(imgWidth > containerWidth) {           
                    scale = (float)containerWidth / imgWidth;           
                    float newHeight = imgHeight * scale;           
                    initY = (containerHeight - (int)newHeight)/2;
                   
                    matrix.setScale(scale, scale);
                    matrix.postTranslate(0, initY);
                }
                else {           
                    scale = (float)containerHeight / imgHeight;
                    float newWidth = imgWidth * scale;
                    initX = (containerWidth - (int)newWidth)/2;
                   
                    matrix.setScale(scale, scale);
                    matrix.postTranslate(initX, 0);
                }
               
                curX = initX;
                curY = initY;
               
                currentScale = scale;
                //minScale = scale;
            }
            else if(defaultScale == ZoomableImageView.DEFAULT_SCALE_FIT_INTEGER) {               
                if(imgWidth > containerWidth) {           
                    scale = Math.min(MAX_INIT_SCALE, Math.max(minScale, Math.round(INIT_SCALE_COEFF * (float)containerWidth / imgWidth)));           
                    float newHeight = imgHeight * scale;           
                    initY = (containerHeight - (int)newHeight)/2;
                   
                    matrix.setScale(scale, scale);
                    matrix.postTranslate(0, initY);
                }
                else {           
                    scale = Math.min(MAX_INIT_SCALE, Math.max(minScale, Math.round(INIT_SCALE_COEFF * (float)containerHeight / imgHeight)));
                    float newWidth = imgWidth * scale;
                    initX = (containerWidth - (int)newWidth)/2;
                   
                    matrix.setScale(scale, scale);
                    matrix.postTranslate(initX, 0);
                }
               
                curX = initX;
                curY = initY;
               
                currentScale = scale;
                //minScale = scale;
            }
            else {
                if(imgWidth > containerWidth) {                                   
                    initY = (containerHeight - (int)imgHeight)/2;                   
                    matrix.postTranslate(0, initY);
                }
                else {                               
                    initX = (containerWidth - (int)imgWidth)/2;                   
                    matrix.postTranslate(initX, 0);
                }
               
                curX = initX;
                curY = initY;
               
                currentScale = 1.0f;
                //minScale = 1.0f;               
            }
            initialScale = currentScale;
            checkImageConstraints();
            invalidate();           
        }
    }
   
    @Override
    protected void onDraw(Canvas canvas) {
    	super.onDraw(canvas);
    	if (canvas != null) {
	        if(imgBitmap != null)                       
	            canvas.drawBitmap(imgBitmap, matrix, background);                                                   
        }
    }
   
    //Checks and sets the target image x and y co-ordinates if out of bounds
    private void checkImageConstraints() {
        if(imgBitmap == null) {
            return;
        }
       
        float[] mvals = new float[9];
        matrix.getValues(mvals);
       
        currentScale = mvals[0];
               
        if(currentScale < minScale) {                               
            float deltaScale = minScale / currentScale;                   
            float px = containerWidth/2;
            float py = containerHeight/2;           
            matrix.postScale(deltaScale, deltaScale, px, py);
            invalidate();
        }       
       
        matrix.getValues(mvals);
        currentScale = mvals[0];
        curX = mvals[2];
        curY = mvals[5];
               
        int rangeLimitX = containerWidth - (int)(imgBitmap.getWidth() * currentScale);
        int rangeLimitY = containerHeight - (int)(imgBitmap.getHeight() * currentScale);
       
       
        boolean toMoveX = false;
        boolean toMoveY = false;   
       
        if(rangeLimitX < 0) {
            if(curX > 0) {
                targetX = 0;
                toMoveX = true;
            }
            else if(curX < rangeLimitX) {
                targetX = rangeLimitX;
                toMoveX = true;
            }
        }
        else {
            targetX = rangeLimitX / 2;
            toMoveX = true;
        }
       
        if(rangeLimitY < 0) {
            if(curY > 0) {
                targetY = 0;
                toMoveY = true;
            }
            else if(curY < rangeLimitY) {
                targetY = rangeLimitY;
                toMoveY = true;
            }
        }
        else {
            targetY = rangeLimitY / 2;
            toMoveY = true;
        }
       
        if(toMoveX == true || toMoveY == true) {
            if(toMoveY == false) {
                targetY = curY;
            }
            if(toMoveX == false) {
                targetX = curX;
            }           
           
            //Disable touch event actions
            isAnimating = true;
            //Initialize timer           
            mHandler.removeCallbacks(mUpdateImagePositionTask);
            mHandler.postDelayed(mUpdateImagePositionTask, 100);
        }
    }       
   
   
    @Override
    public boolean onTouchEvent(MotionEvent event) {       
        if(gestureDetector.onTouchEvent(event)) {
            return true;
        }
       
        if(isAnimating == true) {
            return true;
        }
       
        //Handle touch events here
        
        float[] mvals = new float[9];
        switch(event.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
            if(isAnimating == false) {
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());           
                mode = DRAG;
                haveMovedDragging = false;
            }
        break;
       
        case MotionEvent.ACTION_POINTER_DOWN:
            oldDist = spacing(event);           
            if(oldDist > 10f) {
                savedMatrix.set(matrix);
                midPoint(mid, event);
                mode = ZOOM;
            }
        break;
       
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_POINTER_UP:
            matrix.getValues(mvals);
            curX = mvals[2];
            curY = mvals[5];
            currentScale = mvals[0];
           
            if (((mode == DRAG && haveMovedDragging) || mode != DRAG) && isAnimating == false) {                                       
                checkImageConstraints();
            }
            
            mode = NONE;
        break;
           
        case MotionEvent.ACTION_MOVE:           
            if(mode == DRAG && isAnimating == false) {
                matrix.set(savedMatrix);
                float diffX = event.getX() - start.x;
                float diffY = event.getY() - start.y;
               
                matrix.postTranslate(diffX, diffY);
                               
                matrix.getValues(mvals);
                curX = mvals[2];
                curY = mvals[5];
                currentScale = mvals[0];
                
                haveMovedDragging = true;
            }
            else if(mode == ZOOM && isAnimating == false) {
                float newDist = spacing(event);               
                if(newDist > 10f) {
                    matrix.set(savedMatrix);
                    float scale = newDist / oldDist;                   
                    matrix.getValues(mvals);
                    currentScale = mvals[0];
                                       
                    if(currentScale * scale <= minScale) {
                        matrix.postScale(minScale/currentScale, minScale/currentScale, mid.x, mid.y);
                    }                   
                    else if(currentScale * scale >= maxScale) {
                        matrix.postScale(maxScale/currentScale, maxScale/currentScale, mid.x, mid.y);
                    }
                    else {
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                                                               
                   
                    matrix.getValues(mvals);
                    curX = mvals[2];
                    curY = mvals[5];
                    currentScale = mvals[0];                                       
                }
            }
               
        break;                               
        }
       
        //Calculate the transformations and then invalidate
        invalidate();
        return true;
    }
   
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }
   
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x/2, y/2);
    }
   
    @Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        containerWidth = getWidth();
        containerHeight = getHeight();
        
        if (imgBitmap == null)
        	return;

        int imgHeight = imgBitmap.getHeight();
        int imgWidth = imgBitmap.getWidth();
        
        if (firstTime) {
            float scale;
            int initX = 0;
            int initY = 0;
           
            matrix.reset();
            
            if(defaultScale == ZoomableImageView.DEFAULT_SCALE_FIT_INSIDE) {               
                if(imgWidth > containerWidth) {           
                    scale = (float)containerWidth / imgWidth;           
                    float newHeight = imgHeight * scale;           
                    initY = (containerHeight - (int)newHeight)/2;
                   
                    matrix.setScale(scale, scale);
                    matrix.postTranslate(0, initY);
                }
                else {           
                    scale = (float)containerHeight / imgHeight;
                    float newWidth = imgWidth * scale;
                    initX = (containerWidth - (int)newWidth)/2;
                   
                    matrix.setScale(scale, scale);
                    matrix.postTranslate(initX, 0);
                }
               
                curX = initX;
                curY = initY;
               
                currentScale = scale;
                //minScale = scale;
            }
            else if(defaultScale == ZoomableImageView.DEFAULT_SCALE_FIT_INTEGER) {               
                if(imgWidth > containerWidth) {           
                    scale = Math.min(MAX_INIT_SCALE, Math.max(minScale, Math.round(INIT_SCALE_COEFF * (float)containerWidth / imgWidth)));
                    float newHeight = imgHeight * scale;           
                    initY = (containerHeight - (int)newHeight)/2;
                   
                    matrix.setScale(scale, scale);
                    matrix.postTranslate(0, initY);
                }
                else {           
                    scale = Math.min(MAX_INIT_SCALE, Math.max(minScale, Math.round(INIT_SCALE_COEFF * (float)containerHeight / imgHeight)));
                    float newWidth = imgWidth * scale;
                    initX = (containerWidth - (int)newWidth)/2;
                   
                    matrix.setScale(scale, scale);
                    matrix.postTranslate(initX, 0);
                }
               
                curX = initX;
                curY = initY;
               
                currentScale = scale;
                //minScale = scale;
            }
            else {
                if(imgWidth > containerWidth) {
                    initX = 0;
                    if(imgHeight > containerHeight) {                       
                        initY = 0;
                    }
                    else {                       
                        initY = (containerHeight - (int)imgHeight)/2;
                    }
                }
                else {                               
                    initX = (containerWidth - (int)imgWidth)/2;
                    if(imgHeight > containerHeight) {
                        initY = 0;
                    }
                    else {
                        initY = (containerHeight - (int)imgHeight)/2;
                    }
                }
                matrix.postTranslate(initX, initY);
               
                curX = initX;
                curY = initY;
               
                currentScale = 1.0f;
                //minScale = 1.0f;               
            }
        }
        initialScale = currentScale;
        checkImageConstraints();
        invalidate();
	
		super.onLayout(changed, left, top, right, bottom);
	}

    private boolean firstTime = true;
    
	public void setImageBitmap(final Bitmap b) {
    	firstTime = (imgBitmap == null);
    	if(b != null) {
    		imgBitmap = b;
	    	checkImageConstraints();
	    	requestLayout();
            invalidate();
        } else {
            Log.d(TAG, "bitmap is null");
        }
    }
   
    public Bitmap getPhotoBitmap() {       
        return imgBitmap;
    }
   
   
    private Runnable mUpdateImagePositionTask = new Runnable() {
        public void run() {       
            float[] mvals;
           
            if(Math.abs(targetX - curX) < 5 && Math.abs(targetY - curY) < 5) {
                isAnimating = false;
                mHandler.removeCallbacks(mUpdateImagePositionTask);
               
                mvals = new float[9];
                matrix.getValues(mvals);
               
                currentScale = mvals[0];
                curX = mvals[2];
                curY = mvals[5];
               
                //Set the image parameters and invalidate display
                float diffX = (targetX - curX);
                float diffY = (targetY - curY);
                               
                matrix.postTranslate(diffX, diffY);
            }
            else {
                isAnimating = true;
                mvals = new float[9];
                matrix.getValues(mvals);
               
                currentScale = mvals[0];
                curX = mvals[2];
                curY = mvals[5];
               
                //Set the image parameters and invalidate display
                float diffX = (targetX - curX) * 0.3f;
                float diffY = (targetY - curY) * 0.3f;
                               
                matrix.postTranslate(diffX, diffY);               
                mHandler.postDelayed(this, 25);               
            }
           
            invalidate();           
        }
    };
   
    private Runnable mUpdateImageScale = new Runnable() {
        public void run() {           
            float transitionalRatio = targetScale / currentScale;           
            float dx;
            if(Math.abs(transitionalRatio - 1) > 0.05) {
                isAnimating = true;               
                if(targetScale > currentScale) {                                       
                    dx = transitionalRatio - 1;
                    scaleChange = 1 + dx * 0.2f;
                   
                    currentScale *= scaleChange;
                   
                    if(currentScale > targetScale) {
                        currentScale = currentScale / scaleChange;
                        scaleChange = 1;
                    }
                }
                else {                                   
                    dx = 1 - transitionalRatio;                   
                    scaleChange = 1 - dx * 0.5f;
                    currentScale *= scaleChange;
                   
                    if(currentScale < targetScale) {
                        currentScale = currentScale / scaleChange;
                        scaleChange = 1;
                    }
                }
                                               
               
                if(scaleChange != 1) {
                    matrix.postScale(scaleChange, scaleChange, targetScaleX, targetScaleY);               
                    mHandler.postDelayed(mUpdateImageScale, 15);
                    invalidate();
                }
                else {
                    isAnimating = false;
                    scaleChange = 1;                   
                    matrix.postScale(targetScale/currentScale, targetScale/currentScale, targetScaleX, targetScaleY);
                    currentScale = targetScale;
                    mHandler.removeCallbacks(mUpdateImageScale);
                    invalidate();
                    checkImageConstraints();
                }               
            }
            else {
                isAnimating = false;
                scaleChange = 1;               
                matrix.postScale(targetScale/currentScale, targetScale/currentScale, targetScaleX, targetScaleY);
                currentScale = targetScale;
                mHandler.removeCallbacks(mUpdateImageScale);
                invalidate();
                checkImageConstraints();
            }                               
        }
    };
   
   /** Show an event in the LogCat view, for debugging */
   @SuppressWarnings({ "unused", "deprecation" })
   private void dumpEvent(MotionEvent event) {
      String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE", "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
      StringBuilder sb = new StringBuilder();
      int action = event.getAction();
      int actionCode = action & MotionEvent.ACTION_MASK;
      sb.append("event ACTION_").append(names[actionCode]);
      if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP) {
         sb.append("(pid ").append(action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
         sb.append(")");
      }
      sb.append("[");
     
      for (int i = 0; i < event.getPointerCount(); i++) {
         sb.append("#").append(i);
         sb.append("(pid ").append(event.getPointerId(i));
         sb.append(")=").append((int) event.getX(i));
         sb.append(",").append((int) event.getY(i));
         if (i + 1 < event.getPointerCount())
            sb.append(";");
      }
      sb.append("]");
   }

   class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent event) {           
            if(isAnimating == true) {
                return true;
            }
           
            scaleChange = 1;
            isAnimating = true;
            targetScaleX = event.getX();
            targetScaleY = event.getY();
           
            if(Math.abs(currentScale - maxScale) > 0.1 && Math.abs(currentScale - minScale) > 0.1) {           
                targetScale = maxScale;
            }
            else {
                targetScale = initialScale;
            }
            targetRatio = targetScale / currentScale;
            mHandler.removeCallbacks(mUpdateImageScale);
            mHandler.post(mUpdateImageScale);           
            return true;
        }
       
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return super.onFling(e1, e2, velocityX, velocityY);
        }
       
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }
    }
}
