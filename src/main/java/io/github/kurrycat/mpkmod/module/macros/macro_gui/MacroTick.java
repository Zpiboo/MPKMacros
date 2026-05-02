package io.github.kurrycat.mpkmod.module.macros.macro_gui;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.InputConstants;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Keyboard;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.gui.Theme;
import io.github.kurrycat.mpkmod.gui.components.*;
import io.github.kurrycat.mpkmod.gui.interfaces.MouseInputListener;
import io.github.kurrycat.mpkmod.module.macros.Macro;
import io.github.kurrycat.mpkmod.module.macros.util.LinkedList;
import io.github.kurrycat.mpkmod.util.ItrUtil;
import io.github.kurrycat.mpkmod.util.MathUtil;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;
import io.github.kurrycat.mpkmod.util.input.TickInput;

import java.util.ArrayList;
import java.util.Objects;

public class MacroTick extends ScrollableListItem<MacroTick> {
    public Macro.Node tick;
    public LinkedList<MacroTick>.Node node;
    protected boolean collapsed = true;
    private static final int COLLAPSED_HEIGHT = 24;
    private static final int FULL_HEIGHT = 44;
    private int collapseAnim = 0;
    private static final int COLLAPSE_ANIM_FRAMES = 6;

    private final ArrayList<Component> editButtons = new ArrayList<>();
    private Button delete = null;

    public MacroTick(MacroTickList parent, Macro.Node tick) {
        super(parent);
        this.tick = tick;

        createComponents();
    }

    private void createComponents() {
        if (tick == null) return;
        if (tick.item.tickInput == null) createJumpComponents();
        else createNormalComponents();
        createEditComponents();
    }

    private void createJumpComponents() {

    }

    private void createNormalComponents() {
        Div contentDiv = new Div(new Vector2D(1, 0), new Vector2D(-2, COLLAPSED_HEIGHT - 2));
        contentDiv.borderColor = Theme.lightEdge;
        addChild(contentDiv, PERCENT.NONE, Anchor.CENTER_LEFT);

        Div buttonDiv = new Div(new Vector2D(0, 0), new Vector2D(0.6D, 1));
        contentDiv.addChild(buttonDiv, PERCENT.SIZE, Anchor.CENTER_LEFT);
        String[] keys = new String[]{"W", "A", "S", "D", "P", "N", "J"};
        for (int i = 0; i < keys.length; i++) {
            int finalI = i;
            Button b = new Button(keys[i],
                    new Vector2D(1 / 7D * i, 0),
                    new Vector2D(1 / 7D, 1)
            );
            boolean isDown = tick.item.tickInput.get(1 << finalI);
            b.normalColor = isDown ? Theme.lightBackground : Theme.darkBackground;
            b.textColor = isDown ? Theme.darkText : Theme.defaultText;
            b.pressedColor = isDown ? Theme.darkBackground : Theme.lightBackground;
            b.pressedTextColor = isDown ? Theme.defaultText : Theme.darkText;

            b.hoverColor = b.normalColor;
            b.setButtonCallback(mouseButton -> {
                invertButton(1 << finalI);
                boolean down = tick.item.tickInput.get(1 << finalI);
                b.normalColor = down ? Theme.lightBackground : Theme.darkBackground;
                b.textColor = down ? Theme.darkText : Theme.defaultText;
                b.pressedColor = down ? Theme.darkBackground : Theme.lightBackground;
                b.pressedTextColor = down ? Theme.defaultText : Theme.darkText;

                b.hoverColor = b.normalColor;
            });
            buttonDiv.addChild(b, PERCENT.ALL, Anchor.CENTER_LEFT);
        }

        Div inputDiv = new Div(new Vector2D(0, 0), new Vector2D(0.4D, 1));
        contentDiv.addChild(inputDiv, PERCENT.SIZE, Anchor.CENTER_RIGHT);

        InputField yaw = new InputField(String.valueOf(tick.item.tickInput.getYaw()),
                new Vector2D(0, 0),
                1 / 3D);
        yaw.setFilter("[-0-9.]");
        yaw.setOnContentChange(content -> {
            Float angle = MathUtil.parseFloat(content.getContent(), null);
            yaw.edgeColor = angle == null ? Theme.warnText : Theme.lightEdge;

            TickInput in = tick.item.tickInput;
            tick.item.tickInput = new TickInput(in.getKeyInputs(),
                    in.getL(), in.getR(),
                    angle == null ? in.getYaw() : angle, in.getPitch(), in.getCount());
        });
        inputDiv.addChild(yaw, PERCENT.X, Anchor.TOP_LEFT);
        InputField pitch = new InputField(String.valueOf(tick.item.tickInput.getPitch()),
                new Vector2D(0, 0),
                1 / 3D);
        pitch.setFilter("[-0-9.]");
        pitch.setOnContentChange(content -> {
            Float angle = MathUtil.parseFloat(content.getContent(), null);
            pitch.edgeColor = angle == null ? Theme.warnText : Theme.lightEdge;

            TickInput in = tick.item.tickInput;
            tick.item.tickInput = new TickInput(in.getKeyInputs(),
                    in.getL(), in.getR(),
                    in.getYaw(), angle == null ? in.getPitch() : angle, in.getCount());
        });
        inputDiv.addChild(pitch, PERCENT.X, Anchor.BOTTOM_LEFT);

        InputField L = new InputField(String.valueOf(tick.item.tickInput.getL()),
                new Vector2D(1 / 3D, 0),
                1 / 3D);
        L.setFilter("[0-9]");
        L.setOnContentChange(content -> {
            Integer count = MathUtil.parseInt(content.getContent(), null);
            L.edgeColor = count == null ? Theme.warnText : Theme.lightEdge;

            TickInput in = tick.item.tickInput;
            tick.item.tickInput = new TickInput(in.getKeyInputs(),
                    count == null ? in.getL() : count, in.getR(),
                    in.getYaw(), in.getPitch(), in.getCount());
        });
        inputDiv.addChild(L, PERCENT.X, Anchor.TOP_LEFT);

        InputField R = new InputField(String.valueOf(tick.item.tickInput.getR()),
                new Vector2D(1 / 3D, 0),
                1 / 3D);
        R.setFilter("[0-9]");
        R.setOnContentChange(content -> {
            Integer count = MathUtil.parseInt(content.getContent(), null);
            R.edgeColor = count == null ? Theme.warnText : Theme.lightEdge;

            TickInput in = tick.item.tickInput;
            tick.item.tickInput = new TickInput(in.getKeyInputs(),
                    in.getL(), count == null ? in.getR() : count,
                    in.getYaw(), in.getPitch(), in.getCount());
        });
        inputDiv.addChild(R, PERCENT.X, Anchor.BOTTOM_LEFT);

        InputField countField = new InputField(String.valueOf(tick.item.tickInput.getCount()),
                new Vector2D(2 / 3D, 0),
                1 / 3D);
        countField.setFilter("[0-9]");
        countField.setOnContentChange(content -> {
            Integer count = MathUtil.parseInt(content.getContent(), null);
            if (Objects.equals(count, 0)) count = null;
            countField.edgeColor = count == null ? Theme.warnText : Theme.lightEdge;

            TickInput in = tick.item.tickInput;
            tick.item.tickInput = new TickInput(in.getKeyInputs(),
                    in.getL(), in.getR(),
                    in.getYaw(), in.getPitch(), count == null ? in.getCount() : count);
        });
        inputDiv.addChild(countField, PERCENT.X, Anchor.CENTER_LEFT);
    }

    private void createEditComponents() {
        MacroTickList parent = (MacroTickList) this.parent;
        Button addTop = new Button("+",
                new Vector2D(1 / 4D, -FULL_HEIGHT / 2D),
                new Vector2D(1 / 6D, 10),
                mouseButton -> {
                    Macro.Node n = tick.addBefore(new Macro.Tick());
                    MacroTick t = new MacroTick(parent, n);
                    LinkedList<MacroTick>.Node m = node.addBefore(t);
                    t.setNode(m);
                });
        passPositionTo(addTop, PERCENT.X, Anchor.TOP_CENTER, Anchor.CENTER_LEFT);
        editButtons.add(addTop);
        Button addBottom = new Button("+",
                new Vector2D(1 / 4D, FULL_HEIGHT / 2D),
                new Vector2D(1 / 6D, 10),
                mouseButton -> {
                    Macro.Node n = tick.addAfter(new Macro.Tick());
                    MacroTick t = new MacroTick(parent, n);
                    LinkedList<MacroTick>.Node m = node.addAfter(t);
                    t.setNode(m);
                });
        passPositionTo(addBottom, PERCENT.X, Anchor.BOTTOM_CENTER, Anchor.CENTER_LEFT);
        editButtons.add(addBottom);

        Button jumpTop = new Button("J",
                new Vector2D(1 / 2D, -FULL_HEIGHT / 2D),
                new Vector2D(1 / 6D, 10),
                mouseButton -> {
                    Macro.Node n = tick.addBefore(Macro.Tick.Jump());
                    MacroTick t = new MacroTick(parent, n);
                    LinkedList<MacroTick>.Node m = node.addBefore(t);
                    t.setNode(m);
                });
        passPositionTo(jumpTop, PERCENT.X, Anchor.TOP_CENTER, Anchor.CENTER_LEFT);
        editButtons.add(jumpTop);
        Button jumpBottom = new Button("J",
                new Vector2D(1 / 2D, FULL_HEIGHT / 2D),
                new Vector2D(1 / 6D, 10),
                mouseButton -> {
                    Macro.Node n = tick.addAfter(Macro.Tick.Jump());
                    MacroTick t = new MacroTick(parent, n);
                    LinkedList<MacroTick>.Node m = node.addAfter(t);
                    t.setNode(m);
                });
        passPositionTo(jumpBottom, PERCENT.X, Anchor.BOTTOM_CENTER, Anchor.CENTER_LEFT);
        editButtons.add(jumpBottom);

        delete = new Button("Delete",
                new Vector2D(3 / 4D, -FULL_HEIGHT / 2D),
                new Vector2D(1 / 6D, 10),
                mouseButton -> {
                    node.remove();
                    tick.remove();
                });
        passPositionTo(delete, PERCENT.X, Anchor.TOP_CENTER, Anchor.CENTER_LEFT);
        editButtons.add(delete);
    }

    private void invertButton(int flag) {
        TickInput in = tick.item.tickInput;
        boolean prev = in.get(flag);
        int inputs = in.getKeyInputs() & ~flag;
        if (!prev) inputs |= flag;
        tick.item.tickInput = new TickInput(inputs,
                in.getL(), in.getR(), in.getYaw(), in.getPitch(), in.getCount());
    }

    public void setNode(LinkedList<MacroTick>.Node node) {
        this.node = node;
    }

    public void render(int index, Vector2D pos, Vector2D size, Vector2D mouse) {
        Renderer2D.enableScissor(getDisplayedPos().getX(), getDisplayedPos().getY(),
                getDisplayedSize().getX(), getDisplayedSize().getY());

        renderComponents(mouse);
        if (!collapsed || collapseAnim != 0)
            for (Component c : editButtons) c.render(mouse);

        Renderer2D.disableScissor();

        if (delete != null)
            delete.enabled = ((MacroTickList) parent).items.getSize() > 1;

        boolean isShiftDown = Keyboard.getPressedButtons().contains(InputConstants.KEY_LSHIFT);
        this.setCollapsed(!mouse.isInRectBetweenPS(pos, size) || !isShiftDown);

        if (collapseAnim > 0) collapseAnim--;
    }

    public int getHeight() {
        return (int) MathUtil.map((double) collapseAnim, 0, COLLAPSE_ANIM_FRAMES,
                collapsed ? COLLAPSED_HEIGHT : FULL_HEIGHT,
                collapsed ? FULL_HEIGHT : COLLAPSED_HEIGHT
        );
    }

    @Override
    public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
        return !collapsed && ItrUtil.orMap(
                ItrUtil.getAllOfType(MouseInputListener.class, editButtons),
                e -> e.handleMouseInput(state, mousePos, button)
        ) || super.handleMouseInput(state, mousePos, button);
    }

    private void setCollapsed(boolean collapsed) {
        if (collapsed == this.collapsed) return;
        collapseAnim = (int) MathUtil.map((double) getHeight(),
                this.collapsed ? COLLAPSED_HEIGHT : FULL_HEIGHT,
                this.collapsed ? FULL_HEIGHT : COLLAPSED_HEIGHT,
                COLLAPSE_ANIM_FRAMES, 0
        );
        this.collapsed = collapsed;
    }

    public static class Empty extends MacroTick {
        public Empty(MacroTickList parent) {
            super(parent, null);
            setHeight(1, true);
            addChild(new Button("Click here to create a new macro",
                    new Vector2D(0, 0),
                    new Vector2D(0.8D, 25),
                    mouseButton -> {
                        parent.setMacro(new Macro());
                    }
            ), PERCENT.SIZE_X, Anchor.CENTER);
        }

        @Override
        public void render(int index, Vector2D pos, Vector2D size, Vector2D mouse) {
            renderComponents(mouse);
        }
    }
}

