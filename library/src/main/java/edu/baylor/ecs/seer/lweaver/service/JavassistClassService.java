package edu.baylor.ecs.seer.lweaver.service;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class JavassistClassService {


    public void classPool(){
        ClassPool pool = ClassPool.getDefault();
        CtClass evalClass = pool.makeClass("Eval");
        try {
            evalClass.writeFile();
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }

    }

}
