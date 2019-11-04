import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;

class createFile {
    static boolean replace;
    static boolean synced;
    static String filename;

    private static boolean noDialogue;
    private static PsiFile file;
    private static PsiDirectory dir;
    private static PsiFile[] foundFiles;

    static void newFile(String Name, String FileType, String Text, String GUID, boolean setActive) {
        noDialogue = false;
        Project p = ProjectManager.getInstance().getOpenProjects()[0];
        final FileEditorManager fileEditorManager = FileEditorManager.getInstance(p);

        WriteCommandAction.runWriteCommandAction(p, () -> {
            VirtualFile folder = p.getProjectFile();

            assert folder != null;
            PsiFile d = PsiManager.getInstance(p).findFile(folder);
            assert d != null;
            dir = d.getContainingFile().getContainingDirectory();  // FIXME this is the wrong directory .idea
            filename = Name + " " + GUID + "." + FileType;

            file = PsiFileFactory.getInstance(p).createFileFromText(filename, StdFileTypes.JAVA, Text); // Extension overwrites fileType
            foundFiles = FilenameIndex.getFilesByName(p, filename, GlobalSearchScope.allScope(p)); // Search the entire project if the file exists

            if (foundFiles.length <= 0) { // Object does not exist
                    PsiFile newFile = (PsiFile) dir.add(file);
                    gameSync.syncedFiles.putIfAbsent(filename, GUID);
                    if (setActive) fileEditorManager.openFile(newFile.getVirtualFile(), true);
                    noDialogue = true;
            }

            else if( foundFiles[0].textMatches(Text)) { // Object exists but contents are the same.
                gameSync.syncedFiles.putIfAbsent(filename, GUID);
                noDialogue = true;
            }
        });

        if (!noDialogue) { // Object exists and contents are not the same. Ask if they want to replace.
            replaceConfirmation.main(null); // This cannot run in WriteCommandAction, stops when dialogue is closed
            WriteCommandAction.runWriteCommandAction(p, () -> {
            if (replace) {
                    foundFiles[0].delete(); // TODO ask if there is a replace instead of delete then recreate
                    PsiFile newFile = (PsiFile) dir.add(file);
                    gameSync.syncedFiles.putIfAbsent(filename, GUID);
                    fileEditorManager.openFile(newFile.getVirtualFile(), true); // Always focuses regardless
            }
            else {
                gameSync.syncedFiles.putIfAbsent(filename, GUID);
            }
            });
        }
    }
}

class gotoError {
    static void main(String Text, String GUID, String Filetype, Project p, String errorPrefix) { // FIXME almost certainly will crash if objName has :( or ( - switch to regular expression (
        WriteCommandAction.runWriteCommandAction(p, () -> {
            String[] foundFiles = FilenameIndex.getAllFilenames(p); // We have to do this since the GUID is only partially the filename. The GUID is always unique.
            java.util.regex.Pattern c = java.util.regex.Pattern.compile(GUID + Filetype); // Found the GUID with the given extension
            java.util.regex.Matcher m = c.matcher("");

            for(String s : foundFiles)
            {
                m.reset(s);
                if (m.find()) gotoError.file(s, p, Text, GUID, errorPrefix);
            }
        });
    }

    private static void file(String file_location, Project p, String Text, String GUID, String errorPrefix){
        WriteCommandAction.runWriteCommandAction(p, () -> {
            PsiFile[] foundFiles = FilenameIndex.getFilesByName(p, file_location, GlobalSearchScope.allScope(p));
            final FileEditorManager fileEditorManager = FileEditorManager.getInstance(p);

            String Text2 = Text.substring(Text.indexOf(":(") + 2, Text.indexOf(")"));

            int delimiter_pos = Text2.indexOf(",");
            int column;
            int column2 = 0;

            if (Text2.contains("-")) {
                int delimiter_pos2 = Text2.indexOf("-");
                column = Integer.parseInt(Text2.substring(delimiter_pos + 1, delimiter_pos2));
                column2 = Integer.parseInt(Text2.substring(delimiter_pos2 + 1));
            }
            else {
                column = Integer.parseInt(Text2.substring(delimiter_pos + 1));
            }

            int lineNumber =  Integer.parseInt(Text2.substring(0, delimiter_pos));

            if (foundFiles.length > 0) {
                String errorText = Text + "<";
                Notification notification = new Notification(GUID, errorPrefix, errorText, NotificationType.ERROR);

                int finalColumn = column;
                int finalColumn2 = column2;
                notification.addAction(new NotificationAction("Go to Error") {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                        createLink(foundFiles, fileEditorManager, p, lineNumber, finalColumn, finalColumn2);
                    }
                });

                Notifications.Bus.notify(notification);
                createLink(foundFiles, fileEditorManager, p, lineNumber, column, column2);
            }
        });
    }

    private static void createLink(PsiFile[] foundFiles, FileEditorManager fileEditorManager, Project p, int lineNumber, int column, int column2) {
        VirtualFile file = foundFiles[0].getVirtualFile();

        try {
            fileEditorManager.openFile(file, true);
        } catch (java.lang.IllegalArgumentException e) {
            return; // File doesn't exist (it was either deleted or changed.) stop script.
        }

        Editor editor = FileEditorManager.getInstance(p).getSelectedTextEditor();

        assert editor != null;
        CaretModel caretModel = editor.getCaretModel();
        SelectionModel selectModel = editor.getSelectionModel();

        final TextAttributes attr = new TextAttributes();
        attr.setEffectColor(JBColor.RED);
        attr.setEffectType(EffectType.WAVE_UNDERSCORE);
        System.out.println(file.getLength());

        caretModel.moveToLogicalPosition(new LogicalPosition(lineNumber - 1, column)); // Send the caret to the first position (in the event it doesn't exist.)
        if (column2 != 0) {
            selectModel.setBlockSelection(new LogicalPosition(lineNumber - 1, column), new LogicalPosition(lineNumber - 1, column2));
        }
        System.out.println("THIS SHOULD NOT TRIGGER");
        RangeHighlighter x = editor.getMarkupModel().addLineHighlighter(lineNumber - 1, HighlighterLayer.ERROR, attr);

        ScrollingModel scrollingModel = editor.getScrollingModel();
        scrollingModel.scrollToCaret(ScrollType.CENTER);
    }
}

class getLua extends AnAction { // CTRL-SHIFT-L
    private getLua() {
        super("Get Lua Scripts");
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        String json = "{\"messageID\":0}";
        sendToClient t = new sendToClient(json);
        t.start();
    }
}

class gameSync extends AnAction { // CTRL-SHIFT-S
    static HashMap<String, String> syncedFiles = new HashMap<>(); // This holds the synced file names from the game to save back
    private gameSync() {
        super("Save and Play");
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        if (createFile.synced) {
            Project p = ProjectManager.getInstance().getOpenProjects()[0];
            Editor editor = FileEditorManager.getInstance(p).getSelectedTextEditor();

            assert editor != null;
            editor.getMarkupModel().removeAllHighlighters(); // Get rid of errors

            JsonObject toSave = new JsonObject();
            toSave.addProperty("messageID", 1);
            String[] filename = syncedFiles.keySet().toArray(new String[0]);
            JsonArray files = new JsonArray();

            for (int i = 0; i < syncedFiles.size(); i++) {
                JsonObject file = new JsonObject();
                int finalI = i;
                WriteCommandAction.runWriteCommandAction(p, () -> {

                if (filename[finalI].endsWith(".lua")) {
                    PsiFile[] foundFiles = FilenameIndex.getFilesByName(p, filename[finalI], GlobalSearchScope.allScope(p));

                    if (foundFiles.length > 0) {
                        file.addProperty("guid", syncedFiles.get(filename[finalI]));
                        file.addProperty("script", foundFiles[0].getText());
                        final String[] uiName = {filename[finalI].replaceFirst(".lua", ".xml")};
                        if (syncedFiles.containsKey(uiName[0])) { // Get UI file
                            PsiFile[] foundFiles2 = FilenameIndex.getFilesByName(p, uiName[0], GlobalSearchScope.allScope(p));
                            if (foundFiles2.length > 0) {
                                file.addProperty("ui", foundFiles2[0].getText());
                                }
                            }
                        files.add(file);
                        }
                    else
                        syncedFiles.remove(filename[finalI]); // Remove file from list if it no longer exists in the editor.
                    }
                });

            }
            toSave.add("scriptStates", files);
        sendToClient t = new sendToClient(toSave.toString());
        t.start();
        }
    }
}

class utilities {
    static String convertString(String s) { // Convert from TTS script to Intellij script
        return s.replaceAll("\\r", "");
    }
}