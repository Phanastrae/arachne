package phanastrae.arachne.util;

public class ArrayConversion {

    public static long[] doubleToLong(double[] f) {
        long[] t = new long[f.length];
        for(int i = 0; i < f.length; i++) {
            t[i] = Double.doubleToLongBits(f[i]);
        }
        return t;
    }

    public static double[] longToDouble(long[] f) {
        double[] t = new double[f.length];
        for(int i = 0; i < f.length; i++) {
            t[i] = Double.longBitsToDouble(f[i]);
        }
        return t;
    }

    public static int[] floatToInt(float[] f) {
        int[] t = new int[f.length];
        for(int i = 0; i < f.length; i++) {
            t[i] = Float.floatToIntBits(f[i]);
        }
        return t;
    }

    public static float[] intToFloat(int[] f) {
        float[] t = new float[f.length];
        for(int i = 0; i < f.length; i++) {
            t[i] = Float.intBitsToFloat(f[i]);
        }
        return t;
    }

    public static byte[] boolToByte(boolean[] f) {
        int boolCount = f.length;
        int byteCount = boolCount / 8;
        if(boolCount > byteCount * 8) {
            byteCount++;
        }
        byte[] t = new byte[byteCount];
        for(int i = 0; i < byteCount; i++) {
            int by = 0;
            for(int j = 0; j < 8; j++) {
                int k = i * 8 + j;
                if(k >= boolCount) continue;
                boolean bo = f[k];
                if(bo) {
                    by = by | (1 << j);
                }
            }
            t[i] = (byte)by;
        }
        return t;
    }

    // boolean array will typically have extra trailing 0s at end
    public static boolean[] byteToBool(byte[] f) {
        int byteCount = f.length;
        int boolCount = byteCount * 8;
        boolean[] t = new boolean[boolCount];
        for(int i = 0; i < byteCount; i++) {
            int b = (int)f[i] & 0xFF;
            if(b == 0) continue; // values already set to 0
            for(int j = 0; j < 8; j++) {
                int k = i * 8 + j;
                if((b & (1 << j)) != 0) {
                    t[k] = true;
                }
            }
        }
        return t;
    }
}
