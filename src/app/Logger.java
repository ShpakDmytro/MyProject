package app;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

public class Logger {

    public void log(String message, String status){

        try {
            FileWriter writer = new FileWriter("D:\\projects\\Myprodject\\log.txt", true);
            BufferedWriter bufferWriter = new BufferedWriter(writer);
            String readyToWrite = LocalDate.now() + " " + LocalTime.now() + " [ app ]" + " [" + status + "] " + message +"\n";
            bufferWriter.write(readyToWrite);
            bufferWriter.close();
        }
        catch (IOException e) {
            System.err.println(e);
        }
    }
}
