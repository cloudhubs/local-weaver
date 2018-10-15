package edu.baylor.ecs.cfgg.evaluator.mock;

import java.util.Random;

public class MockClassB {

    private String fieldA;
    private String fieldB;
    private int fieldC;

    public MockClassB() {
        this.fieldA = "fieldA";
        this.fieldB = "fieldB";
        fieldC = (int) (Math.random() * 100);
    }

    public MockClassB(String fieldA, String fieldB, int fieldC) {
        this.fieldA = fieldA;
        this.fieldB = fieldB;
        this.fieldC = fieldC;
    }

    public String getFieldA() {
        return fieldA;
    }

    public void setFieldA(String fieldA) {
        this.fieldA = fieldA;
    }

    public String getFieldB() {
        return fieldB;
    }

    public void setFieldB(String fieldB) {
        this.fieldB = fieldB;
    }

    public int getFieldC() {
        return fieldC;
    }

    public void setFieldC(int fieldC) {
        this.fieldC = fieldC;
    }

    public boolean doSomethingA(){
        for(int i = 0; i < 10; i++){
            this.fieldC = i;
        }
        return true;
    }

    public boolean doSomethingB(){
        boolean run = true;
        while(run){
            int rand = (int)((Math.random() % 10) + 1);
            if(rand < 5){
                run = false;
            }
        }
        return true;
    }

    public boolean doSomethingC(){
        boolean ret = true;
        int rand = (int)((Math.random() % 10) + 1);
        if(rand < 5){
            ret = false;
        }
        return ret;
    }

    public boolean doSomethingD(){
        int rand = (int)((Math.random() % 10) + 1);
        boolean ret;
        if(rand < 5){
            ret = false;
        } else {
            ret = true;
        }
        return ret;
    }
}
