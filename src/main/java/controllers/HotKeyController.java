package controllers;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class HotKeyController implements NativeKeyListener {

    private boolean keyPressed = false;
    private boolean editing = false;

    private int activeKeyCode = -1;

    private Label activeShortcutLabel;
    private Button activeShortcutButton, deleteActiveShortcutButton;

    private Runnable onToggle;


    public HotKeyController(Label activeShortcutLabel, Button activeShortcutButton, Button deleteActiveShortcutButton, Runnable onToggle) {

        this.activeShortcutLabel = activeShortcutLabel;
        this.activeShortcutButton = activeShortcutButton;
        this.deleteActiveShortcutButton = deleteActiveShortcutButton;

        this.onToggle = onToggle;

        Platform.runLater(() -> this.deleteActiveShortcutButton.setVisible(false));

        this.deleteActiveShortcutButton.setOnMouseClicked(e -> {
            if(this.activeKeyCode == -1) {
                return;
            }

            this.activeKeyCode = -1;
            Platform.runLater(() -> {
                this.activeShortcutButton.setText("set");
                this.deleteActiveShortcutButton.setVisible(false);
                this.activeShortcutLabel.setOpacity(1);
                this.activeShortcutLabel.setText("None");
            });
        });



        this.activeShortcutButton.setOnMouseClicked(e -> {
            if(!editing) {
                editing = true;
                Platform.runLater(() -> {
                    this.activeShortcutButton.setText("cancel");
                    this.activeShortcutLabel.setText("Press any key...");
                    this.activeShortcutLabel.setOpacity(0.5);
                    this.deleteActiveShortcutButton.setVisible(false);
                });
            }else {
                editing = false;
                Platform.runLater(() -> {
                    this.activeShortcutButton.setText("set");
                    this.activeShortcutLabel.setOpacity(1);

                    if(activeKeyCode == -1) {
                        this.activeShortcutLabel.setText("None");
                    }else {
                        this.activeShortcutLabel.setText(NativeKeyEvent.getKeyText(activeKeyCode));
                        this.deleteActiveShortcutButton.setVisible(true);
                    }
                });
            }
        });


        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
        GlobalScreen.addNativeKeyListener(this);
    }


    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if(!editing && this.activeKeyCode != -1 && e.getKeyCode() == activeKeyCode && onToggle != null) {
            onToggle.run();
        }
        else if(!keyPressed && editing) {
            this.activeKeyCode = e.getKeyCode();
            this.keyPressed = true;
            this.editing = false;

            Platform.runLater(() -> {
                this.activeShortcutLabel.setOpacity(1);
                this.activeShortcutButton.setText("set");
                this.deleteActiveShortcutButton.setVisible(activeKeyCode != -1);

                if(activeKeyCode == -1) {
                    activeShortcutLabel.setText("None");
                }else {
                    activeShortcutLabel.setText(NativeKeyEvent.getKeyText(activeKeyCode));
                }
            });
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        this.keyPressed = false;
    }
}
