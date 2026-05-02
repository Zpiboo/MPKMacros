package io.github.kurrycat.mpkmod.module.macros;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Player;
import io.github.kurrycat.mpkmod.module.macros.util.FileUtil;
import io.github.kurrycat.mpkmod.module.macros.util.LinkedList;
import io.github.kurrycat.mpkmod.util.input.TickInput;

import java.io.*;

public class Macro extends LinkedList<Macro.Tick> {
    public String name;
    public File macroFile;

    public Macro() {
        setName(null);
    }

    public void setName(String content) {
        if (content == null || content.isEmpty()) {
            name = getDefaultName();
        } else name = content;
        macroFile = new File(FileUtil.MACRO_FOLDER, name + ".csv");
    }

    protected String getDefaultName() {
        File[] files = FileUtil.MACRO_FOLDER.listFiles();
        int num = files == null ? 1 : files.length + 1;
        return "Macro_" + num;
    }

    public Macro(File file) {
        name = file.getName();
        if (name.endsWith(".csv")) name = name.substring(0, name.length() - ".csv".length());
        macroFile = file;

        load();
    }

    public void load() {
        try (BufferedReader br = new BufferedReader(new FileReader(macroFile))) {
            String header = br.readLine();
            if (header == null) {
                MPKMacros.LOGGER.info("File is empty: " + macroFile.getAbsolutePath());
                return;
            }
            TickHeaderFormat headerFormat = new TickHeaderFormat(header);
            String line;

            while ((line = br.readLine()) != null) {
                String[] row = line.split(TickHeaderFormat.CSV_SEPARATOR);
                addLast(new Tick(headerFormat, row));
            }
        } catch (IOException e) {
            MPKMacros.LOGGER.info("Failed to read file: " + macroFile.getAbsolutePath());
        }
    }

    public void run() {
        MPKMacros.currentMacro = new Runner();
    }

    public class Runner {
        private final It it;
        private int count;
        private Tick curr;
        private float savedYaw;
        private float savedPitch;

        public Runner() {
            it = iterator();
            if (it.hasNext()) {
                curr = it.next();
                count = curr.tickInput.getCount();
            }

            Player player = Player.getLatest();
            savedYaw = player.getTrueYaw();
            savedPitch = player.getTruePitch();
        }

        public boolean tick() {
            if (curr == null) return false;
            while (count == 0) {
                if (!it.hasNext()) return false;

                curr = it.next();
                count = curr.tickInput.getCount();
            }

            savedYaw += curr.tickInput.getYaw();
            savedPitch += curr.tickInput.getPitch();

            Minecraft.setInputs(
                    savedYaw, false, savedPitch, false,
                    curr.tickInput.getKeyInputs(), ~curr.tickInput.getKeyInputs(),
                    curr.tickInput.getL(), curr.tickInput.getR()
            );
            count--;
            return true;
        }

        public void stop() {
            Minecraft.setInputs(new TickInput());
        }
    }

    public void save() {
        try {
            //noinspection ResultOfMethodCallIgnored
            macroFile.createNewFile();
        } catch (IOException e) {
            MPKMacros.LOGGER.info("Failed to create file: " + macroFile.getAbsolutePath());
            return;
        }

        try (PrintWriter pw = new PrintWriter(macroFile)) {
            pw.println(TickHeaderFormat.getCSVHeader());
            for (Itr it = super.iterator(); it.hasNext(); ) {
                Tick macroTick = it.next();
                pw.println(macroTick.getCSV());
            }
        } catch (FileNotFoundException e) {
            MPKMacros.LOGGER.info("Failed to create file: " + macroFile.getAbsolutePath());
        }
    }

    public static class Tick {
        public TickInput tickInput;

        protected static Tick lastJump = null;

        protected Node target = null;
        protected int jumpCount;
        protected int counter;
        protected boolean resetCounterOnDifferentJump = true;

        public Tick(TickHeaderFormat headerFormat, String[] row) {
            this(new TickInput(
                    headerFormat.getW(row),
                    headerFormat.getA(row),
                    headerFormat.getS(row),
                    headerFormat.getD(row),
                    headerFormat.getP(row),
                    headerFormat.getN(row),
                    headerFormat.getJ(row),
                    headerFormat.getL(row),
                    headerFormat.getR(row),
                    headerFormat.getYaw(row),
                    headerFormat.getPitch(row),
                    headerFormat.getCount(row)
            ));
        }

        private Tick(TickInput tickInput) {
            super();
            this.tickInput = tickInput;
        }

        public static Tick Jump() {
            return new Tick(null);
        }

        public Tick() {
            this.tickInput = new TickInput();
        }

        @Override
        public String toString() {
            return tickInput == null ? "null" : tickInput.toString();
        }

        public String getCSV() {
            return tickInput.getW() + "," +
                   tickInput.getA() + "," +
                   tickInput.getS() + "," +
                   tickInput.getD() + "," +
                   tickInput.getP() + "," +
                   tickInput.getN() + "," +
                   tickInput.getJ() + "," +
                   tickInput.getYaw() + "," +
                   tickInput.getPitch() + "," +
                   tickInput.getL() + "," +
                   tickInput.getR() + "," +
                   tickInput.getCount();
        }
    }

    @Override
    public It iterator() {
        return new It();
    }

    public class It extends Itr {
        public It() {
            if (curr.item.tickInput == null)
                next();
        }

        public boolean hasNext() {
            return super.hasNext();
        }

        public Tick next() {
            if (curr.item.tickInput != null)
                return super.next();

            boolean hasTarget = curr.item.target != null;
            if (hasTarget && curr.item.resetCounterOnDifferentJump && Tick.lastJump != curr.item) {
                curr.item.counter = curr.item.jumpCount;
                Tick.lastJump = curr.item;
            }
            if (!hasTarget || curr.item.counter == 0) {
                curr = curr.next;
                return next();
            }
            curr.item.counter--;
            curr = curr.item.target;

            return next();
        }
    }
}