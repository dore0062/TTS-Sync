import java.net.*;
import java.io.*;
import com.google.gson.*;
import com.intellij.openapi.actionSystem.*;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.NotNull;

// This class handles incoming/outgoing TTS messages

public class TTSListener extends AnAction {
    private TTSListener() {
        super("Start Listen Server");
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        serverOpen t = new serverOpen();
        t.start();
    }
}

// Client > Server
class serverOpen extends Thread {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private BufferedReader in;
    private Boolean open = true;

    public void run() {
        try {
            serverSocket = new ServerSocket(39998);
            Notifications.Bus.notify(new Notification("notification", "TTS listen server started successfully",
                    "Listener server started successfully on port 39998.", NotificationType.INFORMATION));
        } catch (IOException e) {
            Notifications.Bus.notify(new Notification("sync-error", "Port already in use",
                    "The port 39998 is already in use, a server may already be opened.", NotificationType.ERROR));
            return;
        }

        while (open) {
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            String JSON = "";
            String inputLine;

            while (true) { try { if ((inputLine = in.readLine()) == null) break; } catch (IOException e) { e.printStackTrace(); return; }
                if (".".equals(inputLine)) {
                    break;
                }
                JSON = JSON.concat(inputLine);
            }

            if (!JSON.equals("")) {
                JsonObject jObject = new JsonParser().parse(JSON).getAsJsonObject();
                int aObj = jObject.get("messageID").getAsInt();

                switch(aObj) {
                    case 0: // Loaded from context menu TODO debug
                        Notifications.Bus.notify(new Notification("message", "Script(s) loaded from Tabletop Simulator", "TTS has loaded a new script",
                                NotificationType.INFORMATION));

                        JsonArray initial = jObject.get("scriptStates").getAsJsonArray();
                        JsonObject data = initial.get(0).getAsJsonObject();
                        String name = data.get("name").getAsString();
                        String guid = data.get("guid").getAsString(); // TODO create catch case where two PSI files already have the same GUID in the event TTS sends two of the same GUID
                                                                      // Theoretically, it is possible for TTS to send the same GUID since they hold for one frame.
                        String script = utilities.convertString(data.get("script").getAsString());

                        createFile.newFile(name, "lua", script, guid, true); // Tries to create the file in the system
                        break;

                    case 1: // Loading a New Game - same as getLua.
                        JsonArray initial2 = jObject.get("scriptStates").getAsJsonArray();
                        for (int i=0; i < initial2.size(); i++) {
                            JsonObject data2 = initial2.get(i).getAsJsonObject();
                            String name2 = data2.get("name").getAsString();
                            String guid2 = data2.get("guid").getAsString();
                            String script2 = utilities.convertString(data2.get("script").getAsString());

                            if (i == 0) { // 0 is global, set as focus
                                createFile.newFile(name2, "lua", script2, guid2, true);
                            }
                            else {
                                createFile.newFile(name2, "lua", script2, guid2, false);
                            }

                            if (data2.has("ui")) {
                                createFile.newFile(name2, "xml", utilities.convertString(data2.get("ui").getAsString()), guid2, false);
                            }
                        }
                        createFile.synced = true;
                         break;

                    case 2: // Print/Debug message TODO debug
                        String message = jObject.get("message").getAsString();
                        Notifications.Bus.notify(new Notification("message", "Tabletop Simulator Message", message, NotificationType.INFORMATION));
                        break;

                    case 3: // Error message TODO add XML / test XML
                        String errorPrefix = jObject.get("errorMessagePrefix").getAsString();
                        String guid3 = jObject.get("guid").getAsString();
                        String error = jObject.get("error").getAsString();

                        Project p= ProjectManager.getInstance().getOpenProjects()[0];
                        gotoError.main(error, guid3,".lua", p, errorPrefix);
                         break;

                    case 4: // Custom Message TODO Have a bunch of different cases for / n at some point
                        break;

                    case 5: // We should only return "Script loaded successfully" TODO start
                        System.out.println("Got msg 5");
                        break;

                    case 6: // In-game auto/manual save. No usage.
                        break;
                    case 7: // New object created TODO check if object has lua script via sending a message then import if yes. Check if it is the PLAYER and not a SCRIPT.
                        System.out.println("Got msg 7");
                        break;

                    default: // Anything outside the range of 0 - 7
                        System.out.println("Invalid message received");
                        break;
                }
            }
        }
    }

    public void close() throws IOException {
        in.close();
        open = false;
        clientSocket.close();
        serverSocket.close();
    }
}

// Server > Client
// Expects JSON as a string
// Message IDs:
// 0 : Get Lua Scripts (client sends scripts to server)
// 1 : Save & Play (send scripts)
// 2 : Custom Message
// 3 : Execute lua code (execute a lua string)
class sendToClient extends Thread {
    private final String parameter;
    private Boolean open = true;

    sendToClient(String parameter){
        this.parameter = parameter;
    }

    public void run() {
        Socket socket;
        try {
            socket = new Socket("localhost",39999);
        } catch (IOException e) {
            Notifications.Bus.notify(new Notification("sync-error", "Connection failed",
                    "Could not connect to localhost:39999, a server may already be opened, the last connection may have failed. If this issue occurs frequently, please report it on Github.", NotificationType.ERROR));
            return;
        }

        try (OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream())) {
            out.write(parameter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
