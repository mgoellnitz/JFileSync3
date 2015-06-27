package sevenzip.compression.rangecoder;

import java.io.IOException;


public class BitTreeEncoder {

    private final short[] models;

    private final int numBitLevels;


    public BitTreeEncoder(int numBitLevels) {
        this.numBitLevels = numBitLevels;
        this.models = new short[1<<numBitLevels];
    }


    public void Init() {
        Decoder.InitBitModels(models);
    }


    public void Encode(Encoder rangeEncoder, int symbol) throws IOException {
        int m = 1;
        for (int bitIndex = numBitLevels; bitIndex!=0;) {
            bitIndex--;
            int bit = (symbol>>>bitIndex)&1;
            rangeEncoder.Encode(models, m, bit);
            m = (m<<1)|bit;
        }
    }


    public void ReverseEncode(Encoder rangeEncoder, int symbol) throws IOException {
        int m = 1;
        for (int i = 0; i<numBitLevels; i++) {
            int bit = symbol&1;
            rangeEncoder.Encode(models, m, bit);
            m = (m<<1)|bit;
            symbol >>= 1;
        }
    }


    public int GetPrice(int symbol) {
        int price = 0;
        int m = 1;
        for (int bitIndex = numBitLevels; bitIndex!=0;) {
            bitIndex--;
            int bit = (symbol>>>bitIndex)&1;
            price += Encoder.GetPrice(models[m], bit);
            m = (m<<1)+bit;
        }
        return price;
    }


    public int ReverseGetPrice(int symbol) {
        int price = 0;
        int m = 1;
        for (int i = numBitLevels; i!=0; i--) {
            int bit = symbol&1;
            symbol >>>= 1;
            price += Encoder.GetPrice(models[m], bit);
            m = (m<<1)|bit;
        }
        return price;
    }


    public static int ReverseGetPrice(short[] Models, int startIndex, int NumBitLevels, int symbol) {
        int price = 0;
        int m = 1;
        for (int i = NumBitLevels; i!=0; i--) {
            int bit = symbol&1;
            symbol >>>= 1;
            price += Encoder.GetPrice(Models[startIndex+m], bit);
            m = (m<<1)|bit;
        }
        return price;
    }


    public static void ReverseEncode(short[] Models, int startIndex, Encoder rangeEncoder, int NumBitLevels, int symbol) throws IOException {
        int m = 1;
        for (int i = 0; i<NumBitLevels; i++) {
            int bit = symbol&1;
            rangeEncoder.Encode(Models, startIndex+m, bit);
            m = (m<<1)|bit;
            symbol >>= 1;
        }
    }

}
