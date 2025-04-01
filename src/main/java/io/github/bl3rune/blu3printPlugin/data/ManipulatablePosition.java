package io.github.bl3rune.blu3printPlugin.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.github.bl3rune.blu3printPlugin.enums.Orientation;
import io.github.bl3rune.blu3printPlugin.enums.Rotation;

public class ManipulatablePosition {

    private int xMax;
    private int yMax;
    private int zMax;
    private Orientation orientation;
    private Rotation rotation;
    private int scale;
    private transient String storedEncoding;

    private Iterator<Integer> outerLoop;
    private Iterator<Integer> middleLoop;
    private Iterator<Integer> innerLoop;
    private int outerIndex;
    private int middleIndex;
    private int innerIndex;
    private Dimension[] ordering;

    private static enum Dimension {
        X_PLUS(3), X_MINUS(-3), Y_PLUS(2), Y_MINUS(-2), Z_PLUS(1), Z_MINUS(-1);

        private final int index;

        private Dimension(int index) {
            this.index = index;
        }

        public int getPosIndex() {
            return index < 0 ? (index * -1) - 1 : index - 1;
        }

        public static Dimension getInverted(Dimension d) {
            switch (d) {
                case X_PLUS:
                    return X_MINUS;
                case X_MINUS:
                    return X_PLUS;
                case Y_PLUS:
                    return Y_MINUS;
                case Y_MINUS:
                    return Y_PLUS;
                case Z_PLUS:
                    return Z_MINUS;
                case Z_MINUS:
                    return Z_PLUS;
                default:
                    return null;
            }
        }
    }

    public ManipulatablePosition(int z, int y, int x, Orientation o, Rotation r) {
        this(z, y, x, o, r, 1);
    }

    public ManipulatablePosition(ManipulatablePosition p, int scale) {
        this(p.getZSize(), p.getYSize(), p.getXSize(), p.getOrientation(), p.getRotation(), scale);
    }

    public ManipulatablePosition(int z, int y, int x, Orientation o, Rotation r, int scale) {
        this.xMax = x;
        this.yMax = y;
        this.zMax = z;
        this.orientation = o;
        this.rotation = r;
        this.scale = scale;
        this.ordering = calculateOrdering(o, r);
    }

    public int getXSize() {
        return xMax;
    }

    public int getYSize() {
        return yMax;
    }

    public int getZSize() {
        return zMax;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public Rotation getRotation() {
        return rotation;
    }

    public int getScale() {
        return scale;
    }

    public int getScalingIngredientsMultiplier() {
        return scale * scale * scale;
    }

    public int doubledScale() {
        return scale * 2;
    }

    public String getEncoding() {
        return storedEncoding;
    }

    public void setEncoding(String encoding) {
        this.storedEncoding = encoding;
    }

    // CONVERSION METHODS

    public MaterialData[][][] reorientSelectionGrid(MaterialData[][][] oldGrid, Orientation o, Rotation r) {
        Dimension[] newOrdering = calculateOrdering(o, r);
        Function<Integer[], Integer[]> converter = buildConversionFunction(newOrdering);
        Integer[] sizes = converter.apply(new Integer[] { zMax, yMax, xMax });

        MaterialData[][][] newGrid = new MaterialData[sizes[0]][sizes[1]][sizes[2]];

        for (int z = 0; z < zMax; z++) {
            for (int y = 0; y < yMax; y++) {
                for (int x = 0; x < xMax; x++) {
                    MaterialData data = oldGrid[z][y][x];
                    Integer[] converted = converter.apply(new Integer[] { z, y, x, 0 });
                    newGrid[converted[0]][converted[1]][converted[2]] = data;
                }
            }
        }
        return newGrid;
    }

    private Function<Integer[], Integer[]> buildConversionFunction(Dimension[] newOrdering) {
        Dimension dimz = Arrays.stream(this.ordering).filter(d -> d.getPosIndex() == 0).findFirst().get();
        Dimension dimy = Arrays.stream(this.ordering).filter(d -> d.getPosIndex() == 1).findFirst().get();
        Dimension dimx = Arrays.stream(this.ordering).filter(d -> d.getPosIndex() == 2).findFirst().get();

        int[] reordered = new int[3];

        for (int i = 0; i < 3; i++) {
            reordered[ordering[i].getPosIndex()] = newOrdering[i].getPosIndex();
        }

        boolean flippedZ = Arrays.stream(newOrdering).noneMatch(o -> dimz == o);
        boolean flippedY = Arrays.stream(newOrdering).noneMatch(o -> dimy == o);
        boolean flippedX = Arrays.stream(newOrdering).noneMatch(o -> dimx == o);

        return (coords) -> { // as [z][y][x] for sizes [z][y][x][0] for coordinates
            Integer[] signedCoords = new Integer[3];
            boolean allowNegative = coords.length > 3;

            signedCoords[0] = (allowNegative && flippedZ) ? zMax - coords[0] - 1 : coords[0];
            signedCoords[1] = (allowNegative && flippedY) ? yMax - coords[1] - 1 : coords[1];
            signedCoords[2] = (allowNegative && flippedX) ? xMax - coords[2] - 1 : coords[2];
            return new Integer[] {
                    signedCoords[reordered[0]],
                    signedCoords[reordered[1]],
                    signedCoords[reordered[2]]
            }; // as new arrangement
        };
    }

    // ITERATION METHODS

    public boolean endOfMiddleLoop() {
        return middleLoop != null && !middleLoop.hasNext();
    }

    public boolean endOfInnerLoop() {
        return innerLoop != null && !innerLoop.hasNext();
    }

    public void resetLoops() {
        outerLoop = null;
        middleLoop = null;
        innerLoop = null;

    }

    public int[] next() {
        return next(false);
    }

    /**
     * Gets the next position in the 3D sequence.
     * 
     * @return the next position in the 3D sequence.
     */
    public int[] next(boolean scaled) {
        if (outerLoop == null) {
            resetLoops(scaled);
        }

        if (!innerLoop.hasNext()) {
            if (!middleLoop.hasNext()) {
                if (!outerLoop.hasNext()) {
                    outerLoop = null;
                    return null;
                }
                outerIndex = outerLoop.next();
                middleLoop = getMiddleLoop(scaled);
            }
            middleIndex = middleLoop.next();
            innerLoop = getInnerLoop(scaled);
        }
        innerIndex = innerLoop.next();
        return unscramble(outerIndex, middleIndex, innerIndex);
    }

    /**
     * Calculates the Dimensions in order of manipulation base on
     * 
     * @param o 3D orientation the camera is facing towards
     * @param r 3D rotation the camera is facing towards
     * 
     *          POSITION = 1st(positive or negative) 2nd+/- 3rd+/-
     *          * * * * ROTATION TOP * * * * *
     *          WEST = X- Z- Y+ EAST = X+ Z+ Y+
     *          DOWN = Y- X+ Z- UP = Y+ X- Z+
     *          NORTH = Z- X+ Y+ SOUTH = Z+ X- Y+
     *          * * * * ROTATION RIGHT * * * * *
     *          WEST = X- Y- Z- EAST = X+ Y- Z+
     *          DOWN = Y- Z+ X+ UP = Y+ Z- X-
     *          NORTH = Z- Y- X+ SOUTH = Z+ Y- X-
     *          * * * * ROTATION BOTTOM * * * * *
     *          WEST = X- Z+ Y- EAST = X+ Z- Y-
     *          DOWN = Y- X- Z+ UP = Y+ X+ Z-
     *          NORTH = Z- X- Y- SOUTH = Z+ X+ Y-
     *          * * * * ROTATION LEFT * * * * *
     *          WEST = X- Y+ Z+ EAST = X+ Y+ Z-
     *          DOWN = Y- Z- X- UP = Y+ Z+ X+
     *          NORTH = Z- Y+ X- SOUTH = Z+ Y+ X+
     */
    private Dimension[] calculateOrdering(Orientation o, Rotation r) {
        Dimension[] order = new Dimension[3];
        switch (o) {
            case WEST:
                return innerOrdering(Dimension.X_MINUS, Dimension.Y_PLUS, Dimension.Z_MINUS, r, order);
            case EAST:
                return innerOrdering(Dimension.X_PLUS, Dimension.Y_PLUS, Dimension.Z_PLUS, r, order);
            case DOWN:
                return innerOrdering(Dimension.Y_MINUS, Dimension.Z_MINUS, Dimension.X_MINUS, r, order);
            case UP:
                return innerOrdering(Dimension.Y_PLUS, Dimension.Z_MINUS, Dimension.X_PLUS, r, order);
            case NORTH:
            default:
                return innerOrdering(Dimension.Z_MINUS, Dimension.Y_PLUS, Dimension.X_MINUS, r, order);
            case SOUTH:
                return innerOrdering(Dimension.Z_PLUS, Dimension.Y_PLUS, Dimension.X_PLUS, r, order);
        }
    }

    private Dimension[] innerOrdering(Dimension outer, Dimension middle, Dimension inner, Rotation r,
            Dimension[] order) {
        boolean isLeft = Rotation.LEFT == r;
        boolean isTop = Rotation.TOP == r;
        order[2] = outer;
        if (r.isHorizontal()) {
            order[1] = isLeft ? middle : Dimension.getInverted(middle);
            order[0] = isLeft ? inner : Dimension.getInverted(inner);
        } else {
            order[1] = isTop ? inner : Dimension.getInverted(inner);
            order[0] = isTop ? middle : Dimension.getInverted(middle);
        }
        return order;
    }

    private void resetLoops(boolean scaled) {
        outerLoop = getOuterLoop(scaled);
        middleLoop = getMiddleLoop(scaled);
        innerLoop = getInnerLoop(scaled);
        outerIndex = outerLoop.next();
        middleIndex = middleLoop.next();
    }

    private Iterator<Integer> getOuterLoop(boolean scaled) {
        return getLoop(ordering[2], scaled);
    }

    private Iterator<Integer> getMiddleLoop(boolean scaled) {
        return getLoop(ordering[1], scaled);
    }

    private Iterator<Integer> getInnerLoop(boolean scaled) {
        return getLoop(ordering[0], scaled);
    }

    private Iterator<Integer> getLoop(Dimension dimension, boolean scaled) {

        switch (dimension) {
            case X_PLUS:
            case X_MINUS:
                return generateList((scaled ? xMax * scale : xMax), dimension == Dimension.X_MINUS).iterator();
            case Y_PLUS:
            case Y_MINUS:
                return generateList((scaled ? yMax * scale : yMax), dimension == Dimension.Y_MINUS).iterator();
            case Z_PLUS:
            case Z_MINUS:
            default:
                return generateList((scaled ? zMax * scale : zMax), dimension == Dimension.Z_MINUS).iterator();
        }
    }

    private List<Integer> generateList(int end, boolean reversed) {
        List<Integer> list = IntStream.range(0, end).boxed().collect(Collectors.toList());
        if (reversed) {
            Collections.reverse(list);
        }
        return list;
    }

    /**
     * Unscrambles values using ordering.
     * 
     * @param outer  the outer value.
     * @param middle the middle value.
     * @param inner  the inner value.
     * @return the unscrambled values in [z][y][x] order.
     */
    private int[] unscramble(int outer, int middle, int inner) {
        int[] result = new int[3];
        result[ordering[2].getPosIndex()] = outer;
        result[ordering[1].getPosIndex()] = middle;
        result[ordering[0].getPosIndex()] = inner;
        return result;
    }
}
