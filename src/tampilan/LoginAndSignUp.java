package tampilan;

import service.DBSeeder;
import service.ReservasiService; // <-- Import Service
import java.util.Timer;         // <-- Import Timer
import java.util.TimerTask;

public class LoginAndSignUp {

    public static void main(String[] args) {

        DBSeeder.seedAdminUser(); // <-- Panggil metode seeder

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ReservasiService rs = new ReservasiService();
                rs.checkAndFinishReservations();
            }
        },
                0,
                60 * 60 * 1000
        );
        
        Login LoginFrame = new Login();
        LoginFrame.setVisible(true);
        LoginFrame.pack();
        LoginFrame.setLocationRelativeTo(null);
    }

}
