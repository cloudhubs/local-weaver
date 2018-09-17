package edu.baylor.ecs.cfgg.evaluator.mock;

public class MockClassA {

    private String fieldA;
    private String fieldB;
    private int fieldC;

    public MockClassA() {
        this.fieldA = "fieldA";
        this.fieldB = "fieldB";
        fieldC = (int) (Math.random() * 100);
    }

    public MockClassA(String fieldA, String fieldB, int fieldC) {
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
}
