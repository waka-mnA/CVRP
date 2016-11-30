import java.util.*;

public class Chromosome{

  //private ArrayList<Integer> c = new ArrayList<Integer>();
  private int[] c;
  private int length;
  private double fitness;

  public Chromosome(){ }

  public Chromosome(int l){
    length = l;
    c = new int[l];
    fitness = 0;
  }

  public void set_gene(int index, int gene){
    c[index] = gene;
  }

  public int get(int index){
    return c[index];
  }
  public double get_f(){
    return fitness;
  }
  public void set_f(double f){
    fitness = f;
  }
  public int get_length(){
    return length;
  }
  public void get_rand_chrom(){
    ArrayList<Integer> list = new ArrayList<Integer>();
    for (int i = 0;i<length;i++){
      list.add(c[i]);
    }
    Collections.shuffle(list);
    for (int i = 0;i<length;i++){
      c[i] = list.get(i);
    }
  }
  public Chromosome copy(){
    Chromosome a= new Chromosome(length);
    for (int i = 0;i<length;i++){
      a.set_gene(i, c[i]);
    }
    a.set_f(fitness);
    return a;
  }
  public int[] get_range(int i1, int i2){
    return Arrays.copyOfRange(c, i1, i2);
  }
}
