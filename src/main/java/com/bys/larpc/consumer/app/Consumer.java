package com.bys.larpc.consumer.app;

import com.bys.larpc.consumer.conutil.RpcProxy;
import com.bys.larpc.service.Calculator;

import java.util.ArrayList;

import static java.lang.System.exit;
import static java.lang.Thread.sleep;

public class Consumer {
    public static void main(String[] args) {
        System.out.println(Thread.currentThread());

        Calculator c=(Calculator) new RpcProxy().get("CalculatorImpl",Calculator.class);
        Thread t=null;
        /*for(int i=0;i<10;i++){
            (t=new Thread(new Run(i))).start();
        }*/
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(c.add(1,10));
        System.out.println("??");
        System.getProperties().put("sun.misc.ProxyGenerator.saveGeneratedFiles","true");
        /*try {
            //t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        try {
            sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        exit(1);
    }

}
class Run implements Runnable{
    int i;
    public Run(int i) {
        this.i=i;
    }

    @Override
    public void run() {
        Calculator c=(Calculator) new RpcProxy().get("CalculatorImpl",Calculator.class);
        System.out.println(c.add(i,0));
    }
}