import java.util.*;
import java.io.*;
public class test{
	public static void main(String[] args) throws IOException {
		Scanner sc = new Scanner(new File("metro.txt"));
		String line = sc.nextLine();
		System.out.println(line);
		System.out.println(line.substring(4,6));
		line = sc.nextLine();
		System.out.println(line);
		System.out.println(line.length());
		sc.close();
	}
}