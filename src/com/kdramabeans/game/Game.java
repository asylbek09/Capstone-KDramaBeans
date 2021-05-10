package com.kdramabeans.game;

import org.apache.commons.lang3.StringUtils;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.kdramabeans.game.Gui.*;

public class Game {
    public static Player player = new Player();
    public static Story story;
    public static DataParser item;
    public static BGM music;

    public static Map<String, String> evidenceMap = new HashMap<>() {{
        put("watch", "evidence 1");
        put("business card", "evidence 2");
        put("rose", "evidence 3");
        put("bitcoin", "evidence 4");
        put("voice recorder", "evidence 5");
    }};

    public static String printStatus() {
        String status = "";
        status += (story.printStory() + "\n" + player.printGrabbedItems() + "\n" + player.printEvidence() + "\n" + story.printItems() + "\n" + story.printOptions());
        return status;
    }

    public static void playGame() {
        try {
            String[] input = StringUtils.split(mainTextField.getText().toLowerCase().trim(), " ", 2);
            System.out.println("THIS IS THE INPUT: " + mainTextField.getText());
            for (String s : input) {
                System.out.println(s);
            }
            final String[] Result = new String[1];
            Map<String, Runnable> allActions = new HashMap<>();

            ArrayList<Runnable> runners = new ArrayList<Runnable>() {{
                add(() -> {
                    if (story.hasItem(input[1]) || player.hasGrabbedItem(input[1])) {
                        Result[0] = item.getItemDescription(input[1]);
                    } else {
                        Result[0] = "You cannot examine that.\n";
                    }
                });
                add(() -> {
                    if (story.hasItem(input[1]) && !player.hasGrabbedItem(input[1])) {
                        if (player.grabItem(input[1])) {
                            story.setOptions(input[1]);
                            Result[0] = "You have grabbed " + input[1];
                        } else {

                            Result[0] = "You have too many items! Try dropping one if you really need to grab " + input[1];
                        }
                    } else {
                        Result[0] = "You cannot grab that.\n";
                    }
                });
                add(() -> {
                    String evidence = evidenceMap.get(input[1]);
                    if (player.hasGrabbedItem(input[1]) && story.hasHidden(evidence)) {
                        player.addEvidence(evidence);
                        player.dropItem(input[1]);
                        Result[0] = "You have used : " + input[1] + ", and you collected : " + evidence;
                    } else {
                        Result[0] = "You don't have this item in your inventory or your item does not work here";
                    }
                });
                add(() -> Result[0] = player.dropItem(input[1]));
                add(() -> {
                    if(story.getOptions().containsKey(input[1])) {
                        story.setCurrentOption(input[1]);
                        story.nextScene(true);
                        Result[0] = "You chose option: " + input[1];
                    } else {
                        Result[0] = "Use: [choose,go,fly,teleport,move] [number]\n";
                    }
                });
            }};
            try {
                InputStream in = Game.class.getResourceAsStream("/validVerbs.csv");
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                final int[] counter = {0};
                reader.lines().forEachOrdered(synonyms -> {
                    String[] verbs = synonyms.split(",");
                    for (String verb : verbs) {
                        allActions.put(verb, runners.get(counter[0]));
                    }
                    counter[0]++;
                });
            } catch (Exception except) {
                System.out.println(except);
            }
            allActions.getOrDefault(input[0], () -> {
            }).run();
            statusArea.setText(Result[0]);

            mainTextArea.setText(printStatus());
            mainTextField.setText("");
        } catch (ArrayIndexOutOfBoundsException exception) {
            statusArea.setText("Error: you didn't enter your move correctly");
        }
    }
    static {
        try {
            story = new Story();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static {
        try {
            music = new BGM();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}