package edu.baylor.ecs.seer.lweaver.app.mock;

import org.springframework.stereotype.Service;

@Service
public class MockClass {

    private int a;
    private int b;

    MockClass(){
        a = (int)Math.floor(Math.random());
        b = (int)Math.floor(Math.random());
    }

    public void doSomethingA(){
        this.a = this.a + this.b;
        String s = String.valueOf(this.a);
        for(char c : s.toCharArray()){
            this.a = (int)c;
        }
    }

    public int getA(){
        return this.a;
    }

    public void doSomethingB(){
        int index = 0;
        for(int i = 0; i < 100; i++){
            index = i;
        }

        if(a > 50){
            index = -1;
        }

        if(getA() > 50){
            index = 0;
        }
    }
}
