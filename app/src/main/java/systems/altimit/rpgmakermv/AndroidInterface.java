package systems.altimit.rpgmakermv;

import android.content.Context;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by felixjones on 19/01/2018.
 */
public class AndroidInterface {

    private Player mPlayer;

    public AndroidInterface(Player player) {
        mPlayer = player;
    }

    @JavascriptInterface
    public void toast(String message) {
        Toast.makeText(mPlayer.getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public String mainModuleFilename() {
        return mPlayer.getContext().getFilesDir().getAbsolutePath();
    }

    @JavascriptInterface
    public String pathBasename(String path, String ext) {
        String basename = new Uri.Builder().appendEncodedPath(path).build().getLastPathSegment();
        int extensionIndex = basename.lastIndexOf(ext);
        return extensionIndex > -1 ? basename.substring(0, extensionIndex) : basename;
    }

    @JavascriptInterface
    public String pathDirname(String path) {
        return new Uri.Builder().appendEncodedPath(path).build().getPath();
    }

    @JavascriptInterface
    public String pathJoin(String... paths) {
        Uri.Builder builder = new Uri.Builder();
        for (String path : paths) {
            builder.appendEncodedPath(path);
        }
        return builder.build().toString();
    }

    @JavascriptInterface
    public String fsExistsSync(String path) {
        return new File(path).exists() ? "true" : "false";
    }

    @JavascriptInterface
    public void fsMkdirSync(String path) {
        new File(path).mkdirs();
    }

    @JavascriptInterface
    public void fsWriteFileSync(String file, String data) {
        OutputStreamWriter osw = null;
        try {
            osw = new OutputStreamWriter(new FileOutputStream(new File(file)), "UTF-8");
            osw.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (osw != null) {
                    osw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @JavascriptInterface
    public String fsReadFileSync(String path) {
        StringBuilder text = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(new File(path)));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return text.toString();
    }

    @JavascriptInterface
    public void fsUnlinkSync(String path) {
        new File(path).delete();
    }

}
