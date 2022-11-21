package app;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {

    public void log(String message){

        try {
            FileWriter writer = new FileWriter("D:\\projects\\Myprodject\\log.txt", true);
            BufferedWriter bufferWriter = new BufferedWriter(writer);
            bufferWriter.write(message+ "\n");
            bufferWriter.close();
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }
}
