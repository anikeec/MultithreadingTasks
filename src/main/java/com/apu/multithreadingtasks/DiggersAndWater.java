/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.apu.multithreadingtasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author apu
 * Задача: есть один бак с водой (с одним краником), 25 жаждущих пить рудокопов 
 * и 5 кружек на всех.
 */
public class DiggersAndWater {
    
    public static void main(String[] args) throws InterruptedException {
        CupArray cupArray = new CupArray();
        for(int i=0;i<5;i++) {
            cupArray.add(new Cup(i));
        }
        CountDownLatch cdl = new CountDownLatch(25);
        for(int i=0;i<25;i++) {
            new Thread(new Digger(i, cupArray, cdl)).start();
        }        
    } 
    
}

class CupArray {
    List<Cup> cupsArray  = new ArrayList<>();
    
    public void add(Cup cup) {
        cupsArray.add(cup);
    }
    
    public synchronized Cup getCupFromArray(int threadId) throws InterruptedException {
        Cup cup = null;
        for(Cup c:cupsArray) {
            if(c.take(threadId) == true) {
                return c;
            }
        }
        wait();
        return cup;
    }
    
    public synchronized void returnCupToArray(Cup cup) throws InterruptedException {
        for(Cup c:cupsArray) {
            if(c.equals(cup)) {
                c.give();
                notifyAll();
            }
        }  
        wait();
    }
    
}

class Cup {
    int id;
    Integer threadId = null;

    public Cup(int id) {
        this.id = id;        
    }

    public int getId() {
        return id;
    } 
    
    public boolean take(int threadId) {
        if(this.threadId == null) {
            this.threadId = threadId;
            return true;
        } 
        return false;
    }
    
    public void give() {
        this.threadId = null; 
    }
    
}

class Digger implements Runnable {
    
    private final int id;
    private final CupArray cupArray;
    private int catchCounter = 0;
    private final CountDownLatch cdl;

    public Digger(int id, CupArray cupArray, CountDownLatch cdl) {
        this.id = id;
        this.cupArray = cupArray;
        this.cdl = cdl;
    }   

    @Override
    public void run() {
        Cup cup = null;
        cdl.countDown();
        try {
            cdl.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(Digger.class.getName()).log(Level.SEVERE, null, ex);
        }
        while(true) {            
            try {
                cup = cupArray.getCupFromArray(id);
                if(cup != null) {
                    catchCounter++;
                    System.out.println("Digger " + id + " drink from cup" + cup.getId() + ". Amount of drinks: " + catchCounter);
                }
                cupArray.returnCupToArray(cup);
            } catch (InterruptedException ex) {
                Logger.getLogger(Digger.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
