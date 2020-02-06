
import java.util.regex.*;

public class Test {

    public static void main(String[] args) {
        String a = "150M";
        String b = "81M810N36M33S";

        //Pattern pattern = Pattern.compile("^\\d+M$");
        Pattern pattern = Pattern.compile("(\\d+)([MIDNSHP=X])");
        //Pattern pattern = Pattern.compile("/\\d+M/");

        Matcher matcherA = pattern.matcher(a);
        System.out.println("A:" + matcherA.matches());

        Matcher matcherB = pattern.matcher(b);
        //System.out.println("B:" + matcherB.matches());
        while(matcherB.find()) {
            System.out.println("BBB");
            System.out.println("B1:" + matcherB.group(1));
            System.out.println("B2:" + matcherB.group(2));
        }

        Pattern pattern2 = Pattern.compile("\\d+N");
        Matcher matcherC = pattern2.matcher(b);
        System.out.println("C:" + matcherC.find());


    }
}