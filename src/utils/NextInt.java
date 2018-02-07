package utils;

public class NextInt {
    /**
     * 下一个整十的数，比如传入的数字是15，那么下一个整十的数就是20
     * @param i
     * @return
     */
    public static int nextSortTen(int i){
        while(true){
            i++;
            if(i%10==0){
                break;
            }
        }
        return i;
    }
}
