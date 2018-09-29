//BY: Brian(Yu-en) Shih

import java.util.*;
import java.io.*;


public class Oslinker{
  static Hashtable <String, Integer> symbolTable = new Hashtable<String, Integer>();
  static Hashtable <String, Boolean> usageTable = new Hashtable<String, Boolean>();
  static Hashtable <String,Boolean> dynamicUseList = new Hashtable<String,Boolean>();
  static Enumeration symbols;
  static String key;
  static ArrayList<Integer> address = new ArrayList<Integer>();
  static ArrayList<String> addressElement = new ArrayList<String>();
  static Hashtable<Integer, Integer> moduleBase = new Hashtable<Integer, Integer>();
  static ArrayList<String> variableDefinition = new ArrayList<String>();
  static ArrayList<Integer> moduleVariableCount = new ArrayList<Integer>();

  public static void main(String[] args){
    for(int i = 0; i < args.length;i++){
      System.out.println("-------------------------------");
      pass1(args[i]);
      pass2(args[i]);
      reset();
      System.out.println("-------------------------------");
    }
  }
  //Making the Symbol table
  public static void pass1(String filename){

    int count = 0;
    File file = new File(filename);
    int baseAddress = 0;
    int tempAddress;
    Hashtable<String,Integer> dynamicDefinitionSize = new Hashtable<String,Integer>();

    // catch exceptions if file is non-existent or file empty.
    try {
      Scanner scanner = new Scanner(file);
      int moduleCount = scanner.nextInt();
      String tempSymbol = null;
      String tempUseSymbol = null;
      //Goes through a single module
      for(int i = 0; i < moduleCount; i ++){
        count = scanner.nextInt();
        //First row
        for(int k = 0; k < count; k++){
          tempSymbol = scanner.next();
          tempAddress = scanner.nextInt();
          if(symbolTable.containsKey(tempSymbol)){
            System.out.println(tempSymbol + " is multiply defined; first value is used");
          }else {
            //tempAddress = scanner.nextInt()+baseAddress;
            symbolTable.put(tempSymbol,tempAddress + baseAddress);
            usageTable.put(tempSymbol, false);

          }
          //This has the definition address. used to check if definition is larger than the module size.
          dynamicDefinitionSize.put(tempSymbol,tempAddress);
        }
        count = scanner.nextInt();
        //Second row
        for(int k = 0; k < count; k++){
          tempUseSymbol = scanner.next();
          variableDefinition.add(tempUseSymbol);
          dynamicUseList.put("In module " + i + ", " + tempUseSymbol,false);
        }
        moduleVariableCount.add(count);
        count = scanner.nextInt();
        //Third row
        for(int k = 0; k < count; k++){
          addressElement.add(scanner.next());
          address.add(scanner.nextInt());
        }
        //Error check if def. exceed module size.
        Set<String> end = dynamicDefinitionSize.keySet();
        for(String start: end){
          if(dynamicDefinitionSize.get(start) > count){
            System.out.println("Error: In module " + i + " the def. of " + start + " exceeds the size of the module; zero (realative) used.");
            symbolTable.put(tempSymbol,baseAddress);
          }
        }
        //Same as symbolTable, but has lifespan of module size.
        dynamicDefinitionSize.clear();
        //update counters
        moduleBase.put(i,count);
        baseAddress += count;
      }
    }
    catch(FileNotFoundException ex) {
      System.out.println(
      "Unable to open file '" +
      filename + "'");
    }

    System.out.println("\n\nSymbol Table");
    symbols = symbolTable.keys();
    while(symbols.hasMoreElements()) {
      key = (String) symbols.nextElement();
      System.out.println("\t"+ key + "=" + symbolTable.get(key));
    }
    System.out.print("\n\n");
  }
  //Making the Memory Map
  public static void pass2(String filename){
    Enumeration usage;
    String usageKey;
    char element;
    int addressCount = 0;
    int numOfAddress;
    int runningBaseCount = 0;
    int definitionBase = 0;
    int runningModuleCount = 0;
    System.out.println("\tMemory Map\n");
    for(int i = 0; i < moduleBase.size(); i++){
      numOfAddress = moduleBase.get(i);
      for(int k = 0; k < numOfAddress; k++){
        //For Elements with R
        if(addressElement.get(runningBaseCount+k).equals("R")){
          if((address.get(runningBaseCount+k)%10)>address.size()){
            System.out.println("Error: The relative address " + address.get(runningBaseCount+k)+" exceeds module size; zero used.");
            address.set(runningBaseCount+k,address.get(runningBaseCount+k)-(address.get(runningBaseCount)%1000));
          }else{
            address.set(runningBaseCount+k,address.get(runningBaseCount+k)+runningBaseCount);
          }
        }
        //For Elements with E
        else if(addressElement.get(runningBaseCount+k).equals("E")){
          definitionBase = address.get(runningBaseCount+k)%10;
          try{
            address.set(runningBaseCount+k,(address.get(runningBaseCount+k)-definitionBase)+symbolTable.get(variableDefinition.get(definitionBase+runningModuleCount)));
            usageTable.put(variableDefinition.get(definitionBase+runningModuleCount),true);
            dynamicUseList.put("In module " + i + ", " + variableDefinition.get(definitionBase+runningModuleCount),true);
          }
          catch(NullPointerException e){
            address.set(runningBaseCount+k,(address.get(runningBaseCount+k)-definitionBase));
            System.out.println("Error: The symbol " + variableDefinition.get(definitionBase+runningModuleCount) + " was used but not defined. The value Zero was used instead");
            usageTable.put(variableDefinition.get(definitionBase+runningModuleCount),true);
            dynamicUseList.put("In module " + i + ", " + variableDefinition.get(definitionBase+runningModuleCount),true);
          }
          catch(IndexOutOfBoundsException e){
            System.out.println("The external address " + address.get(runningBaseCount) + " exceeds length of use list; treated as immediate");
          }
          //Error check for def. use in module
          //dynamicUseList.put("In module " + i + ", " + variableDefinition.get(definitionBase+runningModuleCount),true);


        }
        //For Elements with A
        else if(addressElement.get(runningBaseCount+k).equals("A")){
          if((address.get(runningBaseCount+k)%1000) >= 200){
            System.out.println("Error: The absolute address " + address.get(runningBaseCount+k) +" exceeds machine size; zero used. ");
            address.set(runningBaseCount+k,address.get(runningBaseCount+k)-(address.get(runningBaseCount+k)%1000));
          }
        }
        //Change this to print at the ver end all together
        System.out.println(runningBaseCount+k + ":\t" + address.get(runningBaseCount+k));
      }
      runningBaseCount+= numOfAddress;
      runningModuleCount+=moduleVariableCount.get(i);
    }
    System.out.println("\n\n");
    //Checks if there are unused symbols
    usage = usageTable.keys();
    while(usage.hasMoreElements()) {
      usageKey =  (String)usage.nextElement();
      if(!usageTable.get(usageKey)){
        System.out.println("Warning: The symbol " + usageKey + " was defined but not used.");
      }
    }
    //Chesk if the symbol was in the use list but not used.
    usage = dynamicUseList.keys();
    while(usage.hasMoreElements()){
      usageKey = (String)usage.nextElement();
      if(!dynamicUseList.get(usageKey)){
        System.out.println("Warning:" + usageKey + " appeared in the use list but was not used");
      }
    }
  }
  //Resets all the globals for second use;
  public static void reset(){
    symbolTable.clear();
    usageTable.clear();
    dynamicUseList.clear();
    symbols = null;
    key = null;
    address.clear();
    addressElement.clear();
    moduleBase.clear();
    variableDefinition.clear();
    moduleVariableCount.clear();
  }
}
