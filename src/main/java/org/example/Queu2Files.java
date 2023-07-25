package org.example;

import umontreal.ssj.randvar.ExponentialGen;
import umontreal.ssj.randvar.RandomVariateGen;
import umontreal.ssj.rng.MRG32k3a;
import umontreal.ssj.rng.RandomStream;
import umontreal.ssj.simevents.Accumulate;
import umontreal.ssj.simevents.Event;
import umontreal.ssj.simevents.Sim;
import umontreal.ssj.stat.Tally;

import java.util.LinkedList;

public class Queu2Files {

    // Première file
    RandomVariateGen genArr1;
    RandomVariateGen genServ1;

    // Première file
    RandomVariateGen genArr2;
    RandomVariateGen genServ2;
    LinkedList<Customer> waitList1 = new LinkedList<Customer>();
    LinkedList<Customer> waitList2 = new LinkedList<Customer>();

    LinkedList<Customer> servList = new LinkedList<Customer>();
    Tally custWaits1     = new Tally ("Waiting times for the first");
    Accumulate totWait1  = new Accumulate ("Size of queue for the first");

    Tally custWaits2     = new Tally ("Waiting times for the second");
    Accumulate totWait2  = new Accumulate ("Size of queue for the second");
    Integer nbrServer ;
    RandomStream generoutage;
    double p1 =0.4;



    class Customer { double arrivTime, servTime; int type; }

    public Queu2Files (double lambda1, double lambda2, double mu1, double mu2, int s) {
        genArr1 = new ExponentialGen(new MRG32k3a(), lambda1);
        genServ1 = new ExponentialGen (new MRG32k3a(), mu1);
        genArr2 = new ExponentialGen(new MRG32k3a(), lambda2);
        genServ2 = new ExponentialGen (new MRG32k3a(), mu2);
        this.nbrServer = s;
        generoutage = new MRG32k3a();


    }

    public void simulate (double timeHorizon) {
        Sim.init();
        new Queu2Files.EndOfSim().schedule (timeHorizon);
        new Queu2Files.Arrival(1).schedule (genArr1.nextDouble());
        new Queu2Files.Arrival(2).schedule (genArr2.nextDouble());
        Sim.start();
    }

    class Arrival extends Event {
        int type;
        public Arrival(int type){
            this.type = type;
        }
        public void actions() {
            Customer cust = new Customer();   // Cust just arrived.
            if (type==1){
                new Queu2Files.Arrival(1).schedule (genArr1.nextDouble()); // type1 Next arrival.
                cust.servTime = genServ1.nextDouble();
            }else {
                new Queu2Files.Arrival(2).schedule (genArr2.nextDouble()); // type2 Next arrival.
                cust.servTime = genServ2.nextDouble();

            }
            cust.arrivTime = Sim.time();
            cust.type = type;

            if (servList.size() >= nbrServer) {      // Must join the queue.
                if (type==1){
                    waitList1.addLast (cust);
                    totWait1.update (waitList1.size());
                }else {
                    waitList2.addLast (cust);
                    totWait2.update (waitList2.size());
                }

            } else {                       // Starts service.
                if (type==1){
                    custWaits1.add (0.0);
                }else {
                    custWaits2.add (0.0);
                }

                servList.addLast (cust);
                new Queu2Files.Departure().schedule (cust.servTime);
            }
        }
    }

    class Departure extends Event {
        public void actions() {
            servList.removeFirst();
            if(routage2()==1){
                Queu2Files.Customer cust = waitList1.removeFirst();
                totWait1.update (waitList1.size());
                custWaits1.add (Sim.time() - cust.arrivTime);
                new Queu2Files.Departure().schedule (cust.servTime);
                servList.addLast (cust);
            }else if (routage2()==2){
                Queu2Files.Customer cust = waitList2.removeFirst();
                totWait2.update (waitList2.size());
                custWaits2.add (Sim.time() - cust.arrivTime);
                new Queu2Files.Departure().schedule (cust.servTime);
                servList.addLast (cust);
            }

            /*if (waitList1.size() > 0) {
                // Starts service for next one in queue.
                Queu2Files.Customer cust = waitList.removeFirst();
                totWait.update (waitList.size());
                if(Sim.time()>10000)
                    custWaits.add (Sim.time() - cust.arrivTime);
                servList.addLast (cust);
                new Queu2Files.Departure().schedule (cust.servTime);
            }*/
        }

        public int routage(){
            if(waitList1.size() > 0 && waitList2.size() == 0)
                return 1;
            else if (waitList2.size() > 0 && waitList1.size() == 0)
                return 2;
            else if (waitList2.size() > 0 && waitList1.size() > 0){
                if (waitList1.get(0).arrivTime > waitList2.get(0).arrivTime)
                    return 2;
                else {
                    return 1;
                }
            }else
                return 0;

        }
        public int routage2(){
            if(waitList1.size() > 0 && waitList2.size() == 0)
                return 1;
            else if (waitList2.size() > 0 && waitList1.size() == 0)
                return 2;
            else if(waitList2.size() > 0 && waitList1.size() > 0){
                if (generoutage.nextDouble()< p1){
                    return 1;
                }
                else
                    return 2;

            }
            else
                return 0;
        }
    }

    class EndOfSim extends Event {
        public void actions() {
            Sim.stop();
        }
    }

    public static void main (String[] args) {

        double mu1=2.0;
        double lambda1= 2.0;

        double mu2=2.2;
        double lambda2= 2.1;

        int serveurs = 3;

        Queu2Files queue = new Queu2Files (lambda1, lambda2, mu1, mu2, serveurs);
        queue.simulate (100000.0);
        System.out.println (queue.custWaits1.report());
        System.out.println (queue.totWait1.report());
        System.out.println (queue.custWaits2.report());
        System.out.println (queue.totWait2.report());

       /*double Wq=(lambda)/(mu*(mu-lambda));
        System.out.println ("Wq="+Wq);
        double Lq=(lambda*lambda)/(mu*(mu-lambda));
        System.out.println ("Lq="+Lq);
        */
    }
}
