package pendataan.parkir.kedungpane;
import java.util.Random;

public class GenerateRandomValue {
        public static Random RANDOM = new Random();
        public static String getId(int len) {
            final String DATAID = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
            StringBuilder sbi = new StringBuilder(len);
            for (int a = 0; a < len; a++) {
                sbi.append(DATAID.charAt(RANDOM.nextInt(DATAID.length())));
            }
            return sbi.toString();
        }
}