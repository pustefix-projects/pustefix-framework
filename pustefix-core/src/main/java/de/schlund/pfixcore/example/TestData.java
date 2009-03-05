package de.schlund.pfixcore.example;


public class TestData {

    private TestData data;
    
    public TestData() {
    }
    
    private String text;
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String getText() {
        String res = text;
        if(data!=null) res += " "+data.getText();
        return res;
    }
    
    public void setData(TestData data) {
        this.data = data;
    }
    
    //public void setFoo(String foo) {}
    
}
