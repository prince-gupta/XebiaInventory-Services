package com.xebia.common;

import com.xebia.entities.EventMail;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Pgupta on 26-07-2016.
 */
@Component
public class Utility {


    @Autowired
    ResourceLoader resourceLoader;

    static ApplicationProperties property;

    static final Map<String,Object> appCache = new HashMap<>();

    @Autowired
    Utility(ApplicationProperties property){
        this.property = property;
    }

    static Logger log = Logger.getLogger(Utility.class);

    public static boolean isExpired(Date inputDate){
        LocalDate toCompareDate = inputDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate todayDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return toCompareDate.isBefore(todayDate);
    }

    public static String encode(String string){
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(string.getBytes(), 0, string.length());
            String hashedPass = new BigInteger(1, messageDigest.digest()).toString(16);
            if (hashedPass.length() < 32) {
                hashedPass = "0" + hashedPass;
            }
            return hashedPass;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String parseDateToExcelDate(Date date){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/mm/yyyy");
        return simpleDateFormat.format(date);
    }

    public static void copy(String sourceFilePath, String destinationFilePath) throws IOException {
        InputStream resStreamIn = Utility.class.getClassLoader().getResourceAsStream(sourceFilePath);
        File resDestFile = new File(destinationFilePath);
        try {
            OutputStream resStreamOut = new FileOutputStream(resDestFile);
            int readBytes;
            byte[] buffer = new byte[resStreamIn.available()];
            while ((readBytes = resStreamIn.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
            resStreamOut.close();

        } catch (Exception ex) {
            log.error("An error occured while copying a file : " + sourceFilePath + " to " + destinationFilePath, ex);
        }
    }

    public static String getFullTempFilePath(String fileName) {
        return property.getTempFilePath() + Constants.FWD_SLASH
                + fileName;
    }

    public static Object get(String key){
        return appCache.get(key);
    }

    public static void put(String key, Object value){
        appCache.put(key, value);
    }

    public static EventMail createEventMailObject(BigInteger userId, String event, String type, BigInteger empId) {
        EventMail eventMail = new EventMail();
        eventMail.setUser(userId);
        eventMail.setEvent(event);
        eventMail.setType(type);
        eventMail.setRefId(empId);
        return eventMail;
    }
}
