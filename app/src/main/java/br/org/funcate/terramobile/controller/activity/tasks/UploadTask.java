package br.org.funcate.terramobile.controller.activity.tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.controller.activity.MainActivity;
import br.org.funcate.terramobile.util.Message;

/**
 * This AsyncTask upload a geopackage from the server
 */
public class UploadTask extends AsyncTask<String, String, Boolean> {
    private String uploadOriginFilePath;

    private static final String LINE_FEED = "\r\n";

    private MainActivity mainActivity;

    private File originFile;

    // creates a unique boundary based on time stamp
    String boundary = "===" + System.currentTimeMillis() + "===";

    public UploadTask(String uploadOriginFilePath, MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.uploadOriginFilePath = uploadOriginFilePath;
    }

    @Override
    protected void onPreExecute() {
        mainActivity.showLoadingDialog(mainActivity.getString(R.string.send_project_upload));
    }

    public void onPostExecute() {
        try {
            mainActivity.getProgressDialog().dismiss();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected Boolean doInBackground(String... urlToUpload) {
        if(android.os.Debug.isDebuggerConnected()) android.os.Debug.waitForDebugger(); // Para debugar é preciso colocar um breakpoint nessa linha

        if (urlToUpload[0].isEmpty()) {
            Log.e("URL missing", "Variable urlToDownload[0] is empty");
            return false;
        }

        if (uploadOriginFilePath.isEmpty()) {
            Log.e("Path missing", "Variable uploadOriginFilePath is empty");
            return false;
        }
        originFile = new File(uploadOriginFilePath);
        try {
            if (!originFile.exists())
            {
                Log.e("Path missing", "Variable uploadOriginFilePath is empty");
                return false;
            }

            URL url = new URL(urlToUpload[0]);
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setUseCaches(false);
            urlConnection.setDoOutput(true); // indicates POST method
            urlConnection.setDoInput(true);
            urlConnection.setReadTimeout(5000);
            urlConnection.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + boundary);
            urlConnection.setRequestProperty("User-Agent", "TerraMobileApp");
            urlConnection.setRequestProperty("Test", "Bonjour");

            OutputStream outputStream = urlConnection.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream), true);

            addFormField("fileName",originFile.getName(), writer, outputStream);

            addFilePart(originFile.getName(), originFile, writer, outputStream);

            finish(writer, urlConnection);

            return true;
        }catch (IOException e) {
            e.printStackTrace();
            return false;
        }catch (IllegalArgumentException e){
            e.printStackTrace();
            return false;
        }
    }
    public void addFilePart(String fieldName, File uploadFile, PrintWriter writer, OutputStream out)
            throws IOException {
        String fileName = uploadFile.getName();
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append(
                "Content-Disposition: form-data; name=\"" + fieldName
                        + "\"; filename=\"" + fileName + "\"")
                .append(LINE_FEED);
        writer.append(
                "Content-Type: "
                        + URLConnection.guessContentTypeFromName(fileName))
                .append(LINE_FEED);
        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        FileInputStream inputStream = new FileInputStream(uploadFile);
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        out.flush();
        inputStream.close();

        writer.append(LINE_FEED);
        writer.flush();
    }

    /**
     * Adds a form field to the request
     *
     * @param name  field name
     * @param value field value
     */
    public void addFormField(String name, String value, PrintWriter writer, OutputStream out) {
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"" + name + "\"")
                .append(LINE_FEED);
        writer.append("Content-Type: text/plain; charset=").append(
                LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
    }

    /**
     * Called when the cancel button of the ProgressDialog is touched
     * @param aBoolean
     */
    protected void onCancelled(Boolean aBoolean) {
        super.onCancelled(aBoolean);
        mainActivity.getProgressDialog().dismiss();
        Message.showSuccessMessage(mainActivity, R.string.success, R.string.download_cancelled);
    }

    private List<String> finish(PrintWriter writer, HttpURLConnection conn) throws IOException {
        List<String> response = new ArrayList<String>();

        writer.append(LINE_FEED).flush();
        writer.append("--" + boundary + "--").append(LINE_FEED);
        writer.close();

        // checks server's status code first
        int status = conn.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                response.add(line);
            }
            reader.close();
            conn.disconnect();

        } else {
            throw new IOException("Server returned non-OK status: " + status);
        }
        onPostExecute();
        return response;
    }

}