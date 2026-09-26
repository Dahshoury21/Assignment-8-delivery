/** One-shot verification of the Part B worked example (not part of the interactive app). */
public class VerifyExample {
	public static void main(String[] args) throws Exception {
		Platform platform = SampleData.bootstrap();
		System.out.println(SampleData.runWorkedExample(platform));
	}
}
