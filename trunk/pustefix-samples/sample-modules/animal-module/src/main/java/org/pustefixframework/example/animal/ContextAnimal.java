package org.pustefixframework.example.animal;

public class ContextAnimal {
    
    private final static String[] SUPPORTED_ANIMALS = {
        "Elephant",
        "Giraffe",
        "Lion",
        "Rhino",
        "Tiger",
        "Zebra"
    };
    
    private String selectedAnimal;
    
    public String[] getSupportedAnimals() {
        return SUPPORTED_ANIMALS;
    }
    
    public void setSelectedAnimal(String animal) {
        this.selectedAnimal = animal;
    }
    
    public String getSelectedAnimal() {
        return selectedAnimal;
    }
    
}
