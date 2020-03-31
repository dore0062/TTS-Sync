import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import java.io.File;

class GetPath {
    private VirtualFile getPluginVirtualDirectory() {
        IdeaPluginDescriptor descriptor = PluginManager.getPlugin(PluginId.getId("com.tts.ttssync"));
        if (descriptor != null) {
            File pluginPath = descriptor.getPath();
            String url = VfsUtil.pathToUrl(pluginPath.getAbsolutePath());
            return VirtualFileManager.getInstance().findFileByUrl(url);
        } else {
            System.out.print("Did not find plugin");
            return null;
        }
    }

    public String getPluginVirtualFile(String path) {
        VirtualFile directory = this.getPluginVirtualDirectory();
        if (directory != null) {
            String fullPath = directory.getPath() + "/classes/" + path;
            if ((new File(fullPath)).exists()) {
                return fullPath;
            }

            fullPath = directory.getPath() + "/" + path;
            if ((new File(fullPath)).exists()) {
                return fullPath;
            }
        }
        return null;
    }
}
