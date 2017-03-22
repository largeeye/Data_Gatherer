package qut.wearable_project;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandException;

import java.util.UUID;

/**
 * @author James Galloway
 *
 * Contains functions related to the communication of the Band with the user's device.
 */
class ProjectClient {
    private final BandClient bandClient;
    private final ProjectAccelerometer projectAcc;
    private final ProjectGyroscope projectGyro;
    private final UUID tileId;
    private final UUID page1Id;

    ProjectClient(BandClient c, UUID tId, UUID p1Id) {
        bandClient = c;
        tileId = tId;
        page1Id = p1Id;
        projectAcc = new ProjectAccelerometer();
        projectGyro = new ProjectGyroscope();
    }

    /**
     * Removes the project tile from the Band.
     *
     * @return True if the tile was successfully remove, otherwise false
     */
    boolean removeTile() {
        try {
            bandClient.getTileManager().removeTile(tileId).await();
            return true;
        } catch (BandException | InterruptedException ex) {
            ex.printStackTrace();
            return false;
        }
    } // end removeTile

    /**
     * Sends a dialog to the Band.
     *
     * @param title The title of the dialog.
     * @param msg The content of the dialog.
     */
    void sendDialog(String title, String msg) {
        try {
            bandClient.getNotificationManager().showDialog(tileId, title, msg).await();
        } catch (InterruptedException | BandException ex) {
            ex.printStackTrace();
        }
    } // end sendDialog

    BandClient getBandClient() { return bandClient; }

    ProjectAccelerometer getProjectAcc() { return projectAcc; }

    ProjectGyroscope getProjectGyro() { return projectGyro; }
}