import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class HotKeyController implements NativeKeyListener {

    private boolean keyPressed = false;

    private int activeKeyCode = -1;
    private int pressedKeyCode;

    private Label activeShortcutLabel;
    private Button setShortcutButton, cancelShortcutButton;

    private boolean editing = false;

    private Runnable onToggle;

    public HotKeyController(Label activeShortcutLabel, Button setShortcutButton, Button cancelShortcutButton, Runnable onToggle) {

        this.activeShortcutLabel = activeShortcutLabel;
        this.setShortcutButton = setShortcutButton;
        this.cancelShortcutButton = cancelShortcutButton;
        this.onToggle = onToggle;

        this.cancelShortcutButton.setVisible(false);

        this.setShortcutButton.setOnMouseClicked(e -> {
            if(!editing) {
                editing = true;
                Platform.runLater(() -> {
                    this.cancelShortcutButton.setVisible(true);
                    this.setShortcutButton.setText("confirm");
                    this.activeShortcutLabel.setText("Press any key...");
                    this.activeShortcutLabel.setOpacity(0.5);
                });
            }else {
                this.activeKeyCode = pressedKeyCode;
                editing = false;
                Platform.runLater(() -> {
                    this.setShortcutButton.setText("set");
                    this.cancelShortcutButton.setVisible(false);
                    this.activeShortcutLabel.setOpacity(1);
                    activeShortcutLabel.setText(NativeKeyEvent.getKeyText(activeKeyCode));
                });
            }
        });

        this.cancelShortcutButton.setOnMouseClicked(e -> {
            editing = false;
            Platform.runLater(() -> {
                this.cancelShortcutButton.setVisible(false);
                this.setShortcutButton.setText("set");
                this.activeShortcutLabel.setOpacity(1);
                if(activeKeyCode == -1) {
                    activeShortcutLabel.setText("None");
                }else {
                    activeShortcutLabel.setText(NativeKeyEvent.getKeyText(activeKeyCode));
                }

            });
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
        if(!editing && this.activeKeyCode != -1 && e.getKeyCode() == activeKeyCode) {
            onToggle.run();
        }

        if(!keyPressed && editing) {
            pressedKeyCode = e.getKeyCode();
            keyPressed = true;
            Platform.runLater(() -> {
                activeShortcutLabel.setText(NativeKeyEvent.getKeyText(pressedKeyCode));
                this.activeShortcutLabel.setOpacity(1);
            });
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        this.keyPressed = false;
    }
}
