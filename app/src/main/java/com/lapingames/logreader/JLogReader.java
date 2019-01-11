package com.lapingames.logreader;

import android.os.NetworkOnMainThreadException;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.SynchronousQueue;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

public class JLogReader {
    static {
        System.loadLibrary("native-lib");
    }

    private final SynchronousQueue<String> queue = new SynchronousQueue();

    class SearchWorker extends Thread {
        protected SynchronousQueue<String> queue;
        protected int id;
        SearchCallback callback;

        SearchWorker(SynchronousQueue<String> queue, SearchCallback callback, int id) {
            this.queue = queue;
            this.id = id;
            this.callback = callback;
        }

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String line = queue.take();
                    if (AddSourceBlock(line, line.length())) {
                        callback.onSuccess(line);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private final ArrayList<SearchWorker> workers = new ArrayList<>();
    SearchCallback callback;

    JLogReader(SearchCallback callback) {
        this.callback = callback;
        // Создадим 4 потока для параллелного поиска
        for (int i = 0; i < 4; ++i) {
            SearchWorker worker = new SearchWorker(queue, callback, i);
            workers.add(worker);
            worker.start();
        }
    }

    class NetworkThread extends Thread {
        private String url;
        private SearchCallback callback;

        public NetworkThread(SearchCallback callback) {
            super();
            this.callback = callback;
        }

        public void run() {
            try {
                URL logURL = new URL(this.url);
//                Log.v("JLogReader ", "Open file:" + logURL);
                InputStream inputStream = getInputStream(logURL);

                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
//                Log.v("JLogReader ", "Read file:" + logURL);

                String inputLine;
                int lineCount = 0;
                while ((inputLine = in.readLine()) != null && !Thread.currentThread().isInterrupted()) {
                    queue.put(inputLine);
                    ++lineCount;
                }
                if (!Thread.currentThread().isInterrupted()) {
                    callback.onFinish(lineCount);
                }
            } catch (IOException e) { // catch all IOExceptions not handled by previous catch blocks
//                System.out.println("General I/O exception: " + e.getMessage());
                e.printStackTrace();
            } catch (NetworkOnMainThreadException e) {
//                System.out.println("Network Exception: " + e.getMessage());
                e.printStackTrace();

            } catch (Exception e) {
//                System.out.println("Exception: " + e.getMessage());
                e.printStackTrace();
            }

        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    NetworkThread loader;

    public void LoadFile(String url) {
        loader = new NetworkThread(callback);
        loader.setUrl(url);
        loader.start();
    }

    public InputStream getInputStream(URL url) throws Exception {

        switch (url.getProtocol()) {
            case "http": {
                return url.openConnection().getInputStream();
            }
            case "https": {
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
                return urlConnection.getInputStream();
            }
            case "file": {
                return new FileInputStream(url.getFile());
            }
            default:
                throw new Exception("Protocol " + url.getProtocol() + " is not supported yet.");
        }
    }

    //JNI
    public native boolean SetFilter(String pattern, int pattern_size);

    public native boolean AddSourceBlock(String block, int block_size);
}
