package net.senekal.openchrono.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class Hash {
    private static final String TAG = "Hash";

    private Hash() {
    }

    public static boolean doesContentMatch(Context context, String csvFile) {
        return validateAllFiles(context, GetAssetFileHashInfo(context, csvFile));
    }

    private static boolean validateAllFiles(Context context, List<FileInfo> fileInfos) {
        boolean isValid = true;
        String tmpPath = context.getFilesDir().getPath() + "/res/";
        for (FileInfo entry : fileInfos) {
            String fileHash = hashFile("MD5", tmpPath + entry.getFileName());
            if (!entry.getMd5().equals(fileHash)) {
                isValid = false;
                break;
            }
        }
        return isValid;
    }

    private static List<FileInfo> GetAssetFileHashInfo(Context context, String csvFile) {
        byte[] assetFileData = getAssetFileContent(context, csvFile, 10240);
        return ProcessAssetFileHashInfo(assetFileData);
    }

    private static byte[] getAssetFileContent(Context context, String assetFileName, int maxLen) {
        try (InputStream inputStream = context.getAssets().open(assetFileName)) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[maxLen];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            Log.e(TAG, "Error reading asset file: " + assetFileName, e);
            return new byte[0];
        }
    }

    private static String hashFile(String algorithm, String fileName) {
        File file = new File(fileName);
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);

            try (FileInputStream inputStream = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    digest.update(buffer, 0, len);
                }
                byte[] hashBytes = digest.digest();
                StringBuilder hashBuilder = new StringBuilder();
                for (byte b : hashBytes) {
                    hashBuilder.append(String.format("%02x", b));
                }
                return hashBuilder.toString().toUpperCase();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error calculating MD5 hash for file: " + fileName, e);
            return "";
        }
    }

    private static List<FileInfo> ProcessAssetFileHashInfo(byte[] fileContent) {
        List<FileInfo> fileMD5Pairs = new ArrayList<>();

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContent);
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader br = new BufferedReader(inputStreamReader)) {

            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                String fileName = data[0];
                String md5 = data[1].toUpperCase();
                String sha1 = data[2].toUpperCase();
                String size = data[3].toUpperCase();
                fileMD5Pairs.add(new FileInfo(fileName, md5, sha1, size));
            }
        } catch (IOException e) {
            Log.e(TAG, "Error processing asset file hash information", e);
        }

        return fileMD5Pairs;
    }
}
