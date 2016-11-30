import java.io.*;
import java.lang.*;
import java.util.*;
import java.text.DecimalFormat;
public class CVRP{

  public CVRP(){

  }

  //public int NUM_VEHICLES = 10;
  private int NUM_GENERATION = 1000;
  private double MUTATION_RATE = 0.03;
  private int POPULATIONS = 200;

  private CVRPData data = new CVRPData();
  private String fileName = "fruitybun250.vrp";
  private int numNodes = 0;
  private int Q = 0;
  private double prob[] = new double[POPULATIONS];
  private ArrayList<Chromosome> chromosome = new ArrayList<Chromosome>();

  //Retrieve data from vrp file
  public void get_data(){
    data.readFile(fileName);
    numNodes = data.NUM_NODES;
    Q = data.VEHICLE_CAPACITY;
  }

  //Genetic Algorithm
  public void genetic_alg(){
    chromosome = new ArrayList<Chromosome>();
    Chromosome c = new Chromosome(numNodes-1);

    //Do not include depot in the chromosome
    for (int i = 2;i<=c.get_length()+1;i++){
        c.set_gene(i-2, i);
    }

    double sum_f = 0;
    //Initial population
    for (int i = 0;i<POPULATIONS;i++){

      Chromosome new_c = c.copy();
      new_c.get_rand_chrom();
      double f = fitness(new_c);
      new_c.set_f(f);

      chromosome.add(new_c);
      sum_f += f;
    }

    //Fitness evaluation
    double cum = 0;
    for (int i = 0;i<POPULATIONS;i++){
      cum += chromosome.get(i).get_f()/sum_f;
      prob[i] = cum;
    }

    Random rn = new Random();
    int range1, range2;
    double r;
    ArrayList<Chromosome> nextGen= new ArrayList<Chromosome>();
    Chromosome p1, p2;
    //Loop
    for (int i = 0;i< NUM_GENERATION;i++){
      //Generation
      int num = 0;
      nextGen= new ArrayList<Chromosome>();
      while (num <POPULATIONS)
      {
        //Select parents
          r = Math.random();
          int ind = find_index(r);
          p1 =chromosome.get(ind);
          r = Math.random();
          ind = find_index(r);
          p2 =chromosome.get(ind);
        //Order Crossover
        //Choose range
        range1 = rn.nextInt(p1.get_length());
        range2 = rn.nextInt(p1.get_length());
        if (range1 == range2) range2 = rn.nextInt(p1.get_length());
        if (range1>range2) {
          int tmp = range1;
          range1 = range2;
          range2 = tmp;
        }
        //Child 1
        Chromosome child1 = order_crossover(p1, p2, range1, range2);
        //Child 2
        Chromosome child2 = order_crossover(p2, p1, range1, range2);
        //Mutation: swap two selected gene for three times;
        double m_prob = Math.random();
        if (m_prob<MUTATION_RATE){
          for(int k = 0;k<3;k++){
            int point1 = rn.nextInt(p1.get_length());
            int point2 = rn.nextInt(p1.get_length());
            child1 = swap(child1, point1, point2);
            child2 = swap(child2, point1, point2);
          }
        }
        //New population individuals evaluation
        double f_1 = fitness(child1);
        double f_2 = fitness(child2);
        child1.set_f(f_1);
        child2.set_f(f_2);
        //Insert the pair of individuals in the new population
        nextGen.add(child1);
        nextGen.add(child2);

        num+=2;
      }
      sum_f = 0;
      //Initial population
      for (int j = 0;j<POPULATIONS;j++){
        sum_f += nextGen.get(j).get_f();
      }

      cum = 0;
      for (int j = 0;j<POPULATIONS;j++){
        cum += nextGen.get(j).get_f()/sum_f;
        prob[j] = cum;
      }
      //Elitism replacement
      //Terminate Condition
      chromosome = new ArrayList<Chromosome>(nextGen);
    }



  }

  //FIND THE ROUTE WHICH HAS BEST FITNESS VALUE
  private int best_fitness(){
  //System.out.println("Best_fitness START");
    double max = 0;
    int index = 0;
    for (int i = 0;i<chromosome.size();i++){
      if (max<chromosome.get(i).get_f()){
        max = chromosome.get(i).get_f();
        index = i;
      }
    }
    //System.out.println("Best_fitness DONE");
    return index;
  }
  //FUNCTION TO OPERATE ORDER CROSSOVER
  private Chromosome order_crossover(Chromosome p1, Chromosome p2,
      int range1, int range2){
    //System.out.println("Crossover START");

    Chromosome c = new Chromosome(p1.get_length());

    //CREATE LIST TO STORE THE UNCHANGED PARTS
    ArrayList<Integer> list = new ArrayList<Integer>();
    int[] subArray = p1.get_range(range1, range2);
    for (int i = 0;i<subArray.length;i++){
      list.add(subArray[i]);
    }

    int n = 0;    //Cursor on parent chromosome
    int count = 0;//Cursor on child chromosome

    while (count<c.get_length()){

      if (count>=range1 && count<range2){
        c.set_gene(count, p1.get(count));
        count++;
        continue;
      }
      if(!list.contains(p2.get(n))){
        c.set_gene(count, p2.get(n));
        count++;
      }
      n++;
    }
    //System.out.println("Crossover DONE");
    return c;

  }
  private Chromosome swap(Chromosome c, int p1, int p2){
    int tmp = c.get(p1);
    c.set_gene(p1, c.get(p2));
    c.set_gene(p2, tmp);
    return c;
  }
  private double cost (Chromosome c){
    double sum = 0;
    sum += data.getDistance(1, c.get(0));
    int i = 1;
    int node1 = 1;
    int node2 = 1;
    int capacity = data.getDemand(c.get(0));
    while(i<c.get_length()){
      capacity+= data.getDemand(c.get(i));
      node2 = c.get(i);
      if (capacity>Q) node2 = 1;

      sum+= data.getDistance(c.get(i-1), node2);

      if (capacity > Q){
        sum+= data.getDistance(1, c.get(i));
        capacity = data.getDemand(c.get(i));
      }
      i++;
    }
    return sum;
  }
  //Calculate fitness = total sum  of distances for each route
  private double fitness(Chromosome c){
  //System.out.println("Fitness START");
    double sum=cost(c);

    //System.out.println("Fitness DONE");
    return 1/sum * 100;
  }

  private void write(){
    try{
      DecimalFormat df = new DecimalFormat("#.000");
      File file = new File("best_solution.txt");
      BufferedWriter bw = new BufferedWriter(new FileWriter(file));
      PrintWriter pw = new PrintWriter(bw);

      pw.println("login wk13290 68714");
      pw.println("name Wakako Kurata");
      pw.println("algorithm Genetic Algorithm with Order Crossover");

      String str = "1";;
      int index = best_fitness();
      int capacity = 0;
      Chromosome c = chromosome.get(index);

      double cost = cost(c);
      pw.println("cost "+df.format(cost));
      for (int i = 0;i<c.get_length();i++){
        capacity+= data.getDemand(c.get(i));
        if (capacity>Q) {
          pw.println(str+"->1");
          str = "1";
          capacity = data.getDemand(c.get(i));
        }
        str = str+"->"+ c.get(i);

      }

      pw.println(str+"->1");

      pw.close();
    }
    catch(IOException e){
        System.out.println(e);
    }

  }
  public int find_index(double p){
    for (int i = 0;i<POPULATIONS;i++){
      if (prob[i]>=p){
        if (i == 0) return 0;
        return i;
      }
    }
    return POPULATIONS-1;
  }

  public static void main(String[] args){
      CVRP cvrp = new CVRP();
      cvrp.get_data();
      System.out.println("DATA END");
      Chromosome c = new Chromosome(9);
      Chromosome c2 = new Chromosome(9);
      int[] test1 = {10, 2, 3, 4, 5, 6, 7, 8, 9};
      int[] test2 = {3, 8, 5, 4, 9, 2, 7, 6, 1};
      for (int i = 0;i<9;i++){
        c.set_gene(i, test1[i]);
          c2.set_gene(i, test2[i]);
      }
      int range1 = 3;
      int range2 = 6;
      //cvrp.order_crossover(c, c2, range1, range2);
      double costtest = cvrp.cost(c);
      System.out.println(costtest);
      cvrp.genetic_alg();

      System.out.println("GA END");
      cvrp.write();
    return;
  }



}
