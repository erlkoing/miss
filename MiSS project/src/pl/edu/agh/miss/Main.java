package pl.edu.agh.miss;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// @formatter:off
		String[] defargs = new String[] 
			{
				"-gui", "false", 
				"-welcome", "true",
				"-cli", "true",
				"-printpass", "false", 
				"-logging", "false"
			};
		//@formatter:on

		String[] newargs = new String[defargs.length + args.length];
		System.arraycopy(defargs, 0, newargs, 0, defargs.length);
		System.arraycopy(args, 0, newargs, defargs.length, args.length);

		int platformsNumber = 1;
		Platform[] platforms = new Platform[platformsNumber];

		for (int i = 0; i < platformsNumber; ++i) {
			platforms[i] = new Platform();
			platforms[i].createCustomPlatform(newargs, "Workplace" + i);
		}

		System.out.println("Workspaces created");

		for (int i = 0; i < platformsNumber; ++i) {
			platforms[i].createWorkplace();
		}
	}
}
