/*
 * Copyright 2015, Tanmay Parikh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tanmay.blip.utils;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class SpeechSynthesizer implements TextToSpeech.OnInitListener {

    private static SpeechSynthesizer ourInstance;
    private TextToSpeech tts;
    private boolean initialized = false;

    private SpeechSynthesizer(Context context) {
        tts = new TextToSpeech(context, this);
    }

    public static SpeechSynthesizer getInstance() {
        return ourInstance;
    }

    public static void create(Context context) {
        ourInstance = new SpeechSynthesizer(context);
    }

    public void convertToSpeechFlush(String textToSpeak) {
        if (initialized) {
            tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public void convertToSpeechQueue(String textToSpeak) {
        if (initialized) {
            tts.speak(textToSpeak, TextToSpeech.QUEUE_ADD, null);
        }
    }

    public void stopSpeaking() {
        if (initialized) {
            tts.stop();
        }
    }

    public void cleanup() {
        if (initialized) {
            tts.stop();
            tts.shutdown();
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            initialized = true;
            tts.setLanguage(Locale.getDefault());
        }
    }
}
