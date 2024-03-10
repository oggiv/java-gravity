public class BagOfPlanets {

    int index;
    int max;

    public BagOfPlanets(int max) {
        index = -1;
        this.max = max;
    }

    public synchronized int next(){
        if(index == max){
            
        }
        return index++;
    }
}