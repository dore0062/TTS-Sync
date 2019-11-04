import com.intellij.openapi.wm.ToolWindow;
import javax.swing.*;

public class propertiesPanel {
    private JPanel myToolWindowContent;
    private JTree tree1;

    propertiesPanel(ToolWindow toolWindow) {
//        hideToolWindowButton.addActionListener(e -> toolWindow.hide(null));
//        refreshToolWindowButton.addActionListener(e -> currentDateTime());
//        this.currentDateTime();
    }

//    public void currentDateTime() {
//        // Get current date and time
//        Calendar instance = Calendar.getInstance();
//        currentDate.setText(String.valueOf(instance.get(Calendar.DAY_OF_MONTH)) + "/"
//                + String.valueOf(instance.get(Calendar.MONTH) + 1) + "/" +
//                String.valueOf(instance.get(Calendar.YEAR)));
//        currentDate.setIcon(new ImageIcon(getClass().getResource("/myToolWindow/Calendar-icon.png")));
//        int min = instance.get(Calendar.MINUTE);
//        String strMin;
//        if (min < 10) {
//            strMin = "0" + String.valueOf(min);
//        } else {
//            strMin = String.valueOf(min);
//        }
//        currentTime.setText(instance.get(Calendar.HOUR_OF_DAY) + ":" + strMin);
//        currentTime.setIcon(new ImageIcon(getClass().getResource("/myToolWindow/Time-icon.png")));
//        // Get time zone
//        long gmt_Offset = instance.get(Calendar.ZONE_OFFSET); // offset from GMT in milliseconds
//        String str_gmt_Offset = String.valueOf(gmt_Offset / 3600000);
//        str_gmt_Offset = (gmt_Offset > 0) ? "GMT + " + str_gmt_Offset : "GMT - " + str_gmt_Offset;
//        timeZone.setText(str_gmt_Offset);
//        timeZone.setIcon(new ImageIcon(getClass().getResource("/myToolWindow/Time-zone-icon.png")));
//
//
//    }

    JPanel getContent() {
        return myToolWindowContent;
    }
}