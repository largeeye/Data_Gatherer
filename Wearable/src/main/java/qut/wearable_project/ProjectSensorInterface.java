package qut.wearable_project;

import android.app.Activity;
import android.widget.TextView;

import com.microsoft.band.BandClient;
import com.microsoft.band.sensors.SampleRate;

/**
 * @author James Galloway
 *
 * Defines constraints for the use of the Band's sensors.
 */
interface ProjectSensorInterface {

    /**
     * Sets the event listener for the sensor upon any change in data.
     *
     * @param activity The activity containing the TextView elements.
     * @param txtViews Array containing each of the TextView elements that will display the data.
     */
    void setListener(final Activity activity, final TextView[] txtViews);

    /**
     * Registers a given accelerometer event listener with a given sample rate.
     *
     * @param bandClient The BandClient whose event listener is registered.
     * @param rate The sample rate at which to listen.
     * @return True if successfully registered, otherwise false.
     */
    boolean registerListener(BandClient bandClient, SampleRate rate);

    /**
     * Unregisters a given accelerometer event listener.
     *
     * @param bandClient The BandClient whose event listener is unregistered.
     * @return True if successfully unregistered, otherwise false.
     */
    boolean unregisterListener(BandClient bandClient);
}
