package org.marco45.polarheartmonitor;

/**
 * Created by sharad on 15-04-2017.
 */


import java.util.TreeMap;

/**
 * Created by arunabhghosal on 4/14/17.
 */

public interface onPictureCapturedListener {

    void onCaptureDone(String pictureUrl, byte[] pictureData);

    /**
     * a callback called when we've done taking pictures from ALL AVAILABLE cameras
     * OR when NO camera was detected on the device
     * @param picturesTaken : a  Map<PictureUrl, PictureData>
     */
    void onDoneCapturingAllPhotos(TreeMap<String, byte[]> picturesTaken);

}