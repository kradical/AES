import java.io.*;
import java.util.*;

class Encrypter {
    private static byte[] sBox = javax.xml.bind.DatatypeConverter.parseHexBinary("637C777BF26B6FC53001672BFED7AB76CA82C97DFA5947F0ADD4A2AF9CA472C0B7FD9326363FF7CC34A5E5F171D8311504C723C31896059A071280E2EB27B27509832C1A1B6E5AA0523BD6B329E32F8453D100ED20FCB15B6ACBBE394A4C58CFD0EFAAFB434D338545F9027F503C9FA851A3408F929D38F5BCB6DA2110FFF3D2CD0C13EC5F974417C4A77E3D645D197360814FDC222A908846EEB814DE5E0BDBE0323A0A4906245CC2D3AC629195E479E7C8376D8DD54EA96C56F4EA657AAE08BA78252E1CA6B4C6E8DD741F4BBD8B8A703EB5664803F60E613557B986C11D9EE1F8981169D98E949B1E87E9CE5528DF8CA1890DBFE6426841992D0FB054BB16");
    private static byte[] rcon = javax.xml.bind.DatatypeConverter.parseHexBinary("8D01020408102040801B366CD8AB4D9A2F5EBC63C697356AD4B37DFAEFC5913972E4D3BD61C29F254A943366CC831D3A74E8CB8D01020408102040801B366CD8AB4D9A2F5EBC63C697356AD4B37DFAEFC5913972E4D3BD61C29F254A943366CC831D3A74E8CB8D01020408102040801B366CD8AB4D9A2F5EBC63C697356AD4B37DFAEFC5913972E4D3BD61C29F254A943366CC831D3A74E8CB8D01020408102040801B366CD8AB4D9A2F5EBC63C697356AD4B37DFAEFC5913972E4D3BD61C29F254A943366CC831D3A74E8CB8D01020408102040801B366CD8AB4D9A2F5EBC63C697356AD4B37DFAEFC5913972E4D3BD61C29F254A943366CC831D3A74E8CB8D");
    private byte[] key;
    private BufferedReader inFile;
    private FileOutputStream outFile;


    public Encrypter(byte[] key, String inFileName) throws FileNotFoundException {
        this.key = key;
        this.inFile = new BufferedReader(new FileReader(inFileName));
        this.outFile = new FileOutputStream(inFileName + ".enc");
    }

    public void encrypt() throws IOException {
        
        byte[] expandedKey = new byte[240];
        System.arraycopy(this.key, 0, expandedKey, 0, this.key.length);
        this.expandKey(expandedKey);
        
        System.out.println(Arrays.toString(expandedKey));
        
        byte[] buffer;
        for(String line = this.inFile.readLine(); line != null; line = this.inFile.readLine()) {
            buffer = javax.xml.bind.DatatypeConverter.parseHexBinary(line);
            if(buffer.length != 16) {
                System.out.println("Input line is not 128 bits");
                System.exit(1);
            }
            
            for(int i = 0; i < 14; i++){
                if(i == 0) {
                    // this.addRoundKey(buffer);
                }
                
                if(i == 13) {
                    // Final round (no mix columns):
                    // this.subBytes(buffer);
                    // this.shiftRows(buffer);
                    // this.addRoundKey(buffer);
                } else {
                    // non-final round:
                    // this.subBytes(buffer);
                    // this.shiftRows(buffer);
                    // this.mixColumns(buffer);
                    // this.addRoundKey(buffer);
                }   
            }
        }
    }
    
    public static Byte[] toByteArr(byte[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return new Byte[0];
        }
        final Byte[] result = new Byte[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = new Byte(array[i]);
        }
        return result;
    }
    
    private void keyExpansionCore(byte[] prevKey, int i) {
        int ndx;
        
        // rotate
        byte temp = prevKey[0];
        for(ndx = 0; ndx < 3; ndx ++) {
            prevKey[ndx] = prevKey[ndx + 1];
        }
        prevKey[3] = temp;

        // apply s-box
        for(int j = 0; j < 4; j++) {
            prevKey[j] = this.sBox[prevKey[j] + 128];
        }

        // xor first byte with rcon
        prevKey[0] ^= this.rcon[i];
    }
    
    // TODO: finish this method, only does core expansion currently.
    private void expandKey(byte[] fullKey) {
        int ndx = 32; // tracks where in the expanded key array we are.
        byte[] temp = new byte[4];
    
        int i;
        for(i = 1; i < 7; i++) {
            System.arraycopy(fullKey, ndx - 4, temp, 0, 4);
            keyExpansionCore(temp, i);
            System.arraycopy(temp, 0, fullKey, ndx, 4);
            
            for(int j = 0; j < 4; j++) {
                fullKey[ndx + j] ^= fullKey[ndx - 32 + j];
            }
            ndx += 4;

            // TODO: abstract this loop into function
            for(int j = 0; j < 3; j++) {
                System.arraycopy(fullKey, ndx - 4, fullKey, ndx, 4);

                for(int k = 0; k < 4; k++) {
                    fullKey[ndx + j] ^= fullKey[ndx - 32 + j];   
                }
                ndx += 4;
            }
            
            System.arraycopy(fullKey, ndx - 4, fullKey, ndx, 4);
            for(int j = 0; j < 4; j++) {
                fullKey[ndx + j] = this.sBox[fullKey[ndx + j] + 128];
                fullKey[ndx + j] ^= fullKey[ndx - 32 + j];
            }
            ndx += 4;
            
            for(int j = 0; j < 3; j++) {
                System.arraycopy(fullKey, ndx - 4, fullKey, ndx, 4);

                for(int k = 0; k < 4; k++) {
                    fullKey[ndx + k] ^= fullKey[ndx - 32 + k];   
                }
                ndx += 4;
            }
        }
        
        System.arraycopy(fullKey, ndx - 4, temp, 0, 4);
        keyExpansionCore(temp, i);
        System.arraycopy(temp, 0, fullKey, ndx, 4);
        
        for(int j = 0; j < 4; j++) {
            fullKey[ndx + j] ^= fullKey[ndx - 32 + j];
        }
        
        ndx += 4;
        
        for(int j = 0; j < 3; j++) {
            System.arraycopy(fullKey, ndx - 4, fullKey, ndx, 4);

            for(int k = 0; k < 4; k++) {
                fullKey[ndx + j] ^= fullKey[ndx - 32 + j];   
            }
            ndx += 4;
        }
    }
    
    private void addRoundKey(byte[] input) {
        System.out.println("addRoundKey unimplemented");
    }
    
    private void subBytes(byte[] input) {
        System.out.println("subBytes unimplemented");
    }
    
    private void shiftRows(byte[] input) {
        System.out.println("shiftRows unimplemented");
    }
    
    private void mixColumns(byte[] input) {
        System.out.println("mixColumns unimplemented");
    }
}

class AES {
    public static void main(String[] args) {
        if(args.length != 3) {
            System.out.println("USAGE:\tjava AES option keyFile inputFile");
            System.exit(0);
        }

        String option = args[0];
        String keyFileName = args[1];
        String inFileName = args[2];

        byte[] key = { 0 };

        try {
            BufferedReader keyReader = new BufferedReader(new FileReader(keyFileName));
            String keyString = keyReader.readLine();
            key = javax.xml.bind.DatatypeConverter.parseHexBinary(keyString);
            
            if(key.length != 32) {
                System.out.println("Key not 256 bits");
                System.exit(1);
            }
        } catch(FileNotFoundException e) {
            System.out.println("Unable to open file");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Error reading file");
            System.exit(1);
        }

        if(option.equals("e")) {
            try {
                Encrypter enc = new Encrypter(key, inFileName);
                enc.encrypt();
            } catch(FileNotFoundException e) {
                System.out.println("Unable to open file");
                System.exit(1);
            } catch(IOException e) {
                System.out.println("Error reading file");
                System.exit(1);
            }
        } else if(option.equals("d")) {

        } else {
            System.out.println("option flag should be d|e");
            System.exit(0);
        }
    }
}