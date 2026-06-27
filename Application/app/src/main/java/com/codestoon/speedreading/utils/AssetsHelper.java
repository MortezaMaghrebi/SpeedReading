package com.codestoon.speedreading.utils;

import android.content.Context;

import com.codestoon.speedreading.models.VideoModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AssetsHelper {

    // ========== بخش ویدیوها ==========

    public static List<String> getVideoFolders(Context context) {
        List<String> folders = new ArrayList<>();
        try {
            String[] files = context.getAssets().list("videos");
            if (files != null) {
                for (String file : files) {
                    String[] subFiles = context.getAssets().list("videos/" + file);
                    if (subFiles != null && subFiles.length > 0) {
                        folders.add(file);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return folders;
    }

    public static List<VideoModel> getVideosInFolder(Context context, String folder) {
        List<VideoModel> videos = new ArrayList<>();
        try {
            String path = "videos/" + folder;
            String[] files = context.getAssets().list(path);
            if (files != null) {
                for (String file : files) {
                    if (file.endsWith(".mp4") || file.endsWith(".3gp") || file.endsWith(".mov")) {
                        String name = file.replace(".mp4", "")
                                .replace(".3gp", "")
                                .replace(".mov", "")
                                .replace("_", " ");

                        // بررسی وجود thumbnail
                        String baseName = file.substring(0, file.lastIndexOf('.'));
                        String thumbPath = "videos/" + folder + "/" + baseName + "_thumb.jpg";
                        String thumbPathPng = "videos/" + folder + "/" + baseName + "_thumb.png";

                        boolean hasThumb = false;
                        String actualThumbPath = null;

                        // بررسی JPG
                        try {
                            context.getAssets().open(thumbPath);
                            hasThumb = true;
                            actualThumbPath = thumbPath;
                        } catch (IOException e) {
                            // JPG وجود ندارد
                        }

                        // اگر JPG نبود، PNG را بررسی کن
                        if (!hasThumb) {
                            try {
                                context.getAssets().open(thumbPathPng);
                                hasThumb = true;
                                actualThumbPath = thumbPathPng;
                            } catch (IOException e) {
                                // PNG هم وجود ندارد
                            }
                        }

                        // اگر thumbnail وجود داشت، از آن استفاده کن
                        if (hasThumb) {
                            videos.add(new VideoModel(name, file, actualThumbPath));
                        } else {
                            videos.add(new VideoModel(name, file, null));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return videos;
    }

    // ========== بخش متون ==========

    public static List<String> getTextFiles(Context context, String lang) {
        List<String> texts = new ArrayList<>();
        try {
            String path = "texts/" + lang;
            String[] files = context.getAssets().list(path);
            if (files != null) {
                for (String file : files) {
                    if (file.endsWith(".txt")) {
                        texts.add(file);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return texts;
    }

    public static String getTextContent(Context context, String lang, String fileName) {
        StringBuilder content = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("texts/" + lang + "/" + fileName)));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    public static String getRandomText(Context context, String lang) {
        List<String> textFiles = getTextFiles(context, lang);
        if (textFiles.isEmpty()) {
            return lang.equals("fa") ?
                    "هیچ فایل متنی در پوشه texts/fa یافت نشد" :
                    "No text files found in texts/en folder";
        }

        int randomIndex = (int) (Math.random() * textFiles.size());
        String fileName = textFiles.get(randomIndex);
        return getTextContent(context, lang, fileName);
    }

    public static int getTextCount(Context context, String lang) {
        return getTextFiles(context, lang).size();
    }
}