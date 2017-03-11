package com.william.xu;

/**
 * Created by william on 2017/3/11.
 */
public class Main {


    public static void main(String[] args){
        new ServerRegister().regServer();
        while (true){
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
