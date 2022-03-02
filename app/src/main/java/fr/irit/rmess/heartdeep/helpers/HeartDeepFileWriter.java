/***********************************************************************
 Name............ : HeartDeepFileWriter.java
 Description..... : A helper class for writing to files
 Author.......... : Kevin Jiokeng for IRIT, RMESS Team
 Creation Date... : 31/10/2019
 ************************************************************************/

package fr.irit.rmess.heartdeep.helpers;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A helper class for writing to files
 */
public class HeartDeepFileWriter {
    /**
     * Helper function to save readings to file, in recording mode.
     * The file will be created in the internal directory of the app.
     *
     * @param filename The name of the file to be written
     * @param content  The content to be written
     * @param context  The Android context from which to write
     */
    public static String writeToFile(String filename, String content, Context context) {
        if (context == null || content == null) {
            return null;
        }


        if (filename == null || filename == "") {
            DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
            filename = dateFormat.format(new Date());
        }

        FileOutputStream outputStream;
        File file;

        try {
            file = new File(context.getExternalFilesDir(null), filename);
            outputStream = new FileOutputStream(file);
            outputStream.write(content.getBytes());
            outputStream.close();

            return filename;

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("SAVE-FILE", "Error while saving file " + filename);
        }

        return null;
    }
}
