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

public class DrawView extends ImageView {

    Paint pPaint = new Paint();
    Paint lPaint = new Paint();


    PointF topLeft = new PointF(200,200);
    PointF topRight = new PointF(550,200);
    PointF bottomLeft = new PointF(200,550);
    PointF bottomRight = new PointF(550,550);

    float sizeOfRect = 85;

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

        canvas.drawLine(topLeft.x, topLeft.y, topRight.x, topRight.y, lPaint);
        canvas.drawLine(topRight.x, topRight.y, bottomRight.x, bottomRight.y, lPaint);
        canvas.drawLine(bottomRight.x, bottomRight.y, bottomLeft.x, bottomLeft.y, lPaint);
        canvas.drawLine(bottomLeft.x, bottomLeft.y, topLeft.x, topLeft.y, lPaint);

        canvas.drawPoint(topLeft.x, topLeft.y, pPaint);
        canvas.drawPoint(topRight.x, topRight.y, pPaint);
        canvas.drawPoint(bottomLeft.x, bottomLeft.y, pPaint);
        canvas.drawPoint(bottomRight.x, bottomRight.y, pPaint);
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
                    distX = event.getX() - topLeft.x;
                    distY = event.getY() - topLeft.y;
                } else if (topRightTouchArea.contains(event.getX(), event.getY())) {
                    currentTouch = TOUCH_TOP_RIGHT;
                    distX = event.getX() - topRight.x;
                    distY = event.getY() - topRight.y;
                } else if (bottomLeftTouchArea.contains(event.getX(), event.getY())) {
                    currentTouch = TOUCH_BOT_LEFT;
                    distX = event.getX() - bottomLeft.x;
                    distY = event.getY() - bottomLeft.y;
                } else if (bottomRightTouchArea.contains(event.getX(), event.getY())) {
                    currentTouch = TOUCH_BOT_RIGHT;
                    distX = event.getX() - bottomRight.x;
                    distY = event.getY() - bottomRight.y;
                } else {
                    return false; //Return false if user touches none of the corners
                }
                return true; //Return true if the user touches one of the corners
            //Now we know which corner the user is touching.
            //When the user moves their finger, we update the point to the user position and invalidate.
            case MotionEvent.ACTION_MOVE:
                switch (currentTouch) {
                    case TOUCH_TOP_LEFT:
                        topLeft.x = event.getX() - distX;
                        topLeft.y = event.getY() - distY;
                        invalidate();
                        return true;
                    case TOUCH_TOP_RIGHT:
                        topRight.x = event.getX() - distX;
                        topRight.y = event.getY() - distY;
                        invalidate();
                        return true;
                    case TOUCH_BOT_LEFT:
                        bottomLeft.x = event.getX() - distX;
                        bottomLeft.y = event.getY() - distY;
                        invalidate();
                        return true;
                    case TOUCH_BOT_RIGHT:
                        bottomRight.x = event.getX() - distX;
                        bottomRight.y = event.getY() - distY;
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
                        topLeft.x = event.getX() - distX;
                        if (topLeft.x < 0){ topLeft.x = 0; }
                        if (topLeft.x > imgDisplay.viewW){ topLeft.x = imgDisplay.viewW; }
                        topLeft.y = event.getY() - distY;
                        if (topLeft.y < 0){ topLeft.y = 0; }
                        if (topLeft.y > imgDisplay.viewH){ topLeft.y = imgDisplay.viewH; }
                        invalidate();
                        currentTouch = NONE;
                        return true;
                    case TOUCH_TOP_RIGHT:
                        topRight.x = event.getX() - distX;
                        if (topRight.x < 0){ topRight.x = 0; }
                        if (topRight.x > imgDisplay.viewW){ topRight.x = imgDisplay.viewW; }
                        topRight.y = event.getY() - distY;
                        if (topRight.y < 0){ topRight.y = 0; }
                        if (topRight.y > imgDisplay.viewH){ topRight.y = imgDisplay.viewH; }
                        invalidate();
                        currentTouch = NONE;
                        return true;
                    case TOUCH_BOT_LEFT:
                        bottomLeft.x = event.getX() - distX;
                        if (bottomLeft.x < 0){ bottomLeft.x = 0; }
                        if (bottomLeft.x > imgDisplay.viewW){ bottomLeft.x = imgDisplay.viewW; }
                        bottomLeft.y = event.getY() - distY;
                        if (bottomLeft.y < 0){ bottomLeft.y = 0; }
                        if (bottomLeft.y > imgDisplay.viewH){ bottomLeft.y = imgDisplay.viewH; }
                        invalidate();
                        currentTouch = NONE;
                        return true;
                    case TOUCH_BOT_RIGHT:
                        bottomRight.x = event.getX() - distX;
                        if (bottomRight.x < 0){ bottomRight.x = 0; }
                        if (bottomRight.x > imgDisplay.viewW){ bottomRight.x = imgDisplay.viewW; }
                        bottomRight.y = event.getY() - distY;
                        if (bottomRight.y < 0){ bottomRight.y = 0; }
                        if (bottomRight.y > imgDisplay.viewH){ bottomRight.y = imgDisplay.viewH; }
                        invalidate();
                        currentTouch = NONE;
                        return true;
                }
                return false;
        }
        return true;
    }
static float distX, distY;
}
