package qut.wearable_project;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandIOException;
import com.microsoft.band.sensors.BandAccelerometerEvent;
import com.microsoft.band.sensors.BandAccelerometerEventListener;
import com.microsoft.band.sensors.SampleRate;

import java.io.FileOutputStream;
import java.util.Locale;



/**
 * @author James Galloway
 *
 * Implementation of the ProjectSensorInterface for the accelerometer on the device.
 */
class ProjectAccelerometer implements ProjectSensorInterface {
    private BandAccelerometerEventListener listener;
    private float xAcc, yAcc, zAcc;
    private long time;

    /**
     * Sets the event listener for the sensor upon any change in data.
     *
     * @param activity The activity containing the TextView elements.
     * @param txtViews Array containing each of the TextView elements that will display the data
     */
    @Override
    public void setListener(final Activity activity, final TextView[] txtViews) {
        listener = new BandAccelerometerEventListener() {
            @Override
            public void onBandAccelerometerChanged(BandAccelerometerEvent bandAccelerometerEvent) {
                // Low-pass filter to remove effect of gravity
                xAcc = bandAccelerometerEvent.getAccelerationX();
//                        - (0.9 * xAcc + 0.1 * bandAccelerometerEvent.getAccelerationX());
                yAcc = bandAccelerometerEvent.getAccelerationY();
                zAcc = bandAccelerometerEvent.getAccelerationZ();

                time = bandAccelerometerEvent.getTimestamp();

                saveAccData(time, xAcc, yAcc, zAcc, activity);

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtViews[0].setText(String.format(Locale.getDefault(), "%f", xAcc));
                        txtViews[1].setText(String.format(Locale.getDefault(), "%f", yAcc));
                        txtViews[2].setText(String.format(Locale.getDefault(), "%f", zAcc));
                    }
                });
            }
        };
    } // end setListener

    /**
     * Registers a given accelerometer event listener with a given sample rate.
     *
     * @param bandClient The BandClient whose event listener is registered.
     * @param rate The sample rate at which to listen.
     * @return True if successfully registered, otherwise false.
     */
    @Override
    public boolean registerListener(BandClient bandClient, SampleRate rate) {
        try {
            bandClient.getSensorManager().registerAccelerometerEventListener(listener, rate);
            return true;
        } catch (BandIOException ex) {
            ex.printStackTrace();
            return false;
        }
    } // end registerListener

    /**
     * Unregisters a given accelerometer event listener.
     *
     * @param bandClient The BandClient whose event listener is unregistered.
     * @return True if successfully unregistered, otherwise false.
     */
    public boolean unregisterListener(BandClient bandClient) {
        try {
            bandClient.getSensorManager().unregisterAccelerometerEventListener(listener);
            return true;
        } catch (BandIOException ex) {
            ex.printStackTrace();
            return false;
        }
    } // end unregisterListener

    private void saveAccData(long time, float xAcc, float yAcc, float zAcc, Activity activity){
        String FILENAME = "acc_data.txt";
        String x, y, z, t, string;

        x = String.format(Locale.getDefault(), "%f", xAcc);
        y = String.format(Locale.getDefault(), "%f", yAcc);
        z = String.format(Locale.getDefault(), "%f", zAcc);
        t = Long.toString(time);
        string = t + "," + x + "," + y + "," + z + "\n";


        try {
            FileOutputStream fos = activity.openFileOutput(FILENAME, Context.MODE_APPEND);
            fos.write(string.getBytes());
            fos.close();
        } catch (Exception e){

        }
    } // end saveAccData
}
