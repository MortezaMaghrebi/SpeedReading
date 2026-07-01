package com.codestoon.speedreading.games.wordcount;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class WordCountGenerator {

    public static class WordCountResult {
        public String fullText;
        public String targetWord;
        public int targetCount;

        public WordCountResult(String fullText, String targetWord, int targetCount) {
            this.fullText = fullText;
            this.targetWord = targetWord;
            this.targetCount = targetCount;
        }
    }

    /**
     * بارگذاری متن از Assets
     */
    private static String loadTextFromAssets(Context context, String language) {
        StringBuilder content = new StringBuilder();
        try {
            String path = "texts/" + language + "/";
            String[] files = context.getAssets().list(path);
            if (files == null || files.length == 0) {
                return language.equals("en") ? "No text found." : "متنی یافت نشد.";
            }

            Random rand = new Random();
            String fileName = files[rand.nextInt(files.length)];
            if (!fileName.endsWith(".txt")) {
                return language.equals("en") ? "No text found." : "متنی یافت نشد.";
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(path + fileName)));
            String line;
            while ((line = reader.readLine()) != null) {
                if(line.contains("عنوان:")||line.contains("Title:")||line.length()<3) continue;
                content.append(line).append("\r\n");
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
            return language.equals("en") ? "Error loading text." : "خطا در بارگذاری متن.";
        }

        return content.toString();
    }

    /**
     * نرمال‌سازی کلمه - حذف نیم‌فاصله و علائم نگارشی
     */
    private static String normalizeWord(String word) {
        if (word == null || word.isEmpty()) {
            return "";
        }
        // حذف نیم‌فاصله (‌)
        String normalized = word.replace("\u200C", "");
        // حذف علائم نگارشی از ابتدا و انتها
        normalized = normalized.replaceAll("^[^\\w\\s]+|[^\\w\\s]+$", "");
        // حذف فاصله‌های اضافی
        normalized = normalized.trim();
        return normalized;
    }

    /**
     * بررسی اینکه کلمه دارای نیم‌فاصله است یا خیر
     */
    private static boolean hasHalfSpace(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        return word.contains("\u200C");
    }

    /**
     * بررسی برابری کلمات با در نظر گرفتن نیم‌فاصله
     */
    private static boolean isWordMatch(String word1, String word2) {
        if (word1 == null || word2 == null) {
            return false;
        }
        // نرمال‌سازی هر دو کلمه و مقایسه
        String normalized1 = normalizeWord(word1);
        String normalized2 = normalizeWord(word2);
        return normalized1.equalsIgnoreCase(normalized2);
    }

    /**
     * انتخاب کلمه هدف از متن - حداقل 4 حرف، حداقل 2 بار تکرار و بدون نیم‌فاصله
     */
    private static String selectTargetWord(String text, String language) {
        String[] words = text.split("[\\s،.،;:!؟]+");
        Map<String, Integer> wordCount = new HashMap<>();

        for (String word : words) {
            // اگر کلمه نیم‌فاصله دارد، رد کن
            if (hasHalfSpace(word)) {
                continue;
            }
            String cleanWord = normalizeWord(word);
            if (cleanWord.length() >= 4) {
                wordCount.put(cleanWord, wordCount.getOrDefault(cleanWord, 0) + 1);
            }
        }

        // فقط کلماتی که حداقل 5 بار تکرار شده باشند
        List<String> repeatWords = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            if (entry.getValue() >= 5) {
                repeatWords.add(entry.getKey());
            }
        }

        if (!(repeatWords.isEmpty())) {
            return repeatWords.get(new Random().nextInt(repeatWords.size()));
        }

        // فقط کلماتی که حداقل 4 بار تکرار شده باشند
        repeatWords = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            if (entry.getValue() >= 4) {
                repeatWords.add(entry.getKey());
            }
        }

        if (!(repeatWords.isEmpty())) {
            return repeatWords.get(new Random().nextInt(repeatWords.size()));
        }

        // فقط کلماتی که حداقل 3 بار تکرار شده باشند
        repeatWords = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            if (entry.getValue() >= 3) {
                repeatWords.add(entry.getKey());
            }
        }

        if (!(repeatWords.isEmpty())) {
            return repeatWords.get(new Random().nextInt(repeatWords.size()));
        }

        // فقط کلماتی که حداقل 2 بار تکرار شده باشند
        repeatWords = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            if (entry.getValue() >= 2) {
                repeatWords.add(entry.getKey());
            }
        }

        if (!(repeatWords.isEmpty())) {
            return repeatWords.get(new Random().nextInt(repeatWords.size()));
        }

        // اگر کلمه تکراری 4 حرفی پیدا نشد، کلمه 4 حرفی با بیشترین تکرار را انتخاب کن
        String bestWord = "";
        int bestCount = 0;
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            if (entry.getValue() > bestCount) {
                bestCount = entry.getValue();
                bestWord = entry.getKey();
            }
        }

        if (!(bestWord.isEmpty())) {
            return bestWord;
        }

        // در نهایت اگر هیچ کلمه 4 حرفی پیدا نشد
        if (!(words.length == 0)) {
            for (String word : words) {
                if (hasHalfSpace(word)) {
                    continue;
                }
                String cleanWord = normalizeWord(word);
                if (cleanWord.length() >= 4) {
                    return cleanWord;
                }
            }
            // اگر هیچ کلمه 4 حرفی بدون نیم‌فاصله پیدا نشد
            for (String word : words) {
                if (hasHalfSpace(word)) {
                    continue;
                }
                String cleanWord = normalizeWord(word);
                if (!(cleanWord.isEmpty())) {
                    return cleanWord;
                }
            }
            return language.equals("en") ? "word" : "کلمه";
        }

        return language.equals("en") ? "word" : "کلمه";
    }

    /**
     * پیدا کردن موقعیت آخرین نقطه در متن
     */
    private static int findLastSentenceEnd(String text, int startIndex, int maxLength) {
        if (text == null || text.isEmpty()) {
            return -1;
        }

        int searchEnd = Math.min(maxLength, text.length());
        String subText = text.substring(0, searchEnd);

        int lastDot = subText.lastIndexOf('.');
        int lastQuestion = subText.lastIndexOf('?');
        int lastExclamation = subText.lastIndexOf('!');

        int lastSentenceEnd = Math.max(lastDot, Math.max(lastQuestion, lastExclamation));

        if (lastSentenceEnd < 0) {
            lastSentenceEnd = subText.lastIndexOf('۔');
        }

        if (lastSentenceEnd < 0) {
            int lastComma = subText.lastIndexOf('،');
            int lastSemicolon = subText.lastIndexOf('؛');
            lastSentenceEnd = Math.max(lastComma, lastSemicolon);
        }

        if (lastSentenceEnd < 0) {
            return maxLength;
        }

        if (lastSentenceEnd + 1 < text.length() && text.charAt(lastSentenceEnd + 1) == ' ') {
            return lastSentenceEnd + 2;
        }

        return lastSentenceEnd + 1;
    }

    /**
     * تولید محتوای بازی - فقط متن برمی‌گرداند (بدون اضافه کردن کلمه)
     */
    public static WordCountResult generateWordCountResult(Context context, String language, int difficulty) {
        // بارگذاری متن
        String text = loadTextFromAssets(context, language);
        if (text.isEmpty()) {
            text = language.equals("en") ? "Sample text for word counting." : "متن نمونه برای شمارش کلمه.";
        }

        // محدود کردن طول متن بر اساس سطح سختی (افزایش یافته)
        String[] allWords = text.split(" ");
        int wordLimit;
        switch (difficulty) {
            case 1: wordLimit = 50; break;   // آسان - افزایش یافته
            case 2: wordLimit = 100; break;  // متوسط - افزایش یافته
            case 3: wordLimit = 150; break;  // سخت - افزایش یافته
            case 4: wordLimit = 250; break;  // خیلی سخت - افزایش یافته
            default: wordLimit = 50; break;
        }

        // محدود کردن متن به تعداد کلمات مشخص
        StringBuilder limitedText = new StringBuilder();
        int limit = Math.min(wordLimit, allWords.length);
        int charCount = 0;

        for (int i = 0; i < limit; i++) {
            String word = allWords[i];
            limitedText.append(word).append(" ");
            charCount += word.length() + 1;
        }

        String tempText = limitedText.toString().trim();

        // پیدا کردن آخرین جمله کامل (با نقطه)
        int sentenceEnd = findLastSentenceEnd(tempText, 0, tempText.length());

        if (sentenceEnd > 0 && sentenceEnd < tempText.length()) {
            text = tempText.substring(0, sentenceEnd).trim();
        } else {
            text = tempText;
        }

        if (text.length() < 10) {
            text = tempText;
        }

        // انتخاب کلمه هدف (حداقل 4 حرف و ترجیحاً تکراری و بدون نیم‌فاصله)
        String targetWord = selectTargetWord(text, language);

        // شمارش تکرار کلمه هدف با در نظر گرفتن نیم‌فاصله
        String[] words = text.split("[\\s،.،;:!؟]+");
        int targetCount = 0;
        for (String word : words) {
            if (isWordMatch(word, targetWord)) {
                targetCount++;
            }
        }

        return new WordCountResult(text, targetWord, targetCount);
    }

    /**
     * تولید متن هایلایت شده برای نمایش در TextView - زیرخط دار
     */
    public static String getHighlightedText(String fullText, String targetWord) {
        if (fullText == null || fullText.isEmpty()) {
            return "";
        }

        // 1. جایگزینی \n با <br/> (برای نمایش در HTML)
        String textWithBr = fullText.replace("\n", "<br/>");

        // 2. تقسیم به کلمات
        String[] words = textWithBr.split(" ");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            // اگر کلمه خالی است، ادامه بده
            if (word.isEmpty()) {
                continue;
            }

            // اگر برچسب <br/> است، آن را حفظ کن
            if (word.equals("<br/>")) {
                result.append("<br/>");
                continue;
            }

            // اگر کلمه حاوی <br/> است (مثلاً "کلمه<br/>")
            if (word.contains("<br/>")) {
                String[] parts = word.split("<br/>");
                for (int i = 0; i < parts.length; i++) {
                    String part = parts[i];
                    if (!part.isEmpty()) {
                        String cleanWord = normalizeWord(part);
                        if (cleanWord.equalsIgnoreCase(normalizeWord(targetWord))) {
                            result.append("<u><font color='#aa44ff'>").append(part).append("</font></u>");
                        } else {
                            result.append(part);
                        }
                    }
                    if (i < parts.length - 1) {
                        result.append("<br/>");
                    }
                }
                result.append(" ");
                continue;
            }

            // پردازش کلمه عادی
            String cleanWord = normalizeWord(word);
            if (cleanWord.equalsIgnoreCase(normalizeWord(targetWord))) {
                result.append("<u><font color='#aa44ff'>").append(word).append("</font></u> ");
            } else {
                result.append(word).append(" ");
            }
        }

        return result.toString();
    }
}