package com.google.android.glass.sample.klabinterface;

        import android.content.Context;
        import android.graphics.Bitmap;
        import android.graphics.drawable.Drawable;
        import android.os.Handler;
        import android.text.Layout;
        import android.util.AttributeSet;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.widget.FrameLayout;
        import android.widget.ImageView;
        import android.widget.TextView;


public class TemperatureView extends FrameLayout {

    private Bitmap bmp;
    private TextView AvgView;

    /**
     * Interface to listen for changes on the view layout.
     */
    public interface Listener {
        /** Notified of a change in the view. */
        public void onChange();
    }

    /** About 24 FPS, visible for testing. */
    static final long DELAY_MILLIS = 41;

    private ImageView beatingImage;
    private final TextView mTitle;

    private final Handler mHandler = new Handler();
    private final Runnable mUpdateTextRunnable = new Runnable() {

        @Override
        public void run() {
            if (mRunning) {
                updateText();
                postDelayed(mUpdateTextRunnable, DELAY_MILLIS);
            }
        }
    };

    private boolean mRunning;

    public interface ManageBitmap{
        public Bitmap getBitmap();

        public boolean isReady();

        public double getAvg();
    }
    private Listener mChangeListener;

    private ManageBitmap mManage;

    public TemperatureView(Context context) {
        this(context, null, 0);
    }

    public TemperatureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TemperatureView(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
        LayoutInflater.from(context).inflate(R.layout.buso_layout, this);
        beatingImage = (ImageView) findViewById(R.id.image_left);
        mTitle = (TextView) findViewById(R.id.message);
        AvgView = (TextView) findViewById(R.id.avg);

        mTitle.setText("Temperature");
        int id = getResources().getIdentifier(
		         "com.google.android.glass.sample.stopwatch:drawable/layout_color"
				 ,null,null);
        beatingImage.setImageResource(id);
    }
    /**
     * Sets a Listener.
     */
    public void setListener(Listener listener) {
        mChangeListener = listener;
    }

    /**
     * Returns the set Listener.
     */
    public Listener getListener() {
        return mChangeListener;
    }

    /**
     * Starts the chronometer.
     */
    public void start() {
        if (!mRunning) {
            postDelayed(mUpdateTextRunnable, DELAY_MILLIS);
        }
        mRunning = true;
    }

    /**
     * Stops the chronometer.
     */
    public void stop() {
        if (mRunning) {
            removeCallbacks(mUpdateTextRunnable);
        }
        mRunning = false;
    }

    @Override
    public boolean postDelayed(Runnable action, long delayMillis) {
        return mHandler.postDelayed(action, delayMillis);
    }

    @Override
    public boolean removeCallbacks(Runnable action) {
        mHandler.removeCallbacks(action);
        return true;
    }
    /**
     * Sets a Listener.
     */
    public void setManageBPM(ManageBitmap manager) {
        mManage = manager;
    }

    void updateText() {
        if(mManage.isReady())
        {
            bmp = mManage.getBitmap();
            AvgView.setText(Double.toString(mManage.getAvg()));
            beatingImage.setImageBitmap(bmp);

        }
        if (mChangeListener != null) {
            mChangeListener.onChange();
        }
    }
}
