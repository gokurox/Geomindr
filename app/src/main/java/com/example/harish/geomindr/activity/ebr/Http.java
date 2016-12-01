package com.example.harish.geomindr.activity.ebr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class Http {

    //Http url connection is used for retrieving the data from the web url
    String read(String httpUrl) throws IOException {
        String httpData = "";
        HttpURLConnection httpURLConnection;
        URL url = new URL(httpUrl);
        httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.connect();
        try (InputStream inputStream = httpURLConnection.getInputStream()) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuffer = new StringBuilder();
            String line;
            //converting the bufferedReader into String
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            httpData = stringBuffer.toString();
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            httpURLConnection.disconnect();
        }
        return httpData;
    }
}