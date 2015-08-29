package delphiki.testapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Created by Delphiki on 8/28/2015.
 */
public class DrawView extends ImageView {

    Paint pPaint = new Paint();
    Paint lPaint = new Paint();


    PointF topLeft = new PointF(200,200);
    PointF topRight = new PointF(550,200);
    PointF bottomLeft = new PointF(200,550);
    PointF bottomRight = new PointF(550,550);

    float sizeOfRect = 25;

    private final int NONE = -1, TOUCH_TOP_LEFT = 0, TOUCH_TOP_RIGHT = 1, TOUCH_BOT_LEFT = 2, TOUCH_BOT_RIGHT = 3;
    int currentTouch = NONE;

    public DrawView(Context context) {
        super(context);
        setFocusable(true); // necessary for getting the touch events
    }

    public DrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true); // necessary for getting the touch events
    }

    @Override
    protected void onDraw(Canvas canvas) {
        pPaint.setColor(Color.GREEN);
        pPaint.setStyle(Paint.Style.STROKE);
        pPaint.setStrokeWidth(15);
        pPaint.setStrokeCap(Paint.Cap.ROUND);

        lPaint.setColor(Color.RED);
        lPaint.setStyle(Paint.Style.STROKE);
        lPaint.setStrokeWidth(5);

        super.onDraw(canvas);

        canvas.drawPoint(topLeft.x, topLeft.y, pPaint);
        canvas.drawPoint(topRight.x, topRight.y, pPaint);
        canvas.drawPoint(bottomLeft.x, bottomLeft.y, pPaint);
        canvas.drawPoint(bottomRight.x, bottomRight.y, pPaint);

        canvas.drawLine(topLeft.x, topLeft.y, topRight.x, topRight.y, lPaint);
        canvas.drawLine(topRight.x, topRight.y, bottomRight.x, bottomRight.y, lPaint);
        canvas.drawLine(bottomRight.x, bottomRight.y, bottomLeft.x, bottomLeft.y, lPaint);
        canvas.drawLine(bottomLeft.x, bottomLeft.y, topLeft.x, topLeft.y, lPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            //The user just put their finger down.
            //We check to see which corner the user is touching
            //And set our global, currentTouch, to the appropriate constant.
            case MotionEvent.ACTION_DOWN:

                RectF topLeftTouchArea = new RectF(topLeft.x - sizeOfRect, topLeft.y - sizeOfRect, topLeft.x + sizeOfRect, topLeft.y + sizeOfRect);
                RectF topRightTouchArea = new RectF(topRight.x - sizeOfRect, topRight.y - sizeOfRect, topRight.x + sizeOfRect, topRight.y + sizeOfRect);
                RectF bottomLeftTouchArea = new RectF(bottomLeft.x - sizeOfRect, bottomLeft.y - sizeOfRect, bottomLeft.x + sizeOfRect, bottomLeft.y + sizeOfRect);
                RectF bottomRightTouchArea = new RectF(bottomRight.x - sizeOfRect, bottomRight.y - sizeOfRect, bottomRight.x + sizeOfRect, bottomRight.y + sizeOfRect);

                if (topLeftTouchArea.contains(event.getX(), event.getY())) {
                    currentTouch = TOUCH_TOP_LEFT;
                } else if (topRightTouchArea.contains(event.getX(), event.getY())) {
                    currentTouch = TOUCH_TOP_RIGHT;
                } else if (bottomLeftTouchArea.contains(event.getX(), event.getY())) {
                    currentTouch = TOUCH_BOT_LEFT;
                } else if (bottomRightTouchArea.contains(event.getX(), event.getY())) {
                    currentTouch = TOUCH_BOT_RIGHT;
                } else {
                    return false; //Return false if user touches none of the corners
                }
                return true; //Return true if the user touches one of the corners
            //Now we know which corner the user is touching.
            //When the user moves their finger, we update the point to the user position and invalidate.
            case MotionEvent.ACTION_MOVE:
                switch (currentTouch) {
                    case TOUCH_TOP_LEFT:
                        topLeft.x = event.getX();
                        topLeft.y = event.getY();
                        invalidate();
                        return true;
                    case TOUCH_TOP_RIGHT:
                        topRight.x = event.getX();
                        topRight.y = event.getY();
                        invalidate();
                        return true;
                    case TOUCH_BOT_LEFT:
                        bottomLeft.x = event.getX();
                        bottomLeft.y = event.getY();
                        invalidate();
                        return true;
                    case TOUCH_BOT_RIGHT:
                        bottomRight.x = event.getX();
                        bottomRight.y = event.getY();
                        invalidate();
                        return true;
                }
                //We returned true for all of the above cases, because we used the event
                return false; //If currentTouch is none of the above cases, return false

            //Here the user lifts up their finger.
            //We update the points one last time, and set currentTouch to NONE.
            case MotionEvent.ACTION_UP:
                switch (currentTouch) {
                    case TOUCH_TOP_LEFT:
                        topLeft.x = event.getX();
                        topLeft.y = event.getY();
                        invalidate();
                        currentTouch = NONE;
                        return true;
                    case TOUCH_TOP_RIGHT:
                        topRight.x = event.getX();
                        topRight.y = event.getY();
                        invalidate();
                        currentTouch = NONE;
                        return true;
                    case TOUCH_BOT_LEFT:
                        bottomLeft.x = event.getX();
                        bottomLeft.y = event.getY();
                        invalidate();
                        currentTouch = NONE;
                        return true;
                    case TOUCH_BOT_RIGHT:
                        bottomRight.x = event.getX();
                        bottomRight.y = event.getY();
                        invalidate();
                        currentTouch = NONE;
                        return true;
                }
                return false;
        }
        return true;
    }

}
