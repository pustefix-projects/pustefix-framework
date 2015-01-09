package org.pustefixframework.web.mvc.filter;

public class Bean {
    
    private String text;
    private Long number;
    private int no;
    private Value value;
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String getText() {
        return text;
    }
    
    public void setNumber(Long number) {
        this.number = number;
    }
    
    public Long getNumber() {
        return number;
    }

    public void setNo(int no) {
        this.no = no;
    }
    
    public int getNo() {
        return no;
    }
    
    public void setValue(Value value) {
        this.value = value;
    }
    
    public Value getValue() {
        return value;
    }
    
    
    public static class Value {
        
        int value;
        
        public Value(int value) {
            this.value = value;
        }
        
        @Override
        public String toString() {
            return String.valueOf(value);
        }
        
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof Value) {
                return ((Value)obj).value == value;
            }
            return false;
        }
        
    }
    
}
