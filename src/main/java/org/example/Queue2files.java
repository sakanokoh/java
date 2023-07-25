package org.example;

import umontreal.ssj.randvar.ExponentialGen;
import umontreal.ssj.randvar.RandomVariateGen;
import umontreal.ssj.rng.MRG32k3a;
import umontreal.ssj.simevents.Accumulate;
import umontreal.ssj.simevents.Event;
import umontreal.ssj.simevents.Sim;
import umontreal.ssj.stat.Tally;

import java.util.LinkedList;

public class Queue2files {


    RandomVariateGen genArr;
    RandomVariateGen genServ;

    Integer nbrServer ;
    LinkedList<Customer> waitList1 = new LinkedList<Customer> ();
    LinkedList<Customer> waitList2 = new LinkedList<Customer> ();
    LinkedList<Customer> servList = new LinkedList<Customer> ();
    Tally custWaits1     = new Tally ("Waiting times for the first");
    Accumulate totWait1  = new Accumulate ("Size of queue for the first");

    Tally custWaits2     = new Tally ("Waiting times for the second");
    Accumulate totWait2  = new Accumulate ("Size of queue for the second");
    class Customer { double arrivTime, servTime; }

    public Queue2files (double lambda, double mu, Integer s) {
        genArr = new ExponentialGen(new MRG32k3a(), lambda);
        genServ = new ExponentialGen (new MRG32k3a(), mu);
        nbrServer = s;


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
            if (servList.size() >= nbrServer) {       // Must join the queue.
                if (waitList2.size() >= waitList1.size()) {
                    waitList1.addLast(cust);
                    totWait1.update(waitList1.size());
                }else {
                    waitList2.addLast(cust);
                    totWait2.update(waitList1.size());
                }
            } else {                         // Starts service.
                //if(Sim.time()>10000)
                    //custWaits1.add (0.0);
                servList.addLast (cust);
                new Departure().schedule (cust.servTime);
            }
        }
    }

    class Departure extends Event {
        public void actions() {
            servList.removeFirst();
            if (waitList1.size() > 0 && waitList2.size()> 0) {
                // Starts service for next one in queue.
                Customer cust1 = waitList1.removeFirst();
                Customer cust2 = waitList2.removeFirst();
                Customer cust;
                if (cust1.arrivTime <= cust2.arrivTime){
                    cust = cust1;
                    totWait1.update (waitList1.size());
                    waitList2.addFirst(cust2);
                    //if(Sim.time()>10000)
                    custWaits1.add (Sim.time() - cust.arrivTime);
                }else {
                    cust = cust2;
                    totWait2.update (waitList2.size());
                    waitList1.addFirst(cust1);
                    //if(Sim.time()>10000)
                    custWaits2.add (Sim.time() - cust.arrivTime);
                }
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
        Integer s = 3;

        Queue2files queues = new Queue2files (lambda, mu, s);
        queues.simulate (100000.0);

        System.out.println("");
        System.out.println("------------------------------- La première file ----------------------------------");
        System.out.println("");
        System.out.println (queues.custWaits1.report());
        System.out.println (queues.totWait1.report());

        System.out.println("---------------------------------***---------------------------------");
        System.out.println("");

        System.out.println("------------------------------- La deuxième file ----------------------------------");
        System.out.println("");
        System.out.println (queues.custWaits2.report());
        System.out.println (queues.totWait2.report());

        System.out.println("------------------------------------***--------------------------------------");

        double Wq=(lambda)/(mu*(mu-lambda));
        System.out.println ("Wq="+Wq);
        double Lq=(lambda*lambda)/(mu*(mu-lambda));
        System.out.println ("Lq="+Lq);
    }
}
