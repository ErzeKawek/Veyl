package net.erzekawek.veyl.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public class AllPurposeUtility {

    public static class DirectionalUtility {
        public static final Direction[] HORIZONTAL = makeHorizontal();
        public static final Direction[] DIRECTIONS = Direction.values();

        public static Direction[] makeHorizontal() {
            return new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        }
    }

    public static final Random RANDOM = new Random();

    public int nextInt() {
        return ThreadLocalRandom.current().nextInt();
    }

    public int nextInt(int i) {
        return ThreadLocalRandom.current().nextInt(i);
    }

    public static int randRange(int min, int max, RandomSource random) {
        return min + random.nextInt(max - min + 1);
    }

    public static double randRange(double min, double max, RandomSource random) {
        return min + random.nextDouble() * (max - min);
    }

    public static float randRange(float min, float max, RandomSource random) {
        return min + random.nextFloat() * (max - min);
    }

    public static float lengthSqr(float x, float y, float z) {
        return x * x + y * y + z * z;
    }

    public static double lengthSqr(double x, double y, double z) {
        return x * x + y * y + z * z;
    }

    public static float length(float x, float y, float z) {
        return (float) Math.sqrt(lengthSqr(x, y, z));
    }

    public static double length(double x, double y, double z) {
        return Math.sqrt(lengthSqr(x, y, z));
    }

    public static float lengthSqr(float x, float y) {
        return x * x + y * y;
    }

    public static double lengthSqr(double x, double y) {
        return x * x + y * y;
    }

    public static float length(float x, float y) {
        return (float) Math.sqrt(lengthSqr(x, y));
    }

    public static double length(double x, double y) {
        return Math.sqrt(lengthSqr(x, y));
    }

    public static int getSeed(int seed, int x, int y) {
        int h = seed + x * 374761393 + y * 668265263;
        h = (h ^ (h >> 13)) * 1274126177;
        return h ^ (h >> 16);
    }

    public static int getSeed(int seed, int x, int y, int z) {
        int h = seed + x * 374761393 + y * 668265263 + z;
        h = (h ^ (h >> 13)) * 1274126177;
        return h ^ (h >> 16);
    }

    public static void offset(List<Vector3f> spline, Vector3f offset) {
        for (Vector3f v : spline) {
            v.set(offset.x() + v.x(), offset.y() + v.y(), offset.z() + v.z());
        }
    }

    public static int floor(double x) {
        return x < 0 ? (int) (x - 1) : (int) x;
    }

    public static int min(int a, int b) {
        return a < b ? a : b;
    }

    public static int min(int a, int b, int c) {
        return min(a, min(b, c));
    }

    public static int max(int a, int b) {
        return a > b ? a : b;
    }

    public static float min(float a, float b) {
        return a < b ? a : b;
    }

    public static float max(float a, float b) {
        return a > b ? a : b;
    }

    public static float max(float a, float b, float c) {
        return max(a, max(b, c));
    }

    public static int max(int a, int b, int c) {
        return max(a, max(b, c));
    }

    public static <T> void shuffle(T[] array, RandomSource random) {
        for (int i = 0; i < array.length; i++) {
            int i2 = random.nextInt(array.length);
            T element = array[i];
            array[i] = array[i2];
            array[i2] = element;
        }
    }

    private static final int ALPHA = 255 << 24;

    public static int color(int r, int g, int b) {
        return ALPHA | (r << 16) | (g << 8) | b;
    }

    public static int color(String hex) {
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return color(r, g, b);
    }

    public class SplineMath {
        public static void fillLineForce(
                Vector3f start,
                Vector3f end,
                WorldGenLevel level,
                BlockState state,
                BlockPos pos,
                Function<BlockState, Boolean> replace
        ) {
            float dx = end.x() - start.x();
            float dy = end.y() - start.y();
            float dz = end.z() - start.z();
            float max = AllPurposeUtility.max(Math.abs(dx), Math.abs(dy), Math.abs(dz));
            int count = AllPurposeUtility.floor(max + 1);
            dx /= max;
            dy /= max;
            dz /= max;
            float x = start.x();
            float y = start.y();
            float z = start.z();
            boolean down = Math.abs(dy) > 0.2;

            BlockState bState;
            BlockPos.MutableBlockPos bPos = new BlockPos.MutableBlockPos();
            for (int i = 0; i < count; i++) {
                bPos.set(x + pos.getX(), y + pos.getY(), z + pos.getZ());
                bState = level.getBlockState(bPos);
                if (replace.apply(bState)) {
                    level.setBlock(bPos, state, Flags.SILENT);
                    bPos.setY(bPos.getY() - 1);
                    bState = level.getBlockState(bPos);
                    if (down && replace.apply(bState)) {
                        level.setBlock(bPos, state, Flags.SILENT);
                    }
                }
                x += dx;
                y += dy;
                z += dz;
            }
            bPos.set(end.x() + pos.getX(), end.y() + pos.getY(), end.z() + pos.getZ());
            bState = level.getBlockState(bPos);
            if (replace.apply(bState)) {
                level.setBlock(bPos, state, Flags.SILENT);
                bPos.setY(bPos.getY() - 1);
                bState = level.getBlockState(bPos);
                if (down && replace.apply(bState)) {
                    level.setBlock(bPos, state, Flags.SILENT);
                }
            }
        }

        public static void fillSplineForce(
                List<Vector3f> spline,
                WorldGenLevel level,
                BlockState state,
                BlockPos pos,
                Function<BlockState, Boolean> replace
        ) {
            Vector3f startPos = spline.get(0);
            for (int i = 1; i < spline.size(); i++) {
                Vector3f endPos = spline.get(i);
                fillLineForce(startPos, endPos, level, state, pos, replace);
                startPos = endPos;
            }
        }
    }
}
