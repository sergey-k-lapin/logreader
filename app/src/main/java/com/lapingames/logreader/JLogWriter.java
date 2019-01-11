package com.lapingames.logreader;

import android.content.Context;
import java.io.PrintWriter;
import java.util.concurrent.LinkedBlockingQueue;

import static android.content.Context.MODE_APPEND;

public class JLogWriter implements Runnable{

    protected LinkedBlockingQueue<String> inputQueue;
    public String FILE_NAME = "result.log";
    protected PrintWriter out;
    protected Context context;

    public JLogWriter(Context context) {

        inputQueue = new LinkedBlockingQueue<>();
        new Thread(this).start();

        this.context = context;
        try {
            out = new PrintWriter(context.openFileOutput(FILE_NAME, MODE_APPEND));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean add(String block){
        try {
            inputQueue.put(block);
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public void run(){
        while(!Thread.currentThread().isInterrupted()){
            try {
                String line = inputQueue.take();
                out.println(line);
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
