package com.neptune;

import java.lang.reflect.Field;

/**
 * Created by neptune on 16-10-25.
 * 一个具有启发性的反射例子
 */
public class DingDong {
    public static void main(String[] args) {
        try {
            Field field = Class.forName("com.neptune.Demo").getDeclaredField("demo");
            field.setAccessible(true);
            Demo demo = (Demo) field.get(null);
            demo.print();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}

class Demo {
    private static Demo demo = new Demo();

    private Demo() {

    }

    public void print() {
        System.out.println("success");
    }
}
