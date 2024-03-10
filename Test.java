public class Test {
    public static void main(String[] args) {

        int gNumBodies = Integer.parseInt(args[0]);
        int numWorkers = Integer.parseInt(args[1]);

        int stripSize = (gNumBodies % numWorkers == 0) ? (gNumBodies / numWorkers) : ((gNumBodies / numWorkers) + 1);
        int start;
        int end;

        int total = 0;

        for (int i = 0; i < numWorkers; i++) {
            start = i * stripSize;
            end = (i == numWorkers - 1) ? (gNumBodies - 1) : (start + stripSize - 1); // edge case. Giving the last worker extra work if the division is uneven
            total += (end - start) + 1;
            System.out.println("Worker " + i + " has planets from " + start + " to " + end + ", total: " + (end-start)); 
        }

        System.out.println(total + " things were assigned");
        
    }
}
