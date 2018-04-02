package autoplay;

import java.io.IOException;

//Author: Lilly Tong, Eric Crawford
//
// Assumes all the code in ``src`` has been compiled, and the resulting
// class files were stored in ``bin``.
//
// From the root directory of the project, run
//
//     java -cp bin autoplay.AutoplayN n_games
//
// Note: The script is currently set up to have the StudentPlayer play against
// RandomHusPlayer. In order to have different players participate, you need
// to change the variables ``client1_line`` and ``client2_line``. Make sure
// that in those lines, the classpath and the class name is set appropriately
// so that java can find and run the compiled code for the agent that you want
// to test. For example to have StudentPlayer play against itself, you would
// change ``client2_line`` to be equal to ``client1_line``.
//
public class AutoplayN {
    public static void main(String args[]) {
        int n_games;
        try {
            n_games = Integer.parseInt(args[0]);
            if (n_games < 1) {
                throw new Exception();
            }
        } catch (Exception e) {
            System.err.println(
                    "First argument to Autoplay must be a positive int " + "giving the number of games to play.");
            return;
        }

        try {
            ProcessBuilder server_pb = new ProcessBuilder("java", "-cp", "bin", "boardgame.Server", "-ng", "-k");
            server_pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);

            Process server = server_pb.start();
            
            Class[] cArg = new Class[1];
            cArg[0] = Integer.class;
            ProcessBuilder client1_pb = new ProcessBuilder("java", "-cp", "bin", "-Xms520m", "-Xmx520m",
                    "boardgame.Client", "student_player.LearningPlayer1");
            client1_pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);

            ProcessBuilder client2_pb = new ProcessBuilder("java", "-cp", "bin", "-Xms520m", "-Xmx520m",
                    "boardgame.Client", "student_player.LearningPlayer2");
            client2_pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            
            
            /*
             * Stats
             */
            int p1MuscWin = 0;
            int p1SwedWin = 0;
            int draws = 0;
            
            for (int i = 0; i < n_games; i++) {
                System.out.println("Game " + i);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                Process client1 = ((i % 2 == 0) ? client1_pb.start() : client2_pb.start());

                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                Process client2 = ((i % 2 == 0) ? client2_pb.start() : client1_pb.start());

                try {
                    client1.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    client2.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            server.destroy();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}