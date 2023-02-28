import java.util.*;

import javax.naming.event.ObjectChangeListener;

import java.io.*;

public class MainPgm{
    //store label and address in the symbol table
    static HashMap<String, Integer> symbolTable = new HashMap<>();
    static HashMap<String, String> opcodeTable = new HashMap<>();
    static Segments segments = new Segments();
    static int[] address = new int[3];
    //address[0] is the start address
    //address[1] is the current address
    //address[2] is the final address

    static int pass1addresses[] = new int[0];
    static int pccounter = 0;

    static boolean isResw = false;
    static boolean toPrint = false;

    //insert all sicops into opcodetable
    public static void prepOpcodeTable() {
        String fileToRead = "SICOPS.txt";

        try {
            //for file reading
            Scanner scanner = new Scanner(new File(fileToRead));
            //the current line in the file
            String fileLine;

            while(scanner.hasNextLine()) {
                fileLine = scanner.nextLine();

                if(fileLine.isBlank()) {
                    continue;
                } else if (fileLine.charAt(0) == '.') {
                    continue;
                } else {
                    segments.prepareSegments(fileLine);
                    opcodeTable.put(segments.getFirst(), segments.getSecond());
                    //System.out.println("Current line (no comment): " + fileLine);
                    segments.clearSegments();
                }
            } //end while

        } catch (Exception e) {
            e.printStackTrace();
        }
    } //end prepOpcodeTable

    static int hexadecimalToDecimal(String hexVal)
    {
        int len = hexVal.length();
        int base = 1;
        int dec_val = 0;

        for (int i = len - 1; i >= 0; i--) {

            if (hexVal.charAt(i) >= '0'
                && hexVal.charAt(i) <= '9') {
                dec_val += (hexVal.charAt(i) - 48) * base;
  
                // Incrementing base by power
                base = base * 16;
            }

            else if (hexVal.charAt(i) >= 'A'
                     && hexVal.charAt(i) <= 'F') {
                dec_val += (hexVal.charAt(i) - 55) * base;
  
                // Incrementing base by power
                base = base * 16;
            }
        }

        return dec_val;
    }

    static int binaryToDecimal(long binary)
    {
 
        // variable to store the converted
        // binary number
        int decimalNumber = 0, i = 0;
 
        // loop to extract the digits of the binary
        while (binary > 0) {
 
            // extracting the digits by getting
            // remainder on dividing by 10 and
            // multiplying by increasing integral
            // powers of 2
            decimalNumber
                += Math.pow(2, i++) * (binary % 10);
 
            // updating the binary by eliminating
            // the last digit on division by 10
            binary /= 10;
        }
 
        // returning the decimal number
        return decimalNumber;
    }

    static String decimalToHex(long binary)
    {
        // variable to store the output of the
        // binaryToDecimal() method
        int decimalNumber = binaryToDecimal(binary);
 
        // converting the integer to the desired
        // hex string using toHexString() method
        String hexNumber
            = Integer.toHexString(decimalNumber);
 
        // converting the string to uppercase
        // for uniformity
        hexNumber = hexNumber.toUpperCase();
 
        // returning the final hex string
        return hexNumber;
    }

    public static void performPassOne(String line) {
        try {
            
            segments.prepareSegments(line);

            if(segments.getSecond().equals("START")) {
                address[0] = hexadecimalToDecimal(segments.getThird());
                address[1] = address[0];
            }
            
            if (symbolTable.get(segments.getFirst()) == null) {
                if (!segments.getFirst().equals("")) {
                    symbolTable.put(segments.getFirst(), address[1]);
                }
            } else {
                System.out.println("LABEL ALREADY IN SYMTABLE");
            }

            //search for opcode in table
            String opSearch = opcodeTable.get(segments.getSecond());
    
            //System.out.println("OPSEARCH: " + opSearch);
            if(opSearch.equals("resw")) {
                int increment = (Integer.parseInt(segments.getThird()));
                increment *= 3;
                address[1] += increment;
            } else if (opSearch.equals("resb")) {
                int increment = Integer.parseInt(segments.getThird());
                address[1] += increment;
            } else if (segments.getSecond().equals("END")) {
                address[2] = address[1];
            } else {
                if(segments.getSecond().charAt(0) == '+') {
                    address[1] += 4;
                } else if (segments.getSecond().equals("BASE")) {
                    address[1] += 0;
                } else if (segments.getSecond().equals("START")) {
                    address[1] += 0;
                } else if (segments.getSecond().charAt(segments.getSecond().length() - 1) == 'R') {
                    address[1] += 2;
                } else {
                    address[1] += 3;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    } //end performPassOne

    public static String performPassTwo(String line) {
        isResw = false;
        toPrint = false;
        StringBuilder addressbuild = new StringBuilder();
        String address = "";
        String tempAppends = "";
        String stringOpcode = "";
        int currentOpcode = 0;
        String xbpe;
        int tempDisplacement;
        String displacement;
        try {

            segments.prepareSegments(line);

            if (segments.getSecond().equals("RESW") || segments.getSecond().equals("RESB") || segments.getSecond().equals("START") || segments.getSecond().equals("BASE")) {
                if (segments.getSecond().equals("RESW")) {
                    isResw = true;
                }
                return "000000";
            } else if (segments.getSecond().equals("WORD")) {
                int wordtoconvert = Integer.parseInt(segments.getThird());
                String word = Integer.toHexString(wordtoconvert);
                address = ("000000" + word.toUpperCase()).substring(word.length());
                toPrint = true;
                return address;
            } else {
                //step 1: get opcode
                stringOpcode = opcodeTable.get(segments.getSecond());
                currentOpcode = hexadecimalToDecimal(stringOpcode);
                
                //address = Integer.toHexString(currentOpcode).toUpperCase();

                //step 2 check for ni flags
                if (segments.getThird().charAt(0) == '#') {
                    currentOpcode += 1;
                } else if (segments.getThird().charAt(0) == '@') {
                    currentOpcode += 2;
                } else {
                    currentOpcode += 3;
                }

                tempAppends = Integer.toHexString(currentOpcode).toUpperCase();
                if (tempAppends.length() < 2) {
                    tempAppends = "0" + tempAppends;
                }
                addressbuild.append(tempAppends);

                //check for xbpe flags
                if (segments.getSecond().charAt(0) == '+') {
                    xbpe = "1";
                    tempAppends = xbpe;
                } else {
                    xbpe = "2";
                    tempAppends = xbpe;
                }

                addressbuild.append(tempAppends);

                segments.setThird(segments.removeNonAlphanumeric(segments.getThird()));

                //calculate displacement
                if (segments.getSecond().charAt(0) == '+') {
                    addressbuild.append("00");
                    tempDisplacement = symbolTable.get(segments.getThird());
                    displacement = Integer.toHexString(tempDisplacement).toUpperCase();
                } else if (xbpe.equals("1")) {
                    int xbpeconvert = Integer.parseInt(segments.getThird());
                    String immediatevalue = Integer.toHexString(xbpeconvert);
                    displacement = ("000" + immediatevalue.toUpperCase()).substring(immediatevalue.length());
                } else {
                    int numSub = pass1addresses[pccounter];
                    int labelAddress = symbolTable.get(segments.getThird());
                    tempDisplacement = labelAddress - numSub;
                    displacement = Integer.toHexString(tempDisplacement).toUpperCase();

                }

                if (displacement.length() < 3) {
                    switch (displacement.length()) {
                        case 1:
                            displacement = "00" + displacement;
                            break;
                        case 2:
                            displacement = "0" + displacement;
                            break;
                        default:
                            System.out.println("displacement is length 3");
                    }
                }

                if (displacement.length() >= 4) {
                    int start = displacement.length() - 3;
                    for (int i = start; i < displacement.length(); i++) {
                        addressbuild.append(displacement.charAt(i));
                    }
                } else {
                    addressbuild.append(displacement);
                }

                //addressbuild.append(displacement);

                //return address
                address = addressbuild.toString();
                toPrint = true;
                return address;

            }
        } catch (Exception e) {
            System.out.println("SKIPPED: " + segments.getSecond());
        }

        return address;
    } //end performPassTwo

    public static int[] addX(int n, int arr[], int x)
    {
        int i;
    
        // create a new array of size n+1
        int newarr[] = new int[n + 1];
    
        // insert the elements from
        // the old array into the new array
        // insert all elements till n
        // then insert x at n+1
        for (i = 0; i < n; i++)
            newarr[i] = arr[i];
    
        newarr[n] = x;
    
        return newarr;
    }
    public static void main(String[] args) throws Exception {

/**********************************Check File Stuff**************************************/
        String newFileName = args[0].substring(0, args[0].length() - 4);
        File objectFile = new File(newFileName + "lst.txt");
        File codeFile = new File(newFileName + "obj.txt");
        
        if(args.length > 1) {
            System.out.println("Invalid Usage: java className yourFile.txt");
            System.exit(1);
        }

        prepOpcodeTable();

        String fileToRead = args[0];

/**********************************Create File********************************************/

        if (!objectFile.exists()) {
            objectFile.createNewFile();
        }

/*********************************PASS ONE***********************************************/

        int tempAddress = 0;

        try {
            //for file reading
            Scanner scanner = new Scanner(new File(fileToRead));
            //the current line in the file
            String fileLine;
            //System.out.println(fileLine);
            
            while(scanner.hasNextLine()) {
                fileLine = scanner.nextLine();
                if(fileLine.isBlank()) {
                    continue;
                } else if (fileLine.charAt(0) == '.') { //checks if line is a comment
                    //writer.println(fileLine);
                } else { //pass1start
                    tempAddress = address[1];
                    performPassOne(fileLine);
                    pass1addresses = addX(pass1addresses.length, pass1addresses, tempAddress);
                    System.out.printf("Current Address: %X\n", tempAddress);
                    segments.clearSegments();
                }
            } //end while

            System.out.printf("-----------------------------------------\nStart Address:\t0x%X\n", address[0]);
            System.out.printf("Final Address:\t0x%X\n", address[2]);

            System.out.println("PASS 1 ADDRESSES: " + Arrays.toString(pass1addresses));
        } catch (Exception e) {
            e.printStackTrace();
        }

        pass1addresses[0] = pass1addresses[1];

        //perform pass2
        try {
            if (!codeFile.exists()) {
                codeFile.createNewFile();
            }

            PrintWriter writer = new PrintWriter(objectFile);
            PrintWriter objWriter = new PrintWriter(codeFile);

            Scanner scanner = new Scanner(new File(fileToRead));

            String fileLine;

            String opcodeTester;

            objWriter.printf("%06X\n", pass1addresses[0]);
            objWriter.println("000000");

            writer.println("***************************************************\nVincent Almeda n01473764: Project 4\nversion date: 12/4/2022\n***************************************************\n\nASSEMBLER REPORT\n");
            writer.printf("%s\t\t%-10s\t\t\t%-10s\n", "LOC", "OBJECT CODE", "SOURCE");
            writer.printf("%s\t\t%-10s\t\t\t%-10s\n", "---", "-----------", "------");
            while(scanner.hasNextLine()) {
                fileLine = scanner.nextLine();
                if(fileLine.isBlank()) {
                    continue;
                } else if (fileLine.charAt(0) == '.') { //checks if line is a comment
                    //writer.println(fileLine);
                } else { //pass2start
                    pccounter++;
                    opcodeTester = performPassTwo(fileLine);
                    if (toPrint) {
                        objWriter.println(opcodeTester);
                    }
                    if (isResw) {
                        objWriter.println("!");
                        objWriter.printf("%06X\n", pass1addresses[pccounter - 1]);
                        objWriter.println(opcodeTester);
                    }
                    writer.printf("0x%X\t\t%-10s\t\t%-10s\n", pass1addresses[pccounter - 1], opcodeTester, fileLine);
                }
            } //end while
            objWriter.println("!");
            writer.close();
            objWriter.close();
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

/***************************************************************************************/
    } //end main

}//end class MainPgm
