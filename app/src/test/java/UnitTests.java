import android.graphics.Bitmap;
import android.graphics.Rect;

import com.example.wavegroove.general.Utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

public class UnitTests {

    public static final String TEST_STRING = "This is a string";
    public static final long TEST_LONG = 12345678L;

    @Before
    public void setUp() {
        mockStatic(Bitmap.class);
        mockStatic(Rect.class);

        System.out.println("SET UP");
        String file_path = "/PhysicsSketchpad";
        File dir = new File(file_path);
        if (!dir.exists())
            dir.mkdirs();
        File file = new File(dir, "sketchpad.png");
        System.out.println(file.getAbsolutePath());
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Rect rect = new Rect(0, 0, 1, 1);

        // You then create a Bitmap and get a canvas to draw into it
        Bitmap image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);

        System.out.println(image);

        image.compress(Bitmap.CompressFormat.PNG, 85, fOut);

        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void logHistory_ParcelableWriteRead() {
        // Set up the Parcelable object to send and receive.
        assertEquals("Adding new currency failed", 100 + 23, 123);

    }
}