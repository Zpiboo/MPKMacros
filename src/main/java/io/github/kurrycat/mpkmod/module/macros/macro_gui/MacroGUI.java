package io.github.kurrycat.mpkmod.module.macros.macro_gui;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft;
import io.github.kurrycat.mpkmod.gui.ComponentScreen;
import io.github.kurrycat.mpkmod.gui.components.Anchor;
import io.github.kurrycat.mpkmod.gui.components.Button;
import io.github.kurrycat.mpkmod.gui.components.Div;
import io.github.kurrycat.mpkmod.module.macros.Macro;
import io.github.kurrycat.mpkmod.util.Vector2D;

public class MacroGUI extends ComponentScreen {
    private MacroTickList macroTickList;

    public boolean resetOnOpen() {
        return false;
    }

    public boolean shouldCreateKeyBind() {
        return true;
    }

    public void onKeybindPressed() {
        Minecraft.displayGuiScreen(this);
    }

    public Macro getCurrentMacro() {
        return macroTickList.currentMacro;
    }

    public void onGuiInit() {
        super.onGuiInit();

        Div contentDiv = new Div(new Vector2D(10, 10), new Vector2D(-20, -20));
        addChild(contentDiv);
        macroTickList = new MacroTickList(
                new Vector2D(0, 0),
                new Vector2D(0.7D, 1)
        );

        contentDiv.addChild(macroTickList, PERCENT.ALL, Anchor.TOP_LEFT);
        macroTickList.topCover.addChild(
                new Button(
                        "x",
                        new Vector2D(5, 0),
                        new Vector2D(11, 11),
                        mouseButton -> close()
                ),
                PERCENT.NONE, Anchor.CENTER_RIGHT
        );

        Div fileListDiv = new Div(new Vector2D(0, 0), new Vector2D(0.3D, 1));
        contentDiv.addChild(fileListDiv, PERCENT.SIZE, Anchor.TOP_RIGHT);
        fileListDiv.addChild(new MacroFileList(
                macroTickList,
                new Vector2D(0, 0),
                new Vector2D(-10, 1)
        ), PERCENT.SIZE_Y, Anchor.TOP_RIGHT);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        macroTickList.save();
    }
}
