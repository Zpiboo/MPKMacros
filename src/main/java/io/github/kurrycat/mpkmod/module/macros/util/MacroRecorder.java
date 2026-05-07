package io.github.kurrycat.mpkmod.module.macros.util;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.KeyBinding;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Player;
import io.github.kurrycat.mpkmod.module.macros.MPKMacros;
import io.github.kurrycat.mpkmod.module.macros.Macro;
import io.github.kurrycat.mpkmod.util.input.TickInput;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MacroRecorder {

    private static final DateTimeFormatter FILE_SAFE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");

    public static boolean recording = false;
    private static Macro macro = null;
    private static long ticks = 0;

    public static void startRecording() {
        macro = new Macro();
        ticks = 0;
        recording = true;
    }

    public static void endRecording() {
        recording = false;

        macro.setName("Recording " + LocalDateTime.now().format(FILE_SAFE_TIMESTAMP));
        macro.save();

        macro = null;
    }

    // TODO: Can probably just check for input changes in a different event and increment count
    public static void tick() {
        ticks++;
        TickInput tickInput = new TickInput(
                KeyBinding.getByName("key.forward").isKeyDown(),
                KeyBinding.getByName("key.left").isKeyDown(),
                KeyBinding.getByName("key.back").isKeyDown(),
                KeyBinding.getByName("key.right").isKeyDown(),
                KeyBinding.getByName("key.sprint").isKeyDown(),
                KeyBinding.getByName("key.sneak").isKeyDown(),
                KeyBinding.getByName("key.jump").isKeyDown(),
                0,
                0,
                Player.getLatest().getDeltaYaw(),
                Player.getLatest().getDeltaPitch(),
                1
        );
        Macro.Tick tick = new Macro.Tick(tickInput);
        macro.addLast(tick);
    }

}
