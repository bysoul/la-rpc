package com.bys.larpc.consumer.entry;

import com.bys.larpc.consumer.conutil.RpcClient;
import com.bys.larpc.consumer.conutil.RpcProxy;
import com.bys.larpc.service.Calculator;

import static java.lang.Thread.sleep;

public class Consumer {
    public static void main(String[] args) {
        System.out.println(Thread.currentThread());
        Calculator a=(Calculator) new RpcProxy().get("CalculatorImpl",Calculator.class);
        System.out.println(a.add(1,10));
        Calculator b=(Calculator) new RpcProxy().get("CalculatorImpl",Calculator.class);
        System.out.println(b.add(1,10));
        RpcClient.client.clean();
        RpcClient.client.close();
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.getProperties().put("sun.misc.ProxyGenerator.saveGeneratedFiles","true");

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