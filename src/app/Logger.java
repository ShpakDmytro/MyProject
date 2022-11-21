package app;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

public class Logger {
    LocalDate date = LocalDate.now();
    LocalTime time = LocalTime.now();

    public void log(String message, String status, String app){

        try {
            FileWriter writer = new FileWriter("C:\\projects\\Myproject\\log.txt", true);
            BufferedWriter bufferWriter = new BufferedWriter(writer);
            String readyToWrite = date + " " + time + " [" + app + "]" + " [" + status + "]" + " [" + message + "]" +"\n";
            bufferWriter.write(readyToWrite);
            System.out.println(readyToWrite);
            bufferWriter.close();
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }
}
