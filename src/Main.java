public class Main {
	public static void main(String[] args) {
		Platform platform = SampleData.bootstrap();
		System.out.println("Masr Delivery loaded with sample data.");
		System.out.println("Tip: Admin → option 6 verifies the Part B worked example (258.00 EGP).");
		new ConsoleApp(platform).run();
	}
}
