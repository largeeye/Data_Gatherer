package qut.wearable_project;

import android.app.Activity;
import android.widget.TextView;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandIOException;
import com.microsoft.band.sensors.BandGyroscopeEvent;
import com.microsoft.band.sensors.BandGyroscopeEventListener;
import com.microsoft.band.sensors.SampleRate;

import java.util.Locale;

/**
 * @author James Galloway
 *
 * Implementation of the ProjectSensorInterface for the gyroscope on the device.
 */
public class ProjectGyroscope implements ProjectSensorInterface {
    private BandGyroscopeEventListener listener;
    private float xGyro, yGyro, zGyro;

    /**
     * Sets the event listener for the sensor upon any change in data.
     *
     * @param activity The activity containing the TextView elements.
     * @param txtViews Array containing each of the TextView elements that will display the data.
     */
    @Override
    public void setListener(final Activity activity, final TextView[] txtViews) {
        listener = new BandGyroscopeEventListener() {
            @Override
            public void onBandGyroscopeChanged(BandGyroscopeEvent bandGyroscopeEvent) {
                xGyro = bandGyroscopeEvent.getAngularVelocityX();
                yGyro = bandGyroscopeEvent.getAngularVelocityY();
                zGyro = bandGyroscopeEvent.getAngularVelocityZ();

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtViews[0].setText(String.format(Locale.getDefault(), "%f", xGyro));
                        txtViews[1].setText(String.format(Locale.getDefault(), "%f", yGyro));
                        txtViews[2].setText(String.format(Locale.getDefault(), "%f", zGyro));
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
            bandClient.getSensorManager().registerGyroscopeEventListener(listener, rate);
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
    @Override
    public boolean unregisterListener(BandClient bandClient) {
        try {
            bandClient.getSensorManager().unregisterGyroscopeEventListener(listener);
            return true;
        } catch (BandIOException ex) {
            ex.printStackTrace();
            return false;
        }
    } // end unregisterListener
}
