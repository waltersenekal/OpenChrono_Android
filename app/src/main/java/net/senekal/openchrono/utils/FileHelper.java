package net.senekal.openchrono.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class FileHelper {

    private static final String TAG = "FileHelper";


    private FileHelper() {
        // Prevent instantiation
    }


    public static byte[] readFileContentAsBytes(String fileName) {
        File file = new File(fileName);
        return readFileContentAsBytes(file);
    }


    public static byte[] readFileContentAsBytes(File file) {
        byte[] fileData = null;
        try (FileInputStream fis = new FileInputStream(file)) {
            // Create byte array with the size of the file
            fileData = new byte[(int) file.length()];
            // Read file content into the byte array
            fis.read(fileData);
        } catch (IOException e) {
            Log.e(TAG, "readFileContentAsBytes failed:", e);
        }
        return fileData;
    }


    public static String readFileContent(String fileName) {
        File file = new File(fileName);
        return readFileContent(file);
    }


    public static String readFileContent(File file) {
        StringBuilder content = new StringBuilder();

        try (FileReader fileReader = new FileReader(file);
             BufferedReader reader = new BufferedReader(fileReader)) {

            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            content = new StringBuilder();
        }

        return content.toString();
    }


    public static void createDir(@NonNull File dir) throws IOException {
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new IOException("Can't create directory, a file is in the way");
            }
        } else {
            boolean created = dir.mkdirs();
            if (!created && !dir.isDirectory()) {
                throw new IOException("Unable to create directory");
            }
        }
    }


    public static @NonNull String addTrailingSlash(@NonNull String path) {
        if (path.charAt(path.length() - 1) != '/') {
            path += "/";
        }
        return path;
    }


    public static void createDirectory(String path) {
        File directory = new File(path);

        if (!directory.exists()) {
            if (directory.mkdirs()) {
                Log.i(TAG, "Directory created: " + path);
            } else {
                Log.e(TAG, "Failed to create directory: " + path);
            }
        } else {
            Log.i(TAG, "Directory already exists: " + path);
        }
    }


    public static void deleteFolder(File folder) {
        if (folder.isDirectory() && folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!file.delete()) {
                        Log.e(TAG, "Failed to delete file: " + file.getName());
                    }
                }
            }

            if (folder.delete()) {
                Log.i(TAG, "Folder deleted successfully.");
            } else {
                Log.e(TAG, "Failed to delete folder.");
            }
        } else {
            Log.e(TAG, "Folder does not exist or is not a directory.");
        }
    }


    public static void createFile(String fileName, byte[] fileData) throws IOException {
        File file = new File(fileName);

        // Create parent directories if they do not exist
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        // Write the byte array to the file
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(fileData);
            fos.flush(); // Ensure all data is written to the file
            Log.i(TAG, "File created and written successfully: " + fileName);
        } catch (IOException e) {
            Log.e(TAG, "Error writing file: " + fileName, e);
            throw e; // Rethrow the exception for handling by the caller
        }
    }
}

