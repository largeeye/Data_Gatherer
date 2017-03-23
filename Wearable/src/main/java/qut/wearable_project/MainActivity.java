package qut.wearable_project;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.BandPendingResult;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.sensors.SampleRate;
import com.microsoft.band.tiles.BandIcon;
import com.microsoft.band.tiles.BandTile;
import com.microsoft.band.tiles.pages.FlowPanelOrientation;
import com.microsoft.band.tiles.pages.HorizontalAlignment;
import com.microsoft.band.tiles.pages.Margins;
import com.microsoft.band.tiles.pages.PageData;
import com.microsoft.band.tiles.pages.PageLayout;
import com.microsoft.band.tiles.pages.PageRect;
import com.microsoft.band.tiles.pages.ScrollFlowPanel;
import com.microsoft.band.tiles.pages.VerticalAlignment;
import com.microsoft.band.tiles.pages.WrappedTextBlock;
import com.microsoft.band.tiles.pages.WrappedTextBlockData;
import com.microsoft.band.tiles.pages.WrappedTextBlockFont;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;




/**
 * @author James Galloway
 *
 * MainActivity class for Wearable Project.  Contains functions responsible for the initial
 * setup of the application, including the initial contact with the Band as well as event
 * listeners for all screen elements.
 *
 * TODO App name
 * TODO Persistence between values when app is run multiple times
 * TODO Load screen maybe
 */
public class MainActivity extends AppCompatActivity {
    private ProjectClient projectClient;
    private Toast statusTst;
    private final TextView[] accValTxt = new TextView[3];
    private final TextView[] gyroValTxt = new TextView[3];
    private TextView showSavedTxt;

    @SuppressLint("ShowToast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusTst = Toast.makeText(MainActivity.this, null, Toast.LENGTH_LONG);

        accValTxt[0] = (TextView) findViewById(R.id.xAccVal);
        accValTxt[1] = (TextView) findViewById(R.id.yAccVal);
        accValTxt[2] = (TextView) findViewById(R.id.zAccVal);

        gyroValTxt[0] = (TextView) findViewById(R.id.xGyroVal);
        gyroValTxt[1] = (TextView) findViewById(R.id.yGyroVal);
        gyroValTxt[2] = (TextView) findViewById(R.id.zGyroVal);

        saveInit();
        showSavedTxt = (TextView) findViewById(R.id.showSavedTxt);
        setEventListeners();
    }

    /**
     * Set event listeners for each of the elements on the activity.
     */
    private void setEventListeners() {
        /* Install Button */
        Button installBtn = (Button) findViewById(R.id.installBtn);
        installBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InstallAsync installation = new InstallAsync();
                installation.execute();
            }
        });

        /* Uninstall Button */
        Button uninstallBtn = (Button) findViewById(R.id.uninstallBtn);
        uninstallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String response;
                if (projectClient != null) {
                    if (projectClient.removeTile()) {
                        response = "Wearable Project tile has been successfully removed.";
                        findViewById(R.id.accSwitch).setEnabled(false);
                    } else {
                        response = "Could not remove Wearable Project tile.";
                        projectClient.sendDialog("Uninstall", response);
                    }
                    statusTst.setText(response);
                    statusTst.show();
                } else {
                    response = "Not connected to Band.";
                    statusTst.setText(response);
                    statusTst.show();
                }
            }
        });

        /* Accelerometer Switch */
        Switch accSwitch = (Switch) findViewById(R.id.accSwitch);
        accSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ProjectAccelerometer projectAcc = projectClient.getProjectAcc();
                if (isChecked) {
                    projectAcc.registerListener(projectClient.getBandClient(), SampleRate.MS128);
                } else {
                    projectAcc.unregisterListener(projectClient.getBandClient());
                    for (TextView val : accValTxt) {
                        val.setText(R.string.noData);
                    }
                }
            }
        });

        /* Gyroscope Switch */
        Switch gyroSwitch = (Switch) findViewById(R.id.gyroSwitch);
        gyroSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ProjectGyroscope projectGyro = projectClient.getProjectGyro();
                if (isChecked) {
                    projectGyro.registerListener(projectClient.getBandClient(), SampleRate.MS128);
                } else {
                    projectGyro.unregisterListener(projectClient.getBandClient());
                    for (TextView val : gyroValTxt) {
                        val.setText(R.string.noData);
                    }
                }
            }
        });

        /* Show Saved Data */
        Button showSavedBtn = (Button) findViewById(R.id.showSavedBtn);
        showSavedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String filePath = getFilesDir().toString() + "/acc_data";
                                String str = getStrFromFile(filePath);

                                showSavedTxt.setText(str);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                                statusTst.setText("Could not find saved data.");
                                statusTst.show();
                            }
                        }
                    });
            }
        });

        //Send Email Button
        Button send = (Button) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //todo
                try {
                    //stuff
                }
                catch (Exception e){
                    Log.e("SendMail",e.getMessage(),e);
                }
            }

        });
    } // end setEventListeners

    enum TileLayoutIndex {
        MessagesLayout
    }
    enum TileMessagesPageElementId {
        Message
    }

    /**
     * @author James Galloway
     * Private class with functions required to install the Band application.
     */
    private class InstallAsync extends AsyncTask<Void, Void, Boolean> {
        private String response;
        BandClient bandClient;
        UUID tileId;
        UUID page1Id;

        /**
         * Installs the Band application by connecting to the device, creating a tile and
         * configuring the tile's pages.
         *
         * @param params Void.
         * @return True if the installation was successful, otherwise false.
         */
        @Override
        protected Boolean doInBackground(Void...params) {
            return connectToBand() && createTile() && setPageContent();
        } // end doInBackground

        /**
         * Initialise a ProjectClient if the installation was successful.  Set status toast
         * test and show toast.
         *
         * @param installed True if the application was successfully installed, otherwise false.
         */
        @Override
        protected void onPostExecute(Boolean installed) {
            if (installed) {
                projectClient = new ProjectClient(bandClient, tileId, page1Id);
                response = "Installation Successful";
                projectClient.sendDialog(response, "Tap to continue...");

                findViewById(R.id.accSwitch).setEnabled(true);
                findViewById(R.id.gyroSwitch).setEnabled(true);

                ProjectAccelerometer acc = projectClient.getProjectAcc();
                acc.setListener(MainActivity.this, accValTxt);

                ProjectGyroscope gyro = projectClient.getProjectGyro();
                gyro.setListener(MainActivity.this, gyroValTxt);
            }
            statusTst.setText(response);
            statusTst.show();
        } // end onPostExecute

        /**
         * Attempt to connect to Band.
         *
         * @return True if successful, otherwise false.
         */
        private boolean connectToBand() {
            BandInfo[] pairedBands = BandClientManager.getInstance().getPairedBands();

            // Catch error if no paired Band(s) were found
            try {
                bandClient = BandClientManager.getInstance().create(MainActivity.this, pairedBands[0]);
            } catch (IndexOutOfBoundsException ex) {
                ex.printStackTrace();
                response = "Could not find any Microsoft Bands paired with this device.";
                return false;
            }

            // Attempt to connect to Band
            BandPendingResult<ConnectionState> pendingResult = bandClient.connect();
            try {
                ConnectionState state = pendingResult.await();
                if (state == ConnectionState.CONNECTED) {
                    return true;
                } else {
                    response = "Connection failed.";
                    return false;
                }
            } catch (InterruptedException | BandException ex) {
                ex.printStackTrace();
                response = ex.getMessage();
                return false;
            }
        } // end connectToBand
        private boolean reconnectTile() {
            try {
                // get the current set of tiles
                Log.d("test","Reconnecting");
                List<BandTile> tiles =
                        bandClient.getTileManager().getTiles().await();
                return true;
            } catch (BandException e) {
                // handle BandException
                return false;
            } catch (InterruptedException e) {
                // handle InterruptedException
                return false;
            }

        }
        /**
         * Creates the project tile on the Band.
         *
         * @return True if the tile was successfully created, otherwise false.
         */
        private boolean createTile() {
            Bitmap smallIconBitmap = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888);
            BandIcon smallIcon = BandIcon.toBandIcon(smallIconBitmap);

            Bitmap largeIconBitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888);
            BandIcon largeIcon = BandIcon.toBandIcon(largeIconBitmap);

            tileId = UUID.randomUUID();
            PageLayout layout = createLayout();

            BandTile tile = new BandTile.Builder(tileId, "Wearable", largeIcon)
                    .setTileSmallIcon(smallIcon)
                    .setPageLayouts(layout)
                    .build();

                try {
                    if (!bandClient.getTileManager().addTile(MainActivity.this, tile).await()) {
                        response = "Could not add tile to the Band.";
                        return false;
                    }
                    return true;
                } catch (InterruptedException | BandException ex) {
                    ex.printStackTrace();
                    response = ex.getMessage();
                    return false;
                }


        } // end createTile

        /**
         * Creates the layout of the pages on the Band.
         *
         * @return Layout of the pages.
         */
        private PageLayout createLayout() {
            /* Scrollable vertical panel */
            ScrollFlowPanel panel = new ScrollFlowPanel(new PageRect(0, 0, 245, 102));
            panel.setFlowPanelOrientation(FlowPanelOrientation.VERTICAL);
            panel.setHorizontalAlignment(HorizontalAlignment.LEFT);
            panel.setVerticalAlignment(VerticalAlignment.TOP);

            /* Text block */
            WrappedTextBlock textBlock1 =
                    new WrappedTextBlock(new PageRect(0, 0, 245, 102), WrappedTextBlockFont.SMALL);
            textBlock1.setId(TileMessagesPageElementId.Message.ordinal() + 1);
            textBlock1.setMargins(new Margins(15, 0, 15, 0));
            textBlock1.setColor(Color.WHITE);
            textBlock1.setAutoHeightEnabled(true);
            textBlock1.setHorizontalAlignment(HorizontalAlignment.LEFT);
            textBlock1.setVerticalAlignment(VerticalAlignment.TOP);

            panel.addElements(textBlock1);
            return new PageLayout(panel);
        } // end createLayout

        /**
         * Sets the initial content of the pages on the Band.
         *
         * @return True if the page content was set successfully, otherwise false.
         */
        private boolean setPageContent() {
            page1Id = UUID.randomUUID();

            WrappedTextBlockData TBData =
                    new WrappedTextBlockData(TileMessagesPageElementId.Message.ordinal() + 1, "Accelerometer Data");

            PageData data =
                    new PageData(page1Id, TileLayoutIndex.MessagesLayout.ordinal()).update(TBData);

            try {
                bandClient.getTileManager().setPages(tileId, data).await();
                return true;
            } catch (InterruptedException | BandException ex) {
                ex.printStackTrace();
                response = ex.getMessage();
                return false;
            }
        } // end setPageContent
    } // end InstallAsync

    /**
     * @author Lok Sum (Moon) Lo
     * Private method to create local save file for accelerometer data upon startup.
     *
     */
    private void saveInit() {
        String FILENAME = "acc_data";
        String string = "datetime,acc_x,acc_y,acc_z,";

        try {
            FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write(string.getBytes());
            fos.close();
        } catch (Exception e){
            e.printStackTrace();
            statusTst.setText("Can't create save file.");
            statusTst.show();
        }
    } // end saveInit()

    private static String streamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    } // end streamToString

    public static String getStrFromFile (String filePath) throws IOException {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = streamToString(fin);

        fin.close();
        return ret;
    } // end getStrFromFile


}