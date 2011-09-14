package de.schlund.pfixcore.example.test;

import java.util.ArrayList;
import java.util.List;

import de.schlund.pfixcore.generator.annotation.IWrapper;

@IWrapper(ihandler = TypeTestBeanDummyHandler.class)
public class TypeTestBean extends TypeTestBeanBase {

    private int[] numberArray;
    private Integer[] numberObjArray;
    private List<Integer> numberList;
    private ArrayList<Integer> numberArrayList;
    private String[] strArray;
    private List<String> strList;
    private ArrayList<String> strArrayList;
    
    public int[] getNumberArray() {
        return numberArray;
    }
    
    public void setNumberArray(int[] numberArray) {
        this.numberArray = numberArray;
    }
    
    public Integer[] getNumberObjArray() {
        return numberObjArray;
    }
    
    public void setNumberObjArray(Integer[] numberObjArray) {
        this.numberObjArray = numberObjArray;
    }
    
    public List<Integer> getNumberList() {
        return numberList;
    }
    
    public void setNumberList(List<Integer> numberList) {
        this.numberList = numberList;
    }
    
    public ArrayList<Integer> getNumberArrayList() {
        return numberArrayList;
    }

    public void setNumberArrayList(ArrayList<Integer> numberArrayList) {
        this.numberArrayList = numberArrayList;
    }
    
    public String[] getStrArray() {
        return strArray;
    }

    public void setStrArray(String[] strArray) {
        this.strArray = strArray;
    }

    public List<String> getStrList() {
        return strList;
    }

    public void setStrList(List<String> strList) {
        this.strList = strList;
    }

    public ArrayList<String> getStrArrayList() {
        return strArrayList;
    }

    public void setStrArrayList(ArrayList<String> strArrayList) {
        this.strArrayList = strArrayList;
    }

}
