package io.github.kurrycat.mpkmod.module.macros.macro_gui;

import io.github.kurrycat.mpkmod.gui.Theme;
import io.github.kurrycat.mpkmod.gui.components.*;
import io.github.kurrycat.mpkmod.module.macros.util.FileUtil;
import io.github.kurrycat.mpkmod.module.macros.util.MacroRecorder;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MacroFileList extends ScrollableList<MacroFileList.MacroFile> {
    private final MacroTickList macroTickList;
    private final List<MacroFile> allItems = new ArrayList<>();

    private final InputField inputField;

    public MacroFileList(MacroTickList macroTickList, Vector2D pos, Vector2D size) {
        super();
        setPos(pos);
        setSize(size);
        setTitle("Macro Files");
        this.macroTickList = macroTickList;
        bottomCover.setHeight(29, false);
        Div div = new Div(
                new Vector2D(1 / 4D, 0),
                new Vector2D(85, 20)
        );
        bottomCover.addChild(div, PERCENT.POS_X, Anchor.CENTER, Anchor.CENTER_RIGHT);

        div.addChild(new Button(
                "Reload",
                new Vector2D(1 / 2D, 0),
                new Vector2D(40, 20),
                mouseButton -> reloadFiles()
        ), PERCENT.POS_X, Anchor.BOTTOM_CENTER, Anchor.BOTTOM_LEFT);
        div.addChild(new Button(
                "Record",
                new Vector2D(0, 0),
                new Vector2D(40, 20),
                mouseButton -> {
                    MacroRecorder.startRecording();
                }
        ), PERCENT.POS_X, Anchor.BOTTOM_CENTER, Anchor.BOTTOM_RIGHT);

        inputField = new InputField(
                new Vector2D(2 / 7D, 2),
                1 / 2D
        );
        inputField.setFilter(InputField.FILTER_FILENAME);
        inputField.setOnContentChange(content -> filterFiles(content.getContent()));
        bottomCover.addChild(inputField,
                PERCENT.X, Anchor.BOTTOM_CENTER, Anchor.BOTTOM_LEFT);
        bottomCover.addChild(new TextRectangle(
                new Vector2D(2 / 7D, 3),
                new Vector2D(1 / 2D, 10),
                "Filter",
                null,
                Theme.defaultText
        ), PERCENT.X, Anchor.TOP_CENTER, Anchor.TOP_LEFT);
        reloadFiles();
    }

    public void reloadFiles() {
        File[] files = FileUtil.MACRO_FOLDER.listFiles();
        if (files == null) return;

        List<File> fileList = Arrays.stream(files)
                .sorted(Comparator.comparingLong(File::lastModified).reversed())
                .collect(Collectors.toList());

        allItems.clear();
        for (File file : fileList) {
            if (!file.getName().endsWith(".csv")) continue;
            allItems.add(new MacroFile(this, file));
        }
        filterFiles(inputField.content);
    }

    public void filterFiles(String filter) {
        items.clear();
        for (MacroFile m : allItems)
            if (m.file.getName().toLowerCase().contains(filter.toLowerCase()))
                items.add(m);
    }

    public static class MacroFile extends ScrollableListItem<MacroFile> {
        public File file;
        private final Button deleteButton;

        public MacroFile(MacroFileList parent, File file) {
            super(parent);
            setHeight(20);
            this.file = file;
            String name = file.getName();
            if (name.endsWith(".csv")) name = name.substring(0, name.length() - ".csv".length());
            Button selectButton;
            addChild(selectButton = new Button(
                    name,
                    new Vector2D(0, 0),
                    new Vector2D(-2, -2),
                    mouseButton -> parent.macroTickList.load(file)
            ), PERCENT.NONE, Anchor.CENTER);
            selectButton.addChild(this.deleteButton = new Button(
                    "x",
                    new Vector2D(5, 0),
                    new Vector2D(11, 11),
                    mouseButton -> {
                        // TODO: Some sort of confirmation dialogue?
                        file.delete();
                        parent.reloadFiles();
                    }
            ), PERCENT.NONE, Anchor.CENTER_RIGHT);
        }

        public void render(int index, Vector2D pos, Vector2D size, Vector2D mouse) {
            renderDefaultBorder(pos, size);
            renderComponents(mouse);
            this.deleteButton.render(mouse); // TODO
        }

        @Override // TODO
        public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {

            if (this.deleteButton.handleMouseInput(state, mousePos, button)) return true;

            return super.handleMouseInput(state, mousePos, button);
        }
    }
}
