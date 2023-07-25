package org.example;

import umontreal.ssj.randvar.*;
import umontreal.ssj.rng.*;
import umontreal.ssj.simevents.*;
import umontreal.ssj.stat.*;

import java.util.LinkedList;
//    File M/M/1
public class QueueEv {

   RandomVariateGen genArr;
   RandomVariateGen genServ;
   LinkedList<Customer> waitList = new LinkedList<Customer> ();
   //LinkedList<Customer> waitList2 = new LinkedList<Customer> ();
   LinkedList<Customer> servList = new LinkedList<Customer> ();
   Tally custWaits     = new Tally ("Waiting times");
   Accumulate totWait  = new Accumulate ("Size of queue");
    
   class Customer { double arrivTime, servTime; }

   public QueueEv (double lambda, double mu) {
      genArr = new ExponentialGen (new MRG32k3a(), lambda);
      genServ = new ExponentialGen (new MRG32k3a(), mu);
      
      
   }

   public void simulate (double timeHorizon) {
      Sim.init();
      new EndOfSim().schedule (timeHorizon);
      new Arrival().schedule (genArr.nextDouble());
      Sim.start();
   }

   class Arrival extends Event {
      public void actions() {
         new Arrival().schedule (genArr.nextDouble()); // Next arrival.
         Customer cust = new Customer();  // Cust just arrived.
         cust.arrivTime = Sim.time();
         cust.servTime = genServ.nextDouble();
         if (servList.size() >0) {       // Must join the queue.
            waitList.addLast (cust);
            totWait.update (waitList.size());
         } else {                         // Starts service.
         	if(Sim.time()>10000)
        	       custWaits.add (0.0);
            servList.addLast (cust);
            new Departure().schedule (cust.servTime);
         }
      }
   }

   class Departure extends Event {
      public void actions() {
         servList.removeFirst();
         if (waitList.size() > 0) {
            // Starts service for next one in queue.
            Customer cust = waitList.removeFirst();
            totWait.update (waitList.size());
             if(Sim.time()>10000)
               custWaits.add (Sim.time() - cust.arrivTime);
            servList.addLast (cust);
            new Departure().schedule (cust.servTime);
         }
      }
   }

   class EndOfSim extends Event {
      public void actions() {
         Sim.stop();
      }
   }

   public static void main (String[] args) {
	
	  double mu=2.0;
	  double lambda= 1.0;
	  
      QueueEv queue = new QueueEv (lambda, mu);
      queue.simulate (100000.0);
      System.out.println (queue.custWaits.report());
      System.out.println (queue.totWait.report());
    
      double Wq=(lambda)/(mu*(mu-lambda));
      System.out.println ("Wq="+Wq);
      double Lq=(lambda*lambda)/(mu*(mu-lambda));
      System.out.println ("Lq="+Lq);
   }
}
